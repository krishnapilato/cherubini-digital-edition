import { registerHooks } from "node:module";

// typescript-eslint cannot load TypeScript 7 yet. For linting only, every import of
// "typescript" is redirected to the TypeScript 6 API; type-checking and builds still use 7.
registerHooks({
  resolve: (specifier, context, next) => next(specifier.replace(/^typescript(?=$|\/)/, "@typescript/old"), context),
});

const [{ default: js }, { default: jsxA11y }, { default: reactHooks }, { default: reactRefresh }, { default: sonarjs }, { defineConfig, globalIgnores }, { default: globals }, { default: tseslint }] =
  await Promise.all([
    import("@eslint/js"),
    import("eslint-plugin-jsx-a11y"),
    import("eslint-plugin-react-hooks"),
    import("eslint-plugin-react-refresh"),
    import("eslint-plugin-sonarjs"),
    import("eslint/config"),
    import("globals"),
    import("typescript-eslint"),
  ]);

export default defineConfig([
  globalIgnores(["dist", "public"]),
  {
    linterOptions: { reportUnusedDisableDirectives: "error" },
  },
  {
    files: ["**/*.{ts,tsx}"],
    extends: [
      js.configs.recommended,
      tseslint.configs.strictTypeChecked,
      tseslint.configs.stylisticTypeChecked,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
      jsxA11y.flatConfigs.strict,
      sonarjs.configs.recommended,
    ],
    languageOptions: {
      ecmaVersion: "latest",
      globals: globals.browser,
      parserOptions: { projectService: true, tsconfigRootDir: import.meta.dirname },
    },
    rules: {
      "@typescript-eslint/no-unused-vars": "error",
      "@typescript-eslint/consistent-type-imports": "error",
      "@typescript-eslint/no-confusing-void-expression": ["error", { ignoreArrowShorthand: true }],
      "@typescript-eslint/restrict-template-expressions": ["error", { allowNumber: true }],
      // Only relevant to the React Compiler, which this project does not use.
      "react-hooks/incompatible-library": "off",
    },
  },
]);
