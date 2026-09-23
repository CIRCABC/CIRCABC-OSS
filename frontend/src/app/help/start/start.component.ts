import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  resource,
} from '@angular/core';

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

/**
 * Landing page component for the Help section (`cbc-start`).
 *
 * Renders the Help home page, which displays the list of help links,
 * FAQ highlights and the available help categories. For users with
 * administrative privileges it also exposes controls for creating new
 * help categories and help links (via {@link AddHelpCategoryComponent}
 * and {@link AddLinkComponent}) and for editing existing links.
 *
 * The help links and categories are loaded from the backend
 * {@link HelpService} via `resource()`. Administrative visibility is
 * derived from the {@link LoginService}.
 */
@Component({
  selector: 'cbc-start',
  templateUrl: './start.component.html',
  styleUrl: './start.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
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
export class StartComponent {
  /** Backend service used to fetch help links and help categories. */
  private readonly helpService = inject(HelpService);
  /** Service used to determine the current user and their permissions. */
  private readonly loginService = inject(LoginService);

  /**
   * Loads the help links from the backend. Errors are caught and logged so
   * that a failure to load links does not prevent categories from loading.
   */
  private readonly linksResource = resource({
    loader: async () => {
      try {
        return await this.helpService.getHelpLinksAsync();
      } catch (error) {
        console.error(error);
        return [];
      }
    },
    defaultValue: [] as HelpLink[],
  });

  /**
   * Loads the help categories from the backend. Errors are caught and
   * logged so that a failure to load categories does not prevent links
   * from loading.
   */
  private readonly categoriesResource = resource({
    loader: async () => {
      try {
        return await this.helpService.getHelpCategoriesAsync();
      } catch (error) {
        console.error(error);
        return [];
      }
    },
    defaultValue: [] as HelpCategory[],
  });

  /** Help categories currently displayed on the page. */
  public readonly categories = this.categoriesResource.value;
  /** Help links currently displayed on the page. */
  public readonly links = this.linksResource.value;
  /** Identifier of the help link currently selected for editing. */
  public linkId = '';
  /** Whether an asynchronous load operation is in progress. */
  public readonly loading = computed(
    () => this.linksResource.isLoading() || this.categoriesResource.isLoading()
  );
  /** Whether the "create help category" modal is visible. */
  public showCreateModal = false;
  /** Whether the "create/edit help link" modal is visible. */
  public showCreateLinkModal = false;

  /**
   * Determines whether the current user may see administrative controls
   * (creating help categories and links).
   *
   * @returns `true` if the user is authenticated and has the `isAdmin` or
   * `isCircabcAdmin` property set; `false` for guests or non-admin users.
   */
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

  /**
   * Reloads the help categories from the backend, typically invoked after
   * a category has been created or modified.
   *
   * @param _result The result emitted by the triggering action (unused).
   */
  public refresh(_result: ActionEmitterResult) {
    this.categoriesResource.reload();
  }

  /**
   * Closes the create/edit link modal and reloads the help links from the
   * backend, typically invoked after a link has been created or modified.
   *
   * @param _result The optional result emitted by the triggering action
   * (unused).
   */
  public refreshLinks(_result?: ActionEmitterResult) {
    this.showCreateLinkModal = false;
    this.linksResource.reload();
  }

  /**
   * Opens the link modal in edit mode for the given help link.
   *
   * @param linkId Identifier of the help link to edit. When falsy, no
   * action is taken and the modal remains closed.
   */
  public openForEdit(linkId: string) {
    if (linkId) {
      this.linkId = linkId;
      this.showCreateLinkModal = true;
    }
  }
}
