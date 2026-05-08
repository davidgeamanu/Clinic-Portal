import { motion } from "framer-motion";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";

const patients = [
  { name: "Sarah Johnson", age: 34, condition: "Flu", status: "In Treatment", initials: "SJ" },
  { name: "Michael Chen", age: 52, condition: "Hypertension", status: "Admitted", initials: "MC" },
  { name: "Emily Davis", age: 28, condition: "Fracture", status: "Discharged", initials: "ED" },
  { name: "Robert Wilson", age: 67, condition: "Diabetes", status: "In Treatment", initials: "RW" },
  { name: "Lisa Anderson", age: 41, condition: "Migraine", status: "Waiting", initials: "LA" },
];

const statusStyles: Record<string, string> = {
  "In Treatment": "bg-info/10 text-info border-info/20",
  Admitted: "bg-warning/10 text-warning border-warning/20",
  Discharged: "bg-success/10 text-success border-success/20",
  Waiting: "bg-muted text-muted-foreground border-border",
};

export function RecentPatients() {
  return (
    <motion.div
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay: 0.3 }}
      className="rounded-xl border bg-card shadow-sm"
    >
      <div className="flex items-center justify-between border-b p-5">
        <h2 className="text-base font-semibold">Recent Patients</h2>
        <button className="text-sm font-medium text-primary hover:underline">View All</button>
      </div>
      <div className="divide-y">
        {patients.map((p) => (
          <div key={p.name} className="flex items-center justify-between px-5 py-3.5">
            <div className="flex items-center gap-3">
              <Avatar className="h-9 w-9">
                <AvatarFallback className="bg-secondary text-secondary-foreground text-xs font-semibold">
                  {p.initials}
                </AvatarFallback>
              </Avatar>
              <div>
                <p className="text-sm font-medium">{p.name}</p>
                <p className="text-xs text-muted-foreground">{p.condition} · Age {p.age}</p>
              </div>
            </div>
            <Badge variant="outline" className={statusStyles[p.status]}>
              {p.status}
            </Badge>
          </div>
        ))}
      </div>
    </motion.div>
  );
}
