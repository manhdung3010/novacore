package com.novacore.server.service.impl;

import com.novacore.auth.util.SecurityUtils;
import com.novacore.server.domain.Server;
import com.novacore.server.domain.ServerInvite;
import com.novacore.server.domain.ServerJoinRequest;
import com.novacore.server.domain.ServerMember;
import com.novacore.server.domain.ServerSettings;
import com.novacore.server.domain.enums.AllowInviteRole;
import com.novacore.server.domain.enums.JoinRequestStatus;
import com.novacore.server.dto.AcceptInviteResponse;
import com.novacore.server.dto.CreateInviteRequest;
import com.novacore.server.dto.InviteDto;
import com.novacore.server.dto.ServerDto;
import com.novacore.server.mapper.ServerMapper;
import com.novacore.server.repository.ServerInviteRepository;
import com.novacore.server.repository.ServerJoinRequestRepository;
import com.novacore.server.repository.ServerMemberRepository;
import com.novacore.server.repository.ServerRepository;
import com.novacore.server.repository.ServerSettingsRepository;
import com.novacore.server.service.ServerInviteService;
import com.novacore.shared.constants.ErrorCode;
import com.novacore.shared.exception.BusinessException;
import com.novacore.shared.exception.ResourceNotFoundException;
import com.novacore.user.domain.User;
import com.novacore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ServerInviteServiceImpl implements ServerInviteService {

    private static final String INVITE_PATH = "/invite/";
    private static final int INVITE_CODE_LENGTH = 12;

    private final ServerInviteRepository serverInviteRepository;
    private final ServerRepository serverRepository;
    private final ServerSettingsRepository serverSettingsRepository;
    private final ServerMemberRepository serverMemberRepository;
    private final ServerJoinRequestRepository serverJoinRequestRepository;
    private final UserRepository userRepository;
    private final ServerMapper serverMapper;
    private final SecurityUtils securityUtils;

    @Value("${app.invite.base-url:}")
    private String inviteBaseUrl;

    @Override
    @Transactional(readOnly = true)
    public InviteDto resolveInvite(String code) {
        ServerInvite invite = serverInviteRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.SERVER_404_INVITE_NOT_FOUND,
                        "Invite not found: " + code));
        validateInviteStillValid(invite);
        return toInviteDto(invite);
    }

    @Override
    public AcceptInviteResponse acceptInvite(String code) {
        Long currentUserId = securityUtils.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.USER_404_NOT_FOUND,
                        "User not found: " + currentUserId));

        ServerInvite invite = serverInviteRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.SERVER_404_INVITE_NOT_FOUND,
                        "Invite not found: " + code));
        validateInviteStillValid(invite);

        Server server = serverRepository.findById(invite.getServer().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.RESOURCE_404_NOT_FOUND,
                        "Server not found for invite"));
        if (serverMemberRepository.existsByServerIdAndUserId(server.getId(), currentUserId)) {
            throw new BusinessException(
                    ErrorCode.SERVER_409_ALREADY_MEMBER,
                    "Already a member of this server");
        }
        if (server.getOwner() != null && server.getOwner().getId().equals(currentUserId)) {
            throw new BusinessException(
                    ErrorCode.SERVER_409_ALREADY_MEMBER,
                    "You are the owner of this server");
        }

        ServerSettings settings = serverSettingsRepository.findByServerId(server.getId())
                .orElseGet(() -> defaultSettings(server.getId()));

        if (settings.isRequireApproval()) {
            createJoinRequest(server, user, invite.getId());
            incrementInviteUses(invite);
            ServerDto dto = serverMapper.toDto(server);
            return AcceptInviteResponse.builder().joined(false).server(dto).build();
        }

        addMember(server, user);
        incrementInviteUses(invite);
        ServerDto dto = serverMapper.toDto(server);
        return AcceptInviteResponse.builder().joined(true).server(dto).build();
    }

    @Override
    public InviteDto createInvite(Long serverId, CreateInviteRequest request) {
        Long currentUserId = securityUtils.getCurrentUserId();
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.USER_404_NOT_FOUND,
                        "User not found: " + currentUserId));

        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.RESOURCE_404_NOT_FOUND,
                        "Server not found: " + serverId));

        ServerSettings settings = serverSettingsRepository.findByServerId(serverId)
                .orElseGet(() -> defaultSettings(serverId));

        AllowInviteRole allowRole = AllowInviteRole.valueOf(
                settings.getAllowInviteRole() != null ? settings.getAllowInviteRole() : "members");
        if (allowRole == AllowInviteRole.none) {
            throw new BusinessException(
                    ErrorCode.AUTH_403_FORBIDDEN,
                    "This server does not allow creating invites");
        }
        boolean isOwner = server.getOwner() != null && server.getOwner().getId().equals(currentUserId);
        boolean isMember = serverMemberRepository.existsByServerIdAndUserId(serverId, currentUserId);
        if (allowRole == AllowInviteRole.owner_only && !isOwner) {
            throw new BusinessException(
                    ErrorCode.AUTH_403_FORBIDDEN,
                    "Only the server owner can create invites");
        }
        if (!isOwner && !isMember) {
            throw new BusinessException(
                    ErrorCode.AUTH_403_FORBIDDEN,
                    "You must be a member to create invites");
        }

        String code = generateUniqueCode();
        ServerInvite invite = ServerInvite.builder()
                .server(server)
                .code(code)
                .createdBy(creator)
                .uses(0)
                .maxUses(request != null ? request.getMaxUses() : null)
                .expireAt(request != null ? request.getExpireAt() : null)
                .temporary(request != null && Boolean.TRUE.equals(request.getTemporary()))
                .build();
        serverInviteRepository.save(invite);
        return toInviteDto(invite);
    }

    private void validateInviteStillValid(ServerInvite invite) {
        if (invite.getExpireAt() != null && LocalDateTime.now().isAfter(invite.getExpireAt())) {
            throw new BusinessException(
                    ErrorCode.SERVER_400_INVITE_EXPIRED,
                    "Invite has expired");
        }
        if (invite.getMaxUses() != null && invite.getUses() >= invite.getMaxUses()) {
            throw new BusinessException(
                    ErrorCode.SERVER_400_INVITE_EXPIRED,
                    "Invite has reached max uses");
        }
    }

    private void createJoinRequest(Server server, User user, Long inviteId) {
        if (serverJoinRequestRepository.existsByServerIdAndUserIdAndStatus(
                server.getId(), user.getId(), JoinRequestStatus.PENDING)) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_400_ERROR,
                    "You already have a pending join request for this server");
        }
        ServerJoinRequest request = ServerJoinRequest.builder()
                .server(server)
                .user(user)
                .status(JoinRequestStatus.PENDING)
                .invitedByInviteId(inviteId)
                .createdAt(LocalDateTime.now())
                .build();
        serverJoinRequestRepository.save(request);
    }

    private void addMember(Server server, User user) {
        ServerMember member = ServerMember.builder()
                .server(server)
                .user(user)
                .joinedAt(LocalDateTime.now())
                .build();
        serverMemberRepository.save(member);
    }

    private void incrementInviteUses(ServerInvite invite) {
        invite.setUses(invite.getUses() + 1);
        serverInviteRepository.save(invite);
    }

    private ServerSettings defaultSettings(Long serverId) {
        return ServerSettings.builder()
                .serverId(serverId)
                .requireApproval(false)
                .allowInviteRole(AllowInviteRole.members.name())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private String generateUniqueCode() {
        String code;
        int attempts = 0;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, INVITE_CODE_LENGTH);
            if (++attempts > 5) {
                code = UUID.randomUUID().toString().replace("-", "");
            }
        } while (serverInviteRepository.findByCode(code).isPresent());
        return code;
    }

    private InviteDto toInviteDto(ServerInvite invite) {
        Server server = invite.getServer();
        String inviteLink = (inviteBaseUrl != null && !inviteBaseUrl.isBlank()
                ? inviteBaseUrl.replaceAll("/$", "") : "") + INVITE_PATH + invite.getCode();
        return InviteDto.builder()
                .code(invite.getCode())
                .inviteLink(inviteLink)
                .serverId(server != null ? server.getId() : null)
                .serverName(server != null ? server.getName() : null)
                .createdById(invite.getCreatedBy() != null ? invite.getCreatedBy().getId() : null)
                .createdByUsername(invite.getCreatedBy() != null ? invite.getCreatedBy().getUsername() : null)
                .uses(invite.getUses())
                .maxUses(invite.getMaxUses())
                .expireAt(invite.getExpireAt())
                .temporary(invite.getTemporary())
                .build();
    }
}
