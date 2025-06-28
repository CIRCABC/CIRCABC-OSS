import { Component, Input, output } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { assertDefined } from 'app/core/asserts';
import { HelpLink, HelpService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-help-links',
  templateUrl: './help-links.component.html',
  styleUrl: './help-links.component.scss',
  imports: [RouterLink, InlineDeleteComponent, I18nPipe, TranslocoModule],
})
export class HelpLinksComponent {
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  links: HelpLink[] = [];
  readonly linkDeleted = output();
  readonly clickedForEdition = output<string>();

  constructor(
    private sanitizer: DomSanitizer,
    private helpService: HelpService,
    private loginService: LoginService
  ) {}

  public sanitizeLinkRef(href: string | undefined) {
    assertDefined(href);
    return this.sanitizer.bypassSecurityTrustUrl(href);
  }

  public async deleteLink(link: HelpLink) {
    if (link.id) {
      try {
        await firstValueFrom(this.helpService.removeHelpLink(link.id));
        this.linkDeleted.emit();
      } catch (error) {
        console.error(error);
      }
    }
  }

  public isAdminOrSupport(): boolean {
    if (!this.loginService.isGuest()) {
      const user = this.loginService.getUser();
      return (
        user.properties !== undefined &&
        (user.properties.isAdmin === 'true' ||
          user.properties.isCircabcAdmin === 'true')
      );
    }

    return false;
  }

  public edit(link: HelpLink) {
    this.clickedForEdition.emit(link.id as string);
  }

  public getTarget(link: string | undefined) {
    if (link && link.indexOf(window.location.hostname) !== -1) {
      return '_self';
    }
    return '_blank';
  }
}
