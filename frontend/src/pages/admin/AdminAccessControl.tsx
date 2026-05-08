import { useState } from "react";
import { RolePageShell } from "@/components/RolePageShell";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Switch } from "@/components/ui/switch";
import { motion } from "framer-motion";
import { Shield, Users, Stethoscope, UserCog, Eye, Edit, Trash2, Settings, Lock, Key } from "lucide-react";

interface Permission {
  name: string;
  description: string;
  admin: boolean;
  doctor: boolean;
  nurse: boolean;
  staff: boolean;
}

const permissions: Permission[] = [
  { name: "View Patient Records", description: "Access patient medical records", admin: true, doctor: true, nurse: true, staff: false },
  { name: "Edit Patient Records", description: "Modify patient medical data", admin: true, doctor: true, nurse: false, staff: false },
  { name: "Manage Appointments", description: "Create, edit and cancel appointments", admin: true, doctor: true, nurse: true, staff: true },
  { name: "Prescribe Medications", description: "Issue and renew prescriptions", admin: false, doctor: true, nurse: false, staff: false },
  { name: "Manage Billing", description: "Access and manage billing records", admin: true, doctor: false, nurse: false, staff: true },
  { name: "View Analytics", description: "Access reports and dashboards", admin: true, doctor: false, nurse: false, staff: false },
  { name: "Manage Users", description: "Create, edit and deactivate users", admin: true, doctor: false, nurse: false, staff: false },
  { name: "System Configuration", description: "Modify system settings", admin: true, doctor: false, nurse: false, staff: false },
];

const roles = [
  { name: "Administrator", icon: Shield, count: 3, color: "text-primary" },
  { name: "Doctor", icon: Stethoscope, count: 42, color: "text-info" },
  { name: "Nurse", icon: Users, count: 68, color: "text-success" },
  { name: "Staff", icon: UserCog, count: 24, color: "text-warning" },
];

const activityLog = [
  { action: "Role updated", detail: "Dr. Chen granted 'Edit Patient Records' permission", time: "10 min ago", user: "Admin Sarah" },
  { action: "User deactivated", detail: "Staff member J. Cooper account suspended", time: "1 hour ago", user: "Admin Sarah" },
  { action: "New role created", detail: "'Lab Manager' role created with custom permissions", time: "3 hours ago", user: "Admin Sarah" },
  { action: "Password reset", detail: "Password reset initiated for nurse P. Patel", time: "5 hours ago", user: "System" },
  { action: "Access revoked", detail: "Billing access removed from temporary staff", time: "1 day ago", user: "Admin Sarah" },
];

export default function AdminAccessControl() {
  const [permState, setPermState] = useState(permissions);

  const togglePerm = (idx: number, role: "admin" | "doctor" | "nurse" | "staff") => {
    setPermState((prev) => prev.map((p, i) => i === idx ? { ...p, [role]: !p[role] } : p));
  };

  return (
    <RolePageShell title="Access Control" subtitle="Manage roles, permissions and security">
      {/* Roles overview */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {roles.map((role, i) => (
          <motion.div key={role.name} initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.05 }} className="rounded-xl border border-border bg-card p-5">
            <div className="flex items-center gap-3 mb-3">
              <div className="h-10 w-10 rounded-lg bg-muted flex items-center justify-center">
                <role.icon className={`h-5 w-5 ${role.color}`} />
              </div>
              <div>
                <p className="text-sm font-semibold text-foreground">{role.name}</p>
                <p className="text-xs text-muted-foreground">{role.count} users</p>
              </div>
            </div>
            <Button variant="outline" size="sm" className="w-full"><Settings className="h-3.5 w-3.5 mr-2" />Manage Role</Button>
          </motion.div>
        ))}
      </div>

      {/* Permissions matrix */}
      <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }} className="rounded-xl border border-border bg-card p-6">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-semibold text-foreground flex items-center gap-2"><Key className="h-5 w-5" />Permission Matrix</h3>
          <Button size="sm" variant="outline"><Lock className="h-4 w-4 mr-2" />Save Changes</Button>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border">
                <th className="text-left py-3 px-4 text-muted-foreground font-medium">Permission</th>
                <th className="text-center py-3 px-4 text-muted-foreground font-medium">Admin</th>
                <th className="text-center py-3 px-4 text-muted-foreground font-medium">Doctor</th>
                <th className="text-center py-3 px-4 text-muted-foreground font-medium">Nurse</th>
                <th className="text-center py-3 px-4 text-muted-foreground font-medium">Staff</th>
              </tr>
            </thead>
            <tbody>
              {permState.map((perm, idx) => (
                <tr key={perm.name} className="border-b border-border last:border-0 hover:bg-muted/50 transition-colors">
                  <td className="py-3 px-4">
                    <p className="font-medium text-foreground">{perm.name}</p>
                    <p className="text-xs text-muted-foreground">{perm.description}</p>
                  </td>
                  {(["admin", "doctor", "nurse", "staff"] as const).map((role) => (
                    <td key={role} className="text-center py-3 px-4">
                      <Switch checked={perm[role]} onCheckedChange={() => togglePerm(idx, role)} />
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </motion.div>

      {/* Activity log */}
      <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.3 }} className="rounded-xl border border-border bg-card p-6">
        <h3 className="text-lg font-semibold text-foreground mb-4">Security Activity Log</h3>
        <div className="space-y-3">
          {activityLog.map((entry, i) => (
            <div key={i} className="flex items-start gap-3 p-3 rounded-lg hover:bg-muted/50 transition-colors">
              <div className="mt-1 h-2 w-2 rounded-full bg-primary shrink-0" />
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-foreground">{entry.action}</p>
                <p className="text-xs text-muted-foreground">{entry.detail}</p>
              </div>
              <div className="text-right shrink-0">
                <p className="text-xs text-muted-foreground">{entry.time}</p>
                <p className="text-xs text-muted-foreground">{entry.user}</p>
              </div>
            </div>
          ))}
        </div>
      </motion.div>
    </RolePageShell>
  );
}
