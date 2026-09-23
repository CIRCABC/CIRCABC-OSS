import { enableProdMode, enableProfiling } from '@angular/core';
import {
  bootstrapApplication,
  enableDebugTools,
} from '@angular/platform-browser';
import { AppComponent } from 'app/app.component';
import { createAppConfig } from 'app/app.config';
import { loadRuntimeConfig } from 'app/core/init/runtime-config';
import { environment } from 'environments/environment';

/**
 * Application entry point.
 *
 * Orchestration only — provider configuration lives in {@link createAppConfig}
 * and initializer logic in `app/core/init/*`. The order matters:
 * runtime configuration is loaded first so that `createAppConfig()` (and the
 * environment-derived injection tokens it declares) observe the merged values.
 */
(async () => {
  await loadRuntimeConfig();

  if (environment.production) {
    enableProdMode();
  } else {
    enableProfiling();
  }

  const appRef = await bootstrapApplication(AppComponent, createAppConfig());

  if (!environment.production) {
    // `bootstrapApplication` resolves to the ApplicationRef; use its root
    // component to wire up the Angular debug tools in development.
    enableDebugTools(appRef.components[0]);
  }
})();
