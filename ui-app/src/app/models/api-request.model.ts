/**
 * Typed request envelope sent to all Elite CSP Lambda endpoints.
 * Mirrors the backend BaseRequest DTO.
 */
export interface ApiRequest {
  body: string;
  isBase64Encoded: boolean;
}
