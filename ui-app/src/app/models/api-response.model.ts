/**
 * Lambda body payload serialized in API Gateway proxy responses.
 * Mirrors backend BaseResponse DTO.
 */
export interface LambdaBodyResponse<TMessage = string> {
  success: boolean;
  message: TMessage;
  error?: string | null;
}

/**
 * API Gateway proxy response wrapper returned by Lambda integrations.
 */
export interface ApiGatewayResponse<TBody = string> {
  statusCode: number;
  headers?: Record<string, string>;
  body: TBody;
}

/**
 * Backward-compatible alias for endpoints consuming BaseResponse directly.
 */
export type ApiResponse = LambdaBodyResponse<string>;
