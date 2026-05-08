import { RolePageShell } from "@/components/RolePageShell";
import { motion } from "framer-motion";
import { DollarSign, Users } from "lucide-react";
import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
    ResponsiveContainer, LineChart, Line,
} from "recharts";
import { useAdminAnalytics } from "@/hooks/useAdmin";

export default function AdminReports() {
    const { data, isLoading } = useAdminAnalytics();

    return (
        <RolePageShell title="Reports & Analytics" subtitle="System-wide revenue and patient statistics">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <motion.div
                    initial={{ opacity: 0, y: 12 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0 }}
                    className="rounded-xl border border-border bg-card p-5"
                >
                    <div className="flex items-center justify-between mb-2">
                        <span className="text-sm text-muted-foreground">Monthly Revenue</span>
                        <DollarSign className="h-4 w-4 text-muted-foreground" />
                    </div>
                    <p className="text-2xl font-bold text-foreground">
                        {isLoading ? "—" : `$${data!.totalRevenue.toLocaleString()}`}
                    </p>
                    <p className="text-xs mt-1 text-muted-foreground">Current month (completed appointments)</p>
                </motion.div>

                <motion.div
                    initial={{ opacity: 0, y: 12 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.05 }}
                    className="rounded-xl border border-border bg-card p-5"
                >
                    <div className="flex items-center justify-between mb-2">
                        <span className="text-sm text-muted-foreground">Total Patients</span>
                        <Users className="h-4 w-4 text-muted-foreground" />
                    </div>
                    <p className="text-2xl font-bold text-foreground">
                        {isLoading ? "—" : data!.totalPatients.toLocaleString()}
                    </p>
                    <p className="text-xs mt-1 text-muted-foreground">Registered in the system</p>
                </motion.div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <motion.div
                    initial={{ opacity: 0, y: 12 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.1 }}
                    className="rounded-xl border border-border bg-card p-6"
                >
                    <h3 className="text-lg font-semibold text-foreground mb-4">Monthly Revenue</h3>
                    <ResponsiveContainer width="100%" height={260}>
                        <BarChart data={data?.monthlyRevenue ?? []}>
                            <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                            <XAxis dataKey="month" tick={{ fontSize: 12, fill: "hsl(var(--muted-foreground))" }} />
                            <YAxis
                                tick={{ fontSize: 12, fill: "hsl(var(--muted-foreground))" }}
                                tickFormatter={(v) => `$${(v / 1000).toFixed(0)}k`}
                            />
                            <Tooltip
                                contentStyle={{ borderRadius: "0.75rem", border: "1px solid hsl(var(--border))", fontSize: "13px" }}
                                formatter={(v: number) => [`$${v.toLocaleString()}`, "Revenue"]}
                            />
                            <Bar dataKey="revenue" fill="hsl(173, 58%, 39%)" radius={[4, 4, 0, 0]} />
                        </BarChart>
                    </ResponsiveContainer>
                </motion.div>

                <motion.div
                    initial={{ opacity: 0, y: 12 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.15 }}
                    className="rounded-xl border border-border bg-card p-6"
                >
                    <h3 className="text-lg font-semibold text-foreground mb-4">New Patient Registrations</h3>
                    <ResponsiveContainer width="100%" height={260}>
                        <LineChart data={data?.patientTrend ?? []}>
                            <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                            <XAxis dataKey="month" tick={{ fontSize: 12, fill: "hsl(var(--muted-foreground))" }} />
                            <YAxis tick={{ fontSize: 12, fill: "hsl(var(--muted-foreground))" }} />
                            <Tooltip
                                contentStyle={{ borderRadius: "0.75rem", border: "1px solid hsl(var(--border))", fontSize: "13px" }}
                                formatter={(v: number) => [v, "New patients"]}
                            />
                            <Line
                                type="monotone"
                                dataKey="count"
                                stroke="hsl(173, 58%, 39%)"
                                strokeWidth={2}
                                dot={{ r: 4 }}
                            />
                        </LineChart>
                    </ResponsiveContainer>
                </motion.div>
            </div>
        </RolePageShell>
    );
}
