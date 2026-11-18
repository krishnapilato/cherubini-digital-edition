import { useGSAP } from "@gsap/react";
import gsap from "gsap";
import { useRef } from "react";
import { ENTER, EXIT } from "../motion";

interface Props {
  readonly message: string;
  readonly onDone: () => void;
}

export function Toast({ message, onDone }: Props) {
  const ref = useRef<HTMLDivElement>(null);

  useGSAP(
    () => {
      gsap
        .timeline({ onComplete: onDone })
        .from(ref.current, { y: 12, opacity: 0, ...ENTER })
        .to(ref.current, { y: 6, opacity: 0, ...EXIT, delay: 2.6 });
    },
    { dependencies: [message], scope: ref, revertOnUpdate: true },
  );

  return (
    <div ref={ref} className="toast" role="status">
      {message}
    </div>
  );
}
