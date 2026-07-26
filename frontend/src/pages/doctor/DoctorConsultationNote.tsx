import { useEffect, useRef, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useQueryClient } from "@tanstack/react-query";
import { RolePageShell } from "@/components/RolePageShell";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { DocumentRow } from "@/components/DocumentRow";
import { CollapsibleSection } from "@/components/CollapsibleSection";
import { PatientHealthInfo } from "@/components/PatientHealthInfo";
import { formatBloodType, formatDOB, formatGender, calcAge } from "@/components/PatientDetailsSheet";
import {
    ArrowLeft, Mail, Phone, User as UserIcon, Heart, ShieldAlert,
    Upload, Stethoscope, Pill, ClipboardList, StickyNote,
} from "lucide-react";
import {
    useDoctorAppointments,
    useConsultationNote,
    useCreateConsultationNote,
    useUpdateConsultationNote,
    usePatientSummary,
    usePatientHistory,
    useUploadDocument,
} from "@/hooks/useDoctor";
import { queryKeys } from "@/lib/query-keys";
import type { AppointmentStatus, PatientHistoryItem } from "@/types/api";

//Helpers

const STATUS_LABELS: Record<AppointmentStatus, string> = {
    SCHEDULED: "Upcoming", CONFIRMED: "Confirmed",
    IN_PROGRESS: "In Progress", COMPLETED: "Completed", CANCELLED: "Cancelled", NO_SHOW: "No-show",
};

function formatDate(dateStr: string) {
    return new Date(dateStr + "T00:00:00").toLocaleDateString("en-US", {
        weekday: "short", year: "numeric", month: "short", day: "numeric",
    });
}

function formatDateTime(iso: string) {
    return new Date(iso).toLocaleDateString("en-US", {
        weekday: "long", month: "long", day: "numeric", year: "numeric",
    });
}

function formatTime(iso: string) {
    return new Date(iso).toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit", hour12: true });
}

// History Entry (compact for narrow column)

function HistoryEntry({ entry, patientProfileId }: { entry: PatientHistoryItem; patientProfileId: number }) {
    const fileInputRef = useRef<HTMLInputElement>(null);
    const upload = useUploadDocument(entry.noteId, patientProfileId);
    const fields = [
        { icon: Stethoscope,   label: "Diagnosis",    value: entry.diagnosis,    strong: true },
        { icon: ClipboardList, label: "Treatment",    value: entry.treatment },
        { icon: Pill,          label: "Prescription", value: entry.prescription },
        { icon: StickyNote,    label: "Notes",        value: entry.notes },
    ].filter((f) => f.value);

    return (
        <div className="relative pl-5 pb-6 last:pb-0">
            <div className="absolute left-0 top-1.5 bottom-0 w-px bg-border" />
            <div className="absolute left-[-4px] top-1.5 h-2.5 w-2.5 rounded-full border-2 border-primary bg-background" />

            <div className="rounded-lg border border-border bg-card p-3 space-y-2">
                <div className="flex items-start justify-between gap-2">
                    <div className="min-w-0">
                        <p className="text-xs font-semibold text-foreground truncate">{formatDate(entry.appointmentDate)}</p>
                        <p className="text-[10px] text-muted-foreground truncate">Dr. {entry.doctorName}</p>
                    </div>
                    <Badge variant="outline" className="text-[9px] shrink-0 bg-muted/40 px-1.5 h-4">#{entry.appointmentId}</Badge>
                </div>

                <div className="space-y-1.5">
                    {fields.map(({ icon: Icon, label, value, strong }) => (
                        <div key={label} className="flex gap-1.5">
                            <Icon className="h-3 w-3 text-muted-foreground shrink-0 mt-0.5" />
                            <div className="min-w-0">
                                <span className="text-[10px] text-muted-foreground">{label}: </span>
                                <span className={`text-xs ${strong ? "font-medium text-foreground" : "text-foreground"}`}>{value}</span>
                            </div>
                        </div>
                    ))}
                </div>

                <div className="space-y-1 pt-0.5">
                    <div className="flex items-center justify-between">
                        <p className="text-[10px] font-semibold uppercase tracking-wider text-muted-foreground">
                            Documents {entry.documents.length > 0 && `(${entry.documents.length})`}
                        </p>
                        <Button size="sm" variant="ghost" className="h-5 px-1.5 gap-1 text-[10px] text-muted-foreground"
                                disabled={upload.isPending} onClick={() => fileInputRef.current?.click()}>
                            <Upload className="h-2.5 w-2.5" />
                            {upload.isPending ? "Uploading..." : "Upload"}
                        </Button>
                        <input ref={fileInputRef} type="file" className="hidden"
                               accept=".pdf,.jpg,.jpeg,.png,.gif,.doc,.docx"
                               onChange={(e) => { const f = e.target.files?.[0]; if (f) upload.mutate(f); e.target.value = ""; }} />
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

export default function DoctorConsultationNote() {
    const { appointmentId, patientProfileId } = useParams<{ appointmentId: string; patientProfileId: string }>();
    const navigate = useNavigate();
    const qc = useQueryClient();

    const apptId = Number(appointmentId);
    const patId  = Number(patientProfileId);

    const { data: appointments = [] } = useDoctorAppointments();
    const appointment = appointments.find((a) => a.id === apptId) ?? null;

    const { data: patient,  isLoading: profileLoading } = usePatientSummary(patId);
    const { data: history = [], isLoading: historyLoading } = usePatientHistory(patId);
    const { data: existingNote, isLoading: noteLoading }   = useConsultationNote(apptId);

    const [diagnosis,    setDiagnosis]    = useState("");
    const [treatment,    setTreatment]    = useState("");
    const [prescription, setPrescription] = useState("");
    const [noteText,     setNoteText]     = useState("");

    useEffect(() => {
        if (existingNote) {
            setDiagnosis(existingNote.diagnosis ?? "");
            setTreatment(existingNote.treatment ?? "");
            setPrescription(existingNote.prescription ?? "");
            setNoteText(existingNote.notes ?? "");
        }
    }, [existingNote]);

    const afterSave = () => qc.invalidateQueries({ queryKey: queryKeys.doctor.patientHistory(patId) });
    const createNote = useCreateConsultationNote(apptId, afterSave);
    const updateNote = useUpdateConsultationNote(apptId, afterSave);
    const saving = createNote.isPending || updateNote.isPending;

    const handleSave = () => {
        if (!diagnosis.trim()) return;
        const data = {
            diagnosis:    diagnosis.trim(),
            treatment:    treatment.trim()    || undefined,
            prescription: prescription.trim() || undefined,
            notes:        noteText.trim()     || undefined,
        };
        if (existingNote) updateNote.mutate(data);
        else              createNote.mutate(data);
    };

    // Documents for the current note (sourced from patient history)
    const noteDocsRef = useRef<HTMLInputElement>(null);
    const noteEntry = existingNote ? history.find((h) => h.noteId === existingNote.id) : null;
    const noteDocuments = noteEntry?.documents ?? [];
    const uploadCurrent = useUploadDocument(existingNote?.id ?? 0, patId);

    const age  = calcAge(patient?.dateOfBirth ?? null);
    const chips = [
        age !== null ? `${age} yrs` : null,
        formatGender(patient?.gender ?? null) !== "—"    ? formatGender(patient?.gender ?? null)    : null,
        formatBloodType(patient?.bloodType ?? null) !== "—" ? formatBloodType(patient?.bloodType ?? null) : null,
    ].filter(Boolean) as string[];

    const pageTitle    = patient ? `${patient.firstName} ${patient.lastName}` : "Consultation Note";
    const pageSubtitle = appointment
        ? `${formatDateTime(appointment.scheduledAt)} · ${formatTime(appointment.scheduledAt)}`
        : chips.join(" · ");

    const hasEmergency = !!(patient?.emergencyContactName || patient?.emergencyContactPhone);

    return (
        <RolePageShell title={pageTitle} subtitle={pageSubtitle}>
            <div>
                <Button variant="ghost" size="sm" className="gap-1.5 -ml-2" onClick={() => navigate(-1)}>
                    <ArrowLeft className="h-4 w-4" />
                    Back
                </Button>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">

                {/* ── Left column ── */}
                <div className="space-y-4">
                    {profileLoading ? (
                        <div className="rounded-xl border border-border bg-card h-24 animate-pulse" />
                    ) : patient ? (
                        <div className="rounded-xl border border-border bg-card p-5 flex items-center gap-4">
                            <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center text-lg font-bold text-primary shrink-0">
                                {patient.firstName[0]}{patient.lastName[0]}
                            </div>
                            <div className="min-w-0">
                                <p className="font-semibold text-foreground truncate">{patient.firstName} {patient.lastName}</p>
                                {chips.length > 0 && <p className="text-sm text-muted-foreground truncate">{chips.join(" · ")}</p>}
                            </div>
                        </div>
                    ) : null}

                    {/* Medical History — scrollable */}
                    <div className="rounded-xl border border-border bg-card flex flex-col">
                        <div className="flex items-center justify-between p-4 border-b border-border">
                            <h3 className="text-sm font-semibold text-foreground">Medical History</h3>
                            <Badge variant="outline" className="text-[10px]">
                                {history.length} {history.length === 1 ? "record" : "records"}
                            </Badge>
                        </div>
                        <div className="max-h-[520px] overflow-y-auto p-4">
                            {historyLoading ? (
                                <div className="space-y-3">
                                    {[1, 2].map((i) => (
                                        <div key={i} className="h-24 rounded-lg border border-border bg-muted/30 animate-pulse" />
                                    ))}
                                </div>
                            ) : history.length === 0 ? (
                                <div className="text-center py-8">
                                    <Heart className="h-7 w-7 text-muted-foreground mx-auto mb-2 opacity-40" />
                                    <p className="text-xs text-muted-foreground">No medical records on file yet.</p>
                                </div>
                            ) : (
                                <div className="space-y-0">
                                    {history.map((entry) => (
                                        <HistoryEntry key={entry.noteId} entry={entry} patientProfileId={patId} />
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Health info — collapsible sections (allergies, vitals, conditions, family, lifestyle) */}
                    {patient && <PatientHealthInfo patient={patient} />}

                    {/* Contact — collapsible */}
                    {patient && (
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
                    )}

                    {/* Emergency Contact — collapsible */}
                    {hasEmergency && (
                        <CollapsibleSection title="Emergency Contact">
                            {patient?.emergencyContactName && (
                                <div className="flex items-center gap-3 text-sm">
                                    <ShieldAlert className="h-4 w-4 text-muted-foreground shrink-0" />
                                    <span className="text-muted-foreground w-12 shrink-0">Name</span>
                                    <span className="text-foreground">{patient.emergencyContactName}</span>
                                </div>
                            )}
                            {patient?.emergencyContactPhone && (
                                <div className="flex items-center gap-3 text-sm">
                                    <Phone className="h-4 w-4 text-muted-foreground shrink-0" />
                                    <span className="text-muted-foreground w-12 shrink-0">Phone</span>
                                    <span className="text-foreground">{patient.emergencyContactPhone}</span>
                                </div>
                            )}
                        </CollapsibleSection>
                    )}
                </div>

                {/* ── Right column: note form ── */}
                <div className="lg:col-span-2">
                    <div className="rounded-xl border border-border bg-card p-6">
                        <div className="flex items-center justify-between mb-5">
                            <h3 className="text-base font-semibold text-foreground">
                                {existingNote ? "Edit Consultation Note" : "New Consultation Note"}
                            </h3>
                            {appointment && (
                                <Badge variant="outline" className="text-xs">
                                    Appt #{apptId} · {STATUS_LABELS[appointment.status]}
                                </Badge>
                            )}
                        </div>

                        {noteLoading ? (
                            <p className="text-sm text-muted-foreground py-4">Loading...</p>
                        ) : (
                            <div className="space-y-4">
                                <div className="space-y-1.5">
                                    <Label htmlFor="cn-diagnosis">Diagnosis <span className="text-destructive">*</span></Label>
                                    <Textarea id="cn-diagnosis" placeholder="Primary diagnosis..."
                                              value={diagnosis} onChange={(e) => setDiagnosis(e.target.value)}
                                              className="resize-none" rows={2} />
                                </div>
                                <div className="space-y-1.5">
                                    <Label htmlFor="cn-treatment">Treatment</Label>
                                    <Textarea id="cn-treatment" placeholder="Recommended treatment plan..."
                                              value={treatment} onChange={(e) => setTreatment(e.target.value)}
                                              className="resize-none" rows={3} />
                                </div>
                                <div className="space-y-1.5">
                                    <Label htmlFor="cn-prescription">Prescription</Label>
                                    <Textarea id="cn-prescription" placeholder="Medications prescribed..."
                                              value={prescription} onChange={(e) => setPrescription(e.target.value)}
                                              className="resize-none" rows={3} />
                                </div>
                                <div className="space-y-1.5">
                                    <Label htmlFor="cn-notes">Notes</Label>
                                    <Textarea id="cn-notes" placeholder="Additional notes..."
                                              value={noteText} onChange={(e) => setNoteText(e.target.value)}
                                              className="resize-none" rows={3} />
                                </div>

                                {/* Documents — only available after the note has been saved */}
                                <div className="space-y-2 pt-2 border-t border-border">
                                    <div className="flex items-center justify-between">
                                        <Label className="text-sm">
                                            Documents {noteDocuments.length > 0 && <span className="text-muted-foreground">({noteDocuments.length})</span>}
                                        </Label>
                                        <Button
                                            type="button" size="sm" variant="outline" className="gap-1.5"
                                            disabled={!existingNote || uploadCurrent.isPending}
                                            onClick={() => noteDocsRef.current?.click()}
                                        >
                                            <Upload className="h-3.5 w-3.5" />
                                            {uploadCurrent.isPending ? "Uploading..." : "Upload Document"}
                                        </Button>
                                        <input
                                            ref={noteDocsRef} type="file" className="hidden"
                                            accept=".pdf,.jpg,.jpeg,.png,.gif,.doc,.docx"
                                            onChange={(e) => {
                                                const f = e.target.files?.[0];
                                                if (f) uploadCurrent.mutate(f);
                                                e.target.value = "";
                                            }}
                                        />
                                    </div>
                                    {!existingNote ? (
                                        <p className="text-xs text-muted-foreground">Save the note first to attach documents.</p>
                                    ) : noteDocuments.length === 0 ? (
                                        <p className="text-xs text-muted-foreground">No documents attached.</p>
                                    ) : (
                                        <div className="space-y-1">
                                            {noteDocuments.map((doc) => (
                                                <DocumentRow key={doc.id} noteId={existingNote.id} doc={doc} patientProfileId={patId} />
                                            ))}
                                        </div>
                                    )}
                                </div>

                                <div className="flex gap-2 pt-2">
                                    <Button variant="outline" onClick={() => navigate(-1)} disabled={saving}>Cancel</Button>
                                    <Button onClick={handleSave} disabled={saving || !diagnosis.trim()}>
                                        {saving ? "Saving..." : "Save Notes"}
                                    </Button>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </RolePageShell>
    );
}