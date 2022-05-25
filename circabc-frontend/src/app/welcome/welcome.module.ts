import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared/shared.module';
import { WelcomeRoutingModule } from 'app/welcome/welcome-routing.module';
import { WelcomeComponent } from 'app/welcome/welcome.component';
import { CookieService } from 'ngx-cookie-service';

@NgModule({
  imports: [SharedModule, WelcomeRoutingModule],
  exports: [],
  declarations: [WelcomeComponent],
  providers: [CookieService],
})
export class WelcomeModule {}
