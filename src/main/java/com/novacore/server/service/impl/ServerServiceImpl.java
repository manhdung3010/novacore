package com.novacore.server.service.impl;

import com.novacore.auth.util.SecurityUtils;
import com.novacore.server.domain.ChannelType;
import com.novacore.server.domain.Server;
import com.novacore.server.domain.ServerChannel;
import com.novacore.server.domain.ServerJoinRequest;
import com.novacore.server.domain.ServerMember;
import com.novacore.server.domain.ServerSettings;
import com.novacore.server.domain.enums.AllowInviteRole;
import com.novacore.server.domain.enums.JoinRequestStatus;
import com.novacore.server.dto.CreateServerRequest;
import com.novacore.server.dto.JoinServerRequest;
import com.novacore.server.dto.JoinServerResponse;
import com.novacore.server.dto.ServerDto;
import com.novacore.server.dto.ServerListFilter;
import com.novacore.server.dto.ServerListResponse;
import com.novacore.server.dto.UpdateServerRequest;
import com.novacore.server.mapper.ServerMapper;
import com.novacore.server.repository.ServerChannelRepository;
import com.novacore.server.repository.ServerJoinRequestRepository;
import com.novacore.server.repository.ServerMemberRepository;
import com.novacore.server.repository.ServerRepository;
import com.novacore.server.repository.ServerSettingsRepository;
import com.novacore.server.service.ServerService;
import com.novacore.shared.constants.ErrorCode;
import com.novacore.shared.exception.BusinessException;
import com.novacore.shared.exception.ResourceNotFoundException;
import com.novacore.user.domain.User;
import com.novacore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ServerServiceImpl implements ServerService {

    private final ServerRepository serverRepository;
    private final UserRepository userRepository;
    private final ServerMemberRepository serverMemberRepository;
    private final ServerJoinRequestRepository serverJoinRequestRepository;
    private final ServerSettingsRepository serverSettingsRepository;
    private final ServerChannelRepository serverChannelRepository;
    private final ServerMapper serverMapper;
    private final SecurityUtils securityUtils;

    @Override
    public JoinServerResponse joinServer(Long serverId, JoinServerRequest request) {
        Long currentUserId = securityUtils.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.USER_404_NOT_FOUND,
                        "User not found: " + currentUserId));
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.RESOURCE_404_NOT_FOUND,
                        "Server not found: " + serverId));
        if (server.getOwner() != null && server.getOwner().getId().equals(currentUserId)) {
            throw new BusinessException(
                    ErrorCode.SERVER_409_ALREADY_MEMBER,
                    "You are the owner of this server");
        }
        if (serverMemberRepository.existsByServerIdAndUserId(serverId, currentUserId)) {
            throw new BusinessException(
                    ErrorCode.SERVER_409_ALREADY_MEMBER,
                    "Already a member of this server");
        }
        ServerSettings settings = serverSettingsRepository.findByServerId(serverId)
                .orElseGet(() -> ServerSettings.builder()
                        .serverId(serverId)
                        .requireApproval(false)
                        .allowInviteRole(AllowInviteRole.members.name())
                        .updatedAt(LocalDateTime.now())
                        .build());
        if (settings.isRequireApproval()) {
            if (serverJoinRequestRepository.existsByServerIdAndUserIdAndStatus(
                    serverId, currentUserId, JoinRequestStatus.PENDING)) {
                throw new BusinessException(
                        ErrorCode.BUSINESS_400_ERROR,
                        "You already have a pending join request for this server");
            }
            ServerJoinRequest joinRequest = ServerJoinRequest.builder()
                    .server(server)
                    .user(user)
                    .status(JoinRequestStatus.PENDING)
                    .message(request != null ? request.getMessage() : null)
                    .createdAt(LocalDateTime.now())
                    .build();
            serverJoinRequestRepository.save(joinRequest);
            return JoinServerResponse.builder()
                    .joined(false)
                    .server(serverMapper.toDto(server))
                    .build();
        }
        ServerMember member = ServerMember.builder()
                .server(server)
                .user(user)
                .joinedAt(LocalDateTime.now())
                .build();
        serverMemberRepository.save(member);
        return JoinServerResponse.builder()
                .joined(true)
                .server(serverMapper.toDto(server))
                .build();
    }

    @Override
    public ServerDto createServer(CreateServerRequest request) {
        Long currentUserId = securityUtils.getCurrentUserId();
        User owner = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.USER_404_NOT_FOUND,
                        String.format("Owner with ID %d not found", currentUserId)
                ));

        Server server = serverMapper.toEntity(request, owner);
        Server saved = serverRepository.save(server);

        ServerMember ownerMember = ServerMember.builder()
                .server(saved)
                .user(owner)
                .nickname(owner.getFullName())
                .joinedAt(LocalDateTime.now())
                .build();
        serverMemberRepository.save(ownerMember);

        // Create default text and voice channels for the new server
        ServerChannel textChannel = ServerChannel.builder()
                .server(saved)
                .name("general")
                .type(ChannelType.text)
                .position(0)
                .nsfw(false)
                .build();

        ServerChannel voiceChannel = ServerChannel.builder()
                .server(saved)
                .name("General")
                .type(ChannelType.voice)
                .position(1)
                .nsfw(false)
                .build();

        serverChannelRepository.save(textChannel);
        serverChannelRepository.save(voiceChannel);

        return serverMapper.toDto(saved);
    }

    @Override
    public ServerDto updateServer(Long id, UpdateServerRequest request) {
        Server server = serverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.RESOURCE_404_NOT_FOUND,
                        String.format("Server with ID %d not found", id)
                ));

        Long currentUserId = securityUtils.getCurrentUserId();
        if (server.getOwner() == null || !server.getOwner().getId().equals(currentUserId)) {
            throw new BusinessException(
                    ErrorCode.AUTH_403_FORBIDDEN,
                    "You are not allowed to update this server"
            );
        }

        serverMapper.updateEntity(server, request);
        Server updated = serverRepository.save(server);
        return serverMapper.toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ServerDto getServer(Long id) {
        Server server = serverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.RESOURCE_404_NOT_FOUND,
                        String.format("Server with ID %d not found", id)
                ));
        return serverMapper.toDto(server);
    }

    @Override
    @Transactional(readOnly = true)
    public ServerListResponse getMyServers(ServerListFilter filter) {
        Long currentUserId = securityUtils.getCurrentUserId();
        ServerListFilter f = filter != null ? filter : ServerListFilter.builder().build();
        Page<Server> page = serverRepository.findAllByOwnerIdOrMemberUserIdWithFilter(
                currentUserId,
                f.hasNameFilter() ? f.getNameSearchTrimmed() : null,
                f.toPageable());
        return toListResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServerDto> getAllServers() {
        return serverRepository.findAll().stream()
                .map(serverMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ServerListResponse getServersNotJoined(ServerListFilter filter) {
        Long currentUserId = securityUtils.getCurrentUserId();
        ServerListFilter f = filter != null ? filter : ServerListFilter.builder().build();
        Page<Server> page = serverRepository.findAllNotOwnedByAndNotMemberUserIdWithFilter(
                currentUserId,
                f.hasNameFilter() ? f.getNameSearchTrimmed() : null,
                f.toPageable());
        return toListResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ServerListResponse getServers(ServerListFilter filter) {
        ServerListFilter f = filter != null ? filter : ServerListFilter.builder().build();
        return f.isJoined() ? getMyServers(f) : getServersNotJoined(f);
    }

    private ServerListResponse toListResponse(Page<Server> page) {
        List<ServerDto> content = page.getContent().stream()
                .map(serverMapper::toDto)
                .collect(Collectors.toList());
        return ServerListResponse.builder()
                .content(content)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .number(page.getNumber())
                .size(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}

