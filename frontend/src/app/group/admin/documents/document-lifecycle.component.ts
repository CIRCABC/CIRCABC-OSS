import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { DeletedItemsComponent } from './deleted-items/deleted-items.component';
import { ExpiredItemsComponent } from './expired-items/expired-items.component';

/**
 * Group administration screen for managing the lifecycle of a group's
 * documents.
 *
 * The component renders a tabbed view that lets administrators switch between
 * two panels for the current interest group:
 * - {@link ExpiredItemsComponent} — documents that have reached their
 *   expiration date.
 * - {@link DeletedItemsComponent} — documents that have been deleted (recycle
 *   bin).
 *
 * Only one panel is shown at a time. On initialization it reads the group id
 * from the active route and, when the route carries `expired=1`, opens the
 * expired-items panel by default.
 */
@Component({
  selector: 'cbc-document-lifecycle',
  templateUrl: './document-lifecycle.component.html',
  styleUrl: './document-lifecycle.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    DeletedItemsComponent,
    ExpiredItemsComponent,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class DocumentLifecycleComponent implements OnInit {
  /** Active route used to resolve the group id and initial panel state. */
  private readonly route = inject(ActivatedRoute);

  /** Whether the deleted-items (recycle bin) panel is currently displayed. */
  public readonly showDeletedItems = signal(false);
  /** Whether the expired-items panel is currently displayed (default panel). */
  public readonly showExpiredItems = signal(true);
  /** Identifier of the interest group whose documents are being managed. */
  public readonly groupId = signal<string>('');

  /**
   * Angular lifecycle hook. Subscribes to the route parameters to capture the
   * group id and, when the `expired` parameter equals `'1'`, opens the
   * expired-items panel.
   */
  ngOnInit() {
    this.route.params.subscribe((params) => {
      this.groupId.set(params.id);
      if (params.expired === '1') {
        this.showExpired();
      }
    });
  }

  /**
   * Reports whether the deleted-items panel is currently shown.
   *
   * @returns `true` if the deleted-items panel is visible, otherwise `false`.
   */
  public isShowDeleted(): boolean {
    return this.showDeletedItems();
  }

  /**
   * Reports whether the expired-items panel is currently shown.
   *
   * @returns `true` if the expired-items panel is visible, otherwise `false`.
   */
  public isShowExpired(): boolean {
    return this.showExpiredItems();
  }

  /**
   * Activates the deleted-items panel and hides the expired-items panel.
   */
  public showDeleted() {
    this.showDeletedItems.set(true);
    this.showExpiredItems.set(false);
  }

  /**
   * Activates the expired-items panel and hides the deleted-items panel.
   */
  public showExpired() {
    this.showDeletedItems.set(false);
    this.showExpiredItems.set(true);
  }
}
