import { useGSAP } from "@gsap/react";
import gsap from "gsap";
import { X } from "lucide-react";
import { useEffect, useRef, type ReactNode } from "react";

type Props = {
  title: string;
  subtitle: string;
  onClosed: () => void;
  children: (close: () => void) => ReactNode;
};

export function SidePanel({ title, subtitle, onClosed, children }: Props) {
  const scope = useRef<HTMLDivElement>(null);

  const { contextSafe } = useGSAP(
    () => {
      gsap.from(".scrim", { opacity: 0, duration: 0.25 });
      gsap.from(".panel", { x: 48, opacity: 0, duration: 0.45, ease: "power3.out" });
    },
    { scope },
  );

  const close = contextSafe(() => {
    gsap.to(".panel", { x: 48, opacity: 0, duration: 0.25, ease: "power2.in" });
    gsap.to(".scrim", { opacity: 0, duration: 0.25, onComplete: onClosed });
  });

  useEffect(() => {
    const onKey = (event: globalThis.KeyboardEvent) => {
      if (event.key === "Escape") close();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [close]);

  return (
    <div ref={scope} className="overlay">
      <div className="scrim" onClick={close} />
      <aside className="panel" role="dialog" aria-modal="true" aria-label={title}>
        <header className="panel-header">
          <div>
            <h2>{title}</h2>
            <p>{subtitle}</p>
          </div>
          <button type="button" className="icon-button" aria-label="Chiudi" onClick={close}>
            <X size={18} />
          </button>
        </header>
        {children(close)}
      </aside>
    </div>
  );
}
