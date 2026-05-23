import { useState } from "react";
import { RolePageShell } from "@/components/RolePageShell";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Calendar,
  FileText,
  Bell,
  CheckCheck,
  Clock,
  PlayCircle,
  XCircle,
  CheckCircle2,
  Loader2,
} from "lucide-react";
import {
  useMyNotifications,
  useMarkNotificationRead,
  useMarkAllNotificationsRead,
} from "@/hooks/usePatient";
import type { NotificationType } from "@/types/api";

const typeConfig: Record<NotificationType, { icon: typeof Bell; color: string; label: string }> = {
  APPOINTMENT_SCHEDULED:   { icon: Calendar,     color: "bg-warning/10 text-warning",               label: "New Appointment" },
  APPOINTMENT_CONFIRMED:   { icon: CheckCircle2, color: "bg-success/10 text-success",               label: "Appointment Confirmed" },
  APPOINTMENT_STARTED:     { icon: PlayCircle,   color: "bg-primary/10 text-primary",               label: "Appointment Started" },
  APPOINTMENT_CANCELLED:   { icon: XCircle,      color: "bg-destructive/10 text-destructive",       label: "Appointment Cancelled" },
  CONSULTATION_NOTE_ADDED: { icon: FileText,     color: "bg-info/10 text-info",                     label: "Consultation Notes" },
  GENERAL:                 { icon: Bell,         color: "bg-muted text-muted-foreground",           label: "Notification" },
};

function relativeTime(iso: string): string {
  const diffMs = Date.now() - new Date(iso).getTime();
  const min = Math.floor(diffMs / 60_000);
  if (min < 1) return "just now";
  if (min < 60) return `${min} min ago`;
  const hr = Math.floor(min / 60);
  if (hr < 24) return `${hr} hour${hr > 1 ? "s" : ""} ago`;
  const day = Math.floor(hr / 24);
  if (day === 1) return "yesterday";
  if (day < 7) return `${day} days ago`;
  return new Date(iso).toLocaleDateString("en-US", { month: "short", day: "numeric" });
}

export default function DoctorNotifications() {
  const { data: notifications = [], isLoading } = useMyNotifications();
  const markRead = useMarkNotificationRead();
  const markAllRead = useMarkAllNotificationsRead();
  const [filter, setFilter] = useState<"all" | "unread">("all");

  const filtered = filter === "unread" ? notifications.filter((n) => !n.read) : notifications;
  const unreadCount = notifications.filter((n) => !n.read).length;

  return (
    <RolePageShell title="Notifications" subtitle="Stay updated on appointments and patient activity">
      {/* Toolbar */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Button variant={filter === "all" ? "default" : "outline"} size="sm" onClick={() => setFilter("all")}>
            All
          </Button>
          <Button variant={filter === "unread" ? "default" : "outline"} size="sm" onClick={() => setFilter("unread")} className="gap-1.5">
            Unread
            {unreadCount > 0 && (
              <Badge className="h-5 min-w-[20px] px-1.5 text-xs bg-destructive text-destructive-foreground">
                {unreadCount}
              </Badge>
            )}
          </Button>
        </div>
        {unreadCount > 0 && (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => markAllRead.mutate()}
            disabled={markAllRead.isPending}
            className="gap-1.5 text-muted-foreground"
          >
            <CheckCheck className="h-3.5 w-3.5" /> Mark all read
          </Button>
        )}
      </div>

      {/* Notification List */}
      <div className="space-y-2">
        {isLoading ? (
          <div className="rounded-xl border border-border bg-card p-12 text-center">
            <Loader2 className="h-8 w-8 text-muted-foreground/40 mx-auto mb-3 animate-spin" />
            <p className="text-sm text-muted-foreground">Loading notifications...</p>
          </div>
        ) : filtered.length === 0 ? (
          <div className="rounded-xl border border-border bg-card p-12 text-center">
            <Bell className="h-10 w-10 text-muted-foreground/40 mx-auto mb-3" />
            <p className="text-sm text-muted-foreground">
              No {filter === "unread" ? "unread " : ""}notifications
            </p>
          </div>
        ) : (
          filtered.map((n) => {
            const config = typeConfig[n.type] ?? typeConfig.GENERAL;
            const Icon = config.icon;
            return (
              <div
                key={n.id}
                className={`rounded-xl border p-4 transition-colors cursor-pointer hover:shadow-sm ${
                  n.read ? "border-border bg-card" : "border-primary/20 bg-primary/5"
                }`}
                onClick={() => { if (!n.read) markRead.mutate(n.id); }}
              >
                <div className="flex items-start gap-3">
                  <div className={`p-2 rounded-lg shrink-0 ${config.color}`}>
                    <Icon className="h-4 w-4" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between gap-2 mb-1">
                      <h4 className={`text-sm font-medium ${n.read ? "text-foreground" : "text-foreground font-semibold"}`}>
                        {config.label}
                      </h4>
                      <div className="flex items-center gap-2 shrink-0">
                        {!n.read && <span className="h-2 w-2 rounded-full bg-primary" />}
                        <span className="text-xs text-muted-foreground flex items-center gap-1">
                          <Clock className="h-3 w-3" />
                          {relativeTime(n.createdAt)}
                        </span>
                      </div>
                    </div>
                    <p className="text-sm text-muted-foreground">{n.message}</p>
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </RolePageShell>
  );
}
