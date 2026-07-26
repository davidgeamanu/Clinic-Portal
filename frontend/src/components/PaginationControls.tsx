import { Button } from "@/components/ui/button";
import { ChevronLeft, ChevronRight } from "lucide-react";
import type { Page } from "@/types/api";

interface PaginationControlsProps {
  page: Page<unknown>["page"] | undefined;
  onPageChange: (page: number) => void;
}

/** Footer for server-paginated tables: "Page X of Y · Z total" + prev/next. */
export function PaginationControls({ page, onPageChange }: PaginationControlsProps) {
  if (!page || page.totalPages <= 1) return null;

  return (
    <div className="flex items-center justify-between px-4 py-3 border-t border-border">
      <p className="text-xs text-muted-foreground">
        Page {page.number + 1} of {page.totalPages} · {page.totalElements} total
      </p>
      <div className="flex gap-2">
        <Button
          variant="outline"
          size="sm"
          disabled={page.number === 0}
          onClick={() => onPageChange(page.number - 1)}
          className="gap-1"
        >
          <ChevronLeft className="h-4 w-4" /> Previous
        </Button>
        <Button
          variant="outline"
          size="sm"
          disabled={page.number >= page.totalPages - 1}
          onClick={() => onPageChange(page.number + 1)}
          className="gap-1"
        >
          Next <ChevronRight className="h-4 w-4" />
        </Button>
      </div>
    </div>
  );
}
