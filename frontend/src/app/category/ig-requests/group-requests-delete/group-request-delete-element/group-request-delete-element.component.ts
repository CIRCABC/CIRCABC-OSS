import { DatePipe, I18nSelectPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  output,
} from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import type { GroupDeletionRequest } from 'app/core/generated/circabc';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';
import { AcceptDeleteFormComponent } from './accept-delete-form/delete-form.component';
import { DeclineDeleteFormComponent } from './decline-delete-form/decline-delete-form.component';

/**
 * Presentational component that renders a single interest-group deletion
 * request within the category deletion-requests list.
 *
 * It displays the details of one {@link GroupDeletionRequest} (requester
 * information via {@link UserCardComponent}, request date, and message) and
 * provides the moderator actions to accept or reject the request. Accepting
 * opens the {@link AcceptDeleteFormComponent} dialog and rejecting opens the
 * {@link DeclineDeleteFormComponent} dialog; when either dialog completes
 * successfully the component emits {@link reloadGroupRequests} so the parent
 * can refresh the list.
 */
@Component({
  selector: 'cbc-group-request-delete-element',
  templateUrl: './group-request-delete-element.component.html',
  styleUrl: './group-request-delete-element.component.scss',
  providers: [I18nSelectPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    TranslocoModule,
    UserCardComponent,
    DatePipe,
  ],
})
export class GroupRequestDeleteElementComponent {
  /** Required input holding the group deletion request to display and act upon. */
  readonly request = input.required<GroupDeletionRequest>();
  /**
   * Required input with the identifier of the category the request belongs to,
   * forwarded to the accept/reject dialogs. May be `undefined` when unknown.
   */
  readonly categoryId = input.required<string | undefined>();
  /**
   * Output emitted after a request has been successfully accepted or rejected,
   * signalling the parent that the list of group requests should be reloaded.
   */
  readonly reloadGroupRequests = output();
  /** Angular Material dialog service used to open the accept/reject dialogs. */
  dialog = inject(MatDialog);

  /**
   * Opens the accept-deletion dialog for the current request. If the dialog
   * closes with a truthy response (the request was accepted), emits
   * {@link reloadGroupRequests}.
   *
   * @returns A promise that resolves once the dialog has been opened.
   */
  public async acceptDialog() {
    const dialogRef = this.dialog.open(AcceptDeleteFormComponent, {
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
   * Opens the decline-deletion dialog for the current request. If the dialog
   * closes with a truthy response (the request was rejected), emits
   * {@link reloadGroupRequests}.
   *
   * @returns A promise that resolves once the dialog has been opened.
   */
  public async rejectDialog() {
    const dialogRef = this.dialog.open(DeclineDeleteFormComponent, {
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
   * Replaces non-breaking space HTML entities (`&nbsp;`) with regular spaces
   * so the request text can be rendered as plain text.
   *
   * @param text - The raw text that may contain `&nbsp;` entities.
   * @returns The cleaned text, or an empty string when no text is provided.
   */
  cleanSpace(text?: string) {
    if (text) {
      return text.replaceAll('&nbsp;', ' ');
    }
    return '';
  }
}
