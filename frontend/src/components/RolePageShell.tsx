import { ReactNode } from "react";
import { RoleSidebar } from "@/components/RoleSidebar";
import { DashboardHeader } from "@/components/DashboardHeader";
import { useNotificationStream } from "@/hooks/useNotificationStream";

interface RolePageShellProps {
  title: string;
  subtitle?: string;
  children: ReactNode;
}

export function RolePageShell({ title, subtitle, children }: RolePageShellProps) {
  useNotificationStream();
  return (
    <div className="flex min-h-screen bg-background">
      <RoleSidebar />
      <main className="flex-1 ml-[260px] transition-all duration-300">
        <DashboardHeader title={title} subtitle={subtitle} />
        <div className="p-8 space-y-6">{children}</div>
      </main>
    </div>
  );
}
