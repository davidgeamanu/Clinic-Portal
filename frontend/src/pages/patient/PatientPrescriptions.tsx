import { RolePageShell } from "@/components/RolePageShell";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Pill, RefreshCw, Clock, AlertCircle } from "lucide-react";

const prescriptions = [
  { id: "RX001", name: "Lisinopril", dosage: "10mg", frequency: "Once daily, morning", doctor: "Dr. James Williams", startDate: "Oct 15, 2023", refillDate: "Feb 15, 2024", remaining: "22 days", status: "Active", instructions: "Take with water. Avoid potassium-rich foods." },
  { id: "RX002", name: "Aspirin", dosage: "81mg", frequency: "Once daily", doctor: "Dr. James Williams", startDate: "Oct 15, 2023", refillDate: "Mar 1, 2024", remaining: "37 days", status: "Active", instructions: "Take with food to reduce stomach irritation." },
  { id: "RX003", name: "Atorvastatin", dosage: "20mg", frequency: "Once daily, bedtime", doctor: "Dr. James Williams", startDate: "Nov 1, 2023", refillDate: "Feb 20, 2024", remaining: "27 days", status: "Active", instructions: "Take at bedtime. Avoid grapefruit juice." },
  { id: "RX004", name: "Metformin", dosage: "500mg", frequency: "Twice daily", doctor: "Dr. Sarah Chen", startDate: "Jun 1, 2023", refillDate: "Jan 10, 2024", remaining: "0 days", status: "Refill Needed", instructions: "Take with meals. Stay hydrated." },
  { id: "RX005", name: "Amoxicillin", dosage: "250mg", frequency: "Three times daily", doctor: "Dr. Lisa Park", startDate: "Dec 1, 2023", refillDate: "—", remaining: "—", status: "Completed", instructions: "Complete full course. Take until finished." },
];

const statusStyles: Record<string, string> = {
  Active: "bg-success/10 text-success border-success/20",
  "Refill Needed": "bg-warning/10 text-warning border-warning/20",
  Completed: "bg-muted text-muted-foreground border-border",
};

export default function PatientPrescriptions() {
  return (
    <RolePageShell title="My Prescriptions" subtitle="View and manage your medications">
      <div className="space-y-4">
        {prescriptions.map((rx) => (
          <div key={rx.id} className="rounded-xl border border-border bg-card p-5">
            <div className="flex items-start justify-between mb-3">
              <div className="flex items-start gap-3">
                <div className={`p-2.5 rounded-lg ${rx.status === "Refill Needed" ? "bg-warning/10" : "bg-primary/10"}`}>
                  <Pill className={`h-5 w-5 ${rx.status === "Refill Needed" ? "text-warning" : "text-primary"}`} />
                </div>
                <div>
                  <h4 className="text-base font-semibold text-foreground">{rx.name} <span className="text-muted-foreground font-normal">{rx.dosage}</span></h4>
                  <p className="text-sm text-muted-foreground">{rx.frequency}</p>
                  <p className="text-xs text-muted-foreground mt-1">Prescribed by {rx.doctor}</p>
                </div>
              </div>
              <Badge variant="outline" className={statusStyles[rx.status]}>{rx.status}</Badge>
            </div>

            <div className="ml-12 p-3 rounded-lg bg-muted/30 text-sm text-muted-foreground mb-3">
              <p className="flex items-center gap-2"><AlertCircle className="h-3.5 w-3.5 text-info" /> {rx.instructions}</p>
            </div>

            <div className="ml-12 flex items-center justify-between">
              <div className="flex items-center gap-4 text-xs text-muted-foreground">
                <span className="flex items-center gap-1"><Clock className="h-3 w-3" /> Since {rx.startDate}</span>
                {rx.refillDate !== "—" && <span>Refill by {rx.refillDate} ({rx.remaining} left)</span>}
              </div>
              {rx.status === "Refill Needed" && (
                <Button size="sm" className="gap-1.5"><RefreshCw className="h-3.5 w-3.5" /> Request Refill</Button>
              )}
              {rx.status === "Active" && (
                <Button variant="outline" size="sm" className="gap-1.5"><RefreshCw className="h-3.5 w-3.5" /> Request Refill</Button>
              )}
            </div>
          </div>
        ))}
      </div>
    </RolePageShell>
  );
}
