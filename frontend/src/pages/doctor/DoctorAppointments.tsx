import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { RolePageShell } from "@/components/RolePageShell";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog";
import { Search, FileText, User as UserIcon } from "lucide-react";
import { useDoctorAppointments, useUpdateAppointmentStatus } from "@/hooks/useDoctor";
import { ElapsedTimer } from "@/components/ElapsedTimer";
import type { AppointmentResponse, AppointmentStatus } from "@/types/api";


// Constants

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
    { label: "No-show",     value: "NO_SHOW" },
];

// Helpers

function actionText(status: AppointmentStatus): { title: string; description: string; action: string } {
    switch (status) {
        case "CONFIRMED":   return { title: "Confirm this appointment?",   description: "The patient will be notified that their appointment has been confirmed.",      action: "Confirm"  };
        case "IN_PROGRESS": return { title: "Start this consultation?",    description: "This will mark the appointment as in progress and start the elapsed timer.",  action: "Start"    };
        case "COMPLETED":   return { title: "Complete this consultation?", description: "This will mark the consultation as completed. This action cannot be undone.", action: "Complete" };
        case "CANCELLED":   return { title: "Cancel this appointment?",    description: "The patient will be notified. Cancelled appointments cannot be restarted.",   action: "Cancel"   };
        case "NO_SHOW":     return { title: "Mark patient as a no-show?",  description: "Record that the patient did not attend. They will be notified, and this cannot be undone.", action: "Mark no-show" };
        default:            return { title: "Confirm action?",             description: "Are you sure you want to proceed?",                                           action: "Continue" };
    }
}

/** A no-show can only be recorded once the appointment's start time has passed. */
function hasStarted(scheduledAt: string) {
    return new Date(scheduledAt).getTime() <= Date.now();
}

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

// Appointment Row

function AppointmentRow({
                            apt,
                            onNotes,
                            onViewPatient,
                            onAction,
                            isPending,
                        }: {
    apt: { id: number; patientId: number; patientName: string; mode: "IN_PERSON" | "VIDEO"; durationMinutes: number; reason: string | null; status: AppointmentStatus; scheduledAt: string; startedAt: string | null; completedAt: string | null };
    onNotes: (appointmentId: number, patientId: number) => void;
    onViewPatient: (patientId: number) => void;
    onAction: (appointmentId: number, status: AppointmentStatus) => void;
    isPending: boolean;
}) {
    const [hhmm, ampm] = formatTime(apt.scheduledAt).split(" ");
    const canNotes = apt.status === "IN_PROGRESS" || apt.status === "COMPLETED";

    return (
        <div className="flex items-center gap-3 p-3.5 rounded-xl border border-border bg-card hover:bg-muted/20 transition-colors">
            <div className="text-center shrink-0 w-14">
                <p className="text-sm font-semibold text-foreground">{hhmm}</p>
                <p className="text-xs text-muted-foreground">{ampm}</p>
            </div>
            <div className="h-8 w-px bg-border" />
            <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-foreground truncate">{apt.patientName}</p>
                <div className="flex items-center gap-1.5 mt-0.5 flex-wrap">
                    <Badge variant="outline" className={`text-[10px] px-1.5 py-0 h-4 ${MODE_STYLES[apt.mode]}`}>
                        {apt.mode === "IN_PERSON" ? "In-Person" : "Video"}
                    </Badge>
                    <span className="text-xs text-muted-foreground">{apt.durationMinutes} min</span>
                    {apt.reason && (
                        <span className="text-xs text-muted-foreground truncate max-w-[160px]">· {apt.reason}</span>
                    )}
                </div>
                {(apt.startedAt || apt.completedAt) && (
                    <div className="flex items-center gap-3 mt-1 text-xs text-muted-foreground">
                        {apt.startedAt && <span>Started {formatTime(apt.startedAt)}</span>}
                        {apt.status === "IN_PROGRESS" && apt.startedAt && (
                            <ElapsedTimer startedAt={apt.startedAt} />
                        )}
                        {apt.completedAt && <span>· Ended {formatTime(apt.completedAt)}</span>}
                    </div>
                )}
            </div>
            <Badge variant="outline" className={STATUS_STYLES[apt.status]}>
                {STATUS_LABELS[apt.status]}
            </Badge>
            <div className="flex items-center gap-1 shrink-0">
                {apt.status === "SCHEDULED" && (
                    <Button size="sm" variant="outline" disabled={isPending}
                            onClick={() => onAction(apt.id, "CONFIRMED")}>
                        Confirm
                    </Button>
                )}
                {apt.status === "CONFIRMED" && (
                    <Button size="sm" variant="outline" disabled={isPending}
                            onClick={() => onAction(apt.id, "IN_PROGRESS")}>
                        Start
                    </Button>
                )}
                {apt.status === "CONFIRMED" && hasStarted(apt.scheduledAt) && (
                    <Button size="sm" variant="ghost" className="text-muted-foreground hover:text-destructive hover:bg-destructive/10"
                            disabled={isPending}
                            onClick={() => onAction(apt.id, "NO_SHOW")}>
                        No-show
                    </Button>
                )}
                {(apt.status === "SCHEDULED" || apt.status === "CONFIRMED") && (
                    <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive hover:bg-destructive/10"
                            disabled={isPending}
                            onClick={() => onAction(apt.id, "CANCELLED")}>
                        Cancel
                    </Button>
                )}
                {apt.status === "IN_PROGRESS" && (
                    <Button size="sm" disabled={isPending}
                            onClick={() => onAction(apt.id, "COMPLETED")}>
                        Complete
                    </Button>
                )}
                {canNotes && (
                    <Button size="sm" variant="ghost" className="px-2" onClick={() => onNotes(apt.id, apt.patientId)}>
                        <FileText className="h-4 w-4" />
                    </Button>
                )}
                <Button size="sm" variant="ghost" className="px-2" onClick={() => onViewPatient(apt.patientId)}>
                    <UserIcon className="h-4 w-4" />
                </Button>
            </div>
        </div>
    );
}

// Page

export default function DoctorAppointments() {
    const navigate = useNavigate();
    const { data: appointments = [] } = useDoctorAppointments();
    const updateStatus = useUpdateAppointmentStatus();

    const [search, setSearch]             = useState("");
    const [statusFilter, setStatusFilter] = useState("all");
    const [pending, setPending]           = useState<{ appointmentId: number; status: AppointmentStatus } | null>(null);

    const filtered = useMemo(() =>
            appointments.filter((a) => {
                const matchesSearch = a.patientName.toLowerCase().includes(search.toLowerCase());
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
        isPending: updateStatus.isPending,
        onAction: (appointmentId: number, status: AppointmentStatus) => setPending({ appointmentId, status }),
        onNotes: (appointmentId: number, patientId: number) => navigate(`/doctor/consultation/${appointmentId}/${patientId}`),
        onViewPatient: (patientId: number) => navigate(`/doctor/patients/${patientId}`),
    };

    return (
        <RolePageShell title="My Appointments" subtitle="View and manage your appointments">
            {/* Filters */}
            <div className="flex flex-col sm:flex-row gap-3">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                    <Input
                        placeholder="Search by patient name..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        className="pl-10"
                    />
                </div>
                <Select value={statusFilter} onValueChange={setStatusFilter}>
                    <SelectTrigger className="w-[160px]">
                        <SelectValue placeholder="Status" />
                    </SelectTrigger>
                    <SelectContent>
                        {STATUS_OPTIONS.map((o) => (
                            <SelectItem key={o.value} value={o.value}>{o.label}</SelectItem>
                        ))}
                    </SelectContent>
                </Select>
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