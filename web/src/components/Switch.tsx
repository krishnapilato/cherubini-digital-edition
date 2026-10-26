type Props = { checked: boolean; onChange: (checked: boolean) => void; label: string };

export function Switch({ checked, onChange, label }: Props) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      className={checked ? "switch on" : "switch"}
      title="Trova anche le parole che contengono quelle scritte (ferro → ferroso)"
      onClick={() => onChange(!checked)}
    >
      <span className="track">
        <span className="knob" />
      </span>
      {label}
    </button>
  );
}
