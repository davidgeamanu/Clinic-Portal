import { useMemo, useState } from "react";
import { useParams, useNavigate, useSearchParams } from "react-router-dom";
import { RolePageShell } from "@/components/RolePageShell";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
    Dialog, DialogContent, DialogHeader, DialogTitle,
    DialogDescription, DialogFooter,
} from "@/components/ui/dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import { ArrowLeft, Building2, DoorClosed } from "lucide-react";
import { useAdminRooms, useUpdateRoom, useAllDoctorProfiles, useAssignDoctorRoom } from "@/hooks/useAdmin";
import type { RoomResponse, RoomType, RoomStatus } from "@/types/api";

const TYPE_LABELS: Record<RoomType, string> = { CONSULT: "Consult", OR: "OR", IMAGING: "Imaging" };
const STATUS_LABELS: Record<RoomStatus, string> = { FREE: "Free", OCCUPIED: "Occupied" };
const TYPES: RoomType[] = ["CONSULT", "OR", "IMAGING"];
const STATUSES: RoomStatus[] = ["FREE", "OCCUPIED"];

const DEPT_COLORS: Record<string, string> = {
    Cardiology:       "bg-rose-500/15 text-rose-600 border-rose-500/30",
    Neurology:        "bg-violet-500/15 text-violet-600 border-violet-500/30",
    Orthopedics:      "bg-amber-500/15 text-amber-600 border-amber-500/30",
    Pediatrics:       "bg-sky-500/15 text-sky-600 border-sky-500/30",
    "General Surgery" :"bg-emerald-500/15 text-emerald-600 border-emerald-500/30",
    Radiology:        "bg-indigo-500/15 text-indigo-600 border-indigo-500/30",
    Emergency:        "bg-destructive/15 text-destructive border-destructive/30",
};

const STATUS_DOT: Record<RoomStatus, string> = {
    FREE:     "bg-success",
    OCCUPIED: "bg-destructive",
};

const floorLabel = (f: number) => (f === 0 ? "Ground Floor" : `Floor ${f}`);

function deptColor(name: string | null) {
    if (!name) return "bg-muted text-muted-foreground border-border";
    return DEPT_COLORS[name] ?? "bg-muted text-muted-foreground border-border";
}

export default function AdminBuilding() {
    const navigate = useNavigate();
    const { dept } = useParams<{ dept?: string }>();
    const [searchParams, setSearchParams] = useSearchParams();
    const focusDept = dept ? decodeURIComponent(dept) : (searchParams.get("dept") ?? "All");

    const { data: rooms = [], isLoading } = useAdminRooms();
    const { data: doctors = [] } = useAllDoctorProfiles();
    const updateRoom = useUpdateRoom();
    const assignRoom = useAssignDoctorRoom();

    const [editing, setEditing] = useState<RoomResponse | null>(null);
    const [draft, setDraft] = useState<{ specializationId: number | null; type: RoomType; status: RoomStatus } | null>(null);
    const [draftDoctorId, setDraftDoctorId] = useState<number | null>(null);

    const departments = useMemo(() => {
        const names = new Set<string>();
        rooms.forEach((r) => { if (r.specializationName) names.add(r.specializationName); });
        return Array.from(names).sort();
    }, [rooms]);

    const floors = useMemo(() => {
        const grouped: Record<number, RoomResponse[]> = {};
        rooms.forEach((r) => {
            grouped[r.floor] = grouped[r.floor] || [];
            grouped[r.floor].push(r);
        });
        return Object.keys(grouped)
            .map(Number)
            .sort((a, b) => b - a)
            .map((f) => ({ floor: f, rooms: grouped[f].sort((a, b) => a.roomNumber.localeCompare(b.roomNumber)) }));
    }, [rooms]);

    const focusCount = focusDept !== "All"
        ? rooms.filter((r) => r.specializationName === focusDept).length
        : 0;

    const openEdit = (room: RoomResponse) => {
        setEditing(room);
        setDraft({ specializationId: room.specializationId, type: room.type, status: room.status });
        setDraftDoctorId(room.assignedDoctorId);
    };

    const closeEdit = () => { setEditing(null); setDraft(null); setDraftDoctorId(null); };

    const saveEdit = () => {
        if (!editing || !draft) return;

        if (editing.type === "CONSULT" && draftDoctorId !== editing.assignedDoctorId) {
            if (draftDoctorId !== null) {
                assignRoom.mutate({ doctorProfileId: draftDoctorId, roomId: editing.id });
            } else if (editing.assignedDoctorId !== null) {
                assignRoom.mutate({ doctorProfileId: editing.assignedDoctorId, roomId: null });
            }
        }

        updateRoom.mutate(
            { id: editing.id, data: draft },
            { onSuccess: closeEdit }
        );
    };

    if (isLoading) {
        return (
            <RolePageShell title="Building View" subtitle="Assign rooms to departments across floors">
                <p className="text-sm text-muted-foreground">Loading building...</p>
            </RolePageShell>
        );
    }

    return (
        <RolePageShell title="Building View" subtitle="Assign rooms to departments across floors">
            <div className="flex flex-wrap items-center justify-between gap-3">
                <Button variant="ghost" size="sm" onClick={() => navigate("/admin/departments")}>
                    <ArrowLeft className="h-4 w-4" /> Back to Departments
                </Button>
                <div className="flex items-center gap-2">
                    <Label className="text-sm text-muted-foreground">Focus department</Label>
                    <Select
                        value={focusDept}
                        onValueChange={(v) => v === "All" ? setSearchParams({}) : setSearchParams({ dept: v })}
                    >
                        <SelectTrigger className="w-[200px]"><SelectValue /></SelectTrigger>
                        <SelectContent>
                            <SelectItem value="All">All departments</SelectItem>
                            {departments.map((d) => <SelectItem key={d} value={d}>{d}</SelectItem>)}
                        </SelectContent>
                    </Select>
                </div>
            </div>

            {focusDept !== "All" && (
                <div className="rounded-lg border border-border bg-card p-4 flex items-center gap-3">
                    <Building2 className="h-5 w-5 text-primary" />
                    <p className="text-sm text-foreground">
                        Showing <strong>{focusCount}</strong> room{focusCount !== 1 ? "s" : ""} assigned to{" "}
                        <span className="text-primary">{focusDept}</span>.{" "}
                        <span className="text-muted-foreground">Other rooms are dimmed. Click any room to reassign.</span>
                    </p>
                </div>
            )}

            <div className="rounded-lg border border-border bg-card p-4 flex flex-wrap gap-4 text-xs">
                {STATUSES.map((s) => (
                    <div key={s} className="flex items-center gap-2">
                        <span className={`h-2.5 w-2.5 rounded-full ${STATUS_DOT[s]}`} />
                        <span className="text-muted-foreground">{STATUS_LABELS[s]}</span>
                    </div>
                ))}
                <div className="ml-auto flex flex-wrap gap-2">
                    {departments.map((d) => (
                        <span key={d} className={`px-2 py-0.5 rounded border text-[11px] ${deptColor(d)}`}>{d}</span>
                    ))}
                </div>
            </div>

            <div className="space-y-3">
                <div className="text-center text-xs text-muted-foreground tracking-widest uppercase">▲ Rooftop</div>
                {floors.map(({ floor, rooms: floorRooms }) => (
                    <div key={floor} className="rounded-xl border-2 border-border bg-card overflow-hidden shadow-sm">
                        <div className="flex items-center justify-between bg-muted/40 px-4 py-2 border-b border-border">
                            <div className="flex items-center gap-2">
                                <Building2 className="h-4 w-4 text-muted-foreground" />
                                <span className="text-sm font-semibold text-foreground">{floorLabel(floor)}</span>
                            </div>
                            <span className="text-xs text-muted-foreground">{floorRooms.length} rooms</span>
                        </div>
                        <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-8 gap-px bg-border">
                            {floorRooms.map((room) => {
                                const dim = focusDept !== "All" && room.specializationName !== focusDept;
                                return (
                                    <button
                                        key={room.id}
                                        onClick={() => openEdit(room)}
                                        className={`group relative bg-card hover:bg-accent/40 transition-all p-3 text-left min-h-[110px] flex flex-col justify-between ${dim ? "opacity-30 hover:opacity-100" : ""}`}
                                    >
                                        <div className="flex items-start justify-between">
                                            <div className="flex items-center gap-1.5">
                                                <DoorClosed className="h-3.5 w-3.5 text-muted-foreground" />
                                                <span className="text-sm font-bold text-foreground">{room.roomNumber}</span>
                                            </div>
                                            <span className={`h-2 w-2 rounded-full ${STATUS_DOT[room.status]}`} title={STATUS_LABELS[room.status]} />
                                        </div>
                                        <div className="space-y-1.5">
                                            <Badge variant="outline" className="text-[10px] px-1.5 py-0 h-4">
                                                {TYPE_LABELS[room.type]}
                                            </Badge>
                                            <div className={`text-[10px] px-1.5 py-0.5 rounded border truncate ${deptColor(room.specializationName)}`}>
                                                {room.specializationName ?? "Unassigned"}
                                            </div>
                                        </div>
                                    </button>
                                );
                            })}
                        </div>
                    </div>
                ))}
                <div className="text-center text-xs text-muted-foreground tracking-widest uppercase">▼ Foundation</div>
            </div>

            <Dialog open={!!editing} onOpenChange={(o) => { if (!o) closeEdit(); }}>
                <DialogContent className="max-w-md">
                    <DialogHeader>
                        <DialogTitle>Room {editing?.roomNumber}</DialogTitle>
                        <DialogDescription>
                            {editing && floorLabel(editing.floor)} — assign department, type, and status.
                        </DialogDescription>
                    </DialogHeader>
                    {draft && (
                        <div className="space-y-4 py-2">
                            {editing?.type === "CONSULT" && editing.specializationName && (() => {
                                const deptDoctors = doctors.filter((d) =>
                                    d.specializations.some((s) => s.name === editing.specializationName)
                                );
                                return (
                                    <div className="space-y-2">
                                        <Label>Assigned Doctor</Label>
                                        <Select
                                            value={draftDoctorId?.toString() ?? "none"}
                                            onValueChange={(v) => setDraftDoctorId(v === "none" ? null : Number(v))}
                                        >
                                            <SelectTrigger>
                                                <SelectValue placeholder="Unassigned" />
                                            </SelectTrigger>
                                            <SelectContent>
                                                <SelectItem value="none">Unassigned</SelectItem>
                                                {deptDoctors.map((d) => (
                                                    <SelectItem key={d.id} value={d.id.toString()}>
                                                        {d.firstName} {d.lastName}
                                                    </SelectItem>
                                                ))}
                                            </SelectContent>
                                        </Select>
                                    </div>
                                );
                            })()}
                            <div className="space-y-2">
                                <Label>Department</Label>
                                <Select
                                    value={draft.specializationId?.toString() ?? "none"}
                                    onValueChange={(v) => setDraft({ ...draft, specializationId: v === "none" ? null : Number(v) })}
                                >
                                    <SelectTrigger><SelectValue /></SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="none">Unassigned</SelectItem>
                                        {rooms
                                            .filter((r, i, arr) => r.specializationId !== null && arr.findIndex((x) => x.specializationId === r.specializationId) === i)
                                            .map((r) => (
                                                <SelectItem key={r.specializationId!} value={r.specializationId!.toString()}>
                                                    {r.specializationName}
                                                </SelectItem>
                                            ))
                                        }
                                    </SelectContent>
                                </Select>
                            </div>
                            <div className="grid grid-cols-2 gap-3">
                                <div className="space-y-2">
                                    <Label>Type</Label>
                                    <Select value={draft.type} onValueChange={(v) => setDraft({ ...draft, type: v as RoomType })}>
                                        <SelectTrigger><SelectValue /></SelectTrigger>
                                        <SelectContent>
                                            {TYPES.map((t) => <SelectItem key={t} value={t}>{TYPE_LABELS[t]}</SelectItem>)}
                                        </SelectContent>
                                    </Select>
                                </div>
                                <div className="space-y-2">
                                    <Label>Status</Label>
                                    <Select value={draft.status} onValueChange={(v) => setDraft({ ...draft, status: v as RoomStatus })}>
                                        <SelectTrigger><SelectValue /></SelectTrigger>
                                        <SelectContent>
                                            {STATUSES.map((s) => <SelectItem key={s} value={s}>{STATUS_LABELS[s]}</SelectItem>)}
                                        </SelectContent>
                                    </Select>
                                </div>
                            </div>
                        </div>
                    )}
                    <DialogFooter>
                        <Button variant="outline" onClick={closeEdit}>Cancel</Button>
                        <Button onClick={saveEdit} disabled={updateRoom.isPending}>Save changes</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </RolePageShell>
    );
}