import { useState } from "react";
import { Button } from "@/components/ui/button";
import { FileText, Download, X } from "lucide-react";
import { doctorApi } from "@/api/doctor";
import { useDeleteDocument } from "@/hooks/useDoctor";
import type { MedicalDocument } from "@/types/api";

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export function DocumentRow({
  noteId,
  doc,
  patientProfileId,
  canDelete = true,
}: {
  noteId: number;
  doc: MedicalDocument;
  patientProfileId: number;
  canDelete?: boolean;
}) {
  const [downloading, setDownloading] = useState(false);
  const deleteMutation = useDeleteDocument(noteId, patientProfileId);

  const handleDownload = async () => {
    setDownloading(true);
    try {
      await doctorApi.downloadDocument(noteId, doc.id, doc.originalFileName);
    } finally {
      setDownloading(false);
    }
  };

  return (
    <div className="flex items-center justify-between gap-3 p-2.5 rounded-lg border border-border bg-muted/30">
      <div className="flex items-center gap-2.5 min-w-0">
        <FileText className="h-4 w-4 text-muted-foreground shrink-0" />
        <div className="min-w-0">
          <p className="text-sm font-medium text-foreground truncate">{doc.originalFileName}</p>
          <p className="text-xs text-muted-foreground">{formatFileSize(doc.fileSizeBytes)}</p>
        </div>
      </div>
      <div className="flex items-center gap-1 shrink-0">
        <Button
          size="sm"
          variant="ghost"
          className="gap-1.5"
          disabled={downloading}
          onClick={handleDownload}
        >
          <Download className="h-3.5 w-3.5" />
          {downloading ? "..." : "Download"}
        </Button>
        {canDelete && (
          <Button
            size="sm"
            variant="ghost"
            className="text-muted-foreground hover:text-destructive px-2"
            disabled={deleteMutation.isPending}
            onClick={() => deleteMutation.mutate(doc.id)}
          >
            <X className="h-3.5 w-3.5" />
          </Button>
        )}
      </div>
    </div>
  );
}