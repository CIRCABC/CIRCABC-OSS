
## Cypress End-to-End Tests

This project includes Cypress-based end-to-end tests that run as part of the CI/CD pipeline. You can also run them locally, in a Docker-based OSS environment, or in a custom environment.

---

### 1. CI/CD Integration (Default)

These tests are automatically executed during the CI/CD process to verify application functionality using the default `cypress.config.ts` configuration file.

No manual steps are required — Cypress will run with the predefined test suite and environment.

---

### 2. Run with OSS Docker Environment

To run the tests with a Docker-based OSS deployment:

1. Make sure your OSS application is running and accessible at the configured host (localhost) and port (80). 

2. Use the custom Cypress configuration file for the OSS environment:

     ```bash
     npm run cy:run-oss
     ```

3. This command uses `cypress.oss.config.ts` config file which includes docker oss environment-specific overrides such as:

   * `host`, `port`, and `path`
   * `circabc_admin_create: false` (to skip admin creation if not needed)

---

### 3. Run in a Custom Environment (By Modifying Default Config)

If you want to run Cypress in another environment (e.g., staging or QA), you can modify the default configuration file `cypress.config.ts`:

#### Example:

```ts
export default defineConfig({
[ ... ]
  env: {
    host: 'your-custom-host',       // e.g., 'staging.example.com'
    port: 8080,                     // Replace with your app's port
    path: '/base/path',             // Leave empty if root
    circabc_admin_create: false,    // Skip admin creation if needed
[ ... ]
    'admin.username': 'admin',
    'admin.password': 'admin',
    'circabc.admin.username': 'circabc_admin',
    'circabc.admin.password': 'password',
[ ... ]
  }
   e2e: {
    [ ... ] 
        baseUrl: 'http://localhost:4200/ui', // set your frontend url
    },
});
   
```

