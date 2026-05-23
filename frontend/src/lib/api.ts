import axios, { type AxiosError } from "axios";
import type { ApiError } from "@/types/api";

const api = axios.create({
  baseURL: "/api",
  withCredentials: true, // send the HttpOnly JWT cookie on every request
});

// Redirect to /login on 401 (expired or missing token)
api.interceptors.response.use(
  (res) => res,
  (error: AxiosError<ApiError>) => {
    if (
      error.response?.status === 401 &&
      window.location.pathname !== "/login"
    ) {
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

/**
 * Extracts the server-provided error message from an Axios error response.
 * Falls back to a generic message if the response doesn't match ExceptionBody.
 */
export function getApiErrorMessage(
  error: unknown,
  fallback = "Something went wrong."
): string {
  if (axios.isAxiosError(error)) {
    const data = (error as AxiosError<ApiError>).response?.data;
    return data?.message ?? fallback;
  }
  return fallback;
}

export default api;