/**
 * Typed response envelope returned by all Elite CSP Lambda endpoints.
 * Mirrors the backend BaseResponse DTO.
 */
export interface ApiResponse {
  success: boolean;
  message: string;
  body?: string;
  error?: string | null;
}
