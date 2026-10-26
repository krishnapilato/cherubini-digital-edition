import type { ReactNode } from "react";
import type { Range } from "../search";

type Props = { text: string; ranges: Range[]; current?: number };

export function Highlight({ text, ranges, current = -1 }: Props) {
  const parts: ReactNode[] = [];
  let cursor = 0;
  ranges.forEach((range, index) => {
    if (range.start > cursor) parts.push(text.slice(cursor, range.start));
    parts.push(
      <mark key={range.start} className={index === current ? "hit current" : "hit"}>
        {text.slice(range.start, range.end)}
      </mark>,
    );
    cursor = range.end;
  });
  if (cursor < text.length) parts.push(text.slice(cursor));
  return <>{parts}</>;
}

export function Snippet({ text, ranges, length = 110 }: { text: string; ranges: Range[]; length?: number }) {
  const start = ranges.length ? Math.max(0, ranges[0].start - 24) : 0;
  const end = Math.min(text.length, start + length);
  const visible = ranges
    .filter((range) => range.start >= start && range.end <= end)
    .map((range) => ({ start: range.start - start, end: range.end - start }));
  return (
    <>
      {start > 0 && "…"}
      <Highlight text={text.slice(start, end)} ranges={visible} />
      {end < text.length && "…"}
    </>
  );
}
