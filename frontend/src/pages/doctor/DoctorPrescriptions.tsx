import { useState } from "react";
import { RolePageShell } from "@/components/RolePageShell";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Pill, Search, Plus, Calendar, User, RefreshCw, Clock } from "lucide-react";
import { motion } from "framer-motion";

interface Prescription {
  id: string;
  patient: string;
  medication: string;
  dosage: string;
  frequency: string;
  startDate: string;
  endDate: string;
  status: "Active" | "Expired" | "Refill Requested";
  notes: string;
  refills: number;
}

const prescriptions: Prescription[] = [
  { id: "RX001", patient: "John Smith", medication: "Lisinopril", dosage: "10mg", frequency: "Once daily", startDate: "Jan 5, 2024", endDate: "Jul 5, 2024", status: "Active", notes: "Monitor blood pressure weekly.", refills: 3 },
  { id: "RX002", patient: "Emily Davis", medication: "Metformin", dosage: "500mg", frequency: "Twice daily", startDate: "Dec 10, 2023", endDate: "Jun 10, 2024", status: "Active", notes: "Take with meals. Check HbA1c in 3 months.", refills: 2 },
  { id: "RX003", patient: "Michael Brown", medication: "Atorvastatin", dosage: "20mg", frequency: "Once daily at bedtime", startDate: "Nov 15, 2023", endDate: "May 15, 2024", status: "Refill Requested", notes: "Lipid panel in 6 weeks.", refills: 1 },
  { id: "RX004", patient: "Sarah Johnson", medication: "Amoxicillin", dosage: "500mg", frequency: "Three times daily", startDate: "Jan 18, 2024", endDate: "Jan 28, 2024", status: "Expired", notes: "10-day course for sinus infection.", refills: 0 },
  { id: "RX005", patient: "Robert Wilson", medication: "Omeprazole", dosage: "20mg", frequency: "Once daily before breakfast", startDate: "Jan 2, 2024", endDate: "Apr 2, 2024", status: "Active", notes: "Review need at follow-up.", refills: 2 },
  { id: "RX006", patient: "Lisa Anderson", medication: "Amlodipine", dosage: "5mg", frequency: "Once daily", startDate: "Oct 1, 2023", endDate: "Apr 1, 2024", status: "Refill Requested", notes: "BP well controlled. Consider dose reduction.", refills: 0 },
];

const statusStyles: Record<string, string> = {
  Active: "bg-success/10 text-success border-success/20",
  Expired: "bg-muted text-muted-foreground border-border",
  "Refill Requested": "bg-warning/10 text-warning border-warning/20",
};

export default function DoctorPrescriptions() {
  const [search, setSearch] = useState("");
  const [selected, setSelected] = useState<Prescription | null>(null);

  const filtered = prescriptions.filter(
    (p) =>
      p.patient.toLowerCase().includes(search.toLowerCase()) ||
      p.medication.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <RolePageShell title="Prescriptions" subtitle="Manage and review patient prescriptions">
      <div className="flex items-center gap-3 mb-2">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input placeholder="Search by patient or medication..." value={search} onChange={(e) => setSearch(e.target.value)} className="pl-9" />
        </div>
        <Button className="gap-2"><Plus className="h-4 w-4" />New Prescription</Button>
      </div>

      <div className="space-y-3">
        {filtered.map((rx, i) => (
          <motion.div
            key={rx.id}
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: i * 0.04 }}
            onClick={() => setSelected(rx)}
            className="rounded-xl border border-border bg-card p-5 hover:shadow-md transition-shadow cursor-pointer"
          >
            <div className="flex items-start justify-between">
              <div className="flex items-start gap-3">
                <div className="p-2 rounded-lg bg-primary/10">
                  <Pill className="h-5 w-5 text-primary" />
                </div>
                <div>
                  <h4 className="text-sm font-semibold text-foreground">{rx.medication} — {rx.dosage}</h4>
                  <p className="text-xs text-muted-foreground mt-0.5">{rx.frequency}</p>
                  <div className="flex items-center gap-3 mt-1.5 text-xs text-muted-foreground">
                    <span className="flex items-center gap-1"><User className="h-3 w-3" />{rx.patient}</span>
                    <span className="flex items-center gap-1"><Calendar className="h-3 w-3" />{rx.startDate} — {rx.endDate}</span>
                  </div>
                </div>
              </div>
              <div className="flex items-center gap-2">
                {rx.status === "Refill Requested" && (
                  <Button size="sm" variant="outline" className="gap-1 text-xs"><RefreshCw className="h-3 w-3" />Approve</Button>
                )}
                <Badge variant="outline" className={statusStyles[rx.status]}>{rx.status}</Badge>
              </div>
            </div>
          </motion.div>
        ))}
      </div>

      <Dialog open={!!selected} onOpenChange={() => setSelected(null)}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>Prescription Details</DialogTitle>
          </DialogHeader>
          {selected && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <InfoRow icon={Pill} label="Medication" value={`${selected.medication} ${selected.dosage}`} />
                <InfoRow icon={Clock} label="Frequency" value={selected.frequency} />
                <InfoRow icon={User} label="Patient" value={selected.patient} />
                <InfoRow icon={RefreshCw} label="Refills Left" value={String(selected.refills)} />
                <InfoRow icon={Calendar} label="Start" value={selected.startDate} />
                <InfoRow icon={Calendar} label="End" value={selected.endDate} />
              </div>
              <div className="rounded-lg bg-muted/50 p-3">
                <p className="text-xs font-medium text-muted-foreground mb-1">Notes</p>
                <p className="text-sm text-foreground">{selected.notes}</p>
              </div>
              <div className="flex gap-2 justify-end">
                <Button variant="outline" size="sm">Edit</Button>
                <Button size="sm">Renew Prescription</Button>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </RolePageShell>
  );
}

function InfoRow({ icon: Icon, label, value }: { icon: React.ElementType; label: string; value: string }) {
  return (
    <div className="flex items-start gap-2">
      <Icon className="h-4 w-4 text-muted-foreground mt-0.5" />
      <div>
        <p className="text-xs text-muted-foreground">{label}</p>
        <p className="text-sm font-medium text-foreground">{value}</p>
      </div>
    </div>
  );
}
