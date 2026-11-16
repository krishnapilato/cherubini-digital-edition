import { expect, it } from "vitest";
import pom from "../../desktop/pom.xml?raw";
import { version } from "../package.json";

// The two editions are released together: a version changed in only one file would show two
// different numbers in the two footers.
it("the web and desktop editions have the same version", () => {
  const desktop = /<artifactId>cherubini-vocabulary<\/artifactId>\s*<version>([^<]+)<\/version>/.exec(pom)?.[1];
  expect(desktop).toBe(version);
});
