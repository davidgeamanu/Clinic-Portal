import { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { RolePageShell } from "@/components/RolePageShell";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Label } from "@/components/ui/label";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Search, Plus, Mail, Phone, DoorClosed, CreditCard, FileText, Calendar, Clock, Building2 } from "lucide-react";
import {
    useAllDoctorProfiles,
    useCreateDoctor,
    useToggleUserStatus,
    useSpecializations,
    useDoctorAppointments,
} from "@/hooks/useAdmin";
import type { AdminCreateDoctorRequest, AppointmentStatus, DoctorProfileResponse } from "@/types/api";

const APPOINTMENT_STATUS_STYLES: Record<AppointmentStatus, string> = {
    SCHEDULED:   "bg-warning/10 text-warning border-warning/20",
    CONFIRMED:   "bg-success/10 text-success border-success/20",
    IN_PROGRESS: "bg-primary/10 text-primary border-primary/20",
    COMPLETED:   "bg-muted text-muted-foreground border-border",
    CANCELLED:   "bg-muted text-muted-foreground border-border",
};

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

// ── Doctor Details Panel ──────────────────────────────────────────────────────

function DoctorDetails({ doctor, onToggleStatus }: { doctor: DoctorProfileResponse; onToggleStatus: () => void }) {
    const navigate = useNavigate();
    const { data: appointments = [], isLoading } = useDoctorAppointments(doctor.id);
    const [deptPickerOpen, setDeptPickerOpen] = useState(false);

    const upcoming = appointments.filter((a) => a.status === "SCHEDULED" || a.status === "CONFIRMED" || a.status === "IN_PROGRESS");
    const past = appointments.filter((a) => a.status === "COMPLETED" || a.status === "CANCELLED");

    const navigateToDept = (deptName: string) =>
        navigate(`/admin/building?dept=${encodeURIComponent(deptName)}`);

    const handleManageRoom = () => {
        if (doctor.specializations.length > 1) {
            setDeptPickerOpen((o) => !o);
        } else {
            const dept = doctor.specializations[0]?.name;
            dept ? navigateToDept(dept) : navigate("/admin/building");
        }
    };

    return (
        <div className="space-y-5">
            <div className="grid gap-2.5">
                <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Profile</p>
                <InfoRow icon={Mail}       label="Email"        value={doctor.email} />
                {doctor.phoneNumber && <InfoRow icon={Phone} label="Phone" value={doctor.phoneNumber} />}
                {doctor.consultationFee != null && (
                    <InfoRow icon={CreditCard} label="Fee" value={`$${doctor.consultationFee}`} />
                )}
                {doctor.biography && (
                    <div className="flex items-start gap-3 text-sm">
                        <FileText className="h-4 w-4 text-muted-foreground mt-0.5 shrink-0" />
                        <span className="text-muted-foreground w-24 shrink-0">Biography</span>
                        <span className="text-foreground">{doctor.biography}</span>
                    </div>
                )}
                {doctor.specializations.length > 0 && (
                    <div className="flex items-start gap-3 text-sm">
                        <DoorClosed className="h-4 w-4 text-muted-foreground mt-0.5 shrink-0" />
                        <span className="text-muted-foreground w-24 shrink-0">Departments</span>
                        <div className="flex flex-wrap gap-1">
                            {doctor.specializations.map((s) => (
                                <Badge key={s.id} variant="outline" className="text-xs">{s.name}</Badge>
                            ))}
                        </div>
                    </div>
                )}
            </div>

            <div className="space-y-1.5">
                <div className="flex items-center justify-between rounded-lg border border-border bg-muted/30 px-3 py-2">
                    <div className="flex items-center gap-2 text-sm">
                        <DoorClosed className="h-4 w-4 text-muted-foreground shrink-0" />
                        <span className="text-foreground">{doctor.roomNumber ?? "No room assigned"}</span>
                    </div>
                    <Button variant="outline" size="sm" onClick={handleManageRoom}>
                        <Building2 className="h-3.5 w-3.5" />
                        {doctor.specializations.length > 1 && deptPickerOpen ? "Close" : "Manage Room"}
                    </Button>
                </div>
                {deptPickerOpen && doctor.specializations.length > 1 && (
                    <div className="rounded-lg border border-border bg-muted/20 p-2 space-y-1">
                        <p className="text-xs text-muted-foreground px-1 pb-0.5">Browse rooms by department:</p>
                        {doctor.specializations.map((s) => (
                            <button
                                key={s.id}
                                className="w-full text-left text-sm px-2 py-1.5 rounded hover:bg-accent transition-colors text-foreground"
                                onClick={() => navigateToDept(s.name)}
                            >
                                {s.name}
                            </button>
                        ))}
                    </div>
                )}
            </div>

            <Button
                variant={doctor.active ? "destructive" : "default"}
                size="sm"
                className="w-full"
                onClick={onToggleStatus}
            >
                {doctor.active ? "Deactivate Account" : "Activate Account"}
            </Button>

            {isLoading ? (
                <p className="text-sm text-muted-foreground">Loading appointments...</p>
            ) : (
                <>
                    {upcoming.length > 0 && (
                        <div className="space-y-2">
                            <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Upcoming</p>
                            {upcoming.map((a) => (
                                <AppointmentRow key={a.id} patient={a.patientName} date={formatDate(a.scheduledAt)} timeRange={formatTimeRange(a.scheduledAt, a.durationMinutes)} status={a.status} />
                            ))}
                        </div>
                    )}
                    {past.length > 0 && (
                        <div className="space-y-2">
                            <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Past</p>
                            {past.slice(0, 5).map((a) => (
                                <AppointmentRow key={a.id} patient={a.patientName} date={formatDate(a.scheduledAt)} timeRange={formatTimeRange(a.scheduledAt, a.durationMinutes)} status={a.status} />
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

function AppointmentRow({ patient, date, timeRange, status }: { patient: string; date: string; timeRange: string; status: AppointmentStatus }) {
    return (
        <div className="flex items-center justify-between text-sm rounded-lg border border-border px-3 py-2">
            <div className="flex items-center gap-2 min-w-0">
                <Calendar className="h-3.5 w-3.5 text-muted-foreground shrink-0" />
                <span className="text-foreground truncate">{patient}</span>
            </div>
            <div className="flex items-center gap-2 shrink-0">
                <span className="text-xs text-muted-foreground flex items-center gap-1">
                    <Clock className="h-3 w-3" />{date} · {timeRange}
                </span>
                <Badge variant="outline" className={`text-[10px] px-1.5 py-0 h-4 ${APPOINTMENT_STATUS_STYLES[status]}`}>
                    {status.charAt(0) + status.slice(1).toLowerCase()}
                </Badge>
            </div>
        </div>
    );
}

// ── Create Doctor Dialog ──────────────────────────────────────────────────────

const EMPTY_FORM: AdminCreateDoctorRequest = {
    email: "", password: "", firstName: "", lastName: "",
    phoneNumber: "", licenseNumber: "", biography: "", specializationIds: [],
};

function CreateDoctorDialog({ open, onOpenChange }: { open: boolean; onOpenChange: (v: boolean) => void }) {
    const { data: specializations = [] } = useSpecializations();
    const [form, setForm] = useState<AdminCreateDoctorRequest>(EMPTY_FORM);
    const createDoctor = useCreateDoctor(() => { setForm(EMPTY_FORM); onOpenChange(false); });

    const set = (field: keyof AdminCreateDoctorRequest, value: unknown) =>
        setForm((prev) => ({ ...prev, [field]: value }));

    const toggleSpec = (id: number) =>
        set("specializationIds", form.specializationIds?.includes(id)
            ? form.specializationIds.filter((s) => s !== id)
            : [...(form.specializationIds ?? []), id]);

    const handleSubmit = () => {
        if (!form.email || !form.password || !form.firstName || !form.lastName || !form.licenseNumber) return;
        createDoctor.mutate(
            { ...form, phoneNumber: form.phoneNumber || undefined, biography: form.biography || undefined }
        );
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="max-w-lg max-h-[85vh] overflow-y-auto">
                <DialogHeader>
                    <DialogTitle>Add New Doctor</DialogTitle>
                </DialogHeader>
                <div className="grid gap-4 pt-2">
                    <div className="grid grid-cols-2 gap-3">
                        <Field label="First Name *">
                            <Input value={form.firstName} onChange={(e) => set("firstName", e.target.value)} placeholder="John" />
                        </Field>
                        <Field label="Last Name *">
                            <Input value={form.lastName} onChange={(e) => set("lastName", e.target.value)} placeholder="Smith" />
                        </Field>
                    </div>
                    <div className="grid grid-cols-2 gap-3">
                        <Field label="Email *">
                            <Input type="email" value={form.email} onChange={(e) => set("email", e.target.value)} placeholder="doctor@clinic.com" />
                        </Field>
                        <Field label="Password *">
                            <Input type="password" value={form.password} onChange={(e) => set("password", e.target.value)} />
                        </Field>
                    </div>
                    <div className="grid grid-cols-2 gap-3">
                        <Field label="Phone">
                            <Input value={form.phoneNumber} onChange={(e) => set("phoneNumber", e.target.value)} placeholder="+1 555-0000" />
                        </Field>
                        <Field label="License Number *">
                            <Input value={form.licenseNumber} onChange={(e) => set("licenseNumber", e.target.value)} placeholder="LIC-000000" />
                        </Field>
                    </div>
                    <Field label="Biography">
                        <textarea
                            className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm resize-none focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                            rows={3}
                            value={form.biography}
                            onChange={(e) => set("biography", e.target.value)}
                            placeholder="Brief professional summary..."
                        />
                    </Field>
                    {specializations.length > 0 && (
                        <Field label="Departments">
                            <div className="flex flex-wrap gap-2">
                                {specializations.map((s) => {
                                    const selected = form.specializationIds?.includes(s.id);
                                    return (
                                        <button
                                            key={s.id}
                                            type="button"
                                            onClick={() => toggleSpec(s.id)}
                                            className={`px-3 py-1 rounded-full border text-xs transition-colors ${selected ? "bg-primary text-primary-foreground border-primary" : "border-border text-muted-foreground hover:border-primary/50"}`}
                                        >
                                            {s.name}
                                        </button>
                                    );
                                })}
                            </div>
                        </Field>
                    )}
                    <div className="flex justify-end gap-2 pt-1">
                        <Button variant="outline" onClick={() => { setForm(EMPTY_FORM); onOpenChange(false); }}>Cancel</Button>
                        <Button onClick={handleSubmit} disabled={createDoctor.isPending}>Create Doctor</Button>
                    </div>
                </div>
            </DialogContent>
        </Dialog>
    );
}

// ── Main Page ─────────────────────────────────────────────────────────────────

export default function AdminDoctors() {
    const { data: doctors = [], isLoading } = useAllDoctorProfiles();
    const toggleStatus = useToggleUserStatus(() => setSelected(null));

    const [search, setSearch] = useState("");
    const [deptFilter, setDeptFilter] = useState("all");
    const [selected, setSelected] = useState<DoctorProfileResponse | null>(null);
    const [createOpen, setCreateOpen] = useState(false);
    const [searchParams] = useSearchParams();

    const departments = [...new Set(
        doctors.flatMap((d) => d.specializations.map((s) => s.name))
    )].sort();

    useEffect(() => {
        const dept = searchParams.get("dept");
        if (dept && departments.includes(dept)) setDeptFilter(dept);
    }, [searchParams, departments]);

    const filtered = doctors.filter((d) => {
        const name = `${d.firstName} ${d.lastName}`.toLowerCase();
        const specs = d.specializations.map((s) => s.name.toLowerCase()).join(" ");
        const matchesSearch = name.includes(search.toLowerCase()) || specs.includes(search.toLowerCase());
        const matchesDept = deptFilter === "all" || d.specializations.some((s) => s.name === deptFilter);
        return matchesSearch && matchesDept;
    });

    return (
        <RolePageShell title="Manage Doctors" subtitle="Add and manage doctor accounts">
            <div className="flex flex-col sm:flex-row gap-4">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                    <Input placeholder="Search by name or department..." value={search} onChange={(e) => setSearch(e.target.value)} className="pl-10" />
                </div>
                <Select value={deptFilter} onValueChange={setDeptFilter}>
                    <SelectTrigger className="w-[180px]"><SelectValue placeholder="Department" /></SelectTrigger>
                    <SelectContent>
                        <SelectItem value="all">All Departments</SelectItem>
                        {departments.map((d) => <SelectItem key={d} value={d}>{d}</SelectItem>)}
                    </SelectContent>
                </Select>
                <Button className="gap-2" onClick={() => setCreateOpen(true)}>
                    <Plus className="h-4 w-4" /> Add Doctor
                </Button>
            </div>

            <div className="rounded-xl border border-border bg-card overflow-hidden">
                <table className="w-full">
                    <thead>
                    <tr className="border-b border-border bg-muted/50">
                        {["Doctor", "Department", "Room", "Phone", "Rating", "Actions"].map((h) => (
                            <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-muted-foreground uppercase tracking-wider">{h}</th>
                        ))}
                    </tr>
                    </thead>
                    <tbody>
                    {isLoading ? (
                        <tr><td colSpan={6} className="px-4 py-8 text-center text-sm text-muted-foreground">Loading doctors...</td></tr>
                    ) : filtered.length === 0 ? (
                        <tr><td colSpan={6} className="px-4 py-8 text-center text-sm text-muted-foreground">No doctors found.</td></tr>
                    ) : (
                        filtered.map((doc) => (
                            <tr key={doc.id} className="border-b border-border hover:bg-muted/30 transition-colors">
                                <td className="px-4 py-3">
                                    <div className="flex items-center gap-3">
                                        <div className="h-9 w-9 rounded-full bg-primary/10 flex items-center justify-center text-sm font-bold text-primary">
                                            {doc.firstName[0]}{doc.lastName[0]}
                                        </div>
                                        <div>
                                            <p className="text-sm font-medium text-foreground">{doc.firstName} {doc.lastName}</p>
                                            <p className="text-xs text-muted-foreground">{doc.email}</p>
                                        </div>
                                    </div>
                                </td>
                                <td className="px-4 py-3 text-sm text-foreground">
                                    {doc.specializations.length > 0
                                        ? doc.specializations.map((s) => s.name).join(", ")
                                        : <span className="text-muted-foreground">—</span>}
                                </td>
                                <td className="px-4 py-3 text-sm text-foreground">
                                    {doc.roomNumber ?? <span className="text-muted-foreground">—</span>}
                                </td>
                                <td className="px-4 py-3 text-sm text-muted-foreground">
                                    {doc.phoneNumber ?? <span>—</span>}
                                </td>
                                <td className="px-4 py-3 text-sm text-foreground">
                                    {doc.rating != null ? `⭐ ${doc.rating}` : <span className="text-muted-foreground">—</span>}
                                </td>
                                <td className="px-4 py-3">
                                    <Button variant="ghost" size="sm" onClick={() => setSelected(doc)}>Details</Button>
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
                        <DoctorDetails
                            doctor={selected}
                            onToggleStatus={() =>
                                toggleStatus.mutate({ userId: selected.userId, active: !selected.active })
                            }
                        />
                    )}
                </DialogContent>
            </Dialog>

            <CreateDoctorDialog open={createOpen} onOpenChange={setCreateOpen} />
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

function Field({ label, children }: { label: string; children: React.ReactNode }) {
    return (
        <div className="space-y-1.5">
            <Label>{label}</Label>
            {children}
        </div>
    );
}