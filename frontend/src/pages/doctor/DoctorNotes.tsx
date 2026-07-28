import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { pdf } from "@react-pdf/renderer";
import { RolePageShell } from "@/components/RolePageShell";
import { EmptyState } from "@/components/EmptyState";
import { ClipboardIllustration } from "@/components/illustrations/clipboard";
import { MicroscopeIllustration } from "@/components/illustrations/microscope";
import { ConsultationReportPDF } from "@/components/ConsultationReportPDF";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Search, User, Calendar, Stethoscope, ClipboardList, Pill, StickyNote,
  Pencil, Paperclip, FileDown,
} from "lucide-react";
import { useMyNotes, useDoctorProfile } from "@/hooks/useDoctor";
import type { DoctorNoteListItem } from "@/types/api";

function formatDate(dateStr: string) {
  return new Date(dateStr + "T00:00:00").toLocaleDateString("en-US", {
    month: "short", day: "numeric", year: "numeric",
  });
}

function RecordCard({
                      note,
                      doctorName,
                      doctorSpecialization,
                      onEdit,
                      onViewPatient,
                    }: {
  note: DoctorNoteListItem;
  doctorName: string;
  doctorSpecialization: string;
  onEdit: (note: DoctorNoteListItem) => void;
  onViewPatient: (patientProfileId: number) => void;
}) {
  const [generating, setGenerating] = useState(false);

  const snippets = [
    { icon: ClipboardList, label: "Treatment",    value: note.treatment },
    { icon: Pill,          label: "Prescription", value: note.prescription },
    { icon: StickyNote,    label: "Notes",        value: note.notes },
  ].filter((s) => s.value);

  const handleExportPdf = async () => {
    setGenerating(true);
    try {
      const blob = await pdf(
          <ConsultationReportPDF
              note={note}
              doctorName={doctorName}
              doctorSpecialization={doctorSpecialization}
          />
      ).toBlob();
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `report-${note.patientName.replace(/\s+/g, "-")}-${note.appointmentDate}.pdf`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } finally {
      setGenerating(false);
    }
  };

  return (
      <div className="rounded-xl border border-border bg-card p-5 space-y-3 hover:border-primary/20 transition-colors">
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-start gap-3 min-w-0">
            <div className="p-2 rounded-lg bg-primary/10 shrink-0">
              <Stethoscope className="h-4 w-4 text-primary" />
            </div>
            <div className="min-w-0">
              {/* Diagnosis doubles as the card title, but it is free text and can run
                  to a paragraph — clamp it and keep the full value on hover. */}
              <p className="text-sm font-semibold text-foreground line-clamp-2" title={note.diagnosis ?? undefined}>
                {note.diagnosis}
              </p>
              <div className="flex items-center gap-3 mt-1 text-xs text-muted-foreground flex-wrap">
                <button
                    onClick={() => onViewPatient(note.patientProfileId)}
                    className="flex items-center gap-1 hover:text-primary transition-colors"
                >
                  <User className="h-3 w-3" />{note.patientName}
                </button>
                <span className="flex items-center gap-1">
                <Calendar className="h-3 w-3" />{formatDate(note.appointmentDate)}
              </span>
                {note.documents.length > 0 && (
                    <Badge variant="outline" className="gap-1 text-[10px] px-1.5 py-0 h-4 bg-info/10 text-info border-info/20">
                      <Paperclip className="h-2.5 w-2.5" />
                      {note.documents.length} file{note.documents.length !== 1 ? "s" : ""}
                    </Badge>
                )}
              </div>
            </div>
          </div>
          <div className="flex items-center gap-1 shrink-0">
            <Button
                size="sm"
                variant="ghost"
                className="gap-1.5 text-muted-foreground hover:text-foreground"
                disabled={generating}
                onClick={handleExportPdf}
            >
              <FileDown className="h-3.5 w-3.5" />
              {generating ? "Generating..." : "PDF"}
            </Button>
            <Button
                size="sm"
                variant="ghost"
                className="gap-1.5 text-muted-foreground hover:text-foreground"
                onClick={() => onEdit(note)}
            >
              <Pencil className="h-3.5 w-3.5" />
              Edit
            </Button>
          </div>
        </div>

        {snippets.length > 0 && (
            <div className="space-y-1 pl-11">
              {snippets.map(({ label, value }) => (
                  <p key={label} className="text-xs text-muted-foreground line-clamp-1">
                    <span className="font-medium text-foreground/70">{label}:</span> {value}
                  </p>
              ))}
            </div>
        )}
      </div>
  );
}

export default function DoctorNotes() {
  const navigate = useNavigate();
  const { data: notes = [], isLoading } = useMyNotes();
  const { data: profile } = useDoctorProfile();
  const [search, setSearch] = useState("");

  const doctorName = profile ? `${profile.firstName} ${profile.lastName}` : "";
  const doctorSpecialization = profile?.specializations?.[0]?.name ?? "";

  const filtered = useMemo(
      () =>
          notes.filter(
              (n) =>
                  n.patientName.toLowerCase().includes(search.toLowerCase()) ||
                  n.diagnosis.toLowerCase().includes(search.toLowerCase())
          ),
      [notes, search]
  );

  return (
      <RolePageShell title="Medical Records" subtitle="Your consultation notes — edit, attach files, export reports">
        <div className="relative max-w-md">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
              placeholder="Search by patient or diagnosis..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="pl-10"
          />
        </div>

        {isLoading ? (
            <div className="space-y-3">
              {Array.from({ length: 4 }).map((_, i) => (
                  <div key={i} className="rounded-xl border border-border bg-card p-5 h-24 animate-pulse" />
              ))}
            </div>
        ) : notes.length === 0 ? (
            <EmptyState
                illustration={ClipboardIllustration}
                title="No consultation notes yet"
                description="Notes you record during a consultation are collected here."
            />
        ) : filtered.length === 0 ? (
            <EmptyState
                illustration={MicroscopeIllustration}
                title="No notes match your search"
                description="Try a different patient name, diagnosis or date."
            />
        ) : (
            <div className="space-y-3">
              {filtered.map((note) => (
                  <RecordCard
                      key={note.noteId}
                      note={note}
                      doctorName={doctorName}
                      doctorSpecialization={doctorSpecialization}
                      onEdit={(n) => navigate(`/doctor/consultation/${n.appointmentId}/${n.patientProfileId}`)}
                      onViewPatient={(id) => navigate(`/doctor/patients/${id}`)}
                  />
              ))}
            </div>
        )}
      </RolePageShell>
  );
}