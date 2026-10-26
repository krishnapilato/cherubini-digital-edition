const toc = document.querySelector(".toc");
const links = [...toc.querySelectorAll("a")];
const sections = links.map((link) => document.querySelector(link.getAttribute("href")));
const clamp = (value) => Math.min(1, Math.max(0, value));

const highlight = (id) => {
  links.forEach((link) => {
    const current = link.getAttribute("href") === `#${id}`;
    link.classList.toggle("current", current);
    if (current && getComputedStyle(toc).position === "sticky") toc.scrollTo({ left: link.offsetLeft - 16, behavior: "smooth" });
  });
};

if (window.gsap && window.ScrollTrigger) {
  gsap.registerPlugin(ScrollTrigger);
  document.documentElement.classList.add("js");

  gsap.to(".progress span", { scaleX: 1, ease: "none", scrollTrigger: { start: 0, end: "max", scrub: 0.3 } });

  ScrollTrigger.create({
    trigger: ".hero",
    start: "bottom 40%",
    onToggle: (self) => toc.classList.toggle("visible", self.isActive || self.progress === 1),
    end: "max",
  });

  const scrubbed = (demo, apply, start = "top 85%", end = "center 45%") => {
    ScrollTrigger.create({
      trigger: demo,
      start,
      end,
      scrub: true,
      onUpdate: (self) => apply(self.progress),
      onRefresh: (self) => apply(self.progress),
    });
  };

  const switched = (demo) => {
    const target = demo.querySelector(".window");
    ScrollTrigger.create({
      trigger: demo,
      start: "center 62%",
      onUpdate: (self) => target.classList.toggle("on", self.progress > 0),
      onRefresh: (self) => target.classList.toggle("on", self.progress > 0),
    });
  };

  gsap.timeline({ defaults: { ease: "power3.out" } })
    .from(".hero-inner > *", { y: 34, opacity: 0, duration: 0.9, stagger: 0.08 })
    .from(".floater", { scale: 0.6, opacity: 0, duration: 0.8, stagger: 0.07, ease: "back.out(1.8)" }, 0.25)
    .from(".giant", { opacity: 0, y: 60, duration: 1.4, stagger: 0.1 }, 0);

  gsap.utils.toArray(".hero [data-speed]").forEach((element) => {
    gsap.to(element, {
      yPercent: -90 * Number(element.dataset.speed),
      ease: "none",
      scrollTrigger: { trigger: ".hero", start: "top top", end: "bottom top", scrub: true },
    });
  });

  gsap.to(".hero-inner", {
    yPercent: 30,
    opacity: 0,
    ease: "none",
    scrollTrigger: { trigger: ".hero", start: "top top", end: "bottom top", scrub: true },
  });

  if (matchMedia("(pointer: fine)").matches) {
    const movers = gsap.utils.toArray(".hero [data-depth]").map((element) => ({
      depth: Number(element.dataset.depth),
      x: gsap.quickTo(element, "x", { duration: 0.8, ease: "power3.out" }),
      y: gsap.quickTo(element, "y", { duration: 0.8, ease: "power3.out" }),
    }));
    document.querySelector(".hero").addEventListener("pointermove", (event) => {
      const dx = event.clientX / innerWidth - 0.5;
      const dy = event.clientY / innerHeight - 0.5;
      movers.forEach((mover) => {
        mover.x(dx * mover.depth * -28);
        mover.y(dy * mover.depth * -28);
      });
    });
  }

  gsap.utils.toArray(".numeral").forEach((numeral) => {
    gsap.fromTo(numeral, { yPercent: 40 }, {
      yPercent: -60,
      ease: "none",
      scrollTrigger: { trigger: numeral.parentElement, start: "top bottom", end: "bottom top", scrub: true },
    });
  });

  gsap.utils.toArray(".chapter").forEach((chapter, index) => {
    const fromSide = index % 2 === 0 ? 70 : -70;
    gsap.from(chapter.querySelectorAll(".text > *"), {
      y: 36,
      opacity: 0,
      duration: 0.8,
      stagger: 0.08,
      ease: "power3.out",
      scrollTrigger: { trigger: chapter, start: "top 72%", toggleActions: "play none none reverse" },
    });
    const demo = chapter.querySelector(".demo");
    gsap.from(demo, {
      x: fromSide,
      rotation: index % 2 === 0 ? 4 : -4,
      opacity: 0,
      duration: 1,
      ease: "power3.out",
      scrollTrigger: { trigger: chapter, start: "top 68%", toggleActions: "play none none reverse" },
    });
    gsap.fromTo(demo, { yPercent: 12 }, {
      yPercent: -12,
      ease: "none",
      scrollTrigger: { trigger: chapter, start: "top bottom", end: "bottom top", scrub: true },
    });
  });

  document.querySelectorAll("[data-demo]").forEach((demo) => {
    const kind = demo.dataset.demo;

    if (kind === "typing") {
      const typed = demo.querySelector(".typed");
      const text = typed.dataset.text;
      const count = demo.querySelector(".count");
      const rows = [...demo.querySelectorAll(".row")];
      scrubbed(demo, (progress) => {
        typed.textContent = text.slice(0, Math.round(clamp(progress / 0.55) * text.length));
        count.textContent = Math.round(clamp((progress - 0.5) / 0.4) * Number(count.dataset.to));
        rows.forEach((row, i) => row.classList.toggle("shown", progress > 0.55 + i * 0.1));
      });
    }

    if (kind === "toggle") switched(demo);

    if (kind === "occurrences") {
      const marks = [...demo.querySelectorAll("mark")];
      const index = demo.querySelector(".occ-index");
      const key = demo.querySelector(".press");
      let last = -1;
      scrubbed(demo, (progress) => {
        const current = Math.min(marks.length - 1, Math.floor(progress * marks.length));
        marks.forEach((mark, i) => mark.classList.toggle("current", i === current));
        index.textContent = current + 1;
        if (current !== last && last !== -1) {
          key.classList.add("down");
          setTimeout(() => key.classList.remove("down"), 140);
        }
        last = current;
      }, "top 75%", "bottom 35%");
    }

    if (kind === "tiles") {
      gsap.from(demo.querySelectorAll(".tile"), {
        scale: 0.4,
        opacity: 0,
        duration: 0.5,
        stagger: { each: 0.04, grid: [3, 4], from: "start" },
        ease: "back.out(2)",
        scrollTrigger: { trigger: demo, start: "top 75%", toggleActions: "play none none reverse" },
      });
    }

    if (kind === "position") {
      const position = demo.querySelector(".position");
      const fill = demo.querySelector(".nav-fill");
      const target = Number(position.dataset.to);
      scrubbed(demo, (progress) => {
        const value = 1 + Math.round(progress * (target - 1));
        position.textContent = value;
        gsap.set(fill, { scaleX: value / 224 });
      }, "top 80%", "bottom 40%");
    }

    if (kind === "chips") {
      gsap.from(demo.querySelectorAll(".chip"), {
        x: 60,
        opacity: 0,
        duration: 0.6,
        stagger: 0.09,
        ease: "power3.out",
        scrollTrigger: { trigger: demo, start: "top 75%", toggleActions: "play none none reverse" },
      });
    }

    if (kind === "print") {
      const rows = [...demo.querySelectorAll(".print-rows li")];
      const count = demo.querySelector(".print-count");
      scrubbed(demo, (progress) => {
        const checked = Math.min(rows.length, Math.floor(progress * (rows.length + 0.5)));
        rows.forEach((row, i) => row.classList.toggle("checked", i < checked));
        count.textContent = checked;
      });
    }

    if (kind === "abbreviations") {
      gsap.to(demo.querySelector(".soon-icon"), { y: -6, duration: 1.4, ease: "sine.inOut", repeat: -1, yoyo: true });
    }
  });

  gsap.matchMedia().add("(min-width: 900px)", () => {
    const track = document.querySelector(".keys-track");
    const distance = () => track.scrollWidth - document.querySelector(".keys-viewport").clientWidth;
    gsap.to(track, {
      x: () => -distance(),
      ease: "none",
      scrollTrigger: {
        trigger: ".keys",
        start: "center center",
        end: () => `+=${distance()}`,
        pin: true,
        scrub: 0.6,
        invalidateOnRefresh: true,
      },
    });
    gsap.from(".key-card:not(.intro) kbd", {
      y: -20,
      opacity: 0,
      duration: 0.5,
      stagger: 0.05,
      ease: "back.out(2)",
      scrollTrigger: { trigger: ".keys", start: "top 70%", toggleActions: "play none none reverse" },
    });
  });

  gsap.matchMedia().add("(max-width: 899px)", () => {
    gsap.utils.toArray(".key-card").forEach((card) => {
      gsap.from(card, {
        y: 40,
        opacity: 0,
        duration: 0.7,
        ease: "power3.out",
        scrollTrigger: { trigger: card, start: "top 88%", toggleActions: "play none none reverse" },
      });
    });
  });

  sections.forEach((section) => {
    ScrollTrigger.create({
      trigger: section,
      start: "top 50%",
      end: "bottom 50%",
      onToggle: (self) => self.isActive && highlight(section.id),
    });
  });

  addEventListener("load", () => ScrollTrigger.refresh());
} else {
  highlight(sections[0].id);
}
