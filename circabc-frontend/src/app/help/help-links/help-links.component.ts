import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { HelpLink, HelpService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { assertDefined } from 'app/core/asserts';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-help-links',
  templateUrl: './help-links.component.html',
  styleUrls: ['./help-links.component.scss'],
})
export class HelpLinksComponent {
  @Input()
  links: HelpLink[] = [];
  @Output()
  readonly linkDeleted = new EventEmitter();
  @Output()
  readonly clickedForEdition = new EventEmitter<string>();

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
    this.clickedForEdition.emit(link.id);
  }

  public getTarget(link: string | undefined) {
    if (link && link.indexOf(window.location.hostname) !== -1) {
      return '_self';
    } else {
      return '_blank';
    }
  }
}
