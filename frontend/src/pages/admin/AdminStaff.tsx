import { useState } from "react";
import { RolePageShell } from "@/components/RolePageShell";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { motion } from "framer-motion";
import { Search, Plus, Mail, Phone, MapPin, Briefcase, UserCog } from "lucide-react";
import { useToast } from "@/hooks/use-toast";

interface StaffMember {
  id: number;
  name: string;
  role: string;
  department: string;
  email: string;
  phone: string;
  status: string;
  joined: string;
  shift: string;
}

const initialStaff: StaffMember[] = [
  { id: 1, name: "Maria Rodriguez", role: "Head Nurse", department: "Emergency", email: "m.rodriguez@medicare.com", phone: "(555) 234-5678", status: "On Duty", joined: "Jan 2020", shift: "Day" },
  { id: 2, name: "Tom Harris", role: "Lab Technician", department: "Pathology", email: "t.harris@medicare.com", phone: "(555) 345-6789", status: "On Duty", joined: "Mar 2021", shift: "Day" },
  { id: 3, name: "Sarah Kim", role: "Pharmacist", department: "Pharmacy", email: "s.kim@medicare.com", phone: "(555) 456-7890", status: "Off Duty", joined: "Jul 2019", shift: "Night" },
  { id: 4, name: "James Cooper", role: "Radiologist Tech", department: "Radiology", email: "j.cooper@medicare.com", phone: "(555) 567-8901", status: "On Duty", joined: "Sep 2022", shift: "Day" },
  { id: 5, name: "Priya Patel", role: "Nurse", department: "Pediatrics", email: "p.patel@medicare.com", phone: "(555) 678-9012", status: "On Leave", joined: "Nov 2021", shift: "Day" },
  { id: 6, name: "David Chen", role: "Admin Assistant", department: "Administration", email: "d.chen@medicare.com", phone: "(555) 789-0123", status: "On Duty", joined: "Feb 2023", shift: "Day" },
  { id: 7, name: "Lisa Thompson", role: "Receptionist", department: "Front Desk", email: "l.thompson@medicare.com", phone: "(555) 890-1234", status: "On Duty", joined: "May 2020", shift: "Day" },
  { id: 8, name: "Robert Wilson", role: "Maintenance Lead", department: "Facilities", email: "r.wilson@medicare.com", phone: "(555) 901-2345", status: "On Duty", joined: "Aug 2018", shift: "Day" },
];

const statusStyles: Record<string, string> = {
  "On Duty": "bg-success/10 text-success border-success/20",
  "Off Duty": "bg-muted text-muted-foreground border-border",
  "On Leave": "bg-warning/10 text-warning border-warning/20",
};

const ROLES = ["Nurse", "Head Nurse", "Lab Technician", "Pharmacist", "Radiologist Tech", "Admin Assistant", "Receptionist", "Maintenance Lead"];
const DEPARTMENTS = ["Emergency", "Pathology", "Pharmacy", "Radiology", "Pediatrics", "Administration", "Front Desk", "Facilities", "Cardiology", "Neurology"];
const SHIFTS = ["Day", "Night", "Rotating"];

const emptyForm = { name: "", role: "", department: "", email: "", phone: "", shift: "" };

export default function AdminStaff() {
  const [search, setSearch] = useState("");
  const [selected, setSelected] = useState<StaffMember | null>(null);
  const [staffList, setStaffList] = useState<StaffMember[]>(initialStaff);
  const [addOpen, setAddOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const { toast } = useToast();

  const filtered = staffList.filter((s) =>
      s.name.toLowerCase().includes(search.toLowerCase()) ||
      s.role.toLowerCase().includes(search.toLowerCase()) ||
      s.department.toLowerCase().includes(search.toLowerCase())
  );

  const handleAdd = () => {
    if (!form.name || !form.role || !form.department || !form.email) {
      toast({ title: "Missing fields", description: "Please fill in all required fields.", variant: "destructive" });
      return;
    }
    const newMember: StaffMember = {
      id: Date.now(),
      name: form.name,
      role: form.role,
      department: form.department,
      email: form.email,
      phone: form.phone || "—",
      status: "On Duty",
      joined: new Date().toLocaleDateString("en-US", { month: "short", year: "numeric" }),
      shift: form.shift || "Day",
    };
    setStaffList((prev) => [newMember, ...prev]);
    setForm(emptyForm);
    setAddOpen(false);
    toast({ title: "Staff added", description: `${newMember.name} has been added successfully.` });
  };

  const updateField = (field: string, value: string) => setForm((prev) => ({ ...prev, [field]: value }));

  return (
      <RolePageShell title="Staff Management" subtitle="Manage non-physician hospital staff">
        <div className="flex items-center gap-3">
          <div className="relative flex-1 max-w-sm">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input placeholder="Search staff..." value={search} onChange={(e) => setSearch(e.target.value)} className="pl-9" />
          </div>
          <Button size="sm" onClick={() => setAddOpen(true)}><Plus className="h-4 w-4 mr-2" />Add Staff</Button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          {filtered.map((member, i) => (
              <motion.div
                  key={member.id}
                  initial={{ opacity: 0, y: 12 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: i * 0.03 }}
                  onClick={() => setSelected(member)}
                  className="rounded-xl border border-border bg-card p-5 space-y-3 hover:shadow-md transition-shadow cursor-pointer"
              >
                <div className="flex items-start justify-between">
                  <div className="flex items-center gap-3">
                    <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center">
                      <UserCog className="h-5 w-5 text-primary" />
                    </div>
                    <div>
                      <p className="text-sm font-semibold text-foreground">{member.name}</p>
                      <p className="text-xs text-muted-foreground">{member.role}</p>
                    </div>
                  </div>
                  <Badge variant="outline" className={statusStyles[member.status]}>{member.status}</Badge>
                </div>
                <div className="flex items-center gap-4 text-xs text-muted-foreground">
                  <span className="flex items-center gap-1"><Briefcase className="h-3 w-3" />{member.department}</span>
                  <span className="flex items-center gap-1"><Mail className="h-3 w-3" />{member.email.split("@")[0]}</span>
                </div>
              </motion.div>
          ))}
        </div>

        {/* View Staff Detail */}
        <Dialog open={!!selected} onOpenChange={() => setSelected(null)}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>{selected?.name}</DialogTitle>
              <DialogDescription>{selected?.role} — {selected?.department}</DialogDescription>
            </DialogHeader>
            {selected && (
                <div className="space-y-3 pt-2">
                  <InfoRow icon={Mail} label="Email" value={selected.email} />
                  <InfoRow icon={Phone} label="Phone" value={selected.phone} />
                  <InfoRow icon={Briefcase} label="Department" value={selected.department} />
                  <InfoRow icon={MapPin} label="Shift" value={selected.shift} />
                  <InfoRow icon={UserCog} label="Joined" value={selected.joined} />
                  <div className="flex gap-2 pt-4">
                    <Button variant="outline" className="flex-1">Edit Profile</Button>
                    <Button className="flex-1">Contact</Button>
                  </div>
                </div>
            )}
          </DialogContent>
        </Dialog>

        {/* Add Staff Dialog */}
        <Dialog open={addOpen} onOpenChange={setAddOpen}>
          <DialogContent className="sm:max-w-lg">
            <DialogHeader>
              <DialogTitle>Add New Staff Member</DialogTitle>
              <DialogDescription>Fill in the details to register a new staff member.</DialogDescription>
            </DialogHeader>
            <div className="grid gap-4 pt-2">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Full Name *</Label>
                  <Input placeholder="e.g. Jane Doe" value={form.name} onChange={(e) => updateField("name", e.target.value)} />
                </div>
                <div className="space-y-2">
                  <Label>Email *</Label>
                  <Input placeholder="e.g. j.doe@medicare.com" value={form.email} onChange={(e) => updateField("email", e.target.value)} />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Role *</Label>
                  <Select value={form.role} onValueChange={(v) => updateField("role", v)}>
                    <SelectTrigger><SelectValue placeholder="Select role" /></SelectTrigger>
                    <SelectContent>
                      {ROLES.map((r) => <SelectItem key={r} value={r}>{r}</SelectItem>)}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label>Department *</Label>
                  <Select value={form.department} onValueChange={(v) => updateField("department", v)}>
                    <SelectTrigger><SelectValue placeholder="Select department" /></SelectTrigger>
                    <SelectContent>
                      {DEPARTMENTS.map((d) => <SelectItem key={d} value={d}>{d}</SelectItem>)}
                    </SelectContent>
                  </Select>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Phone</Label>
                  <Input placeholder="(555) 000-0000" value={form.phone} onChange={(e) => updateField("phone", e.target.value)} />
                </div>
                <div className="space-y-2">
                  <Label>Shift</Label>
                  <Select value={form.shift} onValueChange={(v) => updateField("shift", v)}>
                    <SelectTrigger><SelectValue placeholder="Select shift" /></SelectTrigger>
                    <SelectContent>
                      {SHIFTS.map((s) => <SelectItem key={s} value={s}>{s}</SelectItem>)}
                    </SelectContent>
                  </Select>
                </div>
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <Button variant="outline" onClick={() => { setForm(emptyForm); setAddOpen(false); }}>Cancel</Button>
                <Button onClick={handleAdd}>Add Staff Member</Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      </RolePageShell>
  );
}

function InfoRow({ icon: Icon, label, value }: { icon: typeof Mail; label: string; value: string }) {
  return (
      <div className="flex items-center gap-3 text-sm">
        <Icon className="h-4 w-4 text-muted-foreground shrink-0" />
        <span className="text-muted-foreground w-24">{label}</span>
        <span className="text-foreground font-medium">{value}</span>
      </div>
  );
}

