import { Component } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { environment } from 'environments/environment';

@Component({
  selector: 'cbc-ec-logo-app',
  templateUrl: './ec-logo-app.component.html',
  styleUrl: './ec-logo-app.component.scss',
})
export class EcLogoAppComponent {
  public circabcRelease = environment.circabcRelease;
  constructor(private translateService: TranslocoService) {}

  public getLang(): string {
    return this.translateService.getActiveLang().toLocaleLowerCase();
  }
}
