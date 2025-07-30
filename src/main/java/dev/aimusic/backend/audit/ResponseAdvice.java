package dev.aimusic.backend.audit;

import dev.aimusic.backend.common.dto.AbstractResponse;
import dev.aimusic.backend.common.dto.RequestStatus;
import dev.aimusic.backend.common.dto.Response;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

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

        if (body == null) {
            var emptyResponse = Response.builder().build();
            emptyResponse.setRequestStatus(RequestStatus.builder()
                    .requestId(requestId)
                    .build());
            return emptyResponse;
        }

        if (body instanceof AbstractResponse abstractResponse) {
            var existingStatus = abstractResponse.getRequestStatus();
            if (existingStatus != null) {
                // 保留已有的错误信息，重新构建包含requestId的RequestStatus
                abstractResponse.setRequestStatus(RequestStatus.builder()
                        .requestId(requestId)
                        .error(existingStatus.getError())
                        .errorMessage(existingStatus.getErrorMessage())
                        .build());
            } else {
                // 创建新的requestStatus
                abstractResponse.setRequestStatus(RequestStatus.builder()
                        .requestId(requestId)
                        .build());
            }
            return body;
        }

        return body;
    }
}