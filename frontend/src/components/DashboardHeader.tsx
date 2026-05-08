import { Bell, Search } from "lucide-react";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { useRole } from "@/contexts/RoleContext";

interface DashboardHeaderProps {
  title?: string;
  subtitle?: string;
}

export function DashboardHeader({
  title = "Dashboard",
  subtitle,
}: DashboardHeaderProps) {
  const { user } = useRole();

  const initials = user?.email
    ? user.email.slice(0, 2).toUpperCase()
    : "?";

  const displaySubtitle = subtitle ?? (user ? `Logged in as ${user.email}` : "");

  return (
    <header className="flex items-center justify-between border-b bg-card/60 backdrop-blur-sm px-8 py-4">
      <div>
        <h1 className="text-xl font-bold tracking-tight">{title}</h1>
        <p className="text-sm text-muted-foreground">{displaySubtitle}</p>
      </div>
      <div className="flex items-center gap-3">
        <div className="relative hidden md:block">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <input
            type="text"
            placeholder="Search patients, doctors..."
            className="h-9 w-64 rounded-lg border bg-background pl-9 pr-4 text-sm outline-none transition-colors focus:border-primary focus:ring-1 focus:ring-ring"
          />
        </div>
        <button className="relative flex h-9 w-9 items-center justify-center rounded-lg border bg-background transition-colors hover:bg-accent">
          <Bell className="h-4 w-4 text-muted-foreground" />
          <span className="absolute -right-0.5 -top-0.5 h-2.5 w-2.5 rounded-full bg-destructive border-2 border-card" />
        </button>
        <Avatar className="h-9 w-9 border">
          <AvatarFallback className="bg-primary text-primary-foreground text-xs font-semibold">
            {initials}
          </AvatarFallback>
        </Avatar>
      </div>
    </header>
  );
}