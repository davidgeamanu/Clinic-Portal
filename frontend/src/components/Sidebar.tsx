import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { cn } from "@/lib/utils";
import {
  LayoutDashboard,
  Users,
  Calendar,
  Stethoscope,
  Pill,
  FileText,
  Settings,
  LogOut,
  ChevronLeft,
  Activity,
} from "lucide-react";

const navItems = [
  { icon: LayoutDashboard, label: "Dashboard", path: "/" },
  { icon: Users, label: "Patients", path: "/patients" },
  { icon: Calendar, label: "Appointments", path: "/appointments" },
  { icon: Stethoscope, label: "Doctors", path: "/doctors" },
  { icon: Pill, label: "Pharmacy", path: "/pharmacy" },
  { icon: FileText, label: "Reports", path: "/reports" },
  { icon: Activity, label: "Lab Results", path: "/lab-results" },
];

const bottomItems = [
  { icon: Settings, label: "Settings" },
  { icon: LogOut, label: "Logout" },
];

export function Sidebar() {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <aside
      className={cn(
        "fixed left-0 top-0 z-40 flex h-screen flex-col bg-sidebar text-sidebar-foreground transition-all duration-300",
        collapsed ? "w-[72px]" : "w-[260px]"
      )}
    >
      <div className="flex h-16 items-center gap-3 px-5 border-b border-sidebar-border">
        <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-sidebar-primary">
          <Activity className="h-5 w-5 text-sidebar-primary-foreground" />
        </div>
        {!collapsed && (
          <span className="text-lg font-bold tracking-tight text-sidebar-primary-foreground">
            MediCare
          </span>
        )}
      </div>

      <nav className="flex-1 space-y-1 px-3 py-4">
        {navItems.map((item) => {
          const isActive = location.pathname === item.path;
          return (
            <button
              key={item.label}
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

      <div className="space-y-1 border-t border-sidebar-border px-3 py-4">
        {bottomItems.map((item) => (
          <button
            key={item.label}
            className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-sidebar-muted transition-colors hover:bg-sidebar-accent hover:text-sidebar-accent-foreground"
          >
            <item.icon className="h-5 w-5 shrink-0" />
            {!collapsed && <span>{item.label}</span>}
          </button>
        ))}
      </div>

      <button
        onClick={() => setCollapsed(!collapsed)}
        className="absolute -right-3 top-20 flex h-6 w-6 items-center justify-center rounded-full border bg-card text-muted-foreground shadow-sm hover:bg-accent transition-colors"
      >
        <ChevronLeft className={cn("h-3.5 w-3.5 transition-transform", collapsed && "rotate-180")} />
      </button>
    </aside>
  );
}
