import { useEffect, useState } from "react";

export function ElapsedTimer({ startedAt }: { startedAt: string }) {
  const [elapsed, setElapsed] = useState(
    () => Math.floor((Date.now() - new Date(startedAt).getTime()) / 1000)
  );

  useEffect(() => {
    const id = setInterval(
      () => setElapsed(Math.floor((Date.now() - new Date(startedAt).getTime()) / 1000)),
      1000
    );
    return () => clearInterval(id);
  }, [startedAt]);

  const m = Math.floor(elapsed / 60);
  const s = elapsed % 60;
  return (
    <span className="font-mono tabular-nums text-primary">
      {String(m).padStart(2, "0")}:{String(s).padStart(2, "0")} elapsed
    </span>
  );
}