import { type ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { useRole } from "@/contexts/RoleContext";
import type { Role } from "@/types/api";

const ROLE_ROUTES: Record<Role, string> = {
  ADMIN: "/admin",
  DOCTOR: "/doctor",
  PATIENT: "/patient",
};

interface ProtectedRouteProps {
  children: ReactNode;
  allowedRoles?: Role[];
}

export function ProtectedRoute({ children, allowedRoles }: ProtectedRouteProps) {
  const { user, isAuthenticated } = useRole();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && user && !allowedRoles.includes(user.role)) {
    return <Navigate to={ROLE_ROUTES[user.role]} replace />;
  }

  return <>{children}</>;
}