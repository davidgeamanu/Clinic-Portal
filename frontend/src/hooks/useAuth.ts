import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { authApi } from "@/api/auth";
import { useRole } from "@/contexts/RoleContext";
import { getApiErrorMessage } from "@/lib/api";
import { queryClient } from "@/lib/query-client";
import type { LoginRequest, RegisterRequest, Role } from "@/types/api";

const ROLE_ROUTES: Record<Role, string> = {
  ADMIN: "/admin",
  DOCTOR: "/doctor",
  PATIENT: "/patient",
};

export function useLogin() {
  const { setUser } = useRole();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (data: LoginRequest) => authApi.login(data),
    onSuccess: (data) => {
      setUser({ userId: data.userId, email: data.email, role: data.role });
      navigate(ROLE_ROUTES[data.role]);
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Invalid email or password."));
    },
  });
}

export function useRegister() {
  const { setUser } = useRole();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (data: RegisterRequest) => authApi.register(data),
    onSuccess: (data) => {
      setUser({ userId: data.userId, email: data.email, role: data.role });
      navigate(ROLE_ROUTES[data.role]);
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Registration failed."));
    },
  });
}

export function useLogout() {
  const { clearUser } = useRole();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: () => authApi.logout(),
    onSettled: () => {
      // Clear cached data and local auth state regardless of server response
      queryClient.clear();
      clearUser();
      navigate("/login");
    },
  });
}