import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  resource,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { DashboardService, UserActionLog } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { removeDuplicates } from 'app/core/util';
import { FileExtensionIconComponent } from 'app/shared/file-extension-icon/file-extension-icon.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';

/**
 * Dashboard widget that renders the current user's recent consultation
 * activity, split into their most recent document downloads and uploads.
 *
 * On initialisation it fetches the logged-in user's download and upload
 * action logs from the {@link DashboardService}, de-duplicates them by node
 * so each document appears only once, and exposes the results together with
 * per-section loading flags. The template uses these to display two toggleable
 * lists (downloads / uploads) with file-extension icons, user cards and links
 * back to the related nodes.
 *
 * Collaborators:
 * - {@link DashboardService} to retrieve the user's download/upload logs.
 * - {@link LoginService} to resolve the current username.
 */
@Component({
  selector: 'cbc-recent-consultation',
  templateUrl: './recent-consultation.component.html',
  styleUrl: './recent-consultation.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    FileExtensionIconComponent,
    RouterLink,
    UserCardComponent,
    DatePipe,
    TranslocoModule,
  ],
})
export class RecentConsultationComponent {
  /** Service used to fetch the current user's download and upload logs. */
  private readonly dashboardService = inject(DashboardService);
  /** Service used to resolve the currently authenticated username. */
  private readonly loginService = inject(LoginService);

  /**
   * The current user's most recent document downloads, loaded reactively.
   * The request stays idle while there is no authenticated user.
   */
  private readonly downloadsResource = resource({
    params: () => this.loginService.getCurrentUsername() || undefined,
    loader: async ({ params: userId }) => {
      try {
        return await this.dashboardService.getUserDownloadsAsync({ userId });
      } catch (error) {
        console.error(error);
        return [];
      }
    },
  });

  /**
   * The current user's most recent document uploads, loaded reactively.
   * The request stays idle while there is no authenticated user.
   */
  private readonly uploadsResource = resource({
    params: () => this.loginService.getCurrentUsername() || undefined,
    loader: async ({ params: userId }) => {
      try {
        return await this.dashboardService.getUserUploadsAsync({ userId });
      } catch (error) {
        console.error(error);
        return [];
      }
    },
  });

  /** De-duplicated list of the user's most recent document downloads. */
  public readonly lastDownloads = computed(() =>
    removeDuplicates(this.downloadsResource.value() ?? [], this.compare)
  );
  /** De-duplicated list of the user's most recent document uploads. */
  public readonly lastUploads = computed(() =>
    removeDuplicates(this.uploadsResource.value() ?? [], this.compare)
  );
  /** Whether the downloads list is currently being loaded. */
  public readonly loadingDownloads = this.downloadsResource.isLoading;
  /** Whether the uploads list is currently being loaded. */
  public readonly loadingUploads = this.uploadsResource.isLoading;
  /** Controls which section is shown: `true` for downloads, `false` for uploads. */
  public showDownloads = true;

  /**
   * Determines whether two action-log entries refer to the same node,
   * used as the equality predicate when de-duplicating the download and
   * upload lists.
   *
   * @param item1 The first action-log entry to compare.
   * @param item2 The second action-log entry to compare.
   * @returns `true` when both entries have a defined node with the same id,
   * otherwise `false`.
   */
  private compare(item1: UserActionLog, item2: UserActionLog): boolean {
    return (
      item1.node !== undefined &&
      item2.node !== undefined &&
      item1.node.id === item2.node.id
    );
  }
}
