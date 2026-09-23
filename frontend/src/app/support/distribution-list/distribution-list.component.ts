import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import {
  AppMessageService,
  DistributionMail,
} from 'app/core/generated/circabc';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { NumberBadgeComponent } from 'app/shared/number-badge/number-badge.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import {
  SelectableDistributionMail,
  SelectablePagedDistributionMails,
} from 'app/support/distribution-list/selectable-paged-distribution-mails';
import { saveAs } from 'file-saver';
import { AddDistributionEmailComponent } from './add-distribution-email/add-distribution-email.component';

/**
 * Support-area component that renders the platform distribution mailing list.
 *
 * It displays a paginated, selectable table of {@link DistributionMail}
 * entries retrieved from the backend through {@link AppMessageService}. From
 * the rendered view users can page through the list, unsubscribe individual
 * emails or a selection of emails, select/deselect all rows, add a new
 * distribution email (via the embedded {@link AddDistributionEmailComponent}
 * modal) and export the full list as an XLSX file.
 *
 * Key collaborators:
 * - {@link AppMessageService}: backend API for reading, deleting and exporting
 *   distribution emails.
 * - {@link PagerComponent}, {@link NumberBadgeComponent},
 *   {@link InlineDeleteComponent}, {@link SpinnerComponent} and
 *   {@link AddDistributionEmailComponent}: child UI components used by the
 *   template.
 */
@Component({
  selector: 'cbc-distribution-list',
  templateUrl: './distribution-list.component.html',
  styleUrl: './distribution-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    PagerComponent,
    NumberBadgeComponent,
    InlineDeleteComponent,
    SpinnerComponent,
    AddDistributionEmailComponent,
    TranslocoModule,
  ],
})
export class DistributionListComponent implements OnInit {
  /** Backend API client used to load, delete and export distribution emails. */
  private readonly appMessageService = inject(AppMessageService);

  /**
   * Current page of distribution emails together with the total count, each
   * row augmented with a `selected` flag for row selection.
   */
  public readonly distributionEmails = signal<SelectablePagedDistributionMails>(
    {
      data: [],
      total: 0,
    }
  );
  /** Paging and sorting options driving {@link loadDistributionEmails}. */
  public readonly listingOptions = signal<ListingOptions>({
    page: 1,
    limit: 25,
    sort: '',
  });
  /** Whether the "add distribution email" modal is currently visible. */
  public showModal = false;
  /** True while a bulk unsubscribe operation is in progress (drives the spinner). */
  public readonly deletingAll = signal(false);
  /** Tracks the current state of the "select all" toggle. */
  public allSelected = false;

  /**
   * Angular lifecycle hook. Loads the first page of distribution emails when
   * the component is initialised.
   */
  ngOnInit() {
    this.loadDistributionEmails();
  }

  /**
   * Fetches the current page of distribution emails from the backend using the
   * active {@link listingOptions} and stores the result in
   * {@link distributionEmails}, mapping each entry to a
   * {@link SelectableDistributionMail}.
   *
   * @returns A promise that resolves once the list has been reloaded.
   */
  public async loadDistributionEmails() {
    const options = this.listingOptions();
    const distribs = await this.appMessageService.getDistributionEmailsAsync({
      search: options.sort,
      limit: options.limit,
      page: options.page,
    });

    const data: SelectableDistributionMail[] = [];
    if (distribs.data) {
      for (const distribMail of distribs.data) {
        data.push(distribMail as SelectableDistributionMail);
      }
    }
    this.distributionEmails.set({
      data,
      total: distribs.total ? distribs.total : 0,
    });
  }

  /**
   * Navigates to the given page and reloads the distribution emails.
   *
   * @param page - The 1-based page number to display.
   */
  public goToPage(page: number) {
    this.listingOptions.update((options) => ({ ...options, page }));
    this.loadDistributionEmails();
  }

  /**
   * Unsubscribes (deletes) a single distribution email and reloads the list
   * from the first page. Errors from the backend call are logged to the
   * console and swallowed. No action is taken when the email has no id.
   *
   * @param distribMail - The distribution email to unsubscribe.
   * @returns A promise that resolves once the deletion and reload complete.
   */
  public async unsubscribeEmail(distribMail: DistributionMail) {
    if (distribMail.id) {
      try {
        await this.appMessageService.deleteDistributionEmailsAsync({
          id: distribMail.id,
        });
        this.listingOptions.set({
          page: 1,
          limit: 25,
          sort: this.listingOptions().sort,
        });
        this.loadDistributionEmails();
      } catch (error) {
        console.error(error);
      }
    }
  }

  /**
   * Indicates whether at least one distribution email in the current page is
   * selected.
   *
   * @returns `true` if one or more rows are selected, otherwise `false`.
   */
  public hasSelectedDistribution() {
    return this.countSelectedDistribution() > 0;
  }

  /**
   * Counts how many distribution emails in the current page are selected.
   *
   * @returns The number of selected rows.
   */
  public countSelectedDistribution() {
    let nbSelection = 0;
    this.distributionEmails().data.forEach((distrib) => {
      if (distrib.selected) {
        nbSelection += 1;
      }
    });

    return nbSelection;
  }

  /**
   * Toggles the selection state of a single distribution email row.
   *
   * @param distrib - The distribution email whose selection is toggled.
   */
  public tickDistributionMail(distrib: SelectableDistributionMail) {
    if (distrib.selected) {
      distrib.selected = false;
    } else {
      distrib.selected = true;
    }
  }

  /**
   * Unsubscribes (deletes) every currently selected distribution email in
   * sequence, toggling {@link deletingAll} while the batch runs, then reloads
   * the list from the first page. Individual backend errors are logged and
   * swallowed so the batch continues. Does nothing when no row is selected.
   *
   * @returns A promise that resolves once all deletions and the reload complete.
   */
  public async unsubscribeSelectedEmail() {
    const toRemove: DistributionMail[] = [];
    this.distributionEmails().data.forEach((distrib) => {
      if (distrib.selected) {
        toRemove.push({ id: distrib.id, emailAddress: distrib.emailAddress });
      }
    });

    if (toRemove.length > 0) {
      this.deletingAll.set(true);
      for (const distrib of toRemove) {
        try {
          if (distrib.id) {
            await this.appMessageService.deleteDistributionEmailsAsync({
              id: distrib.id,
            });
          }
        } catch (error) {
          console.error(error);
        }
      }
      this.listingOptions.set({
        page: 1,
        limit: 25,
        sort: this.listingOptions().sort,
      });
      this.loadDistributionEmails();
      this.deletingAll.set(false);
    }
  }

  /**
   * Toggles the "select all" state and applies it to every distribution email
   * in the current page.
   */
  public selectAll() {
    this.allSelected = !this.allSelected;
    this.distributionEmails().data.forEach((distrib) => {
      distrib.selected = this.allSelected;
    });
  }

  /**
   * Requests the full distribution list export from the backend and triggers a
   * browser download of the resulting `distribution-list.xlsx` file.
   *
   * @returns A promise that resolves once the export request has been issued.
   */
  public async downloadExport() {
    this.appMessageService
      .getDistributionEmailsExport()
      .subscribe((response) => {
        saveAs(response, 'distribution-list.xlsx');
      });
  }
}
