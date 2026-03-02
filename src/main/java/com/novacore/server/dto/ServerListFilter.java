package com.novacore.server.dto;

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
 * Single sort param: "field,desc" | "-field" | "field". Default: createdAt,desc. Max size: 50.
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
    public static final int MAX_PAGE_SIZE = 50;

    /** true = servers user has joined, false = discover (not joined). Default true. */
    @Builder.Default
    private Boolean joined = true;

    /** Search by server name (backend trims). */
    private String name;

    /** Single sort: "field,desc" | "field,asc" | "-field" | "field". Default: createdAt,desc. */
    private String sort;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;

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

    /** Build Pageable. Defaults and cap (size <= 50) applied here. */
    public Pageable toPageable() {
        String[] sortParsed = parseSort(sort);
        int p = (page != null && page >= 0) ? page : 0;
        int s = (size != null && size > 0) ? Math.min(size, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
        return PageRequest.of(p, s, Sort.by(Sort.Direction.fromString(sortParsed[1]), sortParsed[0]));
    }

    public boolean hasNameFilter() {
        return name != null && !name.isBlank();
    }

    /** Trimmed name for repository (internal). */
    public String getNameSearchTrimmed() {
        return hasNameFilter() ? name.trim() : null;
    }

    /** Joined mode: true = my servers, false = discover. Null treated as true. */
    public boolean isJoined() {
        return joined == null || Boolean.TRUE.equals(joined);
    }
}
