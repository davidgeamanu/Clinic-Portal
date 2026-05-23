import { RolePageShell } from "@/components/RolePageShell";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { CreditCard, Download, CheckCircle2, Clock, AlertCircle } from "lucide-react";

const bills = [
  { id: "INV-001", description: "Cardiac Assessment & ECG", date: "Jan 22, 2024", amount: "$245.00", status: "Pending", dueDate: "Feb 1, 2024", department: "Cardiology" },
  { id: "INV-002", description: "Blood Work & Lab Tests", date: "Jan 15, 2024", amount: "$180.00", status: "Paid", dueDate: "Jan 25, 2024", department: "Laboratory" },
  { id: "INV-003", description: "Annual Physical Examination", date: "Dec 20, 2023", amount: "$350.00", status: "Paid", dueDate: "Jan 5, 2024", department: "General" },
  { id: "INV-004", description: "Chest X-Ray", date: "Nov 10, 2023", amount: "$120.00", status: "Paid", dueDate: "Nov 25, 2023", department: "Radiology" },
  { id: "INV-005", description: "Specialist Consultation", date: "Oct 5, 2023", amount: "$200.00", status: "Overdue", dueDate: "Oct 20, 2023", department: "Cardiology" },
];

const statusStyles: Record<string, string> = {
  Paid: "bg-success/10 text-success border-success/20",
  Pending: "bg-warning/10 text-warning border-warning/20",
  Overdue: "bg-destructive/10 text-destructive border-destructive/20",
};

const statusIcons: Record<string, any> = {
  Paid: CheckCircle2,
  Pending: Clock,
  Overdue: AlertCircle,
};

export default function PatientBilling() {
  const totalPending = bills.filter((b) => b.status !== "Paid").reduce((sum, b) => sum + parseFloat(b.amount.replace("$", "")), 0);

  return (
    <RolePageShell title="Billing" subtitle="View and manage your invoices">
      {/* Summary */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="rounded-xl border border-border bg-card p-5 text-center">
          <p className="text-sm text-muted-foreground">Total Outstanding</p>
          <p className="text-2xl font-bold text-foreground mt-1">${totalPending.toFixed(2)}</p>
        </div>
        <div className="rounded-xl border border-border bg-card p-5 text-center">
          <p className="text-sm text-muted-foreground">Paid This Year</p>
          <p className="text-2xl font-bold text-success mt-1">$650.00</p>
        </div>
        <div className="rounded-xl border border-border bg-card p-5 text-center">
          <p className="text-sm text-muted-foreground">Insurance Coverage</p>
          <p className="text-2xl font-bold text-info mt-1">80%</p>
        </div>
      </div>

      {/* Invoices */}
      <div className="rounded-xl border border-border bg-card overflow-hidden">
        <table className="w-full">
          <thead>
            <tr className="border-b border-border bg-muted/50">
              {["Invoice", "Description", "Date", "Amount", "Status", "Actions"].map((h) => (
                <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-muted-foreground uppercase tracking-wider">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {bills.map((bill) => {
              const StatusIcon = statusIcons[bill.status];
              return (
                <tr key={bill.id} className="border-b border-border hover:bg-muted/30 transition-colors">
                  <td className="px-4 py-3 text-sm font-medium text-foreground">{bill.id}</td>
                  <td className="px-4 py-3">
                    <p className="text-sm text-foreground">{bill.description}</p>
                    <p className="text-xs text-muted-foreground">{bill.department}</p>
                  </td>
                  <td className="px-4 py-3 text-sm text-muted-foreground">{bill.date}</td>
                  <td className="px-4 py-3 text-sm font-semibold text-foreground">{bill.amount}</td>
                  <td className="px-4 py-3">
                    <Badge variant="outline" className={`gap-1 ${statusStyles[bill.status]}`}>
                      <StatusIcon className="h-3 w-3" />{bill.status}
                    </Badge>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-1">
                      {bill.status !== "Paid" && <Button size="sm">Pay Now</Button>}
                      <Button variant="ghost" size="sm"><Download className="h-3.5 w-3.5" /></Button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </RolePageShell>
  );
}
