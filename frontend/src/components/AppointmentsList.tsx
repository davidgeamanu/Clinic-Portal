import { motion } from "framer-motion";
import { Clock, Video, MapPin } from "lucide-react";

const appointments = [
  { time: "09:00 AM", patient: "James Miller", type: "Check-up", mode: "In-Person", doctor: "Dr. Smith" },
  { time: "10:30 AM", patient: "Anna White", type: "Follow-up", mode: "Video", doctor: "Dr. Patel" },
  { time: "11:15 AM", patient: "Tom Harris", type: "Consultation", mode: "In-Person", doctor: "Dr. Kim" },
  { time: "02:00 PM", patient: "Grace Lee", type: "Lab Review", mode: "Video", doctor: "Dr. Smith" },
];

export function AppointmentsList() {
  return (
    <motion.div
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay: 0.4 }}
      className="rounded-xl border bg-card shadow-sm"
    >
      <div className="flex items-center justify-between border-b p-5">
        <h2 className="text-base font-semibold">Today's Appointments</h2>
        <button className="text-sm font-medium text-primary hover:underline">Schedule New</button>
      </div>
      <div className="divide-y">
        {appointments.map((a) => (
          <div key={a.time + a.patient} className="flex items-center gap-4 px-5 py-3.5">
            <div className="flex h-11 w-14 flex-col items-center justify-center rounded-lg bg-secondary">
              <Clock className="h-3.5 w-3.5 text-secondary-foreground mb-0.5" />
              <span className="text-[10px] font-semibold text-secondary-foreground">{a.time}</span>
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium truncate">{a.patient}</p>
              <p className="text-xs text-muted-foreground">{a.type} · {a.doctor}</p>
            </div>
            <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
              {a.mode === "Video" ? <Video className="h-3.5 w-3.5" /> : <MapPin className="h-3.5 w-3.5" />}
              {a.mode}
            </div>
          </div>
        ))}
      </div>
    </motion.div>
  );
}
