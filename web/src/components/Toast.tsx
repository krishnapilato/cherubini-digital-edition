import { useGSAP } from "@gsap/react";
import gsap from "gsap";
import { useRef } from "react";

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
        .from(ref.current, { y: 16, opacity: 0, duration: 0.3, ease: "power3.out" })
        .to(ref.current, { y: 8, opacity: 0, duration: 0.3, delay: 2.6 });
    },
    { dependencies: [message], scope: ref, revertOnUpdate: true },
  );

  return (
    <div ref={ref} className="toast" role="status">
      {message}
    </div>
  );
}
