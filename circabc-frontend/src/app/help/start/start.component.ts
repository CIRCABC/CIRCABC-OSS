import { Component, OnInit } from '@angular/core';

import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { ActionEmitterResult } from 'app/action-result';
import {
  HelpCategory,
  HelpLink,
  HelpService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { AddHelpCategoryComponent } from 'app/help/add-help-category/add-help-category.component';
import { AddLinkComponent } from 'app/help/add-link/add-link.component';
import { FaqHighlightsComponent } from 'app/help/faq-highlights/faq-highlights.component';
import { HelpLinksComponent } from 'app/help/help-links/help-links.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-start',
  templateUrl: './start.component.html',
  styleUrl: './start.component.scss',
  imports: [
    HorizontalLoaderComponent,
    RouterLink,
    FaqHighlightsComponent,
    HelpLinksComponent,
    AddHelpCategoryComponent,
    AddLinkComponent,
    I18nPipe,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class StartComponent implements OnInit {
  public categories: HelpCategory[] = [];
  public links: HelpLink[] = [];
  public linkId = '';
  public loading = false;
  public showCreateModal = false;
  public showCreateLinkModal = false;

  constructor(
    private helpService: HelpService,
    private loginService: LoginService
  ) {}

  async ngOnInit() {
    this.loading = true;

    try {
      this.links = await firstValueFrom(this.helpService.getHelpLinks());
    } catch (error) {
      console.error(error);
    }

    try {
      this.categories = await firstValueFrom(
        this.helpService.getHelpCategories()
      );
    } catch (error) {
      console.error(error);
    }

    this.loading = false;
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

  public async refresh(_result: ActionEmitterResult) {
    this.loading = true;

    try {
      this.categories = await firstValueFrom(
        this.helpService.getHelpCategories()
      );
    } catch (error) {
      console.error(error);
    }

    this.loading = false;
  }

  public async refreshLinks(_result?: ActionEmitterResult) {
    this.loading = true;
    this.showCreateLinkModal = false;

    try {
      this.links = await firstValueFrom(this.helpService.getHelpLinks());
    } catch (error) {
      console.error(error);
    }

    this.loading = false;
  }

  public openForEdit(linkId: string) {
    if (linkId) {
      this.linkId = linkId;
      this.showCreateLinkModal = true;
    }
  }
}
