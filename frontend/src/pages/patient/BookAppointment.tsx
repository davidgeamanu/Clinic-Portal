import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { RolePageShell } from "@/components/RolePageShell";
import { EmptyState } from "@/components/EmptyState";
import { DoctorIllustration } from "@/components/illustrations/doctor";
import { StethoscopeIllustration } from "@/components/illustrations/stethoscope";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Calendar } from "@/components/ui/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Badge } from "@/components/ui/badge";
import {
  CalendarIcon, Video, MapPin, Star, ArrowLeft, ArrowRight,
  CheckCircle2, Stethoscope, DollarSign, Sparkles, AlertTriangle,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { format } from "date-fns";
import { motion, AnimatePresence } from "framer-motion";
import {
  useDoctorsList, useBookAppointment, useBookedSlots,
  useDoctorAvailability, useAvailableSlots, useDoctorReviews, useTriage,
} from "@/hooks/usePatient";
import type { AppointmentMode, BookedSlot, DoctorProfileResponse, SpecializationResponse } from "@/types/api";

const TIME_SLOTS = [
  "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
  "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30",
];

const SLOT_DURATION_MINUTES = 30;

function formatSlot(hhmm: string) {
  const [h, m] = hhmm.split(":").map(Number);
  const period = h >= 12 ? "PM" : "AM";
  const display = h % 12 === 0 ? 12 : h % 12;
  return `${display}:${String(m).padStart(2, "0")} ${period}`;
}

function toLocalIso(date: Date, hhmm: string): string {
  const [h, m] = hhmm.split(":").map(Number);
  const y = date.getFullYear();
  const mo = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${mo}-${d}T${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}:00`;
}

function toLocalDateIso(date: Date): string {
  const y = date.getFullYear();
  const mo = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${mo}-${d}`;
}

function isSlotBooked(slotStartMs: number, slotDurationMin: number, booked: BookedSlot[]): boolean {
  const slotEndMs = slotStartMs + slotDurationMin * 60_000;
  return booked.some((b) => {
    const bStart = new Date(b.scheduledAt).getTime();
    const bEnd = bStart + b.durationMinutes * 60_000;
    return slotStartMs < bEnd && bStart < slotEndMs;
  });
}

function isSameDay(a: Date, b: Date) {
  return a.getFullYear() === b.getFullYear()
    && a.getMonth() === b.getMonth()
    && a.getDate() === b.getDate();
}

function StarRating({ rating }: { rating: number | null }) {
  if (rating == null) return <span className="text-xs text-muted-foreground">Not yet rated</span>;
  return (
    <div className="flex items-center gap-1">
      {[1, 2, 3, 4, 5].map((s) => (
        <Star
          key={s}
          className={cn(
            "h-3.5 w-3.5",
            s <= Math.floor(rating)         ? "fill-warning text-warning" :
            s - 0.5 <= rating               ? "fill-warning/50 text-warning" :
                                              "text-muted-foreground/30"
          )}
        />
      ))}
      <span className="text-sm font-semibold text-foreground ml-1">{rating.toFixed(1)}</span>
    </div>
  );
}

export default function BookAppointment() {
  const navigate = useNavigate();
  const { data: doctors = [], isLoading } = useDoctorsList();
  const book = useBookAppointment(() => navigate("/patient/appointments"));

  const [step, setStep] = useState<1 | 2 | 3>(1);

  const [selectedSpec, setSelectedSpec] = useState<SpecializationResponse | null>(null);
  const [selectedDoctor, setSelectedDoctor] = useState<DoctorProfileResponse | null>(null);
  const [date, setDate] = useState<Date | undefined>(undefined);
  const [slot, setSlot] = useState<string>("");
  const [mode, setMode] = useState<AppointmentMode>("IN_PERSON");
  const [reason, setReason] = useState<string>("");

  // AI symptom triage (step 1 helper)
  const [symptoms, setSymptoms] = useState<string>("");
  const triage = useTriage();

  const isoDate = date ? toLocalDateIso(date) : null;
  const { data: bookedSlots = [] } = useBookedSlots(selectedDoctor?.id ?? null, isoDate);

  // Doctors with configured working hours get server-computed free slots;
  // doctors without any keep the legacy fixed grid filtered by booked slots.
  const { data: workingHours = [] } = useDoctorAvailability(selectedDoctor?.id ?? null);
  const hasWorkingHours = workingHours.length > 0;
  const { data: serverSlots = [] } = useAvailableSlots(
    selectedDoctor?.id ?? null, isoDate, hasWorkingHours);
  const { data: doctorReviews = [] } = useDoctorReviews(selectedDoctor?.id ?? null);

  // Build specialization list from doctors (only active doctors)
  const specs = useMemo(() => {
    const map = new Map<number, { spec: SpecializationResponse; count: number }>();
    doctors.filter((d) => d.active).forEach((d) => {
      d.specializations.forEach((s) => {
        const entry = map.get(s.id);
        if (entry) entry.count += 1;
        else       map.set(s.id, { spec: s, count: 1 });
      });
    });
    return Array.from(map.values()).sort((a, b) => a.spec.name.localeCompare(b.spec.name));
  }, [doctors]);

  const filteredDoctors = useMemo(() => {
    if (!selectedSpec) return [];
    return doctors.filter((d) => d.active && d.specializations.some((s) => s.id === selectedSpec.id));
  }, [doctors, selectedSpec]);

  // Time slot filtering — hide past slots when "today" is picked
  const availableSlots = useMemo(() => {
    if (!date) return TIME_SLOTS;
    if (!isSameDay(date, new Date())) return TIME_SLOTS;
    const now = new Date();
    return TIME_SLOTS.filter((t) => {
      const [h, m] = t.split(":").map(Number);
      const slotDate = new Date(date);
      slotDate.setHours(h, m, 0, 0);
      return slotDate.getTime() > now.getTime();
    });
  }, [date]);

  // Unified slot list for rendering: server slots are already free;
  // legacy grid slots may be taken by an existing appointment.
  const slotOptions = useMemo<Array<{ time: string; taken: boolean }>>(() => {
    if (!date) return [];
    if (hasWorkingHours) {
      return serverSlots.map((s) => ({
        time: format(new Date(s.startTime), "HH:mm"),
        taken: false,
      }));
    }
    return availableSlots.map((t) => {
      const [h, m] = t.split(":").map(Number);
      const slotStart = new Date(date);
      slotStart.setHours(h, m, 0, 0);
      return { time: t, taken: isSlotBooked(slotStart.getTime(), SLOT_DURATION_MINUTES, bookedSlots) };
    });
  }, [date, hasWorkingHours, serverSlots, availableSlots, bookedSlots]);

  const canSubmit = !!selectedDoctor && !!date && !!slot && !book.isPending;

  const handleConfirm = () => {
    if (!canSubmit) return;
    book.mutate({
      doctorId: selectedDoctor!.id,
      scheduledAt: toLocalIso(date!, slot),
      durationMinutes: SLOT_DURATION_MINUTES,
      mode,
      reason: reason.trim() || undefined,
    });
  };

  return (
    <RolePageShell title="Book Appointment" subtitle="Find a doctor and schedule your visit">
      {/* Progress steps */}
      <div className="flex items-center gap-2 mb-2">
        {[
          { num: 1, label: "Specialization" },
          { num: 2, label: "Select Doctor" },
          { num: 3, label: "Schedule" },
        ].map((s, i) => (
          <div key={s.num} className="flex items-center gap-2">
            <button
              onClick={() => { if (s.num < step) setStep(s.num as 1 | 2 | 3); }}
              className={cn(
                "flex items-center gap-2 rounded-full px-4 py-2 text-sm font-medium transition-colors",
                step === s.num ? "bg-primary text-primary-foreground"
                  : step > s.num ? "bg-success/10 text-success cursor-pointer"
                  : "bg-muted text-muted-foreground"
              )}
            >
              {step > s.num ? <CheckCircle2 className="h-4 w-4" /> : <span>{s.num}</span>}
              <span className="hidden sm:inline">{s.label}</span>
            </button>
            {i < 2 && <div className={cn("h-px w-8", step > s.num ? "bg-success" : "bg-border")} />}
          </div>
        ))}
      </div>

      <AnimatePresence mode="wait">
        {/* STEP 1: Specialization */}
        {step === 1 && (
          <motion.div key="step1" initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: 20 }} className="space-y-4">
            {/* AI symptom triage */}
            <div className="rounded-xl border border-primary/20 bg-primary/5 p-4 space-y-3">
              <div className="flex items-center gap-2">
                <Sparkles className="h-4 w-4 text-primary" />
                <h4 className="text-sm font-semibold text-foreground">Not sure which department? Describe your symptoms</h4>
              </div>
              <Textarea
                placeholder="e.g. I've had a sharp pain in my lower back for two weeks, worse when I bend over..."
                value={symptoms}
                onChange={(e) => setSymptoms(e.target.value)}
                rows={2}
              />
              <div className="flex items-center justify-between gap-3">
                <p className="text-[11px] text-muted-foreground">
                  This is a routing aid, not medical advice. In an emergency, call your local emergency number.
                </p>
                <Button
                  size="sm"
                  variant="outline"
                  className="gap-1.5 shrink-0"
                  disabled={symptoms.trim().length < 10 || triage.isPending}
                  onClick={() => triage.mutate(symptoms.trim())}
                >
                  <Sparkles className="h-3.5 w-3.5" />
                  {triage.isPending ? "Analyzing..." : "Suggest department"}
                </Button>
              </div>

              {triage.data && (
                <motion.div initial={{ opacity: 0, y: 6 }} animate={{ opacity: 1, y: 0 }}
                  className="rounded-lg border border-border bg-card p-3 space-y-2">
                  {triage.data.urgency === "URGENT" && (
                    <p className="text-xs font-semibold text-destructive inline-flex items-center gap-1">
                      <AlertTriangle className="h-3.5 w-3.5" />
                      This may be urgent — consider seeking immediate care.
                    </p>
                  )}
                  <p className="text-sm text-foreground">{triage.data.reasoning}</p>
                  {triage.data.recommendedSpecialization && (
                    <div className="flex items-center justify-between gap-2">
                      <p className="text-xs text-muted-foreground">
                        Suggested: <span className="font-semibold text-foreground">{triage.data.recommendedSpecialization}</span>
                        {!triage.data.aiPowered && " (basic matching)"}
                      </p>
                      {(() => {
                        const match = specs.find(({ spec }) =>
                          spec.id === triage.data!.specializationId ||
                          spec.name === triage.data!.recommendedSpecialization);
                        return match ? (
                          <Button size="sm" className="gap-1.5"
                            onClick={() => { setSelectedSpec(match.spec); setSelectedDoctor(null); setStep(2); }}>
                            Continue with {match.spec.name} <ArrowRight className="h-3.5 w-3.5" />
                          </Button>
                        ) : (
                          <p className="text-xs text-muted-foreground italic">No doctors available in this department right now.</p>
                        );
                      })()}
                    </div>
                  )}
                </motion.div>
              )}
            </div>

            <h3 className="text-lg font-semibold text-foreground">Choose a Specialization</h3>
            {isLoading ? (
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                {Array.from({ length: 6 }).map((_, i) => (
                  <div key={i} className="h-20 rounded-xl border-2 border-border bg-card animate-pulse" />
                ))}
              </div>
            ) : specs.length === 0 ? (
              <EmptyState
                illustration={DoctorIllustration}
                title="No doctors are available"
                description="Nobody is accepting bookings right now. Please check back a little later."
              />
            ) : (
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                {specs.map(({ spec, count }) => (
                  <button
                    key={spec.id}
                    onClick={() => { setSelectedSpec(spec); setSelectedDoctor(null); setStep(2); }}
                    className={cn(
                      "p-4 rounded-xl border-2 text-left transition-all hover:shadow-md",
                      selectedSpec?.id === spec.id ? "border-primary bg-primary/5" : "border-border bg-card hover:border-primary/50"
                    )}
                  >
                    <div className="flex items-center gap-2 mb-1">
                      <Stethoscope className="h-4 w-4 text-primary" />
                      <p className="text-sm font-semibold text-foreground">{spec.name}</p>
                    </div>
                    <p className="text-xs text-muted-foreground">
                      {count} doctor{count !== 1 ? "s" : ""}
                    </p>
                  </button>
                ))}
              </div>
            )}
          </motion.div>
        )}

        {/* STEP 2: Doctor */}
        {step === 2 && selectedSpec && (
          <motion.div key="step2" initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: 20 }} className="space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-foreground">Select a Doctor</h3>
                <p className="text-sm text-muted-foreground">{selectedSpec.name} · {filteredDoctors.length} available</p>
              </div>
              <Button variant="ghost" size="sm" onClick={() => setStep(1)} className="gap-1">
                <ArrowLeft className="h-4 w-4" /> Change
              </Button>
            </div>

            {filteredDoctors.length === 0 ? (
              <EmptyState
                illustration={StethoscopeIllustration}
                title={`No ${selectedSpec.name} doctors available`}
                description="Nobody in this department is taking bookings yet. Try another specialization."
                action={
                  <Button variant="outline" onClick={() => setStep(1)} className="gap-1.5">
                    <ArrowLeft className="h-4 w-4" /> Choose another department
                  </Button>
                }
              />
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {filteredDoctors.map((doc) => {
                  const fullName = `Dr. ${doc.firstName} ${doc.lastName}`;
                  const initials = `${doc.firstName[0] ?? ""}${doc.lastName[0] ?? ""}`;
                  const selected = selectedDoctor?.id === doc.id;
                  return (
                    <motion.div
                      key={doc.id}
                      initial={{ opacity: 0, y: 8 }}
                      animate={{ opacity: 1, y: 0 }}
                      className={cn(
                        "rounded-xl border-2 p-4 cursor-pointer transition-all hover:shadow-md",
                        selected ? "border-primary bg-primary/5" : "border-border bg-card hover:border-primary/50"
                      )}
                      onClick={() => setSelectedDoctor(doc)}
                    >
                      <div className="flex gap-4">
                        <div className="h-16 w-16 rounded-xl bg-primary/10 flex items-center justify-center text-lg font-bold text-primary shrink-0">
                          {initials}
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-semibold text-foreground">{fullName}</p>
                          <div className="flex flex-wrap gap-1 mt-1">
                            {doc.specializations.map((s) => (
                              <Badge key={s.id} variant="outline" className="text-[10px] px-1.5 py-0 h-4 bg-info/10 text-info border-info/20">
                                {s.name}
                              </Badge>
                            ))}
                          </div>
                          <div className="mt-2"><StarRating rating={doc.rating} /></div>
                        </div>
                      </div>

                      {doc.biography && (
                        <p className="text-xs text-muted-foreground mt-3 line-clamp-2">{doc.biography}</p>
                      )}

                      <div className="flex items-center justify-between mt-3 text-xs">
                        <span className="flex items-center gap-1 text-muted-foreground">
                          <DollarSign className="h-3 w-3" />
                          <span className="font-medium text-foreground">${Number(doc.consultationFee).toFixed(2)}</span>
                          <span>/ visit</span>
                        </span>
                        {doc.roomNumber && (
                          <span className="flex items-center gap-1 text-muted-foreground">
                            <MapPin className="h-3 w-3" /> Room {doc.roomNumber}
                          </span>
                        )}
                      </div>
                    </motion.div>
                  );
                })}
              </div>
            )}

            {selectedDoctor && doctorReviews.length > 0 && (
              <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }}
                className="rounded-xl border border-border bg-card p-4 space-y-3">
                <h4 className="text-sm font-semibold text-foreground">
                  What patients say about Dr. {selectedDoctor.lastName}
                </h4>
                {doctorReviews.slice(0, 3).map((r, i) => (
                  <div key={i} className="border-l-2 border-primary/30 pl-3">
                    <div className="flex items-center gap-2">
                      <StarRating rating={r.rating} />
                      <span className="text-xs text-muted-foreground">
                        {r.patientName} · {r.visitDate}
                      </span>
                    </div>
                    {r.review && (
                      <p className="text-xs text-muted-foreground mt-1 italic line-clamp-2">
                        “{r.review}”
                      </p>
                    )}
                  </div>
                ))}
              </motion.div>
            )}

            {selectedDoctor && (
              <div className="flex justify-end">
                <Button onClick={() => setStep(3)} className="gap-2">
                  Continue with Dr. {selectedDoctor.lastName} <ArrowRight className="h-4 w-4" />
                </Button>
              </div>
            )}
          </motion.div>
        )}

        {/* STEP 3: Schedule */}
        {step === 3 && selectedDoctor && (
          <motion.div key="step3" initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: 20 }} className="space-y-6">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="h-12 w-12 rounded-lg bg-primary/10 flex items-center justify-center text-sm font-bold text-primary shrink-0">
                  {selectedDoctor.firstName[0]}{selectedDoctor.lastName[0]}
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-foreground">Dr. {selectedDoctor.firstName} {selectedDoctor.lastName}</h3>
                  <p className="text-sm text-muted-foreground">
                    {selectedDoctor.specializations.map((s) => s.name).join(" · ") || "—"}
                  </p>
                </div>
              </div>
              <Button variant="ghost" size="sm" onClick={() => setStep(2)} className="gap-1">
                <ArrowLeft className="h-4 w-4" /> Change Doctor
              </Button>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* Left: Date & time */}
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label className="text-sm font-medium">Date *</Label>
                  <Popover>
                    <PopoverTrigger asChild>
                      <Button variant="outline" className={cn("w-full justify-start text-left font-normal", !date && "text-muted-foreground")}>
                        <CalendarIcon className="mr-2 h-4 w-4" />
                        {date ? format(date, "PPP") : "Pick a date"}
                      </Button>
                    </PopoverTrigger>
                    <PopoverContent className="w-auto p-0" align="start">
                      <Calendar
                        mode="single"
                        selected={date}
                        onSelect={(d) => { setDate(d); setSlot(""); }}
                        disabled={(d) => {
                          const today = new Date();
                          today.setHours(0, 0, 0, 0);
                          return d < today;
                        }}
                        initialFocus
                        className={cn("p-3 pointer-events-auto")}
                      />
                    </PopoverContent>
                  </Popover>
                </div>

                <div className="space-y-2">
                  <Label className="text-sm font-medium">Time *</Label>
                  {!date ? (
                    <p className="text-xs text-muted-foreground p-3 rounded-lg border border-dashed border-border">
                      Select a date first.
                    </p>
                  ) : slotOptions.length === 0 ? (
                    <p className="text-xs text-muted-foreground p-3 rounded-lg border border-dashed border-border">
                      {hasWorkingHours
                        ? "The doctor has no available slots on this date. Try another day."
                        : "No slots remain today. Try another date."}
                    </p>
                  ) : (
                    <div className="grid grid-cols-3 gap-2">
                      {slotOptions.map(({ time: t, taken }) => {
                        const selected = slot === t;
                        return (
                          <button
                            key={t}
                            type="button"
                            disabled={taken}
                            onClick={() => setSlot(t)}
                            title={taken ? "Already booked" : undefined}
                            className={cn(
                              "rounded-lg border px-3 py-2 text-xs font-medium transition-colors",
                              taken    ? "border-border bg-muted/40 text-muted-foreground line-through cursor-not-allowed"
                              : selected ? "border-primary bg-primary text-primary-foreground"
                                         : "border-border bg-card text-foreground hover:border-primary/50"
                            )}
                          >
                            {formatSlot(t)}
                          </button>
                        );
                      })}
                    </div>
                  )}
                </div>
              </div>

              {/* Right: Mode, reason */}
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label className="text-sm font-medium">Consultation Mode</Label>
                  <div className="flex gap-2">
                    <Button
                      type="button"
                      variant={mode === "IN_PERSON" ? "default" : "outline"}
                      className="flex-1 gap-2"
                      onClick={() => setMode("IN_PERSON")}
                    >
                      <MapPin className="h-4 w-4" /> In-Person
                    </Button>
                    <Button
                      type="button"
                      variant={mode === "VIDEO" ? "default" : "outline"}
                      className="flex-1 gap-2"
                      onClick={() => setMode("VIDEO")}
                    >
                      <Video className="h-4 w-4" /> Video Call
                    </Button>
                  </div>
                </div>

                <div className="space-y-2">
                  <Label className="text-sm font-medium">Reason (optional)</Label>
                  <Textarea
                    placeholder="Describe your symptoms or reason for visit..."
                    value={reason}
                    onChange={(e) => setReason(e.target.value)}
                    rows={4}
                  />
                </div>
              </div>
            </div>

            {/* Summary */}
            {date && slot && (
              <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} className="rounded-xl border border-primary/20 bg-primary/5 p-4">
                <h4 className="text-sm font-semibold text-foreground mb-2">Appointment Summary</h4>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-sm">
                  <div><p className="text-xs text-muted-foreground">Doctor</p><p className="font-medium text-foreground">Dr. {selectedDoctor.lastName}</p></div>
                  <div><p className="text-xs text-muted-foreground">Date</p><p className="font-medium text-foreground">{format(date, "PPP")}</p></div>
                  <div><p className="text-xs text-muted-foreground">Time</p><p className="font-medium text-foreground">{formatSlot(slot)} · {SLOT_DURATION_MINUTES} min</p></div>
                  <div><p className="text-xs text-muted-foreground">Mode</p><p className="font-medium text-foreground">{mode === "IN_PERSON" ? "In-Person" : "Video"}</p></div>
                </div>
              </motion.div>
            )}

            <div className="flex gap-3 justify-end">
              <Button variant="outline" onClick={() => navigate(-1)} disabled={book.isPending}>Cancel</Button>
              <Button onClick={handleConfirm} disabled={!canSubmit} className="gap-2">
                <CheckCircle2 className="h-4 w-4" />
                {book.isPending ? "Booking..." : "Confirm Booking"}
              </Button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </RolePageShell>
  );
}