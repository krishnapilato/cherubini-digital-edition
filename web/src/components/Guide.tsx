import { useGSAP } from "@gsap/react";
import gsap from "gsap";
import { ExternalLink } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { Modal } from "./Modal";

const GUIDE_URL = `${import.meta.env.BASE_URL}guida/index.html`;
// The loader stays at least this long, so it reads as a deliberate pause rather than a flicker.
const LOADER_MS = 2000;

// The guide runs in a same-origin frame, on the page's own thread: loading it while the dialog
// opens would make the opening animation stutter, so it loads once the dialog is in place,
// behind the loader.
export function GuideDialog({ onClosed }: Readonly<{ onClosed: () => void }>) {
  const [opened, setOpened] = useState(false);
  const [loaded, setLoaded] = useState(false);
  const [waited, setWaited] = useState(false);
  const body = useRef<HTMLDivElement>(null);
  const frame = useRef<HTMLIFrameElement>(null);
  // The guide's own GSAP, which runs its opening animation as soon as it loads.
  const guideAnimations = () =>
    (frame.current?.contentWindow as (Window & { gsap?: typeof gsap }) | null | undefined)?.gsap?.globalTimeline;

  useEffect(() => {
    const timer = setTimeout(() => setWaited(true), LOADER_MS);
    return () => clearTimeout(timer);
  }, []);

  useGSAP(
    () => {
      gsap.to(".loader-dot", {
        y: -6,
        opacity: 1,
        duration: 0.45,
        ease: "sine.inOut",
        stagger: { each: 0.15, repeat: -1, yoyo: true },
      });
    },
    { scope: body },
  );

  useGSAP(
    () => {
      if (!loaded || !waited) return;
      gsap
        .timeline()
        .to(".loader", { opacity: 0, y: -8, duration: 0.3, ease: "power2.in" })
        .set(".loader", { display: "none" })
        .to("iframe", {
          opacity: 1,
          duration: 0.45,
          ease: "power2.out",
          onStart: () => guideAnimations()?.resume(),
        });
    },
    { scope: body, dependencies: [loaded, waited] },
  );

  return (
    <Modal
      className="guide"
      hidden={{ y: 12, scale: 0.99, opacity: 0 }}
      opening={{ duration: 0.55, ease: "power2.out" }}
      title="Guida all'uso"
      onOpened={() => setOpened(true)}
      onClosed={onClosed}
      actions={
        <a
          className="icon-button"
          href={GUIDE_URL}
          target="_blank"
          rel="noopener"
          aria-label="Apri la guida in una nuova scheda"
          title="Apri in una nuova scheda"
        >
          <ExternalLink size={18} aria-hidden="true" />
        </a>
      }
    >
      {(close) => (
        <div className="guide-body" ref={body}>
          <div className="loader" role="status">
            <div className="loader-dots" aria-hidden="true">
              <span className="loader-dot" />
              <span className="loader-dot" />
              <span className="loader-dot" />
            </div>
            <p>Caricamento della guida</p>
          </div>
          {opened && (
            <iframe
              ref={frame}
              src={GUIDE_URL}
              title="Guida all'uso"
              onLoad={(e) => {
                // Held until the guide fades in, so its opening animation plays in view.
                guideAnimations()?.pause();
                setLoaded(true);
                e.currentTarget.contentWindow?.addEventListener("keydown", (key) => {
                  if (key.key === "Escape") close();
                });
              }}
            />
          )}
        </div>
      )}
    </Modal>
  );
}
