# Contributing to CIRCABC

Thank you for your interest in contributing to CIRCABC! This guide explains how
to set up your environment, run the tests, and submit changes.

## Code of conduct

Be respectful and constructive. Assume good intent, and keep discussions focused
on technical merit.

## Getting started

1. Fork the repository and clone your fork.
2. Install the pinned tool versions with [mise](https://mise.jdx.dev/):
   ```bash
   (cd backend && mise install)
   (cd frontend && mise install)
   (cd eu-captcha && mise install)
   ```
3. Bring up the local stack for manual testing — see
   [`docker-test/README.md`](./docker-test/README.md).

## Development workflow

- Create a topic branch from `master`:
  ```bash
  git checkout -b feature/short-description
  ```
- Make focused commits with clear messages.
- Keep changes scoped to a single concern per pull request.

## Building and testing

Run the relevant checks for the area you touched **before** opening a pull
request.

### Backend / EU Captcha (Java)
```bash
cd backend      # or eu-captcha
mvn clean verify
```

### Frontend (Angular)
```bash
cd frontend
npm install
npm run tsc            # type-check
npm run lint           # ESLint
npm run lint-biome     # Biome
npm run stylelint      # styles
npm run format-check   # formatting
npm test               # unit tests
```

### End-to-end (Playwright)
```bash
cd e2e
npm install
npx playwright install --with-deps
npm test
```

Accessibility matters: `e2e` includes axe-core audits (`npm run test:a11y`).
Please do not introduce accessibility regressions.

## Pull requests

- Ensure the build and tests pass locally.
- Describe **what** changed and **why**, and how you tested it.
- Reference any related issue.
- Keep the diff minimal and consistent with the surrounding code style.
- Do not include secrets, credentials, or internal infrastructure details.

## Reporting security issues

Please do not open public issues for security vulnerabilities. Instead, report
them privately to the maintainers so they can be addressed responsibly.

## License

By contributing, you agree that your contributions are licensed under the
EUPL-1.1, the same license as the project.
