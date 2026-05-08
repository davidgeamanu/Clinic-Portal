import { RolePageShell } from "@/components/RolePageShell";
import { StatsCard } from "@/components/StatsCard";
import { Users, Stethoscope, Calendar, DollarSign } from "lucide-react";
import { motion } from "framer-motion";
import {
    AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip,
    ResponsiveContainer, PieChart, Pie, Cell,
} from "recharts";
import { useAdminDashboard } from "@/hooks/useAdmin";

const DEPT_COLORS = [
    "hsl(173, 58%, 39%)",
    "hsl(210, 100%, 52%)",
    "hsl(38, 92%, 50%)",
    "hsl(152, 60%, 42%)",
    "hsl(0, 72%, 51%)",
    "hsl(280, 60%, 55%)",
];

export default function AdminDashboard() {
    const { data, isLoading } = useAdminDashboard();

    return (
        <RolePageShell title="Admin Dashboard" subtitle="System overview & management">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
                <StatsCard
                    title="Total Patients"
                    value={isLoading ? "—" : data!.totalPatients.toLocaleString()}
                    icon={Users}
                    iconBg="bg-primary/10 text-primary"
                    delay={0}
                />
                <StatsCard
                    title="Active Doctors"
                    value={isLoading ? "—" : data!.activeDoctors.toLocaleString()}
                    icon={Stethoscope}
                    iconBg="bg-info/10 text-info"
                    delay={0.05}
                />
                <StatsCard
                    title="Today's Appointments"
                    value={isLoading ? "—" : data!.todaysAppointments.toLocaleString()}
                    icon={Calendar}
                    iconBg="bg-warning/10 text-warning"
                    delay={0.1}
                />
                <StatsCard
                    title="Monthly Revenue"
                    value={isLoading ? "—" : `$${data!.monthlyRevenue.toLocaleString()}`}
                    icon={DollarSign}
                    iconBg="bg-success/10 text-success"
                    delay={0.15}
                />
            </div>

            <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
                <motion.div
                    initial={{ opacity: 0, y: 12 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.2 }}
                    className="lg:col-span-2 rounded-xl border border-border bg-card p-6"
                >
                    <h3 className="text-lg font-semibold text-foreground mb-4">Weekly Overview</h3>
                    <ResponsiveContainer width="100%" height={280}>
                        <AreaChart data={data?.weeklyStats ?? []}>
                            <CartesianGrid strokeDasharray="3 3" stroke="hsl(210, 20%, 90%)" />
                            <XAxis dataKey="day" tick={{ fontSize: 12, fill: "hsl(210, 15%, 50%)" }} />
                            <YAxis tick={{ fontSize: 12, fill: "hsl(210, 15%, 50%)" }} />
                            <Tooltip contentStyle={{ borderRadius: "0.75rem", border: "1px solid hsl(210, 20%, 90%)", fontSize: "13px" }} />
                            <Area type="monotone" dataKey="patients" stroke="hsl(173, 58%, 39%)" fill="hsl(173, 58%, 39%)" fillOpacity={0.1} strokeWidth={2} />
                            <Area type="monotone" dataKey="appointments" stroke="hsl(210, 100%, 52%)" fill="hsl(210, 100%, 52%)" fillOpacity={0.1} strokeWidth={2} />
                        </AreaChart>
                    </ResponsiveContainer>
                </motion.div>

                <motion.div
                    initial={{ opacity: 0, y: 12 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.25 }}
                    className="rounded-xl border border-border bg-card p-6"
                >
                    <h3 className="text-lg font-semibold text-foreground mb-4">Department Load</h3>
                    <ResponsiveContainer width="100%" height={200}>
                        <PieChart>
                            <Pie
                                data={data?.departmentLoad ?? []}
                                cx="50%"
                                cy="50%"
                                innerRadius={50}
                                outerRadius={80}
                                paddingAngle={4}
                                dataKey="value"
                            >
                                {(data?.departmentLoad ?? []).map((_, i) => (
                                    <Cell key={i} fill={DEPT_COLORS[i % DEPT_COLORS.length]} />
                                ))}
                            </Pie>
                            <Tooltip contentStyle={{ borderRadius: "0.75rem", fontSize: "13px" }} />
                        </PieChart>
                    </ResponsiveContainer>
                    <div className="space-y-1.5 mt-2">
                        {(data?.departmentLoad ?? []).map((d, i) => (
                            <div key={d.name} className="flex items-center justify-between text-sm">
                                <div className="flex items-center gap-2">
                                    <div className="h-2.5 w-2.5 rounded-full" style={{ backgroundColor: DEPT_COLORS[i % DEPT_COLORS.length] }} />
                                    <span className="text-muted-foreground">{d.name}</span>
                                </div>
                                <span className="font-medium text-foreground">{d.value}%</span>
                            </div>
                        ))}
                    </div>
                </motion.div>
            </div>
        </RolePageShell>
    );
}
