interface AccountSwitcherProps {
  accountIds: string[];
  selectedAccountId: string;
  onChange: (accountId: string) => void;
}

export function AccountSwitcher({ accountIds, selectedAccountId, onChange }: AccountSwitcherProps) {
  if (accountIds.length <= 1) return null;

  return (
    <label className="flex items-center gap-2 text-xs font-medium uppercase tracking-wide text-ink/40">
      Account
      <select
        value={selectedAccountId}
        onChange={(e) => onChange(e.target.value)}
        className="rounded border border-ink/15 bg-white px-2 py-1 font-mono text-xs normal-case tracking-normal text-ink focus:border-accent focus:outline-none"
      >
        {accountIds.map((id) => (
          <option key={id} value={id}>
            {id}
          </option>
        ))}
      </select>
    </label>
  );
}
