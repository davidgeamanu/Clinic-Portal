import { useState, useMemo } from "react";
import type { ElementType } from "react";
import { RolePageShell } from "@/components/RolePageShell";
import { EmptyState } from "@/components/EmptyState";
import { CalendarIllustration } from "@/components/illustrations/calendar";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog";
import { ChevronLeft, ChevronRight, Video, MapPin, Clock, Calendar, User } from "lucide-react";
import { motion } from "framer-motion";
import { useDoctorAppointments, useUpdateAppointmentStatus } from "@/hooks/useDoctor";
import type { AppointmentMode, AppointmentResponse, AppointmentStatus } from "@/types/api";

const HOURS = Array.from({ length: 9 }, (_, i) => i + 8); // 8 AM – 4 PM
const DAY_ABBR  = ["Mon", "Tue", "Wed", "Thu", "Fri"];
const DAY_FULL  = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"];

const CARD_STYLES: Record<AppointmentStatus, string> = {
  SCHEDULED:   "bg-warning/10 border-warning/30 text-foreground",
  CONFIRMED:   "bg-primary/10 border-primary/30 text-foreground",
  IN_PROGRESS: "bg-success/10 border-success/30 text-foreground",
  COMPLETED:   "bg-muted border-border text-muted-foreground",
  CANCELLED:   "bg-muted border-border text-muted-foreground opacity-40",
  NO_SHOW:     "bg-destructive/10 border-destructive/30 text-destructive opacity-60",
};

const BADGE_STYLES: Record<AppointmentStatus, string> = {
  SCHEDULED:   "bg-warning/10 text-warning border-warning/20",
  CONFIRMED:   "bg-success/10 text-success border-success/20",
  IN_PROGRESS: "bg-primary/10 text-primary border-primary/20",
  COMPLETED:   "bg-muted text-muted-foreground border-border",
  CANCELLED:   "bg-muted text-muted-foreground border-border",
  NO_SHOW:     "bg-destructive/10 text-destructive border-destructive/20",
};

const STATUS_DISPLAY_LABELS: Record<AppointmentStatus, string> = {
  SCHEDULED:   "Awaiting Confirmation",
  CONFIRMED:   "Confirmed",
  IN_PROGRESS: "In Progress",
  COMPLETED:   "Completed",
  CANCELLED:   "Cancelled",
  NO_SHOW:     "No-show",
};

const STATUS_LABEL_COLORS: Record<AppointmentStatus, string> = {
  SCHEDULED:   "text-warning",
  CONFIRMED:   "text-primary",
  IN_PROGRESS: "text-success",
  COMPLETED:   "text-muted-foreground",
  CANCELLED:   "text-muted-foreground",
  NO_SHOW:     "text-destructive",
};

const MODE_LABELS: Record<AppointmentMode, string> = {
  IN_PERSON: "In-Person",
  VIDEO:     "Video",
};

// ── Date helpers ──

function getWeekDates(offset: number): Date[] {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const dow = today.getDay(); // 0=Sun
  const monday = new Date(today);
  monday.setDate(today.getDate() - (dow === 0 ? 6 : dow - 1) + offset * 7);
  return Array.from({ length: 5 }, (_, i) => {
    const d = new Date(monday);
    d.setDate(monday.getDate() + i);
    return d;
  });
}

function formatWeekLabel(offset: number, dates: Date[]): string {
  const fmt = (d: Date) =>
      `${String(d.getDate()).padStart(2, "0")} ${d.toLocaleString("en-US", { month: "short" })}`;
  const range = `${fmt(dates[0])} – ${fmt(dates[4])}`;
  if (offset === 0) return `This Week  (${range})`;
  if (offset === 1) return `Next Week  (${range})`;
  if (offset === -1) return `Last Week  (${range})`;
  return range;
}

function isToday(d: Date): boolean {
  const now = new Date();
  return d.getFullYear() === now.getFullYear() &&
      d.getMonth() === now.getMonth() &&
      d.getDate() === now.getDate();
}

function dayIndexOf(iso: string, weekDates: Date[]): number {
  const d = new Date(iso);
  return weekDates.findIndex(
      (wd) => wd.getFullYear() === d.getFullYear() &&
          wd.getMonth()   === d.getMonth()    &&
          wd.getDate()    === d.getDate()
  );
}

function formatTime(iso: string) {
  return new Date(iso).toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit", hour12: true });
}

function formatDate(iso: string) {
  const d = new Date(iso);
  return `${String(d.getDate()).padStart(2, "0")}-${String(d.getMonth() + 1).padStart(2, "0")}-${d.getFullYear()}`;
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

// ── Component ──

export default function DoctorSchedule() {
  const todayDow = new Date().getDay();
  const [weekOffset,  setWeekOffset]  = useState(0);
  const [view,        setView]        = useState<"week" | "day">("week");
  const [selectedDay, setSelectedDay] = useState(todayDow === 0 || todayDow === 6 ? 0 : todayDow - 1);
  const [selected,    setSelected]    = useState<AppointmentResponse | null>(null);
  const [pending,     setPending]     = useState<{ appointmentId: number; status: AppointmentStatus } | null>(null);

  const { data: appointments = [] } = useDoctorAppointments();
  const updateStatus = useUpdateAppointmentStatus();

  const weekDates = useMemo(() => getWeekDates(weekOffset), [weekOffset]);

  const weekAppts = useMemo(() => {
    const start = weekDates[0];
    const end   = new Date(weekDates[4]);
    end.setHours(23, 59, 59, 999);
    return appointments.filter((a) => {
      const d = new Date(a.scheduledAt);
      return d >= start && d <= end && a.status !== "CANCELLED";
    });
  }, [appointments, weekDates]);

  const dayAppts = useMemo(() =>
          weekAppts
              .filter((a) => dayIndexOf(a.scheduledAt, weekDates) === selectedDay)
              .sort((a, b) => new Date(a.scheduledAt).getTime() - new Date(b.scheduledAt).getTime()),
      [weekAppts, weekDates, selectedDay]
  );

  const triggerAction = (apt: AppointmentResponse, status: AppointmentStatus) => {
    setSelected(null);
    setPending({ appointmentId: apt.id, status });
  };

  return (
      <RolePageShell title="My Schedule" subtitle="Manage your weekly schedule">

        {/* ── Controls ── */}
        <div className="flex items-center justify-between flex-wrap gap-3">
          <div className="flex items-center gap-2">
            <Button variant="outline" size="icon" onClick={() => setWeekOffset(weekOffset - 1)}>
              <ChevronLeft className="h-4 w-4" />
            </Button>
            <span className="text-sm font-medium text-foreground min-w-[230px] text-center">
                        {formatWeekLabel(weekOffset, weekDates)}
                    </span>
            <Button variant="outline" size="icon" onClick={() => setWeekOffset(weekOffset + 1)}>
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
          <Select value={view} onValueChange={(v) => setView(v as "week" | "day")}>
            <SelectTrigger className="w-[130px]"><SelectValue /></SelectTrigger>
            <SelectContent>
              <SelectItem value="week">Week View</SelectItem>
              <SelectItem value="day">Day View</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* ── Week view ── */}
        {view === "week" ? (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="rounded-xl border border-border bg-card overflow-hidden">
              <div className="grid grid-cols-[80px_repeat(5,1fr)] border-b border-border bg-muted/50">
                <div className="p-3 text-xs font-semibold text-muted-foreground uppercase">Time</div>
                {weekDates.map((d, i) => (
                    <div
                        key={i}
                        className={`p-3 text-center text-xs font-semibold uppercase cursor-pointer hover:text-foreground transition-colors ${isToday(d) ? "text-primary" : "text-muted-foreground"}`}
                        onClick={() => { setSelectedDay(i); setView("day"); }}
                    >
                      {DAY_ABBR[i]} <span className="font-normal">{d.getDate()}</span>
                    </div>
                ))}
              </div>

              {HOURS.map((hour) => {
                const hourLabel = `${hour > 12 ? hour - 12 : hour}:00 ${hour >= 12 ? "PM" : "AM"}`;
                const halfLabel = `${hour > 12 ? hour - 12 : hour}:30 ${hour >= 12 ? "PM" : "AM"}`;
                return (
                    <div key={hour} className="grid grid-cols-[80px_repeat(5,1fr)] border-b border-border last:border-0 h-24">
                      <div className="text-xs text-muted-foreground flex flex-col pr-3">
                        <div className="flex-1 flex items-start justify-end pt-1 leading-none">{hourLabel}</div>
                        <div className="flex-1 flex items-start justify-end pt-1 leading-none text-[10px] text-muted-foreground/40">{halfLabel}</div>
                      </div>
                      {weekDates.map((_, dayIdx) => {
                        const early = weekAppts.filter(
                            (a) => dayIndexOf(a.scheduledAt, weekDates) === dayIdx &&
                                new Date(a.scheduledAt).getHours() === hour &&
                                new Date(a.scheduledAt).getMinutes() < 30
                        );
                        const late = weekAppts.filter(
                            (a) => dayIndexOf(a.scheduledAt, weekDates) === dayIdx &&
                                new Date(a.scheduledAt).getHours() === hour &&
                                new Date(a.scheduledAt).getMinutes() >= 30
                        );
                        return (
                            <div key={dayIdx} className="border-l border-border flex flex-col overflow-hidden">
                              <SlotHalf appts={early} onSelect={setSelected} />
                              <SlotHalf appts={late}  onSelect={setSelected} divider />
                            </div>
                        );
                      })}
                    </div>
                );
              })}
            </motion.div>
        ) : (
            /* ── Day view ── */
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-4">
              <div className="flex gap-2 flex-wrap">
                {weekDates.map((d, i) => (
                    <Button
                        key={i}
                        variant={selectedDay === i ? "default" : "outline"}
                        size="sm"
                        onClick={() => setSelectedDay(i)}
                        className={isToday(d) && selectedDay !== i ? "border-primary text-primary" : ""}
                    >
                      {DAY_ABBR[i]} {d.getDate()}
                    </Button>
                ))}
              </div>

              <h3 className="text-lg font-semibold text-foreground">
                {DAY_FULL[selectedDay]}, {weekDates[selectedDay].toLocaleString("en-US", { month: "long", day: "numeric", year: "numeric" })}
              </h3>

              {dayAppts.length === 0 ? (
                  <EmptyState
                      size="md"
                      illustration={CalendarIllustration}
                      title={`Nothing booked for ${DAY_FULL[selectedDay]}`}
                      description="This day is completely free. Pick another day to see its schedule."
                  />
              ) : (
                  <div className="space-y-3">
                    {dayAppts.map((apt, i) => {
                      const [hhmm, ampm] = formatTime(apt.scheduledAt).split(" ");
                      return (
                          <motion.div
                              key={apt.id}
                              initial={{ opacity: 0, x: -8 }}
                              animate={{ opacity: 1, x: 0 }}
                              transition={{ delay: i * 0.05 }}
                              className="flex items-center gap-4 p-4 rounded-xl border border-border bg-card hover:bg-muted/30 transition-colors"
                          >
                            <div className="text-center shrink-0 w-16">
                              <p className="text-sm font-bold text-foreground">{hhmm}</p>
                              <p className="text-xs text-muted-foreground">{ampm}</p>
                            </div>
                            <div className="h-10 w-px bg-border" />
                            <div className="flex-1 min-w-0">
                              <p className="text-sm font-medium text-foreground">{apt.patientName}</p>
                              <div className="flex items-center gap-1.5 mt-0.5">
                                {apt.mode === "VIDEO"
                                    ? <Video className="h-3.5 w-3.5 text-info" />
                                    : <MapPin className="h-3.5 w-3.5 text-muted-foreground" />}
                                <span className="text-xs text-muted-foreground">{MODE_LABELS[apt.mode]} · {apt.durationMinutes} min</span>
                              </div>
                            </div>
                            <Badge variant="outline" className={BADGE_STYLES[apt.status]}>
                              {STATUS_DISPLAY_LABELS[apt.status]}
                            </Badge>
                            {apt.status === "SCHEDULED" && (
                                <Button size="sm" variant="outline" disabled={updateStatus.isPending}
                                        onClick={() => triggerAction(apt, "CONFIRMED")}>Confirm</Button>
                            )}
                            {apt.status === "CONFIRMED" && (
                                <Button size="sm" variant="outline" disabled={updateStatus.isPending}
                                        onClick={() => triggerAction(apt, "IN_PROGRESS")}>Start</Button>
                            )}
                            {apt.status === "IN_PROGRESS" && (
                                <Button size="sm" disabled={updateStatus.isPending}
                                        onClick={() => triggerAction(apt, "COMPLETED")}>Complete</Button>
                            )}
                            {(apt.status === "SCHEDULED" || apt.status === "CONFIRMED") && (
                                <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive hover:bg-destructive/10"
                                        disabled={updateStatus.isPending}
                                        onClick={() => triggerAction(apt, "CANCELLED")}>Cancel</Button>
                            )}
                            <Button variant="outline" size="sm" onClick={() => setSelected(apt)}>Details</Button>
                          </motion.div>
                      );
                    })}
                  </div>
              )}
            </motion.div>
        )}

        {/* ── Summary footer ── */}
        <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }} className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="rounded-xl border border-border bg-card p-4 text-center">
            <p className="text-2xl font-bold text-foreground">{weekAppts.length}</p>
            <p className="text-xs text-muted-foreground">This Week's Appointments</p>
          </div>
          <div className="rounded-xl border border-border bg-card p-4 text-center">
            <p className="text-2xl font-bold text-foreground">{weekAppts.filter((a) => a.mode === "VIDEO").length}</p>
            <p className="text-xs text-muted-foreground">Video Consultations</p>
          </div>
          <div className="rounded-xl border border-border bg-card p-4 text-center">
            <p className="text-2xl font-bold text-foreground">{weekAppts.filter((a) => a.status === "SCHEDULED").length}</p>
            <p className="text-xs text-muted-foreground">Pending Confirmations</p>
          </div>
        </motion.div>

        {/* ── Detail dialog ── */}
        <Dialog open={!!selected} onOpenChange={() => setSelected(null)}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Appointment #{selected?.id}</DialogTitle>
            </DialogHeader>
            {selected && (
                <div className="grid gap-3 text-sm">
                  <InfoRow icon={User}     label="Patient" value={selected.patientName} />
                  <InfoRow icon={Calendar} label="Date"    value={formatDate(selected.scheduledAt)} />
                  <InfoRow icon={Clock}    label="Time"    value={`${formatTime(selected.scheduledAt)} · ${selected.durationMinutes} min`} />
                  <InfoRow icon={selected.mode === "VIDEO" ? Video : MapPin} label="Mode" value={MODE_LABELS[selected.mode]} />
                  {selected.roomNumber && (
                      <InfoRow icon={MapPin} label="Room" value={`${selected.roomNumber}${selected.roomDepartment ? ` · ${selected.roomDepartment}` : ""}`} />
                  )}
                  {selected.reason && (
                      <p className="text-muted-foreground pt-1"><strong>Reason:</strong> {selected.reason}</p>
                  )}
                  {(selected.status === "SCHEDULED" || selected.status === "CONFIRMED" || selected.status === "IN_PROGRESS") && (
                      <div className="flex gap-2 pt-2">
                        {selected.status === "SCHEDULED" && (
                            <Button size="sm" variant="outline" className="flex-1" disabled={updateStatus.isPending}
                                    onClick={() => triggerAction(selected, "CONFIRMED")}>
                              Confirm
                            </Button>
                        )}
                        {selected.status === "CONFIRMED" && (
                            <Button size="sm" variant="outline" className="flex-1" disabled={updateStatus.isPending}
                                    onClick={() => triggerAction(selected, "IN_PROGRESS")}>
                              Start
                            </Button>
                        )}
                        {selected.status === "IN_PROGRESS" && (
                            <Button size="sm" className="flex-1" disabled={updateStatus.isPending}
                                    onClick={() => triggerAction(selected, "COMPLETED")}>
                              Complete
                            </Button>
                        )}
                        {(selected.status === "SCHEDULED" || selected.status === "CONFIRMED") && (
                            <Button size="sm" variant="ghost" className="flex-1 text-destructive hover:text-destructive hover:bg-destructive/10"
                                    disabled={updateStatus.isPending}
                                    onClick={() => triggerAction(selected, "CANCELLED")}>
                              Cancel
                            </Button>
                        )}
                      </div>
                  )}
                </div>
            )}
          </DialogContent>
        </Dialog>

        {/* ── Confirmation dialog ── */}
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

function InfoRow({ icon: Icon, label, value }: { icon: ElementType; label: string; value: string }) {
  return (
      <div className="flex items-center gap-3">
        <Icon className="h-4 w-4 text-muted-foreground shrink-0" />
        <span className="text-muted-foreground w-16 shrink-0">{label}</span>
        <span className="text-foreground">{value}</span>
      </div>
  );
}

function SlotHalf({
                    appts, onSelect, divider = false,
                  }: {
  appts: AppointmentResponse[];
  onSelect: (a: AppointmentResponse) => void;
  divider?: boolean;
}) {
  return (
      <div className={`flex-1 min-h-0 overflow-hidden ${divider ? "border-t border-dashed border-border/40" : ""}`}>
        {appts.map((a) => (
            <div
                key={a.id}
                className={`border h-full px-2 py-1 text-xs cursor-pointer hover:shadow-sm transition-shadow flex flex-col justify-center gap-0.5 ${CARD_STYLES[a.status]}`}
                onClick={() => onSelect(a)}
            >
              <p className="font-medium truncate leading-tight">{a.patientName}</p>
              <div className="flex items-center gap-1 min-w-0">
                {a.mode === "VIDEO"
                    ? <Video  className="h-3 w-3 shrink-0 text-muted-foreground" />
                    : <MapPin className="h-3 w-3 shrink-0 text-muted-foreground" />}
                <span className="text-muted-foreground shrink-0">{a.durationMinutes}m</span>
                <span className={`truncate text-[10px] font-medium ${STATUS_LABEL_COLORS[a.status]}`}>· {STATUS_DISPLAY_LABELS[a.status]}</span>
              </div>
            </div>
        ))}
      </div>
  );
}