import { useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { Sheet, SheetContent, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Mail, Phone, User as UserIcon, Heart, ShieldAlert, ExternalLink } from "lucide-react";
import { usePatientSummary } from "@/hooks/useDoctor";
import type { AppointmentResponse, AppointmentStatus, PatientSummary } from "@/types/api";

// ── Formatters ────────────────────────────────────────────────────────────────

const BLOOD_TYPE_LABELS: Record<string, string> = {
  A_POSITIVE: "A+",  A_NEGATIVE: "A−",
  B_POSITIVE: "B+",  B_NEGATIVE: "B−",
  AB_POSITIVE: "AB+", AB_NEGATIVE: "AB−",
  O_POSITIVE: "O+",  O_NEGATIVE: "O−",
  UNKNOWN: "Unknown",
};

export function formatBloodType(bt: string | null): string {
  if (!bt) return "—";
  return BLOOD_TYPE_LABELS[bt] ?? bt;
}

export function formatDOB(dob: string | null): string {
  if (!dob) return "—";
  const d = new Date(dob + "T00:00:00");
  return `${String(d.getDate()).padStart(2, "0")}-${String(d.getMonth() + 1).padStart(2, "0")}-${d.getFullYear()}`;
}

export function formatGender(g: string | null): string {
  if (!g) return "—";
  return g.charAt(0) + g.slice(1).toLowerCase();
}

export function calcAge(dob: string | null): number | null {
  if (!dob) return null;
  const diff = Date.now() - new Date(dob + "T00:00:00").getTime();
  return Math.floor(diff / (1000 * 60 * 60 * 24 * 365.25));
}

// ── Status styling (mirrors the appointments page) ────────────────────────────

const STATUS_STYLES: Record<AppointmentStatus, string> = {
  SCHEDULED:   "bg-warning/10 text-warning border-warning/20",
  CONFIRMED:   "bg-success/10 text-success border-success/20",
  IN_PROGRESS: "bg-primary/10 text-primary border-primary/20",
  COMPLETED:   "bg-muted text-muted-foreground border-border",
  CANCELLED:   "bg-muted text-muted-foreground border-border",
};

const STATUS_LABELS: Record<AppointmentStatus, string> = {
  SCHEDULED:   "Upcoming",
  CONFIRMED:   "Confirmed",
  IN_PROGRESS: "In Progress",
  COMPLETED:   "Completed",
  CANCELLED:   "Cancelled",
};

function formatTime(iso: string) {
  return new Date(iso).toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit", hour12: true });
}

// ── Sub-sections ──────────────────────────────────────────────────────────────

function ProfileSection({ patient }: { patient: PatientSummary }) {
  const rows = [
    { icon: Mail,     label: "Email",      value: patient.email },
    { icon: Phone,    label: "Phone",      value: patient.phoneNumber },
    { icon: UserIcon, label: "DOB",        value: formatDOB(patient.dateOfBirth) },
    { icon: UserIcon, label: "Gender",     value: formatGender(patient.gender) },
    { icon: Heart,    label: "Blood Type", value: formatBloodType(patient.bloodType) },
  ].filter((r) => r.value && r.value !== "—");

  return (
    <div>
      <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3">Profile</p>
      <div className="space-y-2.5">
        {rows.map(({ icon: Icon, label, value }) => (
          <div key={label} className="flex items-center gap-3 text-sm">
            <Icon className="h-4 w-4 text-muted-foreground shrink-0" />
            <span className="text-muted-foreground w-24 shrink-0">{label}</span>
            <span className="text-foreground">{value}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

function EmergencySection({ patient }: { patient: PatientSummary }) {
  return (
    <div>
      <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3">Emergency Contact</p>
      <div className="space-y-2.5">
        {patient.emergencyContactName && (
          <div className="flex items-center gap-3 text-sm">
            <ShieldAlert className="h-4 w-4 text-muted-foreground shrink-0" />
            <span className="text-muted-foreground w-24 shrink-0">Name</span>
            <span className="text-foreground">{patient.emergencyContactName}</span>
          </div>
        )}
        {patient.emergencyContactPhone && (
          <div className="flex items-center gap-3 text-sm">
            <Phone className="h-4 w-4 text-muted-foreground shrink-0" />
            <span className="text-muted-foreground w-24 shrink-0">Phone</span>
            <span className="text-foreground">{patient.emergencyContactPhone}</span>
          </div>
        )}
      </div>
    </div>
  );
}

function AppointmentHistorySection({ history }: { history: AppointmentResponse[] }) {
  return (
    <div>
      <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3">
        Appointment History ({history.length})
      </p>
      {history.length === 0 ? (
        <p className="text-sm text-muted-foreground">No appointments on record.</p>
      ) : (
        <div className="space-y-2">
          {history.map((a) => (
            <div key={a.id} className="flex items-center justify-between p-2.5 rounded-lg bg-muted/40 text-sm">
              <div>
                <p className="font-medium text-foreground">
                  {new Date(a.scheduledAt).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" })}
                </p>
                <p className="text-xs text-muted-foreground">{formatTime(a.scheduledAt)} · {a.durationMinutes} min</p>
              </div>
              <Badge variant="outline" className={`text-[10px] ${STATUS_STYLES[a.status]}`}>
                {STATUS_LABELS[a.status]}
              </Badge>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

// ── Sheet ─────────────────────────────────────────────────────────────────────

export function PatientDetailsSheet({
  patientProfileId,
  patientName,
  allAppointments,
  onClose,
}: {
  patientProfileId: number | null;
  patientName: string;
  allAppointments: AppointmentResponse[];
  onClose: () => void;
}) {
  const navigate = useNavigate();
  const { data: patient, isLoading } = usePatientSummary(patientProfileId);

  const history = useMemo(
    () =>
      allAppointments
        .filter((a) => a.patientId === patientProfileId)
        .sort((a, b) => new Date(b.scheduledAt).getTime() - new Date(a.scheduledAt).getTime()),
    [allAppointments, patientProfileId]
  );

  return (
    <Sheet open={!!patientProfileId} onOpenChange={onClose}>
      <SheetContent className="w-full sm:max-w-md overflow-y-auto">
        <SheetHeader className="mb-6">
          <div className="flex items-center gap-3">
            <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center text-base font-bold text-primary shrink-0">
              {patientName.split(" ").map((n) => n[0]).join("").slice(0, 2)}
            </div>
            <SheetTitle className="text-lg">{patientName}</SheetTitle>
          </div>
        </SheetHeader>

        {isLoading ? (
          <p className="text-sm text-muted-foreground">Loading patient details...</p>
        ) : patient ? (
          <div className="space-y-6">
            <ProfileSection patient={patient} />
            {(patient.emergencyContactName || patient.emergencyContactPhone) && (
              <EmergencySection patient={patient} />
            )}
            <AppointmentHistorySection history={history} />
          </div>
        ) : (
          <p className="text-sm text-muted-foreground">Could not load patient details.</p>
        )}

        {patientProfileId && (
          <div className="pt-4 border-t border-border">
            <Button
              variant="outline"
              size="sm"
              className="w-full gap-2"
              onClick={() => { onClose(); navigate(`/doctor/patients/${patientProfileId}`); }}
            >
              <ExternalLink className="h-3.5 w-3.5" />
              View Full Record
            </Button>
          </div>
        )}
      </SheetContent>
    </Sheet>
  );
}