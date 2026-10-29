import { ExternalLink } from "lucide-react";
import { Modal } from "./Modal";

const GUIDE_URL = `${import.meta.env.BASE_URL}guida/index.html`;
const HIDDEN = { y: 24, scale: 0.98, opacity: 0 };

export function GuideDialog({ onClosed }: Readonly<{ onClosed: () => void }>) {
  return (
    <Modal
      className="guide"
      hidden={HIDDEN}
      title="Guida all'uso"
      onClosed={onClosed}
      actions={
        <a className="icon-button" href={GUIDE_URL} target="_blank" rel="noopener" aria-label="Apri la guida in una nuova scheda" title="Apri in una nuova scheda">
          <ExternalLink size={18} aria-hidden="true" />
        </a>
      }
    >
      {(close) => (
        <iframe
          src={GUIDE_URL}
          title="Guida all'uso"
          onLoad={(e) => e.currentTarget.contentWindow?.addEventListener("keydown", (key) => { if (key.key === "Escape") close() })}
        />
      )}
    </Modal>
  );
}