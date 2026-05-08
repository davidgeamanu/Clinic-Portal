import * as React from "react";
import { useNavigate } from "react-router-dom";
import { RolePageShell } from "@/components/RolePageShell";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Stethoscope, Building2, Users, DoorClosed, Scan, Scissors, AlertTriangle } from "lucide-react";
import { useAdminDepartments } from "@/hooks/useAdmin";

export default function AdminDepartments() {
    const navigate = useNavigate();
    const { data: departments = [], isLoading } = useAdminDepartments();

    return (
        <RolePageShell title="Departments" subtitle="Hospital departments and room assignments">
            <div className="flex justify-end">
                <Button variant="outline" size="sm" onClick={() => navigate("/admin/building")}>
                    <Building2 className="h-4 w-4" /> Open Building View
                </Button>
            </div>

            {isLoading ? (
                <p className="text-sm text-muted-foreground">Loading departments...</p>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
                    {departments.map((dept) => {
                        const totalRooms = dept.consultRoomCount + dept.orRoomCount + dept.imagingRoomCount;
                        return (
                            <div key={dept.id} className="rounded-xl border border-border bg-card p-5 space-y-4 hover:shadow-md transition-shadow">

                                {/* Header */}
                                <div className="flex items-start justify-between gap-2">
                                    <div className="min-w-0">
                                        <h3 className="text-base font-semibold text-foreground">{dept.name}</h3>
                                        {dept.description && (
                                            <p className="text-xs text-muted-foreground mt-0.5 line-clamp-2">{dept.description}</p>
                                        )}
                                    </div>
                                    {totalRooms === 0 && (
                                        <Badge variant="outline" className="bg-warning/10 text-warning border-warning/20 shrink-0">
                                            <AlertTriangle className="h-3 w-3 mr-1" /> No rooms
                                        </Badge>
                                    )}
                                </div>

                                {/* Doctor count */}
                                <div className="flex items-center gap-3 rounded-lg bg-primary/5 border border-primary/10 px-3 py-2">
                                    <Stethoscope className="h-4 w-4 text-primary shrink-0" />
                                    <span className="text-sm font-semibold text-foreground">{dept.doctorCount}</span>
                                    <span className="text-xs text-muted-foreground">{dept.doctorCount === 1 ? "doctor" : "doctors"}</span>
                                </div>

                                {/* Room breakdown */}
                                <div className="space-y-1.5">
                                    <p className="text-[10px] font-semibold uppercase tracking-wider text-muted-foreground">Rooms</p>
                                    <div className="grid grid-cols-3 gap-2">
                                        <RoomStat icon={DoorClosed} count={dept.consultRoomCount} label="Consult" color="text-info" />
                                        <RoomStat icon={Scissors}   count={dept.orRoomCount}      label="Operating" color="text-destructive" />
                                        <RoomStat icon={Scan}       count={dept.imagingRoomCount} label="Imaging" color="text-muted-foreground" />
                                    </div>
                                </div>

                                {/* Actions */}
                                <div className="grid grid-cols-2 gap-2">
                                    <Button variant="outline" size="sm"
                                        onClick={() => navigate(`/admin/doctors?dept=${encodeURIComponent(dept.name)}`)}>
                                        <Users className="h-4 w-4" /> Personnel
                                    </Button>
                                    <Button variant="outline" size="sm"
                                        onClick={() => navigate(`/admin/building/${encodeURIComponent(dept.name)}`)}>
                                        <Building2 className="h-4 w-4" /> Manage Rooms
                                    </Button>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </RolePageShell>
    );
}

function RoomStat({ icon: Icon, count, label, color }: { icon: React.ElementType; count: number; label: string; color: string }) {
    return (
        <div className="flex flex-col items-center gap-1 rounded-lg bg-muted/50 px-2 py-2">
            <Icon className={`h-3.5 w-3.5 ${color}`} />
            <span className="text-sm font-bold text-foreground">{count}</span>
            <span className="text-[10px] text-muted-foreground">{label}</span>
        </div>
    );
}