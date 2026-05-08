package ca.elitecsp.common.response;

import ca.elitecsp.common.exception.ApiException;
import ca.elitecsp.common.exception.ErrorCode;
import ca.elitecsp.common.model.BaseResponse;
import ca.elitecsp.common.util.Constants;
import ca.elitecsp.common.util.JsonUtils;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.Map;

public final class ApiResponseBuilder {

    private ApiResponseBuilder() {
        // Utility class – do not instantiate
    }

    public static APIGatewayProxyResponseEvent success(String message) {
        BaseResponse body = BaseResponse.builder()
                .success(true)
                .message(message)
                .build();
        return build(200, body);
    }

    public static APIGatewayProxyResponseEvent notFoundError(String message, String errorCode) {
        BaseResponse body = BaseResponse.builder()
                .success(false)
                .message(message)
                .error(errorCode)
                .build();
        return build(404, body);
    }

    public static APIGatewayProxyResponseEvent badRequest(String message, String errorCode) {
        BaseResponse body = BaseResponse.builder()
                .success(false)
                .message(message)
                .error(errorCode)
                .build();
        return build(400, body);
    }

    public static APIGatewayProxyResponseEvent internalError(String message, String errorCode) {
        BaseResponse body = BaseResponse.builder()
                .success(false)
                .message(message)
                .error(errorCode)
                .build();
        return build(500, body);
    }

    public static APIGatewayProxyResponseEvent fromApiException(ApiException ex) {
        BaseResponse body = BaseResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .error(ex.getErrorCode().name())
                .build();
        return build(ex.getHttpStatus(), body);
    }

    public static APIGatewayProxyResponseEvent fromException(Exception ex) {
        BaseResponse body = BaseResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .error(ErrorCode.INTERNAL_ERROR.name())
                .build();
        return build(500, body);
    }

    private static APIGatewayProxyResponseEvent build(int statusCode, BaseResponse body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(defaultHeaders())
                .withBody(JsonUtils.toJson(body));
    }

    private static Map<String, String> defaultHeaders() {
        return Map.of(
                Constants.HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_JSON,
                Constants.HEADER_CORS_ORIGIN, Constants.getCorsAllow()
        );
    }
}
