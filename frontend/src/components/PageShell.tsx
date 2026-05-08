import { ReactNode } from "react";
import { Sidebar } from "@/components/Sidebar";
import { DashboardHeader } from "@/components/DashboardHeader";

interface PageShellProps {
  title: string;
  subtitle: string;
  children: ReactNode;
}

export function PageShell({ title, subtitle, children }: PageShellProps) {
  return (
    <div className="flex min-h-screen bg-background">
      <Sidebar />
      <main className="flex-1 ml-[260px] transition-all duration-300">
        <DashboardHeader title={title} subtitle={subtitle} />
        <div className="p-8 space-y-6">{children}</div>
      </main>
    </div>
  );
}
