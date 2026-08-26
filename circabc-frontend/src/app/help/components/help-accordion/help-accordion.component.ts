import {
  Component,
  inject,
  OnInit,
  ChangeDetectionStrategy,
  signal,
  computed,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  CdkDrag,
  CdkDropList,
  CdkDragDrop,
  CdkDragHandle,
} from '@angular/cdk/drag-drop';
import { moveItemInArray } from '@angular/cdk/drag-drop';
import { firstValueFrom } from 'rxjs';
import { HelpCategoryPanelComponent } from '../help-category-panel/help-category-panel.component';
import { HelpHierarchyService } from '../../services/help-hierarchy.service';
import { LoginService } from '../../../core/login.service';
import { HelpCategoryFormModalComponent } from '../../../help/components/help-category-form-modal/help-category-form-modal.component';
import { AddLinkComponent } from '../../../help/add-link/add-link.component';
import { ActionEmitterResult, ActionResult } from 'app/action-result';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { HelpCategory } from '../../models/help-hierarchy.model';

@Component({
  selector: 'app-help-accordion',
  standalone: true,
  imports: [
    CommonModule,
    CdkDrag,
    CdkDropList,
    CdkDragHandle,
    HelpCategoryPanelComponent,
    HelpCategoryFormModalComponent,
    AddLinkComponent,
    TranslocoModule,
  ],
  templateUrl: './help-accordion.component.html',
  styleUrl: './help-accordion.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HelpAccordionComponent implements OnInit {
  private readonly hierarchyService = inject(HelpHierarchyService);
  private readonly loginService = inject(LoginService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly translocoService = inject(TranslocoService);

  readonly hierarchyData = this.hierarchyService.hierarchyData;
  readonly isLoading = this.hierarchyService.isLoading;
  readonly error = this.hierarchyService.error;
  readonly showCreateCategoryModal = signal<boolean>(false);
  readonly showCreateLinkModal = signal<boolean>(false);

  readonly categories = computed(() => {
    const hierarchy = this.hierarchyData();
    return hierarchy?.categories ? [...hierarchy.categories] : [];
  });

  private readonly localCategories = signal<HelpCategory[]>([]);
  private readonly isDraggingCategories = signal<boolean>(false);

  readonly displayCategories = computed(() =>
    this.isDraggingCategories() ? this.localCategories() : this.categories()
  );

  ngOnInit(): void {
    this.hierarchyService.loadHierarchy();
  }

  isAdminOrSupport(): boolean {
    if (!this.loginService.isGuest()) {
      const user = this.loginService.getUser();
      return (
        user.properties !== undefined &&
        (user.properties['isAdmin'] === 'true' ||
          user.properties['isCircabcAdmin'] === 'true')
      );
    }
    return false;
  }

  onAddCategoryClick(): void {
    this.showCreateCategoryModal.set(true);
  }

  onAddLinkClick(): void {
    this.showCreateLinkModal.set(true);
  }

  async onCategorySaved(result: ActionEmitterResult): Promise<void> {
    if (result.result === ActionResult.SUCCEED) {
      this.hierarchyService.loadHierarchy(true);
    }
  }

  async onLinkSaved(result: ActionEmitterResult): Promise<void> {
    if (result.result === ActionResult.SUCCEED) {
      this.hierarchyService.loadHierarchy(true);
    }
    this.showCreateLinkModal.set(false);
  }

  onCategoryChanged(): void {
    this.hierarchyService.loadHierarchy(true);
  }

  async onCategoryDrop(event: CdkDragDrop<HelpCategory[]>): Promise<void> {
    const current = [...this.categories()];
    this.isDraggingCategories.set(true);

    moveItemInArray(current, event.previousIndex, event.currentIndex);
    this.localCategories.set(current);

    try {
      const categoryIds = current
        .map((c) => c.id)
        .filter((id): id is string => id !== undefined);

      await firstValueFrom(
        this.hierarchyService.putCategoriesOrder(categoryIds)
      );

      this.hierarchyService.loadHierarchy(true);
    } catch (error) {
      console.error('[HelpAccordion] Failed to save category order', error);
    } finally {
      this.isDraggingCategories.set(false);
    }
  }
}
