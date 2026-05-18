import { useEffect, useState } from "react";
import { DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import {
  useConsultationNote,
  useCreateConsultationNote,
  useUpdateConsultationNote,
} from "@/hooks/useDoctor";
import type { AppointmentResponse } from "@/types/api";

export function ConsultationNotesDialog({
  appointment,
  onClose,
}: {
  appointment: AppointmentResponse;
  onClose: () => void;
}) {
  const { data: existingNote, isLoading } = useConsultationNote(appointment.id);

  const [diagnosis,    setDiagnosis]    = useState("");
  const [treatment,    setTreatment]    = useState("");
  const [prescription, setPrescription] = useState("");
  const [notes,        setNotes]        = useState("");

  useEffect(() => {
    if (existingNote) {
      setDiagnosis(existingNote.diagnosis ?? "");
      setTreatment(existingNote.treatment ?? "");
      setPrescription(existingNote.prescription ?? "");
      setNotes(existingNote.notes ?? "");
    }
  }, [existingNote]);

  const createNote = useCreateConsultationNote(appointment.id, onClose);
  const updateNote = useUpdateConsultationNote(appointment.id, onClose);
  const isPending = createNote.isPending || updateNote.isPending;

  const handleSave = () => {
    if (!diagnosis.trim()) return;
    const data = {
      diagnosis: diagnosis.trim(),
      treatment: treatment.trim() || undefined,
      prescription: prescription.trim() || undefined,
      notes: notes.trim() || undefined,
    };
    if (existingNote) {
      updateNote.mutate(data);
    } else {
      createNote.mutate(data);
    }
  };

  return (
    <>
      <DialogHeader>
        <DialogTitle>
          {existingNote ? "Edit Notes" : "Add Notes"} — {appointment.patientName}
        </DialogTitle>
      </DialogHeader>
      {isLoading ? (
        <p className="text-sm text-muted-foreground py-4">Loading...</p>
      ) : (
        <div className="space-y-4 pt-1">
          <div className="space-y-1.5">
            <Label htmlFor="cn-diagnosis">
              Diagnosis <span className="text-destructive">*</span>
            </Label>
            <Textarea
              id="cn-diagnosis"
              placeholder="Primary diagnosis..."
              value={diagnosis}
              onChange={(e) => setDiagnosis(e.target.value)}
              className="resize-none"
              rows={2}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="cn-treatment">Treatment</Label>
            <Textarea
              id="cn-treatment"
              placeholder="Recommended treatment plan..."
              value={treatment}
              onChange={(e) => setTreatment(e.target.value)}
              className="resize-none"
              rows={2}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="cn-prescription">Prescription</Label>
            <Textarea
              id="cn-prescription"
              placeholder="Medications prescribed..."
              value={prescription}
              onChange={(e) => setPrescription(e.target.value)}
              className="resize-none"
              rows={2}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="cn-notes">Notes</Label>
            <Textarea
              id="cn-notes"
              placeholder="Additional notes..."
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              className="resize-none"
              rows={2}
            />
          </div>
          <div className="flex gap-2 pt-1">
            <Button variant="outline" className="flex-1" onClick={onClose} disabled={isPending}>
              Cancel
            </Button>
            <Button
              className="flex-1"
              onClick={handleSave}
              disabled={isPending || !diagnosis.trim()}
            >
              {isPending ? "Saving..." : "Save Notes"}
            </Button>
          </div>
        </div>
      )}
    </>
  );
}