import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  output,
} from '@angular/core';

import { MatDialog } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  CategoryService,
  GroupCreationRequest,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';
import { AcceptFormComponent } from './accept-form/accept-form.component';
import { DeclineFormComponent } from './decline-form/decline-form.component';

/**
 * Presentational component that renders a single interest-group creation
 * request within a category's request list.
 *
 * It displays the details of one {@link GroupCreationRequest} (requester
 * information via {@link UserCardComponent}, request metadata, dates, etc.)
 * and exposes actions to accept or reject the request. Accepting or rejecting
 * opens the corresponding modal dialog ({@link AcceptFormComponent} or
 * {@link DeclineFormComponent}); when the dialog completes successfully the
 * component notifies its parent so the request list can be refreshed.
 */
@Component({
  selector: 'cbc-group-request-create-element',
  templateUrl: './group-request-create-element.component.html',
  styleUrl: './group-request-create-element.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    UserCardComponent,
    RouterLink,
    MatTooltipModule,
    DatePipe,
    I18nPipe,
    TranslocoModule,
  ],
})
export class GroupRequestCreateElementComponent {
  /**
   * Required input holding the group creation request to display. Extends the
   * generated {@link GroupCreationRequest} with an optional `interestGroupId`
   * identifying the associated interest group when available.
   */
  readonly request = input.required<
    GroupCreationRequest & { interestGroupId?: string }
  >();

  /** Required input with the identifier of the category the request belongs to. */
  readonly categoryId = input.required<string>();

  /**
   * Emitted after a request has been successfully accepted or rejected,
   * signalling the parent component to reload the list of group requests.
   */
  readonly reloadGroupRequests = output();

  /** Generated API client used for category-related operations. */
  categoryService = inject(CategoryService);

  /** Angular Material dialog service used to open the accept/decline modals. */
  dialog = inject(MatDialog);

  /**
   * Opens the {@link AcceptFormComponent} dialog for the current request,
   * passing the request and category identifier as dialog data. When the
   * dialog closes with a truthy result, emits {@link reloadGroupRequests} to
   * trigger a refresh of the request list.
   *
   * @returns A promise that resolves once the dialog has been opened.
   */
  public async acceptDialog() {
    const dialogRef = this.dialog.open(AcceptFormComponent, {
      ariaLabel: 'Dialog',
      data: {
        request: this.request(),
        categoryId: this.categoryId(),
      },
    });

    dialogRef.afterClosed().subscribe((response) => {
      if (response) {
        this.reloadGroupRequests.emit();
      }
    });
  }

  /**
   * Opens the {@link DeclineFormComponent} dialog for the current request,
   * passing the request and category identifier as dialog data. When the
   * dialog closes with a truthy result, emits {@link reloadGroupRequests} to
   * trigger a refresh of the request list.
   *
   * @returns A promise that resolves once the dialog has been opened.
   */
  public async rejectDialog() {
    const dialogRef = this.dialog.open(DeclineFormComponent, {
      ariaLabel: 'Dialog',
      data: {
        request: this.request(),
        categoryId: this.categoryId(),
      },
    });

    dialogRef.afterClosed().subscribe((response) => {
      if (response) {
        this.reloadGroupRequests.emit();
      }
    });
  }
}
