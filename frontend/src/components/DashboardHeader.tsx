import { Bell } from "lucide-react";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { useRole } from "@/contexts/RoleContext";
import { useMyNotifications } from "@/hooks/usePatient";
import { useQuery } from "@tanstack/react-query";
import { doctorApi } from "@/api/doctor";
import { patientApi } from "@/api/patient";
import { queryKeys } from "@/lib/query-keys";
import { useNavigate } from "react-router-dom";

interface DashboardHeaderProps {
  title?: string;
  subtitle?: string;
}

export function DashboardHeader({
  title = "Dashboard",
  subtitle,
}: DashboardHeaderProps) {
  const { user } = useRole();
  const navigate = useNavigate();
  const { data: notifications = [] } = useMyNotifications();

  const { data: doctorProfile } = useQuery({
    queryKey: queryKeys.doctor.me(),
    queryFn: doctorApi.getMyProfile,
    enabled: user?.role === "DOCTOR",
  });
  const { data: patientProfile } = useQuery({
    queryKey: queryKeys.patients.me(),
    queryFn: patientApi.getMyProfile,
    enabled: user?.role === "PATIENT",
  });

  const unreadCount = notifications.filter((n) => !n.read).length;

  const profile = user?.role === "DOCTOR" ? doctorProfile : patientProfile;
  const initials = profile?.firstName && profile?.lastName
    ? `${profile.firstName[0]}${profile.lastName[0]}`.toUpperCase()
    : user?.email?.slice(0, 2).toUpperCase() ?? "?";

  const displaySubtitle = subtitle ?? (user ? `Logged in as ${user.email}` : "");

  const notificationsPath =
    user?.role === "DOCTOR" ? "/doctor/notifications"
    : user?.role === "ADMIN" ? "/admin/notifications"
    : "/patient/notifications";

  const settingsPath = `/${user?.role.toLowerCase()}/settings`;

  return (
    <header className="flex items-center justify-between border-b bg-card/60 backdrop-blur-sm px-8 py-4">
      <div>
        <h1 className="text-xl font-bold tracking-tight">{title}</h1>
        <p className="text-sm text-muted-foreground">{displaySubtitle}</p>
      </div>
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate(notificationsPath)}
          className="relative flex h-9 w-9 items-center justify-center rounded-lg border bg-background transition-colors hover:bg-accent"
        >
          <Bell className="h-4 w-4 text-muted-foreground" />
          {unreadCount > 0 && (
            <span className="absolute -right-1 -top-1 flex h-4 min-w-[16px] items-center justify-center rounded-full bg-destructive px-1 text-[10px] font-bold text-destructive-foreground">
              {unreadCount > 99 ? "99+" : unreadCount}
            </span>
          )}
        </button>
        <button onClick={() => navigate(settingsPath)} className="rounded-full focus:outline-none focus:ring-2 focus:ring-ring">
          <Avatar className="h-9 w-9 border cursor-pointer hover:opacity-80 transition-opacity">
            <AvatarFallback className="bg-primary text-primary-foreground text-xs font-semibold">
              {initials}
            </AvatarFallback>
          </Avatar>
        </button>
      </div>
    </header>
  );
}
