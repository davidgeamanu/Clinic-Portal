import { useMemo, useState } from "react";
import { RolePageShell } from "@/components/RolePageShell";
import { StatsCard } from "@/components/StatsCard";
import { Calendar, Users, Clock, CheckCircle2, FileText } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog";
import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import {
  useDoctorProfile,
  useDoctorAppointments,
  useUpdateAppointmentStatus,
  useRecentPatients,
} from "@/hooks/useDoctor";
import { ElapsedTimer } from "@/components/ElapsedTimer";
import type { AppointmentStatus } from "@/types/api";

const STATUS_STYLES: Record<AppointmentStatus, string> = {
  SCHEDULED:   "bg-warning/10 text-warning border-warning/20",
  CONFIRMED:   "bg-success/10 text-success border-success/20",
  IN_PROGRESS: "bg-primary/10 text-primary border-primary/20",
  COMPLETED:   "bg-muted text-muted-foreground border-border",
  CANCELLED:   "bg-muted text-muted-foreground border-border",
  NO_SHOW:     "bg-destructive/10 text-destructive border-destructive/20",
};

const STATUS_LABELS: Record<AppointmentStatus, string> = {
  SCHEDULED:   "Upcoming",
  CONFIRMED:   "Confirmed",
  IN_PROGRESS: "In Progress",
  COMPLETED:   "Completed",
  CANCELLED:   "Cancelled",
  NO_SHOW:     "No-show",
};

const MODE_LABELS = { IN_PERSON: "In-Person", VIDEO: "Video" } as const;
const MODE_STYLES = {
  IN_PERSON: "bg-info/10 text-info border-info/20",
  VIDEO:     "bg-secondary/10 text-secondary-foreground border-secondary/20",
} as const;

function isToday(iso: string): boolean {
  const d = new Date(iso);
  const now = new Date();
  return d.getFullYear() === now.getFullYear()
      && d.getMonth() === now.getMonth()
      && d.getDate() === now.getDate();
}

function relativeDate(isoDate: string): string {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const visit = new Date(isoDate + "T00:00:00");
  visit.setHours(0, 0, 0, 0);
  const diff = Math.round((today.getTime() - visit.getTime()) / 86_400_000);
  if (diff === 0) return "Today";
  if (diff === 1) return "Yesterday";
  if (diff < 0) {
    const d = visit;
    return `${String(d.getDate()).padStart(2, "0")}-${String(d.getMonth() + 1).padStart(2, "0")}-${d.getFullYear()}`;
  }
  return `${diff} days ago`;
}

function formatTime(iso: string) {
  return new Date(iso).toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit", hour12: true });
}

function actionText(status: AppointmentStatus): { title: string; description: string; action: string } {
  switch (status) {
    case "CONFIRMED":   return { title: "Confirm this appointment?",   description: "The patient will be notified that their appointment has been confirmed.",      action: "Confirm"  };
    case "IN_PROGRESS": return { title: "Start this consultation?",    description: "This will mark the appointment as in progress and start the elapsed timer.",  action: "Start"    };
    case "COMPLETED":   return { title: "Complete this consultation?", description: "This will mark the consultation as completed. This action cannot be undone.", action: "Complete" };
    case "CANCELLED":   return { title: "Cancel this appointment?",    description: "The patient will be notified. Cancelled appointments cannot be restarted.",   action: "Cancel"   };
    default:            return { title: "Confirm action?",             description: "Are you sure you want to proceed?",                                           action: "Continue" };
  }
}

// Dashboard

export default function DoctorDashboard() {
  const navigate = useNavigate();
  const { data: profile } = useDoctorProfile();
  const { data: appointments = [] } = useDoctorAppointments();
  const { data: recentPatients = [] } = useRecentPatients();
  const updateStatus = useUpdateAppointmentStatus();

  const [pending, setPending] = useState<{ appointmentId: number; status: AppointmentStatus } | null>(null);

  const todayAppts = useMemo(
      () => appointments
          .filter((a) => isToday(a.scheduledAt) && a.status !== "CANCELLED")
          .sort((a, b) => new Date(a.scheduledAt).getTime() - new Date(b.scheduledAt).getTime()),
      [appointments]
  );

  const stats = useMemo(() => {
    const completedToday = todayAppts.filter((a) => a.status === "COMPLETED").length;
    return { todayCount: todayAppts.length, completedToday };
  }, [appointments, todayAppts]);

  const doctorName = profile ? `Dr. ${profile.firstName} ${profile.lastName}` : "Doctor";

  return (
      <RolePageShell title="Doctor Dashboard" subtitle={`Welcome back, ${doctorName}`}>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <StatsCard title="Today's Appointments" value={String(stats.todayCount)}       icon={Calendar}     iconBg="bg-primary/10 text-primary"  delay={0} />
          <StatsCard title="My Patients"          value={String(profile?.completedPatientCount ?? 0)} icon={Users}        iconBg="bg-info/10 text-info"         delay={0.05} />
          <StatsCard title="Completed Today"      value={String(stats.completedToday)}   icon={CheckCircle2} iconBg="bg-success/10 text-success"   delay={0.1} />
          <StatsCard title="Avg. Consultation"    value={profile?.avgConsultationMinutes != null ? `${Math.round(profile.avgConsultationMinutes)} min` : "—"} icon={Clock} iconBg="bg-warning/10 text-warning" delay={0.15} />
        </div>

        <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
          {/* Today's schedule */}
          <motion.div
              initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }}
              className="lg:col-span-2 rounded-xl border border-border bg-card p-6"
          >
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-foreground">Today's Schedule</h3>
              <Button variant="outline" size="sm" onClick={() => navigate("/doctor/schedule")}>View Full Schedule</Button>
            </div>
            {todayAppts.length === 0 ? (
                <p className="text-sm text-muted-foreground py-6 text-center">No appointments scheduled for today.</p>
            ) : (
                <div className="space-y-3">
                  {todayAppts.map((apt) => {
                    const [hhmm, ampm] = formatTime(apt.scheduledAt).split(" ");
                    const canNotes = apt.status === "IN_PROGRESS" || apt.status === "COMPLETED";
                    return (
                        <div key={apt.id} className="flex items-center gap-3 p-3 rounded-lg border border-border hover:bg-muted/30 transition-colors">
                          <div className="text-center shrink-0 w-16">
                            <p className="text-sm font-semibold text-foreground">{hhmm}</p>
                            <p className="text-xs text-muted-foreground">{ampm}</p>
                          </div>
                          <div className="h-8 w-px bg-border" />
                          <div className="flex-1 min-w-0">
                            <p className="text-sm font-medium text-foreground">{apt.patientName}</p>
                            <div className="flex items-center gap-1.5 mt-0.5 flex-wrap">
                              <Badge variant="outline" className={`text-[10px] px-1.5 py-0 h-4 ${MODE_STYLES[apt.mode]}`}>
                                {MODE_LABELS[apt.mode]}
                              </Badge>
                              <span className="text-xs text-muted-foreground">{apt.durationMinutes} min</span>
                            </div>
                            {apt.status === "IN_PROGRESS" && apt.startedAt && (
                                <div className="mt-1 text-xs">
                                  <ElapsedTimer startedAt={apt.startedAt} />
                                </div>
                            )}
                          </div>
                          <Badge variant="outline" className={STATUS_STYLES[apt.status]}>
                            {STATUS_LABELS[apt.status]}
                          </Badge>
                          {apt.status === "SCHEDULED" && (
                              <Button size="sm" variant="outline" disabled={updateStatus.isPending}
                                      onClick={() => setPending({ appointmentId: apt.id, status: "CONFIRMED" })}>
                                Confirm
                              </Button>
                          )}
                          {apt.status === "CONFIRMED" && (
                              <Button size="sm" variant="outline" disabled={updateStatus.isPending}
                                      onClick={() => setPending({ appointmentId: apt.id, status: "IN_PROGRESS" })}>
                                Start
                              </Button>
                          )}
                          {(apt.status === "SCHEDULED" || apt.status === "CONFIRMED") && (
                              <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive hover:bg-destructive/10"
                                      disabled={updateStatus.isPending}
                                      onClick={() => setPending({ appointmentId: apt.id, status: "CANCELLED" })}>
                                Cancel
                              </Button>
                          )}
                          {apt.status === "IN_PROGRESS" && (
                              <Button size="sm" disabled={updateStatus.isPending}
                                      onClick={() => setPending({ appointmentId: apt.id, status: "COMPLETED" })}>
                                Complete
                              </Button>
                          )}
                          {canNotes && (
                              <Button size="sm" variant="ghost" className="px-2"
                                      onClick={() => navigate(`/doctor/consultation/${apt.id}/${apt.patientId}`)}>
                                <FileText className="h-4 w-4" />
                              </Button>
                          )}
                        </div>
                    );
                  })}
                </div>
            )}
          </motion.div>

          {/* Recent patients */}
          <motion.div
              initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.25 }}
              className="rounded-xl border border-border bg-card p-6"
          >
            <h3 className="text-lg font-semibold text-foreground mb-4">Recent Patients</h3>
            {recentPatients.length === 0 ? (
                <p className="text-sm text-muted-foreground">No recent patients on record.</p>
            ) : (
                <div className="space-y-4">
                  {recentPatients.map((p) => (
                      // Glance card: name, recency, and a clamped diagnosis. Consultation
                      // notes are full paragraphs and live on the patient record, one click away.
                      <button
                          key={p.patientProfileId}
                          type="button"
                          onClick={() => navigate(`/doctor/patients/${p.patientProfileId}`)}
                          className="w-full text-left p-3 rounded-lg bg-muted/30 hover:bg-muted/60 transition-colors"
                      >
                        <div className="flex items-center gap-3">
                          <div className="h-8 w-8 shrink-0 rounded-full bg-primary/10 flex items-center justify-center text-xs font-bold text-primary">
                            {p.firstName[0]}{p.lastName[0]}
                          </div>
                          <div className="flex-1 min-w-0">
                            <p className="text-sm font-medium text-foreground truncate">{p.firstName} {p.lastName}</p>
                            <p className="text-xs text-muted-foreground">Last visit: {relativeDate(p.lastVisitDate)}</p>
                          </div>
                        </div>
                        {p.diagnosis && (
                            <p className="text-xs text-muted-foreground mt-2 line-clamp-2" title={p.diagnosis}>
                              {p.diagnosis}
                            </p>
                        )}
                      </button>
                  ))}
                </div>
            )}
            <Button variant="outline" size="sm" className="w-full mt-4" onClick={() => navigate("/doctor/patients")}>
              View All Patients
            </Button>
          </motion.div>
        </div>

        {/* Confirmation dialog */}
        <AlertDialog open={!!pending} onOpenChange={(open) => { if (!open) setPending(null); }}>
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>{pending ? actionText(pending.status).title : ""}</AlertDialogTitle>
              <AlertDialogDescription>{pending ? actionText(pending.status).description : ""}</AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel>Go back</AlertDialogCancel>
              <AlertDialogAction
                  onClick={() => { if (pending) { updateStatus.mutate(pending); setPending(null); } }}
                  className={pending?.status === "CANCELLED" ? "bg-destructive hover:bg-destructive/90" : ""}>
                {pending ? actionText(pending.status).action : ""}
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </RolePageShell>
  );
}