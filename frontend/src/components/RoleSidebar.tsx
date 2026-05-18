import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { cn } from "@/lib/utils";
import { useRole, type UserRole } from "@/contexts/RoleContext";
import { useLogout } from "@/hooks/useAuth";
import {
  LayoutDashboard, Users, Calendar, Stethoscope, Pill, FileText,
  Settings, LogOut, ChevronLeft, Activity, Building2, BarChart3,
  UserCog, ClipboardList, Clock, Heart, CreditCard, Bell, BookOpen,
  FolderOpen, Shield,
} from "lucide-react";
import type { LucideIcon } from "lucide-react";

interface NavItem {
  icon: LucideIcon;
  label: string;
  path: string;
}

const NAV_ITEMS: Record<UserRole, NavItem[]> = {
  ADMIN: [
    { icon: LayoutDashboard, label: "Dashboard",      path: "/admin" },
    { icon: Users,           label: "Patients",       path: "/admin/patients" },
    { icon: Stethoscope,     label: "Doctors",        path: "/admin/doctors" },
    { icon: Calendar,        label: "Appointments",   path: "/admin/appointments" },
    { icon: Building2,       label: "Departments",    path: "/admin/departments" },
    { icon: BarChart3,       label: "Reports",        path: "/admin/reports" },
    // { icon: UserCog,         label: "Staff",          path: "/admin/staff" },
    // { icon: Shield,          label: "Access Control", path: "/admin/access" },
  ],
  DOCTOR: [
    { icon: LayoutDashboard, label: "Dashboard",       path: "/doctor" },
    { icon: Calendar,        label: "My Appointments", path: "/doctor/appointments" },
    { icon: Users,           label: "My Patients",     path: "/doctor/patients" },
    { icon: Clock,           label: "Schedule",        path: "/doctor/schedule" },
    // { icon: ClipboardList,   label: "Prescriptions",   path: "/doctor/prescriptions" },
    { icon: FileText,        label: "Medical Records", path: "/doctor/notes" },
  ],
  PATIENT: [
    { icon: LayoutDashboard, label: "Dashboard",       path: "/patient" },
    { icon: Calendar,        label: "My Appointments", path: "/patient/appointments" },
    { icon: FolderOpen,      label: "Medical Records", path: "/patient/records" },
    // { icon: Pill,            label: "Prescriptions",   path: "/patient/prescriptions" },
    // { icon: CreditCard,      label: "Billing",         path: "/patient/billing" },
    { icon: Heart,           label: "Health Profile",  path: "/patient/health" },
    { icon: Bell,            label: "Notifications",   path: "/patient/notifications" },
  ],
};

const ROLE_LABELS: Record<UserRole, string> = {
  ADMIN:   "Administrator",
  DOCTOR:  "Doctor Portal",
  PATIENT: "Patient Portal",
};

export function RoleSidebar() {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useRole();
  const { mutate: logout, isPending: isLoggingOut } = useLogout();

  if (!user) return null;

  const items = NAV_ITEMS[user.role];

  return (
      <aside
          className={cn(
              "fixed left-0 top-0 z-40 flex h-screen flex-col bg-sidebar text-sidebar-foreground transition-all duration-300",
              collapsed ? "w-[72px]" : "w-[260px]"
          )}
      >
        {/* Logo */}
        <div className="flex h-16 items-center gap-3 px-5 border-b border-sidebar-border">
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-sidebar-primary">
            <Activity className="h-5 w-5 text-sidebar-primary-foreground" />
          </div>
          {!collapsed && (
              <div className="flex flex-col">
            <span className="text-base font-bold tracking-tight text-sidebar-primary-foreground">
              MediCare
            </span>
                <span className="text-[11px] text-sidebar-muted">
              {ROLE_LABELS[user.role]}
            </span>
              </div>
          )}
        </div>

        {/* Nav */}
        <nav className="flex-1 space-y-1 px-3 py-4 overflow-y-auto">
          {items.map((item) => {
            const isActive = location.pathname === item.path;
            return (
                <button
                    key={item.path}
                    onClick={() => navigate(item.path)}
                    className={cn(
                        "flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors",
                        isActive
                            ? "bg-sidebar-accent text-sidebar-primary"
                            : "text-sidebar-muted hover:bg-sidebar-accent hover:text-sidebar-accent-foreground"
                    )}
                >
                  <item.icon className="h-5 w-5 shrink-0" />
                  {!collapsed && <span>{item.label}</span>}
                </button>
            );
          })}
        </nav>

        {/* User info + actions */}
        <div className="border-t border-sidebar-border px-3 py-4 space-y-1">
          {!collapsed && (
              <div className="px-3 py-2 mb-2">
                <p className="text-xs text-sidebar-muted truncate">{user.email}</p>
              </div>
          )}
          <button
              onClick={() => navigate(`/${user.role.toLowerCase()}/settings`)}
              className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-sidebar-muted hover:bg-sidebar-accent hover:text-sidebar-accent-foreground transition-colors"
          >
            <Settings className="h-5 w-5 shrink-0" />
            {!collapsed && <span>Settings</span>}
          </button>
          <button
              onClick={() => logout()}
              disabled={isLoggingOut}
              className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-sidebar-muted hover:bg-sidebar-accent hover:text-destructive transition-colors disabled:opacity-50"
          >
            <LogOut className="h-5 w-5 shrink-0" />
            {!collapsed && <span>{isLoggingOut ? "Logging out…" : "Logout"}</span>}
          </button>
        </div>

        {/* Collapse toggle */}
        <button
            onClick={() => setCollapsed(!collapsed)}
            className="absolute -right-3 top-20 flex h-6 w-6 items-center justify-center rounded-full border bg-card text-muted-foreground shadow-sm hover:bg-accent transition-colors"
        >
          <ChevronLeft
              className={cn(
                  "h-3.5 w-3.5 transition-transform",
                  collapsed && "rotate-180"
              )}
          />
        </button>
      </aside>
  );
}