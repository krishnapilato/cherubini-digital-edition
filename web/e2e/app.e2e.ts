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

test.afterEach(() => {
  expect(problems).toEqual([]);
});

test("searches the definitions while typing", async ({ page }) => {
  await search(page).fill("ferro");
  await expect(page.getByRole("button", { name: "Definizioni 97" })).toHaveAttribute("aria-pressed", "true");
  await expect(page.getByRole("button", { name: "Lemmi 0" })).toBeDisabled();
  await expect(reading(page).getByRole("heading", { name: "Anèll" })).toBeVisible();
});

test("browses a letter", async ({ page }) => {
  await page.getByRole("button", { name: "J: 1 lemmi" }).click();
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
  await expect(page.frameLocator("iframe[title=\"Guida all'uso\"]").getByRole("heading", { name: "Vocabolario milanese–italiano" })).toBeVisible();
  await page.keyboard.press("Escape");
  await expect(guide).toBeHidden();
  await expect(page.getByRole("button", { name: "Guida all'uso" })).toBeFocused();
});
