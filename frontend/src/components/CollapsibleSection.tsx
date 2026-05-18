import { useState } from "react";
import type { ReactNode } from "react";
import { ChevronDown } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
import type { LucideIcon } from "lucide-react";

export function CollapsibleSection({
  title, icon: Icon, accent, badge, defaultOpen = false, children,
}: {
  title: string;
  icon?: LucideIcon;
  accent?: string;        // tailwind text color e.g. "text-destructive"
  badge?: ReactNode;
  defaultOpen?: boolean;
  children: ReactNode;
}) {
  const [open, setOpen] = useState(defaultOpen);
  return (
    <div className="rounded-xl border border-border bg-card overflow-hidden">
      <button
        type="button"
        onClick={() => setOpen(!open)}
        className="w-full flex items-center justify-between gap-3 p-4 hover:bg-muted/30 transition-colors"
      >
        <div className="flex items-center gap-2 min-w-0">
          {Icon && <Icon className={`h-3.5 w-3.5 shrink-0 ${accent ?? "text-muted-foreground"}`} />}
          <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground truncate">{title}</p>
          {badge}
        </div>
        <ChevronDown className={`h-4 w-4 text-muted-foreground shrink-0 transition-transform ${open ? "rotate-180" : ""}`} />
      </button>
      <AnimatePresence initial={false}>
        {open && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: "auto", opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            transition={{ duration: 0.2, ease: "easeInOut" }}
            className="overflow-hidden"
          >
            <div className="px-4 pb-4 space-y-3">{children}</div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}