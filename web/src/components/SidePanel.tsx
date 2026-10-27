import { useGSAP } from "@gsap/react";
import gsap from "gsap";
import { X } from "lucide-react";
import { useEffect, useId, useState, type ReactNode } from "react";

interface Props {
  readonly title: string;
  readonly subtitle: string;
  readonly onClosed: () => void;
  readonly children: (close: () => void) => ReactNode;
}

// A native modal <dialog>: the browser traps focus, handles Esc, makes the page behind
// inert and gives focus back to the button that opened it. Esc normally plays the closing
// animation, but the browser may close the dialog by itself, so "close" is the single
// place where the panel is reported as closed.
export function SidePanel({ title, subtitle, onClosed, children }: Props) {
  const [dialog, setDialog] = useState<HTMLDialogElement | null>(null);
  const titleId = useId();

  useEffect(() => {
    if (dialog && !dialog.open) dialog.showModal();
  }, [dialog]);

  const { contextSafe } = useGSAP(
    () => {
      if (dialog) gsap.from(dialog, { x: 48, opacity: 0, duration: 0.45, ease: "power3.out" });
    },
    { dependencies: [dialog] },
  );

  const close = contextSafe(() => {
    gsap.to(dialog, {
      x: 48,
      opacity: 0,
      duration: 0.25,
      ease: "power2.in",
      overwrite: true,
      onComplete: () => dialog?.close(),
    });
  });

  return (
    <dialog
      ref={setDialog}
      className="panel"
      aria-labelledby={titleId}
      onCancel={(event) => {
        event.preventDefault();
        close();
      }}
      onClose={onClosed}
    >
      <header className="panel-header">
        <div>
          <h2 id={titleId}>{title}</h2>
          <p>{subtitle}</p>
        </div>
        <button type="button" className="icon-button" aria-label="Chiudi" onClick={close}>
          <X size={18} aria-hidden="true" />
        </button>
      </header>
      {children(close)}
    </dialog>
  );
}
