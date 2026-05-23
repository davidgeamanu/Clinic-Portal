import { useMemo, useState } from "react";
import { pdf } from "@react-pdf/renderer";
import { RolePageShell } from "@/components/RolePageShell";
import { ConsultationReportPDF } from "@/components/ConsultationReportPDF";
import { DocumentRow } from "@/components/DocumentRow";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Search, User, Calendar, Stethoscope, Paperclip, FileDown } from "lucide-react";
import { useMyMedicalRecords, usePatientProfile } from "@/hooks/usePatient";
import type { DoctorNoteListItem, PatientHistoryItem } from "@/types/api";

function formatDate(dateStr: string) {
  return new Date(dateStr + "T00:00:00").toLocaleDateString("en-US", {
    month: "short", day: "numeric", year: "numeric",
  });
}

function RecordCard({
  record,
  patientName,
}: {
  record: PatientHistoryItem;
  patientName: string;
}) {
  const [generating, setGenerating] = useState(false);

  const snippets = [
    { label: "Treatment",    value: record.treatment },
    { label: "Prescription", value: record.prescription },
    { label: "Notes",        value: record.notes },
  ].filter((s) => s.value);

  const handleExportPdf = async () => {
    setGenerating(true);
    try {
      // ConsultationReportPDF was built for the doctor's note shape — adapt minimal fields.
      const pdfNote: DoctorNoteListItem = {
        noteId: record.noteId,
        appointmentId: record.appointmentId,
        patientProfileId: 0,
        patientName,
        appointmentDate: record.appointmentDate,
        diagnosis: record.diagnosis,
        treatment: record.treatment,
        prescription: record.prescription,
        notes: record.notes,
        documents: record.documents,
      };
      const blob = await pdf(
        <ConsultationReportPDF
          note={pdfNote}
          doctorName={record.doctorName}
          doctorSpecialization=""
        />
      ).toBlob();
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `report-${record.doctorName.replace(/\s+/g, "-")}-${record.appointmentDate}.pdf`;
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
            <p className="text-sm font-semibold text-foreground">{record.diagnosis}</p>
            <div className="flex items-center gap-3 mt-1 text-xs text-muted-foreground flex-wrap">
              <span className="flex items-center gap-1">
                <User className="h-3 w-3" />Dr. {record.doctorName}
              </span>
              <span className="flex items-center gap-1">
                <Calendar className="h-3 w-3" />{formatDate(record.appointmentDate)}
              </span>
              {record.documents.length > 0 && (
                <Badge variant="outline" className="gap-1 text-[10px] px-1.5 py-0 h-4 bg-info/10 text-info border-info/20">
                  <Paperclip className="h-2.5 w-2.5" />
                  {record.documents.length} file{record.documents.length !== 1 ? "s" : ""}
                </Badge>
              )}
            </div>
          </div>
        </div>
        <Button
          size="sm"
          variant="ghost"
          className="gap-1.5 shrink-0 text-muted-foreground hover:text-foreground"
          disabled={generating}
          onClick={handleExportPdf}
        >
          <FileDown className="h-3.5 w-3.5" />
          {generating ? "Generating..." : "PDF"}
        </Button>
      </div>

      {snippets.length > 0 && (
        <div className="space-y-1 pl-11">
          {snippets.map(({ label, value }) => (
            <p key={label} className="text-xs text-muted-foreground line-clamp-2">
              <span className="font-medium text-foreground/70">{label}:</span> {value}
            </p>
          ))}
        </div>
      )}

      {record.documents.length > 0 && (
        <div className="pl-11 space-y-1.5">
          {record.documents.map((doc) => (
            <DocumentRow
              key={doc.id}
              noteId={record.noteId}
              doc={doc}
              patientProfileId={0}
              canDelete={false}
            />
          ))}
        </div>
      )}
    </div>
  );
}

export default function PatientRecords() {
  const { data: records = [], isLoading } = useMyMedicalRecords();
  const { data: profile } = usePatientProfile();
  const [search, setSearch] = useState("");

  const patientName = profile ? `${profile.firstName} ${profile.lastName}` : "";

  const filtered = useMemo(
    () =>
      records.filter(
        (r) =>
          r.doctorName.toLowerCase().includes(search.toLowerCase()) ||
          r.diagnosis.toLowerCase().includes(search.toLowerCase())
      ),
    [records, search]
  );

  return (
    <RolePageShell title="Medical Records" subtitle="Your consultation history — view notes, download attached files and reports">
      <div className="relative max-w-md">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Search by doctor or diagnosis..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="pl-10"
        />
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="rounded-xl border border-border bg-card p-5 h-28 animate-pulse" />
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <p className="text-center text-muted-foreground py-12">
          {records.length === 0 ? "You don't have any consultation notes yet." : "No records match your search."}
        </p>
      ) : (
        <div className="space-y-3">
          {filtered.map((record) => (
            <RecordCard key={record.noteId} record={record} patientName={patientName} />
          ))}
        </div>
      )}
    </RolePageShell>
  );
}