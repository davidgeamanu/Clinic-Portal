import { useState, useEffect } from "react";
import { RolePageShell } from "@/components/RolePageShell";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import {
  Mail, Phone, Lock, User,
  CheckCircle2, AlertCircle, Save, KeyRound,
} from "lucide-react";
import { usePatientProfile, useUpdatePatientProfile } from "@/hooks/usePatient";
import { useChangePassword } from "@/hooks/useAuth";
import { cn } from "@/lib/utils";

export default function PatientSettings() {
  const { data: profile, isLoading } = usePatientProfile();
  const updateProfile = useUpdatePatientProfile();
  const changePassword = useChangePassword();

  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  useEffect(() => {
    if (profile) {
      setFirstName(profile.firstName ?? "");
      setLastName(profile.lastName ?? "");
      setPhoneNumber(profile.phoneNumber ?? "");
    }
  }, [profile]);

  const hasChanges =
    profile &&
    (firstName !== (profile.firstName ?? "") ||
      lastName !== (profile.lastName ?? "") ||
      phoneNumber !== (profile.phoneNumber ?? ""));

  const handleSave = () => {
    updateProfile.mutate({
      firstName: firstName || undefined,
      lastName: lastName || undefined,
      phoneNumber: phoneNumber || undefined,
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
    <RolePageShell title="Profile" subtitle="Manage your personal information and account security">
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
                <h2 className="text-2xl font-bold text-foreground truncate">
                  {profile.firstName} {profile.lastName}
                </h2>
                <div className="flex flex-wrap items-center gap-x-4 gap-y-1 mt-1 text-sm text-muted-foreground">
                  <span className="inline-flex items-center gap-1.5">
                    <Mail className="h-3.5 w-3.5" /> {profile.email}
                  </span>
                  {profile.phoneNumber && (
                    <span className="inline-flex items-center gap-1.5">
                      <Phone className="h-3.5 w-3.5" /> {profile.phoneNumber}
                    </span>
                  )}
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Personal Information */}
        <Card className="border-border/60">
          <CardHeader>
            <div className="flex items-start gap-3">
              <div className="h-10 w-10 rounded-lg bg-primary/10 text-primary flex items-center justify-center shrink-0">
                <User className="h-5 w-5" />
              </div>
              <div className="flex-1">
                <CardTitle className="text-lg">Personal Information</CardTitle>
                <CardDescription>Update your name and contact details</CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent className="space-y-5">
            <div className="grid sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="firstName" className="text-sm font-medium">First Name</Label>
                <Input
                  id="firstName"
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                  placeholder="First name"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="lastName" className="text-sm font-medium">Last Name</Label>
                <Input
                  id="lastName"
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                  placeholder="Last name"
                />
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="phoneNumber" className="text-sm font-medium">Phone Number</Label>
              <div className="relative">
                <Phone className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  id="phoneNumber"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  placeholder="e.g. 0712345678"
                  className="pl-9"
                />
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
