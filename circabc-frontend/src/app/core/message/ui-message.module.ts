import { CommonModule } from '@angular/common';
import { NgModule, Optional, SkipSelf } from '@angular/core';
import { RouterModule } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { UiMessageRendererComponent } from 'app/core/message/ui-message.renderer.component';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { UiMessageSystemComponent } from 'app/core/message/ui-message.system.component';

@NgModule({
  imports: [
    CommonModule,
    TranslocoModule,
    RouterModule,
    UiMessageSystemComponent,
    UiMessageRendererComponent,
  ],
  exports: [UiMessageSystemComponent, UiMessageRendererComponent],
  providers: [UiMessageService],
})
export class UiMessageModule {
  constructor(@Optional() @SkipSelf() parentModule?: UiMessageModule) {
    if (parentModule) {
      throw new Error(
        'UiMessageModule is already loaded. Import it in the AppModule only'
      );
    }
  }
}
