package com.novacore.shared.response;

import com.novacore.shared.constants.ErrorCode;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Globally wraps successful controller responses in ApiResponse.
 * Controllers return DTO (or ResponseEntity with DTO body); this advice wraps the body.
 * Skips wrapping when the body is already an ApiResponse (e.g. from exception handlers).
 */
@RestControllerAdvice(basePackages = "com.novacore")
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return ResponseEntity.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (body == null) {
            return null;
        }
        if (body instanceof ApiResponse) {
            return body;
        }
        return ApiResponseBuilder.success(ErrorCode.SUCCESS_200_OK.getDefaultMessage(), body);
    }
}
