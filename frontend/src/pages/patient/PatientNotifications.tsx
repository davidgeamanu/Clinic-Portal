import { useState } from "react";
import { RolePageShell } from "@/components/RolePageShell";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Calendar, Pill, FileText, CreditCard, Bell, CheckCheck, Clock } from "lucide-react";

interface Notification {
  id: string;
  title: string;
  message: string;
  time: string;
  type: "appointment" | "prescription" | "record" | "billing" | "system";
  read: boolean;
}

const initialNotifications: Notification[] = [
  { id: "N001", title: "Upcoming Appointment", message: "Reminder: You have an appointment with Dr. James Williams tomorrow at 10:00 AM.", time: "2 hours ago", type: "appointment", read: false },
  { id: "N002", title: "Prescription Refill Needed", message: "Your Metformin 500mg prescription needs to be refilled. Contact your doctor to request a refill.", time: "5 hours ago", type: "prescription", read: false },
  { id: "N003", title: "Lab Results Available", message: "Your blood work results from Jan 15, 2024 are now available in your medical records.", time: "1 day ago", type: "record", read: false },
  { id: "N004", title: "Invoice Payment Due", message: "Invoice INV-001 for $245.00 is due on Feb 1, 2024. Please make payment to avoid late fees.", time: "2 days ago", type: "billing", read: true },
  { id: "N005", title: "Appointment Confirmed", message: "Your appointment with Dr. Sarah Chen on Feb 10, 2024 at 2:30 PM has been confirmed.", time: "3 days ago", type: "appointment", read: true },
  { id: "N006", title: "New Health Tip", message: "Check out our latest article on managing blood pressure through diet and exercise.", time: "5 days ago", type: "system", read: true },
  { id: "N007", title: "Prescription Updated", message: "Dr. James Williams has updated your Lisinopril dosage. Please review the changes.", time: "1 week ago", type: "prescription", read: true },
];

const typeConfig: Record<string, { icon: any; color: string }> = {
  appointment: { icon: Calendar, color: "bg-primary/10 text-primary" },
  prescription: { icon: Pill, color: "bg-success/10 text-success" },
  record: { icon: FileText, color: "bg-info/10 text-info" },
  billing: { icon: CreditCard, color: "bg-warning/10 text-warning" },
  system: { icon: Bell, color: "bg-muted text-muted-foreground" },
};

export default function PatientNotifications() {
  const [notifications, setNotifications] = useState(initialNotifications);
  const [filter, setFilter] = useState<"all" | "unread">("all");

  const markAllRead = () => setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
  const toggleRead = (id: string) => setNotifications((prev) => prev.map((n) => n.id === id ? { ...n, read: !n.read } : n));

  const filtered = filter === "unread" ? notifications.filter((n) => !n.read) : notifications;
  const unreadCount = notifications.filter((n) => !n.read).length;

  return (
    <RolePageShell title="Notifications" subtitle="Stay updated on your health activities">
      {/* Toolbar */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Button variant={filter === "all" ? "default" : "outline"} size="sm" onClick={() => setFilter("all")}>
            All
          </Button>
          <Button variant={filter === "unread" ? "default" : "outline"} size="sm" onClick={() => setFilter("unread")} className="gap-1.5">
            Unread
            {unreadCount > 0 && <Badge className="h-5 min-w-[20px] px-1.5 text-xs bg-destructive text-destructive-foreground">{unreadCount}</Badge>}
          </Button>
        </div>
        {unreadCount > 0 && (
          <Button variant="ghost" size="sm" onClick={markAllRead} className="gap-1.5 text-muted-foreground">
            <CheckCheck className="h-3.5 w-3.5" /> Mark all read
          </Button>
        )}
      </div>

      {/* Notification List */}
      <div className="space-y-2">
        {filtered.length === 0 ? (
          <div className="rounded-xl border border-border bg-card p-12 text-center">
            <Bell className="h-10 w-10 text-muted-foreground/40 mx-auto mb-3" />
            <p className="text-sm text-muted-foreground">No {filter === "unread" ? "unread " : ""}notifications</p>
          </div>
        ) : (
          filtered.map((n) => {
            const config = typeConfig[n.type];
            const Icon = config.icon;
            return (
              <div
                key={n.id}
                className={`rounded-xl border p-4 transition-colors cursor-pointer hover:shadow-sm ${
                  n.read ? "border-border bg-card" : "border-primary/20 bg-primary/5"
                }`}
                onClick={() => toggleRead(n.id)}
              >
                <div className="flex items-start gap-3">
                  <div className={`p-2 rounded-lg shrink-0 ${config.color}`}>
                    <Icon className="h-4 w-4" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between gap-2 mb-1">
                      <h4 className={`text-sm font-medium ${n.read ? "text-foreground" : "text-foreground font-semibold"}`}>{n.title}</h4>
                      <div className="flex items-center gap-2 shrink-0">
                        {!n.read && <span className="h-2 w-2 rounded-full bg-primary" />}
                        <span className="text-xs text-muted-foreground flex items-center gap-1"><Clock className="h-3 w-3" />{n.time}</span>
                      </div>
                    </div>
                    <p className="text-sm text-muted-foreground">{n.message}</p>
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </RolePageShell>
  );
}
