import { createContext, useContext, useState, type ReactNode } from "react";
import type { Role } from "@/types/api";

export interface AuthUser {
  userId: number;
  email: string;
  role: Role;
}

interface RoleContextType {
  user: AuthUser | null;
  setUser: (user: AuthUser) => void;
  clearUser: () => void;
  isAuthenticated: boolean;
}

const RoleContext = createContext<RoleContextType>({} as RoleContextType);

const STORAGE_KEY = "clinic_user";

export function RoleProvider({ children }: { children: ReactNode }) {
  const [user, setUserState] = useState<AuthUser | null>(() => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      return stored ? (JSON.parse(stored) as AuthUser) : null;
    } catch {
      return null;
    }
  });

  const setUser = (newUser: AuthUser) => {
    setUserState(newUser);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(newUser));
  };

  const clearUser = () => {
    setUserState(null);
    localStorage.removeItem(STORAGE_KEY);
  };

  return (
    <RoleContext.Provider
      value={{ user, setUser, clearUser, isAuthenticated: !!user }}
    >
      {children}
    </RoleContext.Provider>
  );
}

export const useRole = () => useContext(RoleContext);

// Re-export for components that still import UserRole from here
export type { Role as UserRole };