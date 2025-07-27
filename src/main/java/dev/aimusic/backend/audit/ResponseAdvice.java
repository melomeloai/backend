package dev.aimusic.backend.audit;

import dev.aimusic.backend.common.AbstractResponse;
import dev.aimusic.backend.common.RequestStatus;
import dev.aimusic.backend.common.Response;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Field;

import static dev.aimusic.backend.common.Constants.X_REQUEST_ID;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    private final HttpServletResponse httpServletResponse;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        var requestId = httpServletResponse.getHeader(X_REQUEST_ID);
        var requestStatus = RequestStatus.builder()
                .requestId(requestId)
                .build();

        if (body == null) {
            var emptyResponse = Response.builder().build();
            emptyResponse.setRequestStatus(requestStatus);
            return emptyResponse;
        }

        if (body instanceof AbstractResponse) {
            // Set request status field use reflection
            try {
                Field requestStatusField = ReflectionUtils.findField(body.getClass(), "requestStatus");
                if (requestStatusField != null) {
                    ReflectionUtils.makeAccessible(requestStatusField);
                    ReflectionUtils.setField(requestStatusField, body, requestStatus);
                }
            } catch (Exception e) {
                log.error("Failed to set request status on response: {}", e.getMessage());
            }
            return body;
        }

        return body;
    }
}