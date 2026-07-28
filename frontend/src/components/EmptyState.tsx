import { motion } from "framer-motion";
import type { ComponentType, ReactNode, SVGProps } from "react";
import { cn } from "@/lib/utils";

type EmptyStateSize = "sm" | "md" | "lg";

/** sm sits inside detail panels, md inside table cells, lg on a full page. */
const SIZES: Record<EmptyStateSize, { wrap: string; art: string; title: string; body: string }> = {
  sm: { wrap: "py-6 gap-1.5", art: "h-12 w-12", title: "text-sm", body: "text-xs max-w-[16rem]" },
  md: { wrap: "py-10 gap-2", art: "h-16 w-16", title: "text-base", body: "text-sm max-w-xs" },
  lg: { wrap: "py-14 gap-2", art: "h-24 w-24", title: "text-lg", body: "text-sm max-w-sm" },
};

interface EmptyStateProps {
  illustration: ComponentType<SVGProps<SVGSVGElement>>;
  title: string;
  description?: string;
  action?: ReactNode;
  size?: EmptyStateSize;
  className?: string;
}

export function EmptyState({
  illustration: Illustration,
  title,
  description,
  action,
  size = "lg",
  className,
}: EmptyStateProps) {
  const s = SIZES[size];

  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
      className={cn("flex flex-col items-center justify-center text-center", s.wrap, className)}
    >
      <Illustration className={cn("mb-1", s.art)} />
      <p className={cn("font-semibold text-foreground", s.title)}>{title}</p>
      {description && <p className={cn("text-muted-foreground", s.body)}>{description}</p>}
      {action && <div className="mt-3">{action}</div>}
    </motion.div>
  );
}
