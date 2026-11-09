interface Props {
  readonly checked: boolean;
  readonly onChange: (checked: boolean) => void;
  readonly label: string;
}

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
      <span className="track" aria-hidden="true">
        <span className="knob" />
      </span>
      {label}
    </button>
  );
}
