import api from "@/lib/api";
import type { AuthResponse, LoginRequest, RegisterRequest } from "@/types/api";

export const authApi = {
  login: (data: LoginRequest) =>
      api.post<AuthResponse>("/auth/login", data).then((r) => r.data),

  register: (data: RegisterRequest) =>
      api.post<AuthResponse>("/auth/register", data).then((r) => r.data),

  logout: () => api.post("/auth/logout"),

  changePassword: (data: { currentPassword: string; newPassword: string }) =>
      api.patch("/auth/password", data),
};