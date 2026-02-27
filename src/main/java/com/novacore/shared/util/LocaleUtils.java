package com.novacore.shared.util;

import com.novacore.shared.constants.RequestHeaderConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

@Slf4j
public final class LocaleUtils {

    private LocaleUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Locale resolveLocale(HttpServletRequest request) {
        Locale requestLocale = request.getLocale();
        if (requestLocale != null && !Locale.getDefault().equals(requestLocale)) {
            return requestLocale;
        }

        String acceptLanguage = request.getHeader(RequestHeaderConstants.ACCEPT_LANGUAGE_HEADER);
        if (acceptLanguage != null && !acceptLanguage.isBlank()) {
            Locale localeFromHeader = parseAcceptLanguage(acceptLanguage);
            if (localeFromHeader != null) {
                return localeFromHeader;
            }
        }

        return requestLocale != null ? requestLocale : Locale.getDefault();
    }

    private static Locale parseAcceptLanguage(String acceptLanguage) {
        try {
            String[] parts = acceptLanguage.split(",");
            if (parts.length == 0) {
                return null;
            }

            String langTag = parts[0].trim().split(";")[0].trim();
            if (langTag.isEmpty()) {
                return null;
            }

            if (langTag.contains("-")) {
                String[] langParts = langTag.split("-", 2);
                if (langParts.length == 2) {
                    return new Locale(langParts[0], langParts[1]);
                }
            }

            return new Locale(langTag);
        } catch (Exception e) {
            log.debug("Failed to parse Accept-Language header: {}", acceptLanguage, e);
            return null;
        }
    }
}


















