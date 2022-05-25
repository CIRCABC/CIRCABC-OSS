import { ApplicationRef, enableProdMode } from '@angular/core';
import { enableDebugTools } from '@angular/platform-browser';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { AppModule } from 'app/app.module';
import { environment } from 'environments/environment';

if (environment.production) {
  enableProdMode();
}
// eslint-disable-next-line @typescript-eslint/no-floating-promises
platformBrowserDynamic()
  .bootstrapModule(AppModule)
  .then((module) => {
    if (!environment.production) {
      const applicationRef = module.injector.get(ApplicationRef);
      const appComponent = applicationRef.components[0];
      enableDebugTools(appComponent);
    }
  });
