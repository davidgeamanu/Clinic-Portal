import { useEffect, useState } from "react";

/**
 * Returns `value` delayed by `delayMs`, resetting the timer on every change.
 *
 * Used for server-side search boxes so a request fires once the user stops
 * typing rather than on every keystroke.
 */
export function useDebounce<T>(value: T, delayMs = 300): T {
  const [debounced, setDebounced] = useState(value);

  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delayMs);
    return () => clearTimeout(timer);
  }, [value, delayMs]);

  return debounced;
}
