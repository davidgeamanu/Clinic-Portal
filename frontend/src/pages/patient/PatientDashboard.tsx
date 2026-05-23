import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { RolePageShell } from "@/components/RolePageShell";
import { StatsCard } from "@/components/StatsCard";
import { Calendar, CheckCircle2, Bell, CalendarClock, MapPin, Video, User, Plus } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { motion } from "framer-motion";
import {
  usePatientProfile,
  usePatientAppointments,
  useUpdatePatientAppointmentStatus,
  useMyNotifications,
  useMarkNotificationRead,
} from "@/hooks/usePatient";
import type { AppointmentStatus, NotificationType } from "@/types/api";

const STATUS_STYLES: Record<AppointmentStatus, string> = {
  SCHEDULED:   "bg-warning/10 text-warning border-warning/20",
  CONFIRMED:   "bg-success/10 text-success border-success/20",
  IN_PROGRESS: "bg-primary/10 text-primary border-primary/20",
  COMPLETED:   "bg-muted text-muted-foreground border-border",
  CANCELLED:   "bg-muted text-muted-foreground border-border",
};

const STATUS_LABELS: Record<AppointmentStatus, string> = {
  SCHEDULED:   "Awaiting Confirmation",
  CONFIRMED:   "Confirmed",
  IN_PROGRESS: "In Progress",
  COMPLETED:   "Completed",
  CANCELLED:   "Cancelled",
};

const MODE_LABELS = { IN_PERSON: "In-Person", VIDEO: "Video" } as const;
const MODE_STYLES = {
  IN_PERSON: "bg-info/10 text-info border-info/20",
  VIDEO:     "bg-secondary/10 text-secondary-foreground border-secondary/20",
} as const;

const NOTIFICATION_ACCENTS: Record<NotificationType, string> = {
  APPOINTMENT_SCHEDULED: "bg-warning",
  APPOINTMENT_CONFIRMED: "bg-success",
  APPOINTMENT_STARTED:   "bg-primary",
  APPOINTMENT_CANCELLED: "bg-destructive",
  CONSULTATION_NOTE_ADDED: "bg-info",
  GENERAL: "bg-muted-foreground",
};

function formatTime(iso: string) {
  return new Date(iso).toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit", hour12: true });
}

function formatShortDate(iso: string) {
  return new Date(iso).toLocaleDateString("en-US", { month: "short", day: "numeric" });
}

function relativeTime(iso: string): string {
  const diffMs = Date.now() - new Date(iso).getTime();
  const min = Math.floor(diffMs / 60_000);
  if (min < 1)    return "just now";
  if (min < 60)   return `${min} min ago`;
  const hr = Math.floor(min / 60);
  if (hr < 24)    return `${hr} hour${hr > 1 ? "s" : ""} ago`;
  const day = Math.floor(hr / 24);
  if (day === 1)  return "yesterday";
  if (day < 7)    return `${day} days ago`;
  return new Date(iso).toLocaleDateString("en-US", { month: "short", day: "numeric" });
}

export default function PatientDashboard() {
  const navigate = useNavigate();
  const { data: profile } = usePatientProfile();
  const { data: appointments = [] } = usePatientAppointments();
  const { data: notifications = [] } = useMyNotifications();
  const updateStatus = useUpdatePatientAppointmentStatus();
  const markRead = useMarkNotificationRead();

  const [pendingCancel, setPendingCancel] = useState<number | null>(null);

  const now = Date.now();

  const upcoming = useMemo(
    () => appointments
      .filter((a) =>
        a.status !== "CANCELLED" &&
        a.status !== "COMPLETED" &&
        new Date(a.scheduledAt).getTime() >= now - 60 * 60_000   // include things starting within last hour
      )
      .sort((a, b) => new Date(a.scheduledAt).getTime() - new Date(b.scheduledAt).getTime()),
    [appointments, now]
  );

  const stats = useMemo(() => {
    const pastVisits = appointments.filter((a) => a.status === "COMPLETED").length;
    const unread = notifications.filter((n) => !n.read).length;
    const nextDate = upcoming[0]?.scheduledAt;
    return {
      upcomingCount: upcoming.length,
      pastVisits,
      unread,
      nextDate,
    };
  }, [appointments, notifications, upcoming]);

  const firstName = profile?.firstName ?? "";
  const recentNotifications = notifications.slice(0, 6);

  return (
    <RolePageShell title="Patient Dashboard" subtitle={firstName ? `Welcome back, ${firstName}` : "Welcome back"}>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatsCard title="Upcoming Appointments" value={String(stats.upcomingCount)} icon={Calendar}      iconBg="bg-primary/10 text-primary" delay={0} />
        <StatsCard title="Past Visits"           value={String(stats.pastVisits)}    icon={CheckCircle2}  iconBg="bg-success/10 text-success" delay={0.05} />
        <StatsCard title="Unread Notifications"  value={String(stats.unread)}        icon={Bell}          iconBg="bg-warning/10 text-warning" delay={0.1} />
        <StatsCard title="Next Visit"            value={stats.nextDate ? formatShortDate(stats.nextDate) : "—"} icon={CalendarClock} iconBg="bg-info/10 text-info" delay={0.15} />
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Upcoming appointments */}
        <motion.div
          initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }}
          className="lg:col-span-2 rounded-xl border border-border bg-card p-6"
        >
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-foreground">My Appointments</h3>
            <Button size="sm" className="gap-1.5" onClick={() => navigate("/patient/book")}>
              <Plus className="h-4 w-4" /> Book New
            </Button>
          </div>

          {upcoming.length === 0 ? (
            <div className="text-center py-10 space-y-3">
              <p className="text-sm text-muted-foreground">No upcoming appointments.</p>
              <Button variant="outline" size="sm" onClick={() => navigate("/patient/book")}>
                Book your first appointment
              </Button>
            </div>
          ) : (
            <div className="space-y-3">
              {upcoming.map((apt) => {
                const [hhmm, ampm] = formatTime(apt.scheduledAt).split(" ");
                const canCancel = apt.status === "SCHEDULED" || apt.status === "CONFIRMED";
                return (
                  <div key={apt.id} className="flex items-center gap-3 p-3 rounded-lg border border-border hover:bg-muted/30 transition-colors">
                    <div className="text-center shrink-0 w-16">
                      <p className="text-xs text-muted-foreground">{formatShortDate(apt.scheduledAt)}</p>
                      <p className="text-sm font-semibold text-foreground">{hhmm}</p>
                      <p className="text-[10px] text-muted-foreground">{ampm}</p>
                    </div>
                    <div className="h-10 w-px bg-border" />
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-1.5 text-sm font-medium text-foreground">
                        <User className="h-3.5 w-3.5 text-muted-foreground" />
                        Dr. {apt.doctorName}
                      </div>
                      <div className="flex items-center gap-2 mt-1 flex-wrap text-xs text-muted-foreground">
                        <Badge variant="outline" className={`text-[10px] px-1.5 py-0 h-4 ${MODE_STYLES[apt.mode]}`}>
                          {apt.mode === "VIDEO" ? <Video className="h-2.5 w-2.5 mr-0.5" /> : <MapPin className="h-2.5 w-2.5 mr-0.5" />}
                          {MODE_LABELS[apt.mode]}
                        </Badge>
                        <span>{apt.durationMinutes} min</span>
                        {apt.roomNumber && <span>· Room {apt.roomNumber}</span>}
                      </div>
                    </div>
                    <Badge variant="outline" className={STATUS_STYLES[apt.status]}>
                      {STATUS_LABELS[apt.status]}
                    </Badge>
                    {canCancel && (
                      <Button
                        size="sm" variant="ghost"
                        className="text-destructive hover:text-destructive hover:bg-destructive/10"
                        disabled={updateStatus.isPending}
                        onClick={() => setPendingCancel(apt.id)}
                      >
                        Cancel
                      </Button>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </motion.div>

        {/* Notifications */}
        <motion.div
          initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.25 }}
          className="rounded-xl border border-border bg-card p-6"
        >
          <div className="flex items-center gap-2 mb-4">
            <Bell className="h-5 w-5 text-warning" />
            <h3 className="text-lg font-semibold text-foreground">Notifications</h3>
            {stats.unread > 0 && (
              <Badge variant="outline" className="ml-auto bg-warning/10 text-warning border-warning/20 text-xs">
                {stats.unread} new
              </Badge>
            )}
          </div>

          {recentNotifications.length === 0 ? (
            <p className="text-sm text-muted-foreground py-6 text-center">You're all caught up.</p>
          ) : (
            <div className="space-y-2">
              {recentNotifications.map((n) => (
                <button
                  key={n.id}
                  onClick={() => !n.read && markRead.mutate(n.id)}
                  disabled={n.read}
                  className={`w-full text-left flex items-start gap-3 p-2 rounded-lg transition-colors ${
                    n.read ? "opacity-60" : "hover:bg-muted/30"
                  }`}
                >
                  <div className={`mt-1.5 h-2 w-2 rounded-full shrink-0 ${n.read ? "bg-muted-foreground/30" : NOTIFICATION_ACCENTS[n.type]}`} />
                  <div className="min-w-0">
                    <p className="text-sm text-foreground line-clamp-2">{n.message}</p>
                    <p className="text-xs text-muted-foreground mt-0.5">{relativeTime(n.createdAt)}</p>
                  </div>
                </button>
              ))}
            </div>
          )}

          {notifications.length > recentNotifications.length && (
            <Button variant="outline" size="sm" className="w-full mt-4" onClick={() => navigate("/patient/notifications")}>
              View All Notifications
            </Button>
          )}
        </motion.div>
      </div>

      {/* Cancel confirmation */}
      <AlertDialog open={pendingCancel !== null} onOpenChange={(open) => { if (!open) setPendingCancel(null); }}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Cancel this appointment?</AlertDialogTitle>
            <AlertDialogDescription>
              Your doctor will be notified. Cancelled appointments cannot be restored — you'll need to book a new one.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Keep appointment</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => {
                if (pendingCancel !== null) {
                  updateStatus.mutate({ appointmentId: pendingCancel, status: "CANCELLED" });
                  setPendingCancel(null);
                }
              }}
              className="bg-destructive hover:bg-destructive/90"
            >
              Cancel appointment
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </RolePageShell>
  );
}