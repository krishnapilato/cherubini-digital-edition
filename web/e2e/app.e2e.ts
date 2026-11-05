import { expect, test, type Page } from "@playwright/test";

const search = (page: Page) => page.getByRole("searchbox", { name: "Cerca nel vocabolario" });
const reading = (page: Page) => page.getByRole("article", { name: "Voce" });

// Every test also fails if the page logs an error or a warning.
let problems: string[] = [];

test.beforeEach(async ({ page }) => {
  problems = [];
  page.on("console", (message) => {
    if (message.type() === "error" || message.type() === "warning") problems.push(message.text());
  });
  page.on("pageerror", (error) => problems.push(error.message));
  await page.goto("./");
  await expect(search(page)).toBeVisible();
});

test.afterEach(() => expect(problems).toEqual([]));

test("searches the definitions while typing", async ({ page }) => {
  await search(page).fill("ferro");
  await expect(page.getByRole("button", { name: "Definizioni 97" })).toHaveAttribute("aria-pressed", "true");
  await expect(page.getByRole("button", { name: "Lemmi 0" })).toBeDisabled();
  await expect(reading(page).getByRole("heading", { name: "Anèll" })).toBeVisible();
});

test("parts of a word already find results while typing", async ({ page }) => {
  await search(page).fill("tel");
  await expect(reading(page).getByRole("heading")).toBeVisible();
});

test("with no results the list offers contained words and the entry hides its commands", async ({ page }) => {
  await search(page).fill("tel");
  await page.getByRole("switch").click();
  await expect(reading(page).getByText("Nessuna voce da mostrare.")).toBeVisible();
  await expect(reading(page).getByRole("button")).toHaveCount(0);
  await page.getByRole("button", { name: "Attiva «Parole contenute»" }).click();
  await expect(reading(page).getByRole("heading")).toBeVisible();
});

test("Back goes from the results to the start page", async ({ page }) => {
  await search(page).fill("ferro");
  await expect(reading(page).getByRole("heading", { name: "Anèll" })).toBeVisible();
  await page.goBack();
  await expect(page.getByRole("heading", { name: "Vocabolario milanese–italiano" })).toBeVisible();
  await expect(search(page)).toHaveValue("");
});

test.describe("on a phone", () => {
  test.use({ viewport: { width: 375, height: 812 } });

  test("an entry takes the screen and Back returns to the list, then to the start page", async ({ page }) => {
    await search(page).fill("ferro");
    await page.getByRole("button", { name: /^Anèll/ }).click();
    await expect(reading(page).getByRole("button", { name: "Torna all'elenco" })).toBeVisible();
    await expect(search(page)).toBeHidden();
    await expect(reading(page).getByRole("button", { name: "Prima voce" })).toBeHidden();
    await page.goBack();
    await expect(page.getByRole("button", { name: /^Anèll/ })).toBeVisible();
    await expect(search(page)).toHaveValue("ferro");
    await page.goBack();
    await expect(page.getByRole("heading", { name: "Vocabolario milanese–italiano" })).toBeVisible();
  });

  test("the back arrow of an entry returns to the list", async ({ page }) => {
    await search(page).fill("ferro");
    await page.getByRole("button", { name: /^Anèll/ }).click();
    await reading(page).getByRole("button", { name: "Torna all'elenco" }).click();
    await expect(page.getByRole("button", { name: /^Anèll/ })).toBeVisible();
  });

  test("the page does not scroll sideways and the controls are large enough to touch", async ({ page }) => {
    await search(page).fill("ferro");
    await expect(page.getByRole("button", { name: /^Anèll/ })).toBeVisible();
    for (const control of await page.locator(".topbar button, .row-check, .segmented button:not(:disabled)").all()) {
      const box = await control.boundingBox();
      expect(Math.min(box?.width ?? 0, box?.height ?? 0)).toBeGreaterThanOrEqual(44);
    }
    expect(await page.evaluate("document.documentElement.scrollWidth")).toBe(375);
  });
});

test("browses a letter", async ({ page }) => {
  await page.getByRole("button", { name: "J: 1 lemma" }).click();
  await expect(page.getByRole("heading", { name: "J", exact: true })).toBeVisible();
  await expect(reading(page).getByRole("heading", { name: "Jùre(De)" })).toBeVisible();
});

test("the arrows move between entries from anywhere on the page", async ({ page }) => {
  await page.getByRole("button", { name: "A: 131 lemmi" }).click();
  await expect(reading(page).getByRole("heading", { name: "Abàcch" })).toBeVisible();
  await reading(page).getByRole("heading", { name: "Abàcch" }).click();
  await page.keyboard.press("ArrowDown");
  await expect(reading(page).getByRole("heading", { name: "Abachin" })).toBeVisible();
  await page.keyboard.press("ArrowUp");
  await expect(reading(page).getByRole("heading", { name: "Abàcch" })).toBeVisible();
});

test("F3 goes to the next occurrence and then to the next entry", async ({ page }) => {
  await search(page).fill("ferro");
  await expect(reading(page).getByRole("heading", { name: "Anèll" })).toBeVisible();
  // Anèll has a single occurrence: the first F3 reaches it, the second moves on.
  await page.keyboard.press("F3");
  await expect(reading(page).getByRole("heading", { name: "Anèll" })).toBeVisible();
  await page.keyboard.press("F3");
  await expect(reading(page).getByRole("heading", { name: "Bocchètta" })).toBeVisible();
});

test("a reload keeps the search, and closing the tab forgets it", async ({ page, context }) => {
  await search(page).fill("ferro");
  await expect(reading(page).getByRole("heading", { name: "Anèll" })).toBeVisible();
  await page.reload();
  await expect(search(page)).toHaveValue("ferro");
  await expect(reading(page).getByRole("heading", { name: "Anèll" })).toBeVisible();

  const tab = await context.newPage();
  await tab.goto("./");
  await expect(search(tab)).toHaveValue("");
});

test("Enter saves a recent search, which can be removed one by one", async ({ page }) => {
  await search(page).fill("ferro");
  await search(page).press("Enter");
  await page.getByRole("button", { name: "Pagina iniziale" }).click();
  const recent = page.getByRole("group", { name: "Ricerche recenti" });
  await expect(recent.getByRole("button", { name: "ferro", exact: true })).toBeVisible();
  await recent.getByRole("button", { name: "Togli «ferro» dalle recenti" }).click();
  await expect(recent).toBeHidden();
});

test("the print panel and the guide open and close with Esc", async ({ page }) => {
  await page.getByRole("button", { name: "Stampa" }).click();
  await expect(page.getByRole("dialog", { name: "Stampa" })).toBeVisible();
  await page.keyboard.press("Escape");
  await expect(page.getByRole("dialog", { name: "Stampa" })).toBeHidden();

  await page.getByRole("button", { name: "Guida all'uso" }).click();
  const guide = page.getByRole("dialog", { name: "Guida all'uso" });
  await expect(guide).toBeVisible();
  await expect(
    page.frameLocator('iframe[title="Guida all\'uso"]').getByRole("heading", { name: "Vocabolario milanese–italiano" }),
  ).toBeVisible();
  await page.keyboard.press("Escape");
  await expect(guide).toBeHidden();
  await expect(page.getByRole("button", { name: "Guida all'uso" })).toBeFocused();
});
