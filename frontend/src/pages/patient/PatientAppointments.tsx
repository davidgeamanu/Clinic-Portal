import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { RolePageShell } from "@/components/RolePageShell";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import {
  AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent,
  AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Search, Star, MapPin, Video, Plus } from "lucide-react";
import { RateDoctorDialog } from "@/components/RateDoctorDialog";
import {
  usePatientAppointments,
  useUpdatePatientAppointmentStatus,
  useRateAppointment,
} from "@/hooks/usePatient";
import type { AppointmentResponse, AppointmentStatus } from "@/types/api";

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

const MODE_STYLES = {
  IN_PERSON: "bg-info/10 text-info border-info/20",
  VIDEO:     "bg-secondary/10 text-secondary-foreground border-secondary/20",
} as const;

const STATUS_OPTIONS = [
  { label: "All",         value: "all" },
  { label: "Upcoming",    value: "SCHEDULED" },
  { label: "Confirmed",   value: "CONFIRMED" },
  { label: "In Progress", value: "IN_PROGRESS" },
  { label: "Completed",   value: "COMPLETED" },
  { label: "Cancelled",   value: "CANCELLED" },
];

function getDateStr(scheduledAt: string) {
  return scheduledAt.split("T")[0];
}

function formatTime(iso: string) {
  return new Date(iso).toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit", hour12: true });
}

function formatSectionHeader(dateStr: string): string {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const date = new Date(dateStr + "T00:00:00");
  const diff = Math.round((date.getTime() - today.getTime()) / 86_400_000);
  const label = date.toLocaleDateString("en-US", { month: "long", day: "numeric" });
  if (diff === 0)  return `Today — ${label}`;
  if (diff === 1)  return `Tomorrow — ${label}`;
  if (diff === -1) return `Yesterday — ${label}`;
  return date.toLocaleDateString("en-US", { weekday: "long", month: "long", day: "numeric" });
}

function groupByDate(appointments: AppointmentResponse[]): [string, AppointmentResponse[]][] {
  const map = new Map<string, AppointmentResponse[]>();
  for (const apt of appointments) {
    const key = getDateStr(apt.scheduledAt);
    if (!map.has(key)) map.set(key, []);
    map.get(key)!.push(apt);
  }
  return Array.from(map.entries());
}

function StaticStars({ rating }: { rating: number }) {
  return (
    <div className="flex items-center gap-0.5">
      {[1, 2, 3, 4, 5].map((s) => (
        <Star
          key={s}
          className={`h-3.5 w-3.5 ${s <= rating ? "fill-warning text-warning" : "text-muted-foreground/30"}`}
        />
      ))}
    </div>
  );
}

function AppointmentRow({
  apt,
  onCancel,
  onRate,
  isCancelling,
}: {
  apt: AppointmentResponse;
  onCancel: (id: number) => void;
  onRate: (apt: AppointmentResponse) => void;
  isCancelling: boolean;
}) {
  const [hhmm, ampm] = formatTime(apt.scheduledAt).split(" ");
  const canCancel = apt.status === "SCHEDULED" || apt.status === "CONFIRMED";
  const canRate   = apt.status === "COMPLETED" && apt.rating == null;
  const isRated   = apt.status === "COMPLETED" && apt.rating != null;

  return (
    <div className="flex items-center gap-3 p-3.5 rounded-xl border border-border bg-card hover:bg-muted/20 transition-colors">
      <div className="text-center shrink-0 w-14">
        <p className="text-sm font-semibold text-foreground">{hhmm}</p>
        <p className="text-xs text-muted-foreground">{ampm}</p>
      </div>
      <div className="h-8 w-px bg-border" />
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium text-foreground truncate">Dr. {apt.doctorName}</p>
        <div className="flex items-center gap-1.5 mt-0.5 flex-wrap">
          <Badge variant="outline" className={`text-[10px] px-1.5 py-0 h-4 ${MODE_STYLES[apt.mode]}`}>
            {apt.mode === "VIDEO" ? <Video className="h-2.5 w-2.5 mr-0.5" /> : <MapPin className="h-2.5 w-2.5 mr-0.5" />}
            {apt.mode === "IN_PERSON" ? "In-Person" : "Video"}
          </Badge>
          <span className="text-xs text-muted-foreground">{apt.durationMinutes} min</span>
          {apt.roomNumber && <span className="text-xs text-muted-foreground">· Room {apt.roomNumber}</span>}
          {apt.reason && (
            <span className="text-xs text-muted-foreground truncate max-w-[180px]">· {apt.reason}</span>
          )}
        </div>
        {isRated && (
          <div className="flex items-center gap-1.5 mt-1">
            <StaticStars rating={apt.rating!} />
            <span className="text-[10px] text-muted-foreground">Your rating</span>
          </div>
        )}
      </div>
      <Badge variant="outline" className={STATUS_STYLES[apt.status]}>
        {STATUS_LABELS[apt.status]}
      </Badge>
      <div className="flex items-center gap-1 shrink-0">
        {canCancel && (
          <Button size="sm" variant="ghost"
            className="text-destructive hover:text-destructive hover:bg-destructive/10"
            disabled={isCancelling}
            onClick={() => onCancel(apt.id)}>
            Cancel
          </Button>
        )}
        {canRate && (
          <Button size="sm" variant="outline" className="gap-1.5" onClick={() => onRate(apt)}>
            <Star className="h-3.5 w-3.5" /> Rate Visit
          </Button>
        )}
      </div>
    </div>
  );
}

export default function PatientAppointments() {
  const navigate = useNavigate();
  const { data: appointments = [] } = usePatientAppointments();
  const updateStatus = useUpdatePatientAppointmentStatus();
  const rate = useRateAppointment(() => setRatingTarget(null));

  const [search, setSearch]               = useState("");
  const [statusFilter, setStatusFilter]   = useState("all");
  const [pendingCancel, setPendingCancel] = useState<number | null>(null);
  const [ratingTarget, setRatingTarget]   = useState<AppointmentResponse | null>(null);

  const filtered = useMemo(() =>
    appointments.filter((a) => {
      const matchesSearch = a.doctorName.toLowerCase().includes(search.toLowerCase())
        || (a.reason ?? "").toLowerCase().includes(search.toLowerCase());
      const matchesStatus = statusFilter === "all" || a.status === statusFilter;
      return matchesSearch && matchesStatus;
    }),
    [appointments, search, statusFilter]
  );

  const { upcoming, past } = useMemo(() => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const up = filtered
      .filter((a) => new Date(getDateStr(a.scheduledAt) + "T00:00:00") >= today)
      .sort((a, b) => new Date(a.scheduledAt).getTime() - new Date(b.scheduledAt).getTime());
    const ps = filtered
      .filter((a) => new Date(getDateStr(a.scheduledAt) + "T00:00:00") < today)
      .sort((a, b) => new Date(b.scheduledAt).getTime() - new Date(a.scheduledAt).getTime());
    return { upcoming: up, past: ps };
  }, [filtered]);

  const upcomingGroups = useMemo(() => groupByDate(upcoming), [upcoming]);
  const pastGroups     = useMemo(() => groupByDate(past),     [past]);

  const rowProps = {
    isCancelling: updateStatus.isPending,
    onCancel: (id: number) => setPendingCancel(id),
    onRate:   (apt: AppointmentResponse) => setRatingTarget(apt),
  };

  return (
    <RolePageShell title="My Appointments" subtitle="View, cancel, and rate your appointments">
      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search by doctor or reason..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-10"
          />
        </div>
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Status" />
          </SelectTrigger>
          <SelectContent>
            {STATUS_OPTIONS.map((o) => (
              <SelectItem key={o.value} value={o.value}>{o.label}</SelectItem>
            ))}
          </SelectContent>
        </Select>
        <Button className="gap-1.5" onClick={() => navigate("/patient/book")}>
          <Plus className="h-4 w-4" /> Book New
        </Button>
      </div>

      {/* Upcoming */}
      {upcomingGroups.length > 0 && (
        <div className="space-y-4">
          {upcomingGroups.map(([dateStr, apts]) => (
            <div key={dateStr}>
              <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-2 px-1">
                {formatSectionHeader(dateStr)}
              </p>
              <div className="space-y-2">
                {apts.map((a) => <AppointmentRow key={a.id} apt={a} {...rowProps} />)}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Past */}
      {pastGroups.length > 0 && (
        <div className="space-y-4">
          {upcomingGroups.length > 0 && (
            <div className="flex items-center gap-3 py-1">
              <div className="flex-1 h-px bg-border" />
              <span className="text-xs text-muted-foreground font-medium">Past Appointments</span>
              <div className="flex-1 h-px bg-border" />
            </div>
          )}
          {pastGroups.map(([dateStr, apts]) => (
            <div key={dateStr}>
              <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-2 px-1">
                {formatSectionHeader(dateStr)}
              </p>
              <div className="space-y-2">
                {apts.map((a) => <AppointmentRow key={a.id} apt={a} {...rowProps} />)}
              </div>
            </div>
          ))}
        </div>
      )}

      {upcomingGroups.length === 0 && pastGroups.length === 0 && (
        <p className="text-center text-muted-foreground py-12">No appointments found.</p>
      )}

      {/* Cancel confirmation */}
      <AlertDialog open={pendingCancel !== null} onOpenChange={(o) => { if (!o) setPendingCancel(null); }}>
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
              className="bg-destructive hover:bg-destructive/90"
              onClick={() => {
                if (pendingCancel !== null) {
                  updateStatus.mutate({ appointmentId: pendingCancel, status: "CANCELLED" });
                  setPendingCancel(null);
                }
              }}>
              Cancel appointment
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Rate dialog */}
      {ratingTarget && (
        <RateDoctorDialog
          open={ratingTarget !== null}
          onOpenChange={(o) => { if (!o) setRatingTarget(null); }}
          doctor={`Dr. ${ratingTarget.doctorName}`}
          department={ratingTarget.roomDepartment ?? undefined}
          submitting={rate.isPending}
          onSubmit={(rating, review) =>
            rate.mutate({ appointmentId: ratingTarget.id, data: { rating, review: review || undefined } })
          }
        />
      )}
    </RolePageShell>
  );
}