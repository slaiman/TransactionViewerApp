export type View = "transactions" | "dashboard" | "accounts" | "audit";

interface NavTabsProps {
  active: View;
  onChange: (view: View) => void;
}

const TABS: Array<{ value: View; label: string }> = [
  { value: "transactions", label: "Transactions" },
  { value: "dashboard", label: "Dashboard" },
  { value: "accounts", label: "Accounts" },
  { value: "audit", label: "Audit Log" },
];

export function NavTabs({ active, onChange }: NavTabsProps) {
  return (
    <nav className="mb-8 flex gap-1 border-b border-ink/10" aria-label="Main">
      {TABS.map((tab) => {
        const isActive = tab.value === active;
        return (
          <button
            key={tab.value}
            type="button"
            onClick={() => onChange(tab.value)}
            aria-current={isActive ? "page" : undefined}
            className={`-mb-px border-b-2 px-3 py-2 text-sm font-medium transition-colors ${
              isActive
                ? "border-ink text-ink"
                : "border-transparent text-ink/40 hover:text-ink/70"
            }`}
          >
            {tab.label}
          </button>
        );
      })}
    </nav>
  );
}
