import { useState } from "react";
import { RolePageShell } from "@/components/RolePageShell";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Search, Clock, User, Calendar, DoorClosed, MapPin } from "lucide-react";
import { useAllAppointments, useCancelAppointment } from "@/hooks/useAdmin";
import type { AppointmentMode, AppointmentResponse, AppointmentStatus, RoomType } from "@/types/api";

const STATUS_STYLES: Record<AppointmentStatus, string> = {
    SCHEDULED:    "bg-warning/10 text-warning border-warning/20",
    CONFIRMED:    "bg-success/10 text-success border-success/20",
    COMPLETED:    "bg-muted text-muted-foreground border-border",
    CANCELLED:    "bg-muted text-muted-foreground border-border",
    RESCHEDULED:  "bg-info/10 text-info border-info/20",
};

const MODE_STYLES: Record<AppointmentMode, string> = {
    IN_PERSON: "bg-primary/10 text-primary border-primary/20",
    VIDEO:     "bg-info/10 text-info border-info/20",
};

const MODE_LABELS: Record<AppointmentMode, string> = {
    IN_PERSON: "In-Person",
    VIDEO:     "Video",
};

const ROOM_TYPE_LABELS: Record<RoomType, string> = {
    CONSULT: "Consult Room",
    OR:      "Operating Room",
    IMAGING: "Imaging Room",
};

const STATUS_OPTIONS: Array<{ label: string; value: AppointmentStatus | "all" }> = [
    { label: "All Statuses", value: "all" },
    { label: "Scheduled",    value: "SCHEDULED" },
    { label: "Confirmed",    value: "CONFIRMED" },
    { label: "Completed",    value: "COMPLETED" },
    { label: "Cancelled",    value: "CANCELLED" },
    { label: "Rescheduled",  value: "RESCHEDULED" },
];

const MODE_OPTIONS: Array<{ label: string; value: AppointmentMode | "all" }> = [
    { label: "All Modes",  value: "all" },
    { label: "In-Person",  value: "IN_PERSON" },
    { label: "Video",      value: "VIDEO" },
];

function formatDate(iso: string) {
    const d = new Date(iso);
    const dd = String(d.getDate()).padStart(2, "0");
    const mm = String(d.getMonth() + 1).padStart(2, "0");
    return `${dd}-${mm}-${d.getFullYear()}`;
}

function formatTimeRange(iso: string, durationMinutes: number) {
    const start = new Date(iso);
    const end = new Date(start.getTime() + durationMinutes * 60_000);
    const fmt = (d: Date) => d.toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit", hour12: true });
    return `${fmt(start)} – ${fmt(end)}`;
}

export default function AdminAppointments() {
    const { data: appointments = [], isLoading } = useAllAppointments();
    const cancelAppointment = useCancelAppointment();

    const [search, setSearch] = useState("");
    const [statusFilter, setStatusFilter] = useState<AppointmentStatus | "all">("all");
    const [modeFilter, setModeFilter] = useState<AppointmentMode | "all">("all");
    const [selected, setSelected] = useState<AppointmentResponse | null>(null);

    const filtered = appointments.filter((a) => {
        const matchesSearch =
            a.patientName.toLowerCase().includes(search.toLowerCase()) ||
            a.doctorName.toLowerCase().includes(search.toLowerCase());
        const matchesStatus = statusFilter === "all" || a.status === statusFilter;
        const matchesMode = modeFilter === "all" || a.mode === modeFilter;
        return matchesSearch && matchesStatus && matchesMode;
    });

    const canCancel = (status: AppointmentStatus) =>
        status !== "COMPLETED" && status !== "CANCELLED";

    return (
        <RolePageShell title="Manage Appointments" subtitle="View and manage all appointments">
            <div className="flex flex-col sm:flex-row gap-4">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                    <Input
                        placeholder="Search by patient or doctor..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        className="pl-10"
                    />
                </div>
                <Select value={statusFilter} onValueChange={(v) => setStatusFilter(v as AppointmentStatus | "all")}>
                    <SelectTrigger className="w-[180px]">
                        <SelectValue placeholder="Status" />
                    </SelectTrigger>
                    <SelectContent>
                        {STATUS_OPTIONS.map((o) => (
                            <SelectItem key={o.value} value={o.value}>{o.label}</SelectItem>
                        ))}
                    </SelectContent>
                </Select>
                <Select value={modeFilter} onValueChange={(v) => setModeFilter(v as AppointmentMode | "all")}>
                    <SelectTrigger className="w-[160px]">
                        <SelectValue placeholder="Mode" />
                    </SelectTrigger>
                    <SelectContent>
                        {MODE_OPTIONS.map((o) => (
                            <SelectItem key={o.value} value={o.value}>{o.label}</SelectItem>
                        ))}
                    </SelectContent>
                </Select>
            </div>

            <div className="rounded-xl border border-border bg-card overflow-hidden">
                <table className="w-full">
                    <thead>
                        <tr className="border-b border-border bg-muted/50">
                            {["Patient", "Doctor", "Date & Time", "Duration", "Mode", "Room", "Status", "Actions"].map((h) => (
                                <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                                    {h}
                                </th>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {isLoading ? (
                            <tr>
                                <td colSpan={8} className="px-4 py-8 text-center text-sm text-muted-foreground">
                                    Loading appointments...
                                </td>
                            </tr>
                        ) : filtered.length === 0 ? (
                            <tr>
                                <td colSpan={8} className="px-4 py-8 text-center text-sm text-muted-foreground">
                                    No appointments found.
                                </td>
                            </tr>
                        ) : (
                            filtered.map((a) => (
                                    <tr key={a.id} className="border-b border-border hover:bg-muted/30 transition-colors">
                                        <td className="px-4 py-3 text-sm font-medium text-foreground">{a.patientName}</td>
                                        <td className="px-4 py-3 text-sm text-muted-foreground">{a.doctorName}</td>
                                        <td className="px-4 py-3 text-sm text-foreground">
                                            {formatDate(a.scheduledAt)}
                                            <br />
                                            <span className="text-xs text-muted-foreground">{formatTimeRange(a.scheduledAt, a.durationMinutes)}</span>
                                        </td>
                                        <td className="px-4 py-3 text-sm text-foreground">{a.durationMinutes} min</td>
                                        <td className="px-4 py-3">
                                            <Badge variant="outline" className={MODE_STYLES[a.mode]}>
                                                {MODE_LABELS[a.mode]}
                                            </Badge>
                                        </td>
                                        <td className="px-4 py-3 text-sm">
                                            {a.roomNumber ? (
                                                <div>
                                                    <p className="font-medium text-foreground">{a.roomNumber}</p>
                                                    <p className="text-xs text-muted-foreground">{a.roomDepartment ?? "—"}</p>
                                                </div>
                                            ) : (
                                                <span className="text-muted-foreground">—</span>
                                            )}
                                        </td>
                                        <td className="px-4 py-3">
                                            <Badge variant="outline" className={STATUS_STYLES[a.status]}>
                                                {a.status.charAt(0) + a.status.slice(1).toLowerCase()}
                                            </Badge>
                                        </td>
                                        <td className="px-4 py-3">
                                            <Button variant="ghost" size="sm" onClick={() => setSelected(a)}>
                                                Details
                                            </Button>
                                        </td>
                                    </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            <Dialog open={!!selected} onOpenChange={() => setSelected(null)}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Appointment #{selected?.id}</DialogTitle>
                    </DialogHeader>
                    {selected && (
                            <div className="grid gap-3 text-sm">
                                <InfoRow icon={User}     label="Patient"  value={selected.patientName} />
                                <InfoRow icon={User}     label="Doctor"   value={selected.doctorName} />
                                <InfoRow icon={Calendar} label="Date"     value={formatDate(selected.scheduledAt)} />
                                <InfoRow icon={Clock}    label="Time"     value={formatTimeRange(selected.scheduledAt, selected.durationMinutes)} />
                                <InfoRow icon={Calendar} label="Mode"     value={MODE_LABELS[selected.mode]} />
                                {selected.roomNumber && (
                                    <>
                                        <div className="border-t border-border pt-3 mt-1" />
                                        <InfoRow icon={DoorClosed} label="Room"       value={selected.roomNumber} />
                                        {selected.roomType && (
                                            <InfoRow icon={MapPin} label="Room Type"  value={ROOM_TYPE_LABELS[selected.roomType]} />
                                        )}
                                        {selected.roomDepartment && (
                                            <InfoRow icon={MapPin} label="Department" value={selected.roomDepartment} />
                                        )}
                                    </>
                                )}
                                {selected.reason && (
                                    <p className="pt-2 text-muted-foreground">
                                        <strong>Reason:</strong> {selected.reason}
                                    </p>
                                )}
                                {canCancel(selected.status) && (
                                    <div className="pt-2">
                                        <Button
                                            variant="destructive"
                                            className="w-full"
                                            disabled={cancelAppointment.isPending}
                                            onClick={() =>
                                                cancelAppointment.mutate(selected.id, {
                                                    onSuccess: () => setSelected(null),
                                                })
                                            }
                                        >
                                            Cancel Appointment
                                        </Button>
                                    </div>
                                )}
                            </div>
                    )}
                </DialogContent>
            </Dialog>
        </RolePageShell>
    );
}

function InfoRow({ icon: Icon, label, value }: { icon: React.ElementType; label: string; value: string }) {
    return (
        <div className="flex items-center gap-3">
            <Icon className="h-4 w-4 text-muted-foreground" />
            <span className="text-muted-foreground w-20">{label}</span>
            <span className="text-foreground">{value}</span>
        </div>
    );
}
