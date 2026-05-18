import { CollapsibleSection } from "@/components/CollapsibleSection";
import { Badge } from "@/components/ui/badge";
import { AlertTriangle, Activity, Users, Coffee, Ruler } from "lucide-react";
import { formatBloodType } from "@/components/PatientDetailsSheet";
import type { PatientSummary } from "@/types/api";

function splitLines(text: string | null): string[] {
  if (!text) return [];
  return text.split(/\r?\n/).map((l) => l.trim()).filter(Boolean);
}

export function PatientHealthInfo({ patient }: { patient: PatientSummary }) {
  const allergies  = splitLines(patient.allergies);
  const conditions = splitLines(patient.chronicConditions);
  const family     = splitLines(patient.familyHistory);

  const lifestyle: { label: string; value: string | null }[] = [
    { label: "Smoking",  value: patient.lifestyleSmoking },
    { label: "Alcohol",  value: patient.lifestyleAlcohol },
    { label: "Exercise", value: patient.lifestyleExercise },
    { label: "Diet",     value: patient.lifestyleDiet },
  ];
  const lifestyleHasData = lifestyle.some((l) => l.value);

  const hasVitals = patient.heightCm != null || patient.weightKg != null || patient.bloodType != null;

  return (
    <>
      {/* Allergies — open by default, most clinically critical */}
      <CollapsibleSection
        title="Allergies"
        icon={AlertTriangle}
        accent="text-destructive"
        defaultOpen
        badge={
          allergies.length > 0 ? (
            <Badge variant="outline" className="text-[10px] px-1.5 py-0 h-4 bg-destructive/10 text-destructive border-destructive/20">
              {allergies.length}
            </Badge>
          ) : null
        }
      >
        {allergies.length === 0 ? (
          <p className="text-xs text-muted-foreground italic">None recorded.</p>
        ) : (
          <div className="flex flex-wrap gap-1.5">
            {allergies.map((a) => (
              <Badge key={a} variant="outline" className="bg-destructive/10 text-destructive border-destructive/20">{a}</Badge>
            ))}
          </div>
        )}
      </CollapsibleSection>

      {/* Vitals */}
      <CollapsibleSection title="Vitals" icon={Ruler} accent="text-primary">
        {!hasVitals ? (
          <p className="text-xs text-muted-foreground italic">Not recorded.</p>
        ) : (
          <div className="grid grid-cols-3 gap-2">
            <div className="rounded-lg bg-muted/40 p-2 text-center">
              <p className="text-[10px] uppercase tracking-wider text-muted-foreground">Height</p>
              <p className="text-sm font-semibold text-foreground mt-0.5">
                {patient.heightCm != null ? `${patient.heightCm} cm` : "—"}
              </p>
            </div>
            <div className="rounded-lg bg-muted/40 p-2 text-center">
              <p className="text-[10px] uppercase tracking-wider text-muted-foreground">Weight</p>
              <p className="text-sm font-semibold text-foreground mt-0.5">
                {patient.weightKg != null ? `${patient.weightKg} kg` : "—"}
              </p>
            </div>
            <div className="rounded-lg bg-muted/40 p-2 text-center">
              <p className="text-[10px] uppercase tracking-wider text-muted-foreground">Blood</p>
              <p className="text-sm font-semibold text-foreground mt-0.5">
                {patient.bloodType ? formatBloodType(patient.bloodType) : "—"}
              </p>
            </div>
          </div>
        )}
      </CollapsibleSection>

      {/* Chronic Conditions */}
      <CollapsibleSection
        title="Chronic Conditions"
        icon={Activity}
        accent="text-warning"
        badge={
          conditions.length > 0 ? (
            <Badge variant="outline" className="text-[10px] px-1.5 py-0 h-4 bg-warning/10 text-warning border-warning/20">
              {conditions.length}
            </Badge>
          ) : null
        }
      >
        {conditions.length === 0 ? (
          <p className="text-xs text-muted-foreground italic">None recorded.</p>
        ) : (
          <div className="flex flex-wrap gap-1.5">
            {conditions.map((c) => (
              <Badge key={c} variant="outline" className="bg-warning/10 text-warning border-warning/20">{c}</Badge>
            ))}
          </div>
        )}
      </CollapsibleSection>

      {/* Family History */}
      <CollapsibleSection title="Family History" icon={Users} accent="text-info">
        {family.length === 0 ? (
          <p className="text-xs text-muted-foreground italic">None recorded.</p>
        ) : (
          <ul className="space-y-1.5">
            {family.map((f) => (
              <li key={f} className="text-sm text-foreground flex items-start gap-2">
                <span className="mt-1.5 h-1.5 w-1.5 rounded-full bg-info shrink-0" />
                {f}
              </li>
            ))}
          </ul>
        )}
      </CollapsibleSection>

      {/* Lifestyle */}
      <CollapsibleSection title="Lifestyle" icon={Coffee} accent="text-success">
        {!lifestyleHasData ? (
          <p className="text-xs text-muted-foreground italic">Not recorded.</p>
        ) : (
          <div className="grid grid-cols-2 gap-2">
            {lifestyle.map(({ label, value }) => (
              <div key={label} className="rounded-lg bg-muted/40 p-2">
                <p className="text-[10px] uppercase tracking-wider text-muted-foreground">{label}</p>
                <p className="text-sm font-medium text-foreground mt-0.5">{value || "—"}</p>
              </div>
            ))}
          </div>
        )}
      </CollapsibleSection>
    </>
  );
}
