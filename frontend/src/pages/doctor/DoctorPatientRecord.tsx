import { useParams, useNavigate } from "react-router-dom";
import { RolePageShell } from "@/components/RolePageShell";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
    ArrowLeft, Mail, Phone, User as UserIcon, Heart, ShieldAlert,
    Upload, Stethoscope, Pill, ClipboardList, StickyNote,
} from "lucide-react";
import { usePatientSummary, usePatientHistory, useUploadDocument } from "@/hooks/useDoctor";
import { DocumentRow } from "@/components/DocumentRow";
import { CollapsibleSection } from "@/components/CollapsibleSection";
import { PatientHealthInfo } from "@/components/PatientHealthInfo";
import {
    formatBloodType, formatDOB, formatGender, calcAge,
} from "@/components/PatientDetailsSheet";
import type { PatientHistoryItem } from "@/types/api";
import { useRef } from "react";

// Helpers

function formatDate(dateStr: string) {
    return new Date(dateStr + "T00:00:00").toLocaleDateString("en-US", {
        weekday: "long", year: "numeric", month: "long", day: "numeric",
    });
}

// History Entry

function HistoryEntry({ entry, patientProfileId }: { entry: PatientHistoryItem; patientProfileId: number }) {
    const fileInputRef = useRef<HTMLInputElement>(null);
    const upload = useUploadDocument(entry.noteId, patientProfileId);
    const fields = [
        { icon: Stethoscope, label: "Diagnosis",    value: entry.diagnosis,    strong: true },
        { icon: ClipboardList, label: "Treatment",   value: entry.treatment },
        { icon: Pill,         label: "Prescription", value: entry.prescription },
        { icon: StickyNote,   label: "Notes",        value: entry.notes },
    ].filter((f) => f.value);

    return (
        <div className="relative pl-6 pb-8 last:pb-0">
            {/* Timeline line */}
            <div className="absolute left-0 top-1.5 bottom-0 w-px bg-border" />
            {/* Timeline dot */}
            <div className="absolute left-[-4px] top-1.5 h-2.5 w-2.5 rounded-full border-2 border-primary bg-background" />

            <div className="rounded-xl border border-border bg-card p-4 space-y-3">
                <div className="flex items-start justify-between gap-2">
                    <div>
                        <p className="text-sm font-semibold text-foreground">{formatDate(entry.appointmentDate)}</p>
                        <p className="text-xs text-muted-foreground">Dr. {entry.doctorName}</p>
                    </div>
                    <Badge variant="outline" className="text-[10px] shrink-0 bg-muted/40">
                        #{entry.appointmentId}
                    </Badge>
                </div>

                <div className="space-y-2">
                    {fields.map(({ icon: Icon, label, value, strong }) => (
                        <div key={label} className="flex gap-2.5">
                            <Icon className="h-4 w-4 text-muted-foreground shrink-0 mt-0.5" />
                            <div>
                                <span className="text-xs text-muted-foreground">{label}: </span>
                                <span className={`text-sm ${strong ? "font-medium text-foreground" : "text-foreground"}`}>
                  {value}
                </span>
                            </div>
                        </div>
                    ))}
                </div>

                <div className="space-y-1.5 pt-1">
                    <div className="flex items-center justify-between">
                        <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                            Documents {entry.documents.length > 0 && `(${entry.documents.length})`}
                        </p>
                        <Button
                            size="sm"
                            variant="ghost"
                            className="h-6 px-2 gap-1 text-xs text-muted-foreground"
                            disabled={upload.isPending}
                            onClick={() => fileInputRef.current?.click()}
                        >
                            <Upload className="h-3 w-3" />
                            {upload.isPending ? "Uploading..." : "Upload"}
                        </Button>
                        <input
                            ref={fileInputRef}
                            type="file"
                            className="hidden"
                            accept=".pdf,.jpg,.jpeg,.png,.gif,.doc,.docx"
                            onChange={(e) => {
                                const file = e.target.files?.[0];
                                if (file) upload.mutate(file);
                                e.target.value = "";
                            }}
                        />
                    </div>
                    {entry.documents.map((doc) => (
                        <DocumentRow key={doc.id} noteId={entry.noteId} doc={doc} patientProfileId={patientProfileId} />
                    ))}
                </div>
            </div>
        </div>
    );
}

// Page

export default function DoctorPatientRecord() {
    const { patientProfileId } = useParams<{ patientProfileId: string }>();
    const navigate = useNavigate();
    const id = patientProfileId ? Number(patientProfileId) : null;

    const { data: patient, isLoading: profileLoading } = usePatientSummary(id);
    const { data: history = [], isLoading: historyLoading } = usePatientHistory(id);

    const age = calcAge(patient?.dateOfBirth ?? null);
    const chips = [
        age !== null ? `${age} yrs` : null,
        formatGender(patient?.gender ?? null) !== "—" ? formatGender(patient?.gender ?? null) : null,
        formatBloodType(patient?.bloodType ?? null) !== "—" ? formatBloodType(patient?.bloodType ?? null) : null,
    ].filter(Boolean);

    return (
        <RolePageShell
            title={profileLoading ? "Patient Record" : `${patient?.firstName ?? ""} ${patient?.lastName ?? ""}`}
            subtitle={chips.join(" · ")}
        >
            {/* Back button */}
            <div>
                <Button variant="ghost" size="sm" className="gap-1.5 -ml-2" onClick={() => navigate(-1)}>
                    <ArrowLeft className="h-4 w-4" />
                    Back
                </Button>
            </div>

            {profileLoading ? (
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                    <div className="rounded-xl border border-border bg-card p-6 h-64 animate-pulse" />
                    <div className="lg:col-span-2 rounded-xl border border-border bg-card p-6 h-64 animate-pulse" />
                </div>
            ) : patient ? (
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
                    {/* Left sidebar — profile */}
                    <div className="space-y-4">
                        {/* Avatar + name */}
                        <div className="rounded-xl border border-border bg-card p-5 flex items-center gap-4">
                            <div className="h-14 w-14 rounded-full bg-primary/10 flex items-center justify-center text-xl font-bold text-primary shrink-0">
                                {patient.firstName[0]}{patient.lastName[0]}
                            </div>
                            <div>
                                <p className="font-semibold text-foreground">{patient.firstName} {patient.lastName}</p>
                                {chips.length > 0 && (
                                    <p className="text-sm text-muted-foreground">{chips.join(" · ")}</p>
                                )}
                            </div>
                        </div>

                        {/* Health info — collapsible sections (allergies, vitals, conditions, family, lifestyle) */}
                        <PatientHealthInfo patient={patient} />

                        {/* Contact — collapsible */}
                        <CollapsibleSection title="Contact">
                            {[
                                { icon: Mail,     label: "Email",  value: patient.email },
                                { icon: Phone,    label: "Phone",  value: patient.phoneNumber },
                                { icon: UserIcon, label: "DOB",    value: formatDOB(patient.dateOfBirth) },
                            ].filter((r) => r.value && r.value !== "—").map(({ icon: Icon, label, value }) => (
                                <div key={label} className="flex items-center gap-3 text-sm">
                                    <Icon className="h-4 w-4 text-muted-foreground shrink-0" />
                                    <span className="text-muted-foreground w-12 shrink-0">{label}</span>
                                    <span className="text-foreground break-all">{value}</span>
                                </div>
                            ))}
                        </CollapsibleSection>

                        {/* Emergency Contact — collapsible */}
                        {(patient.emergencyContactName || patient.emergencyContactPhone) && (
                            <CollapsibleSection title="Emergency Contact">
                                {patient.emergencyContactName && (
                                    <div className="flex items-center gap-3 text-sm">
                                        <ShieldAlert className="h-4 w-4 text-muted-foreground shrink-0" />
                                        <span className="text-muted-foreground w-12 shrink-0">Name</span>
                                        <span className="text-foreground">{patient.emergencyContactName}</span>
                                    </div>
                                )}
                                {patient.emergencyContactPhone && (
                                    <div className="flex items-center gap-3 text-sm">
                                        <Phone className="h-4 w-4 text-muted-foreground shrink-0" />
                                        <span className="text-muted-foreground w-12 shrink-0">Phone</span>
                                        <span className="text-foreground">{patient.emergencyContactPhone}</span>
                                    </div>
                                )}
                            </CollapsibleSection>
                        )}
                    </div>

                    {/* Right — medical history */}
                    <div className="lg:col-span-2 rounded-xl border border-border bg-card p-6">
                        <div className="flex items-center justify-between mb-6">
                            <h3 className="text-base font-semibold text-foreground">Medical History</h3>
                            <Badge variant="outline" className="text-xs">
                                {history.length} {history.length === 1 ? "record" : "records"}
                            </Badge>
                        </div>

                        {historyLoading ? (
                            <div className="space-y-4">
                                {[1, 2].map((i) => (
                                    <div key={i} className="h-28 rounded-xl border border-border bg-muted/30 animate-pulse" />
                                ))}
                            </div>
                        ) : history.length === 0 ? (
                            <div className="text-center py-12">
                                <Heart className="h-8 w-8 text-muted-foreground mx-auto mb-3 opacity-40" />
                                <p className="text-sm text-muted-foreground">No medical records on file.</p>
                            </div>
                        ) : (
                            <div className="space-y-0">
                                {history.map((entry) => (
                                    <HistoryEntry key={entry.noteId} entry={entry} patientProfileId={id!} />
                                ))}
                            </div>
                        )}
                    </div>
                </div>
            ) : (
                <p className="text-sm text-muted-foreground">Patient not found.</p>
            )}
        </RolePageShell>
    );
}