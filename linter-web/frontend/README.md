# Linter Web Frontend

This module contains the React and Vite frontend scaffold. The current page is
the starter interface and is not yet connected to a linting API.

## Requirements

Node.js 22 and npm, matching the frontend CI configuration.

## Local development

From the repository root:

```bash
cd linter-web/frontend
npm ci
npm run dev
```

Open the local URL printed by Vite. Application code lives in `src/`, with
`src/App.jsx` defining the current page.

## Available commands

Run these commands from `linter-web/frontend` after installing dependencies:

| Command | Purpose |
| --- | --- |
| `npm run dev` | Start the Vite development server |
| `npm run lint` | Run Oxlint |
| `npm run build` | Generate the production build in `dist/` |
| `npm run preview` | Preview the production build locally after building |

No frontend test script is configured yet. Linting and a successful build do
not replace tests of user behaviour.

## Automated checks

GitHub Actions runs lint and build as separate jobs using Node.js 22 and
`npm ci`. The CodeQL workflow is configured to scan JavaScript/TypeScript for
potential security issues.

The root Maven build does not build this frontend. See the
[main README](../../README.md) for the complete development workflow.
