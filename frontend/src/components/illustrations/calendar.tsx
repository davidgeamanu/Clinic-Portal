import type { SVGProps } from "react";

/**
 * Hand-drawn to match the svgrepo medical set used by the other illustrations
 * here — same 1024 viewBox, same two-tone line style, 15-unit outlines.
 * That set has no calendar, and appointments are our most common empty state.
 */
export function CalendarIllustration(props: SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 1024 1024" aria-hidden="true" focusable="false" {...props}>
      <path className="il-tint" d="M140 298a48 48 0 0 1 48-48h648a48 48 0 0 1 48 48v92H140z" />

      <rect className="il-tint" x="216" y="470" width="150" height="140" rx="24" />
      <rect className="il-accent" x="437" y="470" width="150" height="140" rx="24" />
      <rect className="il-tint" x="658" y="470" width="150" height="140" rx="24" />
      <rect className="il-tint" x="216" y="670" width="150" height="140" rx="24" />
      <rect className="il-tint" x="437" y="670" width="150" height="140" rx="24" />
      <rect className="il-tint" x="658" y="670" width="150" height="140" rx="24" />

      <rect className="il-stroke" x="140" y="250" width="744" height="650" rx="48" strokeWidth="15" />
      <path className="il-stroke" d="M140 390h744" strokeWidth="15" />
      <path className="il-stroke" d="M322 172v82M702 172v82" strokeWidth="15" strokeLinecap="round" />
    </svg>
  );
}
