import { useState, useEffect } from "react";
import { RolePageShell } from "@/components/RolePageShell";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Switch } from "@/components/ui/switch";
import {
  Stethoscope, DollarSign, FileText, Mail, Hash, MapPin, Lock,
  ShieldCheck, CheckCircle2, AlertCircle, Save, KeyRound, Star, Clock,
} from "lucide-react";
import {
  useDoctorProfile, useUpdateDoctorProfile,
  useMyAvailability, useUpdateMyAvailability, useMyReviews,
} from "@/hooks/useDoctor";
import { useChangePassword } from "@/hooks/useAuth";
import { cn } from "@/lib/utils";
import type { AvailabilityWindow, DayOfWeek } from "@/types/api";

const WEEK_DAYS: Array<{ day: DayOfWeek; label: string }> = [
  { day: "MONDAY",    label: "Monday" },
  { day: "TUESDAY",   label: "Tuesday" },
  { day: "WEDNESDAY", label: "Wednesday" },
  { day: "THURSDAY",  label: "Thursday" },
  { day: "FRIDAY",    label: "Friday" },
  { day: "SATURDAY",  label: "Saturday" },
  { day: "SUNDAY",    label: "Sunday" },
];

type DaySchedule = { enabled: boolean; start: string; end: string };

const DEFAULT_DAY: DaySchedule = { enabled: false, start: "09:00", end: "17:00" };

function ReviewStars({ rating }: { rating: number }) {
  return (
    <div className="flex items-center gap-0.5">
      {[1, 2, 3, 4, 5].map((s) => (
        <Star key={s} className={cn(
          "h-3.5 w-3.5",
          s <= rating ? "fill-yellow-400 text-yellow-400" : "text-muted-foreground/30"
        )} />
      ))}
    </div>
  );
}

function PatientFeedbackCard() {
  const { data: reviews = [], isLoading } = useMyReviews();

  return (
    <Card className="border-border/60">
      <CardHeader>
        <div className="flex items-start gap-3">
          <div className="h-10 w-10 rounded-lg bg-primary/10 text-primary flex items-center justify-center shrink-0">
            <Star className="h-5 w-5" />
          </div>
          <div className="flex-1">
            <CardTitle className="text-lg">Patient Feedback</CardTitle>
            <CardDescription>Ratings and reviews from your completed appointments</CardDescription>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <div className="h-24 rounded-xl bg-muted/50 animate-pulse" />
        ) : reviews.length === 0 ? (
          <p className="text-sm text-muted-foreground italic">No reviews yet.</p>
        ) : (
          <div className="space-y-4">
            {reviews.slice(0, 5).map((r, i) => (
              <div key={i} className="border-l-2 border-primary/30 pl-3">
                <div className="flex items-center gap-2">
                  <ReviewStars rating={r.rating} />
                  <span className="text-xs text-muted-foreground">
                    {r.patientName} · {r.visitDate}
                  </span>
                </div>
                {r.review && (
                  <p className="text-sm text-muted-foreground mt-1 italic">“{r.review}”</p>
                )}
              </div>
            ))}
            {reviews.length > 5 && (
              <p className="text-xs text-muted-foreground">
                Showing the 5 most recent of {reviews.length} reviews.
              </p>
            )}
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function WorkingHoursCard() {
  const { data: windows, isLoading } = useMyAvailability();
  const updateAvailability = useUpdateMyAvailability();

  const [schedule, setSchedule] = useState<Record<DayOfWeek, DaySchedule>>(() =>
    Object.fromEntries(WEEK_DAYS.map(({ day }) => [day, { ...DEFAULT_DAY }])) as Record<DayOfWeek, DaySchedule>
  );

  // The editor manages one window per day; if the API holds several, the first is shown
  useEffect(() => {
    if (!windows) return;
    setSchedule((prev) => {
      const next = { ...prev };
      WEEK_DAYS.forEach(({ day }) => {
        const w = windows.find((x) => x.dayOfWeek === day);
        next[day] = w
          ? { enabled: true, start: w.startTime.slice(0, 5), end: w.endTime.slice(0, 5) }
          : { ...DEFAULT_DAY };
      });
      return next;
    });
  }, [windows]);

  const setDay = (day: DayOfWeek, patch: Partial<DaySchedule>) =>
    setSchedule((prev) => ({ ...prev, [day]: { ...prev[day], ...patch } }));

  const invalidDays = WEEK_DAYS.filter(
    ({ day }) => schedule[day].enabled && schedule[day].start >= schedule[day].end
  );

  const handleSave = () => {
    const payload: AvailabilityWindow[] = WEEK_DAYS
      .filter(({ day }) => schedule[day].enabled)
      .map(({ day }) => ({
        dayOfWeek: day,
        startTime: `${schedule[day].start}:00`,
        endTime: `${schedule[day].end}:00`,
      }));
    updateAvailability.mutate(payload);
  };

  return (
    <Card className="border-border/60">
      <CardHeader>
        <div className="flex items-start gap-3">
          <div className="h-10 w-10 rounded-lg bg-primary/10 text-primary flex items-center justify-center shrink-0">
            <Clock className="h-5 w-5" />
          </div>
          <div className="flex-1">
            <CardTitle className="text-lg">Working Hours</CardTitle>
            <CardDescription>
              Patients can only book inside these windows. Leave every day off to accept any time.
            </CardDescription>
          </div>
        </div>
      </CardHeader>
      <CardContent className="space-y-3">
        {isLoading ? (
          <div className="h-48 rounded-xl bg-muted/50 animate-pulse" />
        ) : (
          <>
            {WEEK_DAYS.map(({ day, label }) => {
              const d = schedule[day];
              const invalid = d.enabled && d.start >= d.end;
              return (
                <div key={day} className="flex items-center gap-4 py-1">
                  <Switch
                    checked={d.enabled}
                    onCheckedChange={(checked) => setDay(day, { enabled: checked })}
                    aria-label={`Toggle ${label}`}
                  />
                  <span className={cn("w-24 text-sm font-medium", !d.enabled && "text-muted-foreground")}>
                    {label}
                  </span>
                  {d.enabled ? (
                    <div className="flex items-center gap-2">
                      <Input
                        type="time"
                        value={d.start}
                        onChange={(e) => setDay(day, { start: e.target.value })}
                        className={cn("w-32", invalid && "border-destructive")}
                      />
                      <span className="text-muted-foreground text-sm">to</span>
                      <Input
                        type="time"
                        value={d.end}
                        onChange={(e) => setDay(day, { end: e.target.value })}
                        className={cn("w-32", invalid && "border-destructive")}
                      />
                    </div>
                  ) : (
                    <span className="text-sm text-muted-foreground italic">Not working</span>
                  )}
                </div>
              );
            })}

            {invalidDays.length > 0 && (
              <p className="text-xs text-destructive inline-flex items-center gap-1">
                <AlertCircle className="h-3 w-3" /> Start time must be before end time
              </p>
            )}

            <Separator />
            <div className="flex justify-end">
              <Button
                onClick={handleSave}
                disabled={invalidDays.length > 0 || updateAvailability.isPending}
                className="gap-2"
              >
                <Save className="h-4 w-4" />
                {updateAvailability.isPending ? "Saving..." : "Save Working Hours"}
              </Button>
            </div>
          </>
        )}
      </CardContent>
    </Card>
  );
}

export default function DoctorSettings() {
  const { data: profile, isLoading } = useDoctorProfile();
  const updateProfile = useUpdateDoctorProfile();
  const changePassword = useChangePassword();

  const [biography, setBiography] = useState("");
  const [consultationFee, setConsultationFee] = useState("");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  useEffect(() => {
    if (profile) {
      setBiography(profile.biography ?? "");
      setConsultationFee(profile.consultationFee?.toString() ?? "");
    }
  }, [profile]);

  const hasChanges =
      profile &&
      (biography !== (profile.biography ?? "") ||
          consultationFee !== (profile.consultationFee?.toString() ?? ""));

  const handleSave = () => {
    updateProfile.mutate({
      biography: biography || undefined,
      consultationFee: consultationFee ? parseFloat(consultationFee) : undefined,
    });
  };

  const handleChangePassword = () => {
    changePassword.mutate(
        { currentPassword, newPassword },
        { onSuccess: () => { setCurrentPassword(""); setNewPassword(""); setConfirmPassword(""); } }
    );
  };

  const passwordValid = currentPassword && newPassword.length >= 8 && newPassword === confirmPassword;

  if (isLoading) {
    return (
        <RolePageShell title="Profile">
          <div className="max-w-3xl mx-auto space-y-6">
            <div className="h-40 rounded-2xl bg-muted/50 animate-pulse" />
            <div className="h-64 rounded-2xl bg-muted/50 animate-pulse" />
            <div className="h-64 rounded-2xl bg-muted/50 animate-pulse" />
          </div>
        </RolePageShell>
    );
  }

  if (!profile) return null;

  const initials = `${profile.firstName?.[0] ?? ""}${profile.lastName?.[0] ?? ""}`.toUpperCase();

  return (
      <RolePageShell title="Profile" subtitle="Manage your professional information and account security">
        <div className="max-w-3xl mx-auto space-y-6">
          {/* Profile header banner */}
          <Card className="overflow-hidden border-border/60">
            <div className="h-24 bg-gradient-to-r from-primary/20 via-primary/10 to-accent" />
            <CardContent className="pt-0 pb-6">
              <div className="flex flex-col sm:flex-row sm:items-end gap-4 -mt-12">
                <Avatar className="h-24 w-24 ring-4 ring-background shadow-lg">
                  <AvatarFallback className="bg-primary text-primary-foreground text-2xl font-semibold">
                    {initials}
                  </AvatarFallback>
                </Avatar>
                <div className="flex-1 min-w-0 sm:pb-2">
                  <div className="flex items-center gap-2 flex-wrap">
                    <h2 className="text-2xl font-bold text-foreground truncate">
                      Dr. {profile.firstName} {profile.lastName}
                    </h2>
                    <Badge variant="secondary" className="gap-1">
                      <ShieldCheck className="h-3 w-3" /> Verified
                    </Badge>
                    {profile.rating != null && (
                      <Badge variant="outline" className="gap-1 bg-background">
                        <Star className="h-3 w-3 fill-yellow-400 text-yellow-400" />
                        {profile.rating.toFixed(1)}
                      </Badge>
                    )}
                  </div>
                  <div className="flex flex-wrap items-center gap-x-4 gap-y-1 mt-1 text-sm text-muted-foreground">
                  <span className="inline-flex items-center gap-1.5">
                    <Mail className="h-3.5 w-3.5" /> {profile.email}
                  </span>
                    <span className="inline-flex items-center gap-1.5">
                    <Hash className="h-3.5 w-3.5" /> {profile.licenseNumber}
                  </span>
                    {profile.roomNumber && (
                        <span className="inline-flex items-center gap-1.5">
                      <MapPin className="h-3.5 w-3.5" /> Room {profile.roomNumber}
                    </span>
                    )}
                  </div>
                </div>
              </div>

              {/* Specializations */}
              <div className="mt-5 pt-5 border-t border-border/60">
                <Label className="text-xs text-muted-foreground inline-flex items-center gap-1.5">
                  <Stethoscope className="h-3.5 w-3.5" /> Specializations
                </Label>
                <div className="flex flex-wrap gap-1.5 mt-2">
                  {profile.specializations.length > 0 ? (
                      profile.specializations.map((s) => (
                          <Badge key={s.id} variant="secondary" className="font-medium">{s.name}</Badge>
                      ))
                  ) : (
                      <p className="text-sm text-muted-foreground italic">None assigned</p>
                  )}
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Professional Details */}
          <Card className="border-border/60">
            <CardHeader>
              <div className="flex items-start gap-3">
                <div className="h-10 w-10 rounded-lg bg-primary/10 text-primary flex items-center justify-center shrink-0">
                  <FileText className="h-5 w-5" />
                </div>
                <div className="flex-1">
                  <CardTitle className="text-lg">Professional Details</CardTitle>
                  <CardDescription>Visible to patients when booking with you</CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent className="space-y-5">
              <div className="grid sm:grid-cols-[1fr_auto] gap-4 items-start">
                <div className="space-y-2">
                  <Label htmlFor="consultationFee" className="text-sm font-medium">
                    Consultation Fee
                  </Label>
                  <div className="relative">
                    <DollarSign className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                    <Input
                        id="consultationFee"
                        type="number"
                        step="0.01"
                        min="0"
                        placeholder="150.00"
                        value={consultationFee}
                        onChange={(e) => setConsultationFee(e.target.value)}
                        className="pl-9"
                    />
                  </div>
                  <p className="text-xs text-muted-foreground">Charged per standard appointment</p>
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="biography" className="text-sm font-medium">Biography</Label>
                <Textarea
                    id="biography"
                    placeholder="Share your background, experience, and areas of expertise..."
                    value={biography}
                    onChange={(e) => setBiography(e.target.value)}
                    rows={6}
                    maxLength={1000}
                    className="resize-none"
                />
                <div className="flex items-center justify-between">
                  <p className="text-xs text-muted-foreground">Tip: keep it warm and patient-friendly</p>
                  <p className={cn(
                      "text-xs tabular-nums",
                      biography.length > 900 ? "text-warning" : "text-muted-foreground"
                  )}>
                    {biography.length}/1000
                  </p>
                </div>
              </div>

              <Separator />
              <div className="flex items-center justify-between gap-3">
                <p className="text-xs text-muted-foreground">
                  {hasChanges ? (
                      <span className="inline-flex items-center gap-1.5 text-warning">
                    <AlertCircle className="h-3.5 w-3.5" /> You have unsaved changes
                  </span>
                  ) : (
                      <span className="inline-flex items-center gap-1.5">
                    <CheckCircle2 className="h-3.5 w-3.5 text-success" /> All changes saved
                  </span>
                  )}
                </p>
                <Button
                    onClick={handleSave}
                    disabled={!hasChanges || updateProfile.isPending}
                    className="gap-2"
                >
                  <Save className="h-4 w-4" />
                  {updateProfile.isPending ? "Saving..." : "Save Changes"}
                </Button>
              </div>
            </CardContent>
          </Card>

          {/* Working Hours */}
          <WorkingHoursCard />

          {/* Patient Feedback */}
          <PatientFeedbackCard />

          {/* Change Password */}
          <Card className="border-border/60">
            <CardHeader>
              <div className="flex items-start gap-3">
                <div className="h-10 w-10 rounded-lg bg-primary/10 text-primary flex items-center justify-center shrink-0">
                  <Lock className="h-5 w-5" />
                </div>
                <div className="flex-1">
                  <CardTitle className="text-lg">Security</CardTitle>
                  <CardDescription>Update your password to keep your account secure</CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="currentPassword" className="text-sm font-medium">Current Password</Label>
                <Input
                    id="currentPassword"
                    type="password"
                    value={currentPassword}
                    onChange={(e) => setCurrentPassword(e.target.value)}
                    placeholder="••••••••"
                />
              </div>

              <div className="grid sm:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="newPassword" className="text-sm font-medium">New Password</Label>
                  <Input
                      id="newPassword"
                      type="password"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      placeholder="At least 8 characters"
                  />
                  {newPassword && newPassword.length < 8 && (
                      <p className="text-xs text-destructive inline-flex items-center gap-1">
                        <AlertCircle className="h-3 w-3" /> Must be at least 8 characters
                      </p>
                  )}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="confirmPassword" className="text-sm font-medium">Confirm Password</Label>
                  <Input
                      id="confirmPassword"
                      type="password"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      placeholder="Repeat new password"
                  />
                  {confirmPassword && newPassword !== confirmPassword && (
                      <p className="text-xs text-destructive inline-flex items-center gap-1">
                        <AlertCircle className="h-3 w-3" /> Passwords do not match
                      </p>
                  )}
                  {confirmPassword && newPassword === confirmPassword && newPassword.length >= 8 && (
                      <p className="text-xs text-success inline-flex items-center gap-1">
                        <CheckCircle2 className="h-3 w-3" /> Passwords match
                      </p>
                  )}
                </div>
              </div>

              <Separator />
              <div className="flex justify-end">
                <Button
                    onClick={handleChangePassword}
                    disabled={!passwordValid || changePassword.isPending}
                    className="gap-2"
                >
                  <KeyRound className="h-4 w-4" />
                  {changePassword.isPending ? "Changing..." : "Change Password"}
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </RolePageShell>
  );
}
