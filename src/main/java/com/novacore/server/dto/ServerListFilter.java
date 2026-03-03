package com.novacore.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Filter for server list API. Bound from query via @ModelAttribute.
 * Spring binds: ?joined=&name=&sort=&page=&size= → fields with defaults below.
 * Single sort param: "field,desc" | "field,asc" | "-field" | "field". Default: createdAt,desc. Max size: 100.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerListFilter {

    public static final String SORT_NAME = "name";
    public static final String SORT_CREATED_AT = "createdAt";
    public static final String SORT_UPDATED_AT = "updatedAt";
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * Joined mode filter.
     * true  = servers user has joined (my servers),
     * false = servers user has not joined (discover).
     * null  = treated as true (default: my servers).
     */
    @Builder.Default
    private Boolean joined = true;

    /** Search by server name (backend trims). */
    private String name;

    /** Single sort: "field,desc" | "field,asc" | "-field" | "field". Default: createdAt,desc. */
    private String sort;

    @Builder.Default
    @Min(0)
    private Integer page = 0;

    @Builder.Default
    @Min(1)
    @Max(MAX_PAGE_SIZE)
    private Integer size = DEFAULT_PAGE_SIZE;

    /** Parse sort string into [field, direction]. Used by toPageable(). */
    private static String[] parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return new String[]{SORT_CREATED_AT, "desc"};
        }
        String s = sort.trim();
        String field = SORT_CREATED_AT;
        String dir = "desc";
        if (s.startsWith("-")) {
            field = s.substring(1).trim();
            dir = "desc";
        } else {
            int comma = s.indexOf(',');
            if (comma > 0) {
                field = s.substring(0, comma).trim();
                dir = s.substring(comma + 1).trim().equalsIgnoreCase("asc") ? "asc" : "desc";
            } else {
                field = s;
                dir = "asc";
            }
        }
        if (!field.equals(SORT_NAME) && !field.equals(SORT_CREATED_AT) && !field.equals(SORT_UPDATED_AT)) {
            field = SORT_CREATED_AT;
        }
        return new String[]{field, dir};
    }

    /** Build Pageable. Defaults and cap (size <= MAX_PAGE_SIZE) applied here. */
    public Pageable toPageable() {
        String[] sortParsed = parseSort(sort);
        int p = (page != null && page >= 0) ? page : 0;
        int s = (size != null && size > 0) ? Math.min(size, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
        return PageRequest.of(p, s, Sort.by(Sort.Direction.fromString(sortParsed[1]), sortParsed[0]));
    }

    public boolean hasNameFilter() {
        return name != null && !name.isBlank();
    }

    /**
     * Trimmed name used only internally for repository queries.
     * Hidden from API schema and JSON.
     */
    @JsonIgnore
    @Schema(hidden = true)
    public String getNameSearchTrimmed() {
        return hasNameFilter() ? name.trim() : null;
    }

    /** Joined mode: true = my servers, false = discover. Null treated as true (default my servers). */
    public boolean isJoined() {
        return joined == null || Boolean.TRUE.equals(joined);
    }
}
