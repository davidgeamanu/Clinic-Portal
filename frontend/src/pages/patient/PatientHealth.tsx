import { useState, useEffect } from "react";
import { RolePageShell } from "@/components/RolePageShell";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Heart, Activity, Droplets, Weight, Ruler, Droplet, Edit, Info } from "lucide-react";
import { usePatientProfile, useUpdatePatientProfile } from "@/hooks/usePatient";
import type { PatientProfileResponse, PatientProfileUpdateRequest } from "@/types/api";

const BLOOD_TYPES = ["A_POSITIVE","A_NEGATIVE","B_POSITIVE","B_NEGATIVE","AB_POSITIVE","AB_NEGATIVE","O_POSITIVE","O_NEGATIVE"] as const;

function formatBloodType(bt: string | null): string {
  if (!bt) return "—";
  const map: Record<string, string> = {
    A_POSITIVE: "A+", A_NEGATIVE: "A-",
    B_POSITIVE: "B+", B_NEGATIVE: "B-",
    AB_POSITIVE: "AB+", AB_NEGATIVE: "AB-",
    O_POSITIVE: "O+", O_NEGATIVE: "O-",
  };
  return map[bt] ?? bt;
}

function splitLines(text: string | null): string[] {
  if (!text) return [];
  return text.split(/\r?\n/).map((l) => l.trim()).filter(Boolean);
}

const SMOKING_OPTIONS  = ["Never", "Former", "Occasional", "Daily"];
const ALCOHOL_OPTIONS  = ["Never", "Rarely", "Occasional", "Weekly", "Daily"];
const EXERCISE_OPTIONS = ["Never", "1x/week", "2-3x/week", "4-5x/week", "Daily"];
const DIET_OPTIONS     = ["Balanced", "Low sodium", "Low carb", "Vegetarian", "Vegan", "Mediterranean", "Diabetic"];

// ── Vitals ────────────────────────────────────────────────────────────────────

function VitalCard({
  label, icon: Icon, value, unit, normal, notImplemented,
}: {
  label: string;
  icon: typeof Heart;
  value: string;
  unit?: string;
  normal?: string;
  notImplemented?: boolean;
}) {
  return (
    <div className="rounded-xl border border-border bg-card p-5">
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-lg bg-primary/10">
            <Icon className="h-4 w-4 text-primary" />
          </div>
          <span className="text-sm font-medium text-muted-foreground">{label}</span>
        </div>
        {notImplemented && (
          <Badge variant="outline" className="text-[10px] gap-1 bg-muted/40 text-muted-foreground border-border">
            <Info className="h-2.5 w-2.5" />
            Not tracked yet
          </Badge>
        )}
      </div>
      {notImplemented ? (
        <p className="text-sm text-muted-foreground italic">Coming soon — no device integration yet.</p>
      ) : (
        <>
          <p className="text-2xl font-bold text-foreground">
            {value} {unit && <span className="text-sm font-normal text-muted-foreground">{unit}</span>}
          </p>
          {normal && <p className="text-xs text-muted-foreground mt-1">Normal: {normal}</p>}
        </>
      )}
    </div>
  );
}

// ── Edit Vitals dialog (height, weight, blood type) ────────────────────────

function EditVitalsDialog({
  open, onOpenChange, profile, onSave, submitting,
}: {
  open: boolean;
  onOpenChange: (o: boolean) => void;
  profile: PatientProfileResponse;
  onSave: (patch: PatientProfileUpdateRequest) => void;
  submitting: boolean;
}) {
  const [height, setHeight]       = useState<string>("");
  const [weight, setWeight]       = useState<string>("");
  const [bloodType, setBloodType] = useState<string>("");

  useEffect(() => {
    if (open) {
      setHeight(profile.heightCm?.toString() ?? "");
      setWeight(profile.weightKg?.toString() ?? "");
      setBloodType(profile.bloodType ?? "");
    }
  }, [open, profile]);

  const handleSave = () => {
    const patch: PatientProfileUpdateRequest = {};
    patch.heightCm = height === "" ? null : Number(height);
    patch.weightKg = weight === "" ? null : Number(weight);
    if (bloodType) patch.bloodType = bloodType;
    onSave(patch);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Edit Vitals</DialogTitle>
          <DialogDescription>Update your height, weight, and blood type.</DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-2">
              <Label htmlFor="height">Height (cm)</Label>
              <Input id="height" type="number" min={30} max={250} value={height}
                onChange={(e) => setHeight(e.target.value)} placeholder="e.g. 175" />
            </div>
            <div className="space-y-2">
              <Label htmlFor="weight">Weight (kg)</Label>
              <Input id="weight" type="number" min={1} max={500} step="0.1" value={weight}
                onChange={(e) => setWeight(e.target.value)} placeholder="e.g. 72.5" />
            </div>
          </div>
          <div className="space-y-2">
            <Label>Blood Type</Label>
            <Select value={bloodType} onValueChange={setBloodType}>
              <SelectTrigger><SelectValue placeholder="Select blood type" /></SelectTrigger>
              <SelectContent>
                {BLOOD_TYPES.map((b) => (
                  <SelectItem key={b} value={b}>{formatBloodType(b)}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={submitting}>Cancel</Button>
          <Button onClick={handleSave} disabled={submitting}>{submitting ? "Saving..." : "Save"}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

// ── Edit list dialog (allergies / conditions / family history) ─────────────

function EditListDialog({
  open, onOpenChange, title, description, initialValue, onSave, submitting,
}: {
  open: boolean;
  onOpenChange: (o: boolean) => void;
  title: string;
  description: string;
  initialValue: string;
  onSave: (text: string) => void;
  submitting: boolean;
}) {
  const [text, setText] = useState<string>(initialValue);

  useEffect(() => { if (open) setText(initialValue); }, [open, initialValue]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>{description} One item per line.</DialogDescription>
        </DialogHeader>
        <Textarea value={text} onChange={(e) => setText(e.target.value)} rows={8} placeholder="Type one item per line..." />
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={submitting}>Cancel</Button>
          <Button onClick={() => onSave(text)} disabled={submitting}>{submitting ? "Saving..." : "Save"}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

// ── Edit Lifestyle dialog ─────────────────────────────────────────────────

function LifestyleField({
  label, value, options, onChange,
}: {
  label: string;
  value: string;
  options: string[];
  onChange: (v: string) => void;
}) {
  return (
    <div className="space-y-2">
      <Label>{label}</Label>
      <Select value={value} onValueChange={onChange}>
        <SelectTrigger><SelectValue placeholder={`Select ${label.toLowerCase()}`} /></SelectTrigger>
        <SelectContent>
          {options.map((o) => <SelectItem key={o} value={o}>{o}</SelectItem>)}
        </SelectContent>
      </Select>
    </div>
  );
}

function EditLifestyleDialog({
  open, onOpenChange, profile, onSave, submitting,
}: {
  open: boolean;
  onOpenChange: (o: boolean) => void;
  profile: PatientProfileResponse;
  onSave: (patch: PatientProfileUpdateRequest) => void;
  submitting: boolean;
}) {
  const [smoking, setSmoking]   = useState("");
  const [alcohol, setAlcohol]   = useState("");
  const [exercise, setExercise] = useState("");
  const [diet, setDiet]         = useState("");

  useEffect(() => {
    if (open) {
      setSmoking(profile.lifestyleSmoking ?? "");
      setAlcohol(profile.lifestyleAlcohol ?? "");
      setExercise(profile.lifestyleExercise ?? "");
      setDiet(profile.lifestyleDiet ?? "");
    }
  }, [open, profile]);

  const handleSave = () => {
    onSave({
      lifestyleSmoking:  smoking,
      lifestyleAlcohol:  alcohol,
      lifestyleExercise: exercise,
      lifestyleDiet:     diet,
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Edit Lifestyle</DialogTitle>
          <DialogDescription>Tell your doctors a bit about your daily habits.</DialogDescription>
        </DialogHeader>
        <div className="grid grid-cols-2 gap-3">
          <LifestyleField label="Smoking"  value={smoking}  options={SMOKING_OPTIONS}  onChange={setSmoking}  />
          <LifestyleField label="Alcohol"  value={alcohol}  options={ALCOHOL_OPTIONS}  onChange={setAlcohol}  />
          <LifestyleField label="Exercise" value={exercise} options={EXERCISE_OPTIONS} onChange={setExercise} />
          <LifestyleField label="Diet"     value={diet}     options={DIET_OPTIONS}     onChange={setDiet}     />
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={submitting}>Cancel</Button>
          <Button onClick={handleSave} disabled={submitting}>{submitting ? "Saving..." : "Save"}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

// ── Page ───────────────────────────────────────────────────────────────────

type DialogKind = "vitals" | "allergies" | "conditions" | "family" | "lifestyle" | null;

export default function PatientHealth() {
  const { data: profile, isLoading } = usePatientProfile();
  const update = useUpdatePatientProfile();
  const [openDialog, setOpenDialog] = useState<DialogKind>(null);

  if (isLoading || !profile) {
    return (
      <RolePageShell title="Health Profile" subtitle="Your health summary and vital signs">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="rounded-xl border border-border bg-card p-5 h-28 animate-pulse" />
          ))}
        </div>
      </RolePageShell>
    );
  }

  const allergies  = splitLines(profile.allergies);
  const conditions = splitLines(profile.chronicConditions);
  const family     = splitLines(profile.familyHistory);

  const handleSave = (patch: PatientProfileUpdateRequest) => {
    update.mutate(patch, { onSuccess: () => setOpenDialog(null) });
  };

  return (
    <RolePageShell title="Health Profile" subtitle="Your health summary and vital signs">
      {/* Vitals - 2 rows of 3 */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-semibold text-foreground">Vitals</h3>
          <Button variant="outline" size="sm" className="gap-1.5" onClick={() => setOpenDialog("vitals")}>
            <Edit className="h-3.5 w-3.5" /> Edit Vitals
          </Button>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <VitalCard label="Blood Pressure" icon={Activity} value="-" notImplemented />
          <VitalCard label="Heart Rate"     icon={Heart}    value="-" notImplemented />
          <VitalCard label="Blood Sugar"    icon={Droplets} value="-" notImplemented />
          <VitalCard label="Weight"     icon={Weight} value={profile.weightKg != null ? String(profile.weightKg) : "—"} unit={profile.weightKg != null ? "kg" : undefined} />
          <VitalCard label="Height"     icon={Ruler}  value={profile.heightCm != null ? String(profile.heightCm) : "—"} unit={profile.heightCm != null ? "cm" : undefined} />
          <VitalCard label="Blood Type" icon={Droplet} value={formatBloodType(profile.bloodType)} />
        </div>
      </div>

      {/* Health Info Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Allergies */}
        <div className="rounded-xl border border-border bg-card p-5">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-semibold text-foreground">Allergies</h3>
            <Button variant="ghost" size="sm" onClick={() => setOpenDialog("allergies")}><Edit className="h-3.5 w-3.5" /></Button>
          </div>
          {allergies.length === 0 ? (
            <p className="text-xs text-muted-foreground">None recorded.</p>
          ) : (
            <div className="flex flex-wrap gap-2">
              {allergies.map((a) => (
                <Badge key={a} variant="outline" className="bg-destructive/10 text-destructive border-destructive/20">{a}</Badge>
              ))}
            </div>
          )}
        </div>

        {/* Conditions */}
        <div className="rounded-xl border border-border bg-card p-5">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-semibold text-foreground">Chronic Conditions</h3>
            <Button variant="ghost" size="sm" onClick={() => setOpenDialog("conditions")}><Edit className="h-3.5 w-3.5" /></Button>
          </div>
          {conditions.length === 0 ? (
            <p className="text-xs text-muted-foreground">None recorded.</p>
          ) : (
            <div className="flex flex-wrap gap-2">
              {conditions.map((c) => (
                <Badge key={c} variant="outline" className="bg-warning/10 text-warning border-warning/20">{c}</Badge>
              ))}
            </div>
          )}
        </div>

        {/* Family History */}
        <div className="rounded-xl border border-border bg-card p-5">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-semibold text-foreground">Family History</h3>
            <Button variant="ghost" size="sm" onClick={() => setOpenDialog("family")}><Edit className="h-3.5 w-3.5" /></Button>
          </div>
          {family.length === 0 ? (
            <p className="text-xs text-muted-foreground">None recorded.</p>
          ) : (
            <ul className="space-y-2">
              {family.map((f) => (
                <li key={f} className="text-sm text-muted-foreground flex items-center gap-2">
                  <span className="h-1.5 w-1.5 rounded-full bg-primary" />
                  {f}
                </li>
              ))}
            </ul>
          )}
        </div>

        {/* Lifestyle */}
        <div className="rounded-xl border border-border bg-card p-5">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-semibold text-foreground">Lifestyle</h3>
            <Button variant="ghost" size="sm" onClick={() => setOpenDialog("lifestyle")}><Edit className="h-3.5 w-3.5" /></Button>
          </div>
          <div className="grid grid-cols-2 gap-3">
            {[
              { label: "Smoking",  value: profile.lifestyleSmoking },
              { label: "Alcohol",  value: profile.lifestyleAlcohol },
              { label: "Exercise", value: profile.lifestyleExercise },
              { label: "Diet",     value: profile.lifestyleDiet },
            ].map(({ label, value }) => (
              <div key={label}>
                <p className="text-xs text-muted-foreground">{label}</p>
                <p className="text-sm font-medium text-foreground">{value || "—"}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Edit dialogs */}
      <EditVitalsDialog
        open={openDialog === "vitals"}
        onOpenChange={(o) => !o && setOpenDialog(null)}
        profile={profile}
        onSave={handleSave}
        submitting={update.isPending}
      />
      <EditListDialog
        open={openDialog === "allergies"}
        onOpenChange={(o) => !o && setOpenDialog(null)}
        title="Edit Allergies"
        description="Drug, food, environmental - anything you react to."
        initialValue={profile.allergies ?? ""}
        onSave={(text) => handleSave({ allergies: text })}
        submitting={update.isPending}
      />
      <EditListDialog
        open={openDialog === "conditions"}
        onOpenChange={(o) => !o && setOpenDialog(null)}
        title="Edit Chronic Conditions"
        description="Ongoing diagnoses you're managing."
        initialValue={profile.chronicConditions ?? ""}
        onSave={(text) => handleSave({ chronicConditions: text })}
        submitting={update.isPending}
      />
      <EditListDialog
        open={openDialog === "family"}
        onOpenChange={(o) => !o && setOpenDialog(null)}
        title="Edit Family History"
        description="Relevant diagnoses in close family members."
        initialValue={profile.familyHistory ?? ""}
        onSave={(text) => handleSave({ familyHistory: text })}
        submitting={update.isPending}
      />
      <EditLifestyleDialog
        open={openDialog === "lifestyle"}
        onOpenChange={(o) => !o && setOpenDialog(null)}
        profile={profile}
        onSave={handleSave}
        submitting={update.isPending}
      />
    </RolePageShell>
  );
}