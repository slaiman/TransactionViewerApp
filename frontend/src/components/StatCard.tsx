interface StatCardProps {
  label: string;
  value: string;
  accentClass?: string;
}

export function StatCard({ label, value, accentClass = "text-ink" }: StatCardProps) {
  return (
    <div className="rounded-md border border-ink/10 bg-white p-5">
      <p className="text-xs font-medium uppercase tracking-wide text-ink/40">{label}</p>
      <p className={`mt-2 font-mono text-2xl font-semibold tabular-nums ${accentClass}`}>{value}</p>
    </div>
  );
}
