import { motion } from "framer-motion";
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";

const data = [
  { name: "Mon", patients: 42, appointments: 28 },
  { name: "Tue", patients: 56, appointments: 35 },
  { name: "Wed", patients: 48, appointments: 30 },
  { name: "Thu", patients: 63, appointments: 42 },
  { name: "Fri", patients: 51, appointments: 38 },
  { name: "Sat", patients: 34, appointments: 20 },
  { name: "Sun", patients: 22, appointments: 12 },
];

export function ActivityChart() {
  return (
    <motion.div
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay: 0.2 }}
      className="rounded-xl border bg-card p-5 shadow-sm"
    >
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-base font-semibold">Weekly Activity</h2>
        <div className="flex gap-4 text-xs">
          <span className="flex items-center gap-1.5">
            <span className="h-2 w-2 rounded-full bg-primary" />
            Patients
          </span>
          <span className="flex items-center gap-1.5">
            <span className="h-2 w-2 rounded-full bg-info" />
            Appointments
          </span>
        </div>
      </div>
      <ResponsiveContainer width="100%" height={240}>
        <AreaChart data={data}>
          <defs>
            <linearGradient id="colorPatients" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor="hsl(173, 58%, 39%)" stopOpacity={0.2} />
              <stop offset="95%" stopColor="hsl(173, 58%, 39%)" stopOpacity={0} />
            </linearGradient>
            <linearGradient id="colorAppointments" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor="hsl(210, 100%, 52%)" stopOpacity={0.2} />
              <stop offset="95%" stopColor="hsl(210, 100%, 52%)" stopOpacity={0} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="hsl(210, 20%, 90%)" vertical={false} />
          <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: "hsl(210, 15%, 50%)" }} />
          <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: "hsl(210, 15%, 50%)" }} />
          <Tooltip
            contentStyle={{
              borderRadius: "0.5rem",
              border: "1px solid hsl(210, 20%, 90%)",
              boxShadow: "0 4px 12px rgba(0,0,0,0.08)",
              fontSize: "12px",
            }}
          />
          <Area type="monotone" dataKey="patients" stroke="hsl(173, 58%, 39%)" strokeWidth={2} fill="url(#colorPatients)" />
          <Area type="monotone" dataKey="appointments" stroke="hsl(210, 100%, 52%)" strokeWidth={2} fill="url(#colorAppointments)" />
        </AreaChart>
      </ResponsiveContainer>
    </motion.div>
  );
}
