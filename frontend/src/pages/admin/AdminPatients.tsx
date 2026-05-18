import { useState } from "react";
import { RolePageShell } from "@/components/RolePageShell";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Search, User, Phone, Mail, Droplets, MapPin, AlertCircle, Calendar, Clock } from "lucide-react";
import { useAdminPatients, usePatientAppointments, useToggleUserStatus } from "@/hooks/useAdmin";
import type { AdminPatient, AppointmentStatus } from "@/types/api";
import * as React from "react";

const STATUS_STYLES: Record<AppointmentStatus, string> = {
    SCHEDULED:   "bg-warning/10 text-warning border-warning/20",
    CONFIRMED:   "bg-success/10 text-success border-success/20",
    IN_PROGRESS: "bg-primary/10 text-primary border-primary/20",
    COMPLETED:   "bg-muted text-muted-foreground border-border",
    CANCELLED:   "bg-muted text-muted-foreground border-border",
};

function calcAge(dob: string | null): string {
    if (!dob) return "—";
    const diff = Date.now() - new Date(dob).getTime();
    return String(Math.floor(diff / (1000 * 60 * 60 * 24 * 365.25)));
}

const BLOOD_TYPE_LABELS: Record<string, string> = {
    A_POSITIVE: "A+", A_NEGATIVE: "A−",
    B_POSITIVE: "B+", B_NEGATIVE: "B−",
    AB_POSITIVE: "AB+", AB_NEGATIVE: "AB−",
    O_POSITIVE: "O+", O_NEGATIVE: "O−",
    UNKNOWN: "Unknown",
};

function formatBloodType(bt: string | null): string {
    if (!bt) return "—";
    return BLOOD_TYPE_LABELS[bt] ?? bt;
}

function formatGender(g: string | null) {
    if (!g) return "—";
    return g.charAt(0) + g.slice(1).toLowerCase();
}

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

function PatientDetails({ patient, onToggleStatus }: { patient: AdminPatient; onToggleStatus: () => void }) {
    const { data: appointments = [], isLoading } = usePatientAppointments(patient.patientProfileId);

    const upcoming = appointments.filter((a) => a.status === "SCHEDULED" || a.status === "CONFIRMED" || a.status === "IN_PROGRESS");
    const past = appointments.filter((a) => a.status === "COMPLETED" || a.status === "CANCELLED");

    return (
        <div className="space-y-5">
            <div className="grid gap-2.5">
                <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Profile</p>
                <InfoRow icon={User}       label="Gender"    value={formatGender(patient.gender)} />
                <InfoRow icon={Droplets}   label="Blood Type" value={formatBloodType(patient.bloodType)} />
                <InfoRow icon={Phone}      label="Phone"     value={patient.phoneNumber ?? "—"} />
                <InfoRow icon={Mail}       label="Email"     value={patient.email} />
                {patient.address && (
                    <InfoRow icon={MapPin} label="Address"   value={patient.address} />
                )}
                {patient.emergencyContactName && (
                    <InfoRow icon={AlertCircle} label="Emergency"
                             value={`${patient.emergencyContactName}${patient.emergencyContactPhone ? ` · ${patient.emergencyContactPhone}` : ""}`}
                    />
                )}
            </div>

            <Button
                variant={patient.active ? "destructive" : "default"}
                size="sm"
                className="w-full"
                onClick={onToggleStatus}
            >
                {patient.active ? "Deactivate Account" : "Activate Account"}
            </Button>

            {isLoading ? (
                <p className="text-sm text-muted-foreground">Loading appointments...</p>
            ) : (
                <>
                    {upcoming.length > 0 && (
                        <div className="space-y-2">
                            <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Upcoming</p>
                            {upcoming.map((a) => (
                                <AppointmentRow key={a.id} doctor={a.doctorName} date={formatDate(a.scheduledAt)} timeRange={formatTimeRange(a.scheduledAt, a.durationMinutes)} status={a.status} />
                            ))}
                        </div>
                    )}
                    {past.length > 0 && (
                        <div className="space-y-2">
                            <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Past</p>
                            {past.slice(0, 5).map((a) => (
                                <AppointmentRow key={a.id} doctor={a.doctorName} date={formatDate(a.scheduledAt)} timeRange={formatTimeRange(a.scheduledAt, a.durationMinutes)} status={a.status} />
                            ))}
                        </div>
                    )}
                    {upcoming.length === 0 && past.length === 0 && (
                        <p className="text-sm text-muted-foreground">No appointments on record.</p>
                    )}
                </>
            )}
        </div>
    );
}

function AppointmentRow({ doctor, date, timeRange, status }: { doctor: string; date: string; timeRange: string; status: AppointmentStatus }) {
    return (
        <div className="flex items-center justify-between text-sm rounded-lg border border-border px-3 py-2">
            <div className="flex items-center gap-2 min-w-0">
                <Calendar className="h-3.5 w-3.5 text-muted-foreground shrink-0" />
                <span className="text-foreground truncate">{doctor}</span>
            </div>
            <div className="flex items-center gap-2 shrink-0">
                <span className="text-xs text-muted-foreground flex items-center gap-1">
                    <Clock className="h-3 w-3" />{date} · {timeRange}
                </span>
                <Badge variant="outline" className={`text-[10px] px-1.5 py-0 h-4 ${STATUS_STYLES[status]}`}>
                    {status.charAt(0) + status.slice(1).toLowerCase()}
                </Badge>
            </div>
        </div>
    );
}

export default function AdminPatients() {
    const { data: patients = [], isLoading } = useAdminPatients();
    const [search, setSearch] = useState("");
    const [statusFilter, setStatusFilter] = useState<"all" | "active" | "inactive">("all");
    const [selected, setSelected] = useState<AdminPatient | null>(null);
    const toggleStatus = useToggleUserStatus(() => setSelected(null));

    const filtered = patients.filter((p) => {
        const name = `${p.firstName} ${p.lastName}`.toLowerCase();
        const matchesSearch = name.includes(search.toLowerCase()) || p.email.toLowerCase().includes(search.toLowerCase());
        const matchesStatus = statusFilter === "all" || (statusFilter === "active" ? p.active : !p.active);
        return matchesSearch && matchesStatus;
    });

    return (
        <RolePageShell title="Manage Patients" subtitle="View and manage all patient records">
            <div className="flex flex-col sm:flex-row gap-4">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                    <Input placeholder="Search by name or email..." value={search} onChange={(e) => setSearch(e.target.value)} className="pl-10" />
                </div>
                <Select value={statusFilter} onValueChange={(v) => setStatusFilter(v as typeof statusFilter)}>
                    <SelectTrigger className="w-[160px]"><SelectValue placeholder="Status" /></SelectTrigger>
                    <SelectContent>
                        <SelectItem value="all">All Patients</SelectItem>
                        <SelectItem value="active">Active</SelectItem>
                        <SelectItem value="inactive">Inactive</SelectItem>
                    </SelectContent>
                </Select>
            </div>

            <div className="rounded-xl border border-border bg-card overflow-hidden">
                <table className="w-full">
                    <thead>
                    <tr className="border-b border-border bg-muted/50">
                        {["Patient", "Age", "Gender", "Phone", "Status", "Actions"].map((h) => (
                            <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-muted-foreground uppercase tracking-wider">{h}</th>
                        ))}
                    </tr>
                    </thead>
                    <tbody>
                    {isLoading ? (
                        <tr><td colSpan={6} className="px-4 py-8 text-center text-sm text-muted-foreground">Loading patients...</td></tr>
                    ) : filtered.length === 0 ? (
                        <tr><td colSpan={6} className="px-4 py-8 text-center text-sm text-muted-foreground">No patients found.</td></tr>
                    ) : (
                        filtered.map((p) => (
                            <tr key={p.patientProfileId} className="border-b border-border hover:bg-muted/30 transition-colors">
                                <td className="px-4 py-3">
                                    <div className="flex items-center gap-3">
                                        <div className="h-9 w-9 rounded-full bg-primary/10 flex items-center justify-center text-sm font-bold text-primary">
                                            {p.firstName[0]}{p.lastName[0]}
                                        </div>
                                        <div>
                                            <p className="text-sm font-medium text-foreground">{p.firstName} {p.lastName}</p>
                                            <p className="text-xs text-muted-foreground">{p.email}</p>
                                        </div>
                                    </div>
                                </td>
                                <td className="px-4 py-3 text-sm text-foreground">{calcAge(p.dateOfBirth)}</td>
                                <td className="px-4 py-3 text-sm text-foreground">{formatGender(p.gender)}</td>
                                <td className="px-4 py-3 text-sm text-muted-foreground">{p.phoneNumber ?? "—"}</td>
                                <td className="px-4 py-3">
                                    <Badge variant="outline" className={p.active ? "bg-success/10 text-success border-success/20" : "bg-muted text-muted-foreground border-border"}>
                                        {p.active ? "Active" : "Inactive"}
                                    </Badge>
                                </td>
                                <td className="px-4 py-3">
                                    <Button variant="ghost" size="sm" onClick={() => setSelected(p)}>Details</Button>
                                </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>

            <Dialog open={!!selected} onOpenChange={() => setSelected(null)}>
                <DialogContent className="max-w-lg max-h-[85vh] overflow-y-auto">
                    <DialogHeader>
                        <DialogTitle>{selected?.firstName} {selected?.lastName}</DialogTitle>
                    </DialogHeader>
                    {selected && (
                        <PatientDetails
                            patient={selected}
                            onToggleStatus={() =>
                                toggleStatus.mutate({ userId: selected.userId, active: !selected.active })
                            }
                        />
                    )}
                </DialogContent>
            </Dialog>
        </RolePageShell>
    );
}

function InfoRow({ icon: Icon, label, value }: { icon: React.ElementType; label: string; value: string }) {
    return (
        <div className="flex items-start gap-3 text-sm">
            <Icon className="h-4 w-4 text-muted-foreground mt-0.5 shrink-0" />
            <span className="text-muted-foreground w-24 shrink-0">{label}</span>
            <span className="text-foreground">{value}</span>
        </div>
    );
}