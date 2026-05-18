import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { RolePageShell } from "@/components/RolePageShell";
import { formatBloodType, formatGender, calcAge } from "@/components/PatientDetailsSheet";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Search } from "lucide-react";
import { useMyPatients } from "@/hooks/useDoctor";
import type { DoctorPatientListItem } from "@/types/api";

function relativeDate(dateStr: string | null): string {
    if (!dateStr) return "No completed visits";
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const date = new Date(dateStr + "T00:00:00");
    const diff = Math.round((today.getTime() - date.getTime()) / 86_400_000);
    if (diff === 0) return "Today";
    if (diff === 1) return "Yesterday";
    if (diff < 7)  return `${diff} days ago`;
    return date.toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });
}

function PatientCard({
                         patient,
                         onClick,
                     }: {
    patient: DoctorPatientListItem;
    onClick: () => void;
}) {
    const initials = `${patient.firstName[0]}${patient.lastName[0]}`;
    const age = calcAge(patient.dateOfBirth);
    const bloodType = formatBloodType(patient.bloodType);
    const gender = formatGender(patient.gender);

    const meta = [
        age !== null ? `${age} yrs` : null,
        gender !== "—" ? gender : null,
        bloodType !== "—" ? bloodType : null,
    ].filter(Boolean).join(" · ");

    return (
        <div
            onClick={onClick}
            className="rounded-xl border border-border bg-card p-5 space-y-3 hover:shadow-md hover:border-primary/20 transition-all cursor-pointer"
        >
            <div className="flex items-start justify-between gap-2">
                <div className="flex items-center gap-3 min-w-0">
                    <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center text-sm font-bold text-primary shrink-0">
                        {initials}
                    </div>
                    <div className="min-w-0">
                        <p className="text-sm font-semibold text-foreground truncate">
                            {patient.firstName} {patient.lastName}
                        </p>
                        {meta && <p className="text-xs text-muted-foreground">{meta}</p>}
                    </div>
                </div>
                <Badge variant="outline" className="text-[10px] shrink-0 bg-muted/50 text-muted-foreground border-border">
                    {patient.appointmentCount} {patient.appointmentCount === 1 ? "visit" : "visits"}
                </Badge>
            </div>

            <div className="p-2.5 rounded-lg bg-muted/40 space-y-0.5">
                {patient.lastDiagnosis ? (
                    <p className="text-xs font-medium text-foreground truncate">{patient.lastDiagnosis}</p>
                ) : (
                    <p className="text-xs text-muted-foreground italic">No diagnosis on record</p>
                )}
                <p className="text-xs text-muted-foreground">Last visit: {relativeDate(patient.lastVisitDate)}</p>
            </div>
        </div>
    );
}

export default function DoctorPatients() {
    const navigate = useNavigate();
    const { data: patients = [], isLoading } = useMyPatients();

    const [search, setSearch] = useState("");

    const filtered = useMemo(
        () =>
            patients.filter((p) =>
                `${p.firstName} ${p.lastName}`.toLowerCase().includes(search.toLowerCase()) ||
                (p.lastDiagnosis ?? "").toLowerCase().includes(search.toLowerCase())
            ),
        [patients, search]
    );

    return (
        <RolePageShell title="My Patients" subtitle="All patients you have had appointments with">
            <div className="relative max-w-md">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                    placeholder="Search by name or diagnosis..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    className="pl-10"
                />
            </div>

            {isLoading ? (
                <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
                    {Array.from({ length: 6 }).map((_, i) => (
                        <div key={i} className="rounded-xl border border-border bg-card p-5 h-32 animate-pulse" />
                    ))}
                </div>
            ) : filtered.length === 0 ? (
                <p className="text-center text-muted-foreground py-12">
                    {patients.length === 0 ? "No patients yet." : "No patients match your search."}
                </p>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
                    {filtered.map((p) => (
                        <PatientCard
                            key={p.patientProfileId}
                            patient={p}
                            onClick={() => navigate(`/doctor/patients/${p.patientProfileId}`)}
                        />
                    ))}
                </div>
            )}

        </RolePageShell>
    );
}