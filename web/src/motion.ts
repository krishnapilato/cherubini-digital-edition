// One way of moving for the whole app: soft at both ends, steady in between, never overshooting.
// Things come in a little slower than they go away.
export const ENTER = { duration: 0.5, ease: "sine.inOut" } satisfies gsap.TweenVars;
export const EXIT = { duration: 0.3, ease: "sine.inOut" } satisfies gsap.TweenVars;
