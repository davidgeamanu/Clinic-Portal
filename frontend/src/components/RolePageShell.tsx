import { ReactNode } from "react";
import { RoleSidebar } from "@/components/RoleSidebar";
import { DashboardHeader } from "@/components/DashboardHeader";
import { useNotificationStream } from "@/hooks/useNotificationStream";
import { useSidebar } from "@/contexts/SidebarContext";
import { cn } from "@/lib/utils";

interface RolePageShellProps {
  title: string;
  subtitle?: string;
  children: ReactNode;
}

export function RolePageShell({ title, subtitle, children }: RolePageShellProps) {
  useNotificationStream();
  const { collapsed } = useSidebar();

  return (
    <div className="flex min-h-screen bg-background">
      <RoleSidebar />
      {/* The sidebar is fixed, so this margin stands in for its width. Same
          duration and easing as the sidebar's own width transition. */}
      <main
        className={cn(
          "flex-1 transition-[margin-left] duration-300",
          collapsed ? "ml-[72px]" : "ml-[260px]",
        )}
      >
        <DashboardHeader title={title} subtitle={subtitle} />
        <div className="p-8 space-y-6">{children}</div>
      </main>
    </div>
  );
}
