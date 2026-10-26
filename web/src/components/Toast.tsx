import { useGSAP } from "@gsap/react";
import gsap from "gsap";
import { useRef } from "react";

export function Toast({ message, onDone }: { message: string; onDone: () => void }) {
  const ref = useRef<HTMLDivElement>(null);

  useGSAP(
    () => {
      gsap
        .timeline({ onComplete: onDone })
        .from(ref.current, { y: 16, opacity: 0, duration: 0.3, ease: "power3.out" })
        .to(ref.current, { y: 8, opacity: 0, duration: 0.3, delay: 2.6 });
    },
    { dependencies: [message], scope: ref },
  );

  return (
    <div ref={ref} className="toast" role="status">
      {message}
    </div>
  );
}
