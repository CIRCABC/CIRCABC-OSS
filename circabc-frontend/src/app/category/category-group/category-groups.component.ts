import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { MatDialog } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  CategoryService,
  GroupLockService,
  InterestGroup,
} from 'app/core/generated/circabc';
import { GroupLockInfo } from 'app/core/generated/circabc/model/groupLockInfo';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { sortI18nProperty } from 'app/core/util';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';
import {
  LockGroupDialogComponent,
  LockGroupDialogData,
} from 'app/group/admin/lock-group/lock-group-dialog.component';

@Component({
  selector: 'cbc-category-group',
  templateUrl: './category-groups.component.html',
  styleUrl: './category-groups.component.scss',
  imports: [DataCyDirective, RouterLink, TranslocoModule, MatTooltipModule],
})
export class CategoryGroupsComponent implements OnInit {
  public interestGroups: InterestGroup[] = [];
  public lockInfoMap: Map<string, GroupLockInfo> = new Map();

  constructor(
    private route: ActivatedRoute,
    private categoryService: CategoryService,
    private translateService: TranslocoService,
    private uiMessageService: UiMessageService,
    private i18nPipe: I18nPipe,
    private dialog: MatDialog,
    private groupLockService: GroupLockService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(async (params) => {
      await this.listInterestGroups(params.id);
    });
  }

  public async listInterestGroups(categoryId: string) {
    try {
      if (categoryId) {
        const unsortedInterestGroups = await firstValueFrom(
          this.categoryService.getInterestGroupsByCategoryId(
            categoryId,
            this.getCurrentLang()
          )
        );
        unsortedInterestGroups.sort((a: InterestGroup, b: InterestGroup) =>
          sortI18nProperty(
            a.title,
            b.title,
            this.getDefaultLang(),
            this.getCurrentLang(),
            a.name,
            b.name
          )
        );
        this.interestGroups = unsortedInterestGroups;
        await this.loadLockInfoForGroups();
      }
    } catch (err) {
      this.uiMessageService.addErrorMessage(err);
    }
  }

  getNameOrTitle(item: InterestGroup): string {
    let result = '';

    if (item.title && Object.keys(item.title).length > 0) {
      result = this.i18nPipe.transform(item.title);
    }

    if (result === '' && item.name) {
      result = item.name;
    }

    return result;
  }

  isGroupLocked(groupId: string | undefined): boolean {
    if (!groupId) {
      return false;
    }
    const lockInfo = this.lockInfoMap.get(groupId);
    return lockInfo?.locked === true;
  }

  openLockDialog(groupId: string): void {
    const dialogRef = this.dialog.open(LockGroupDialogComponent, {
      data: { groupId } as LockGroupDialogData,
    });

    dialogRef.afterClosed().subscribe((result: boolean) => {
      if (result) {
        this.refreshList();
      }
    });
  }

  async unlockGroup(groupId: string): Promise<void> {
    try {
      await firstValueFrom(this.groupLockService.unlockGroup(groupId));
      this.uiMessageService.addSuccessMessage(
        'successfully unlocked the interest group',
        true
      );
      await this.refreshList();
    } catch (err) {
      this.uiMessageService.addErrorMessage(err);
    }
  }

  private async refreshList(): Promise<void> {
    const params = this.route.snapshot.params;
    if (params.id) {
      await this.listInterestGroups(params.id);
    }
  }

  private async loadLockInfoForGroups(): Promise<void> {
    const lockInfoPromises = this.interestGroups
      .filter((ig) => ig.id)
      .map(async (ig) => {
        const igId = ig.id;
        if (!igId) return;
        try {
          const lockInfo = await firstValueFrom(
            this.groupLockService.getGroupLockInfo(igId)
          );
          this.lockInfoMap.set(igId, lockInfo);
        } catch {
          // If the endpoint returns an error (e.g., 404), treat as not locked
          this.lockInfoMap.set(igId, { locked: false });
        }
      });

    await Promise.all(lockInfoPromises);
  }

  private getCurrentLang(): string {
    return this.translateService.getActiveLang();
  }

  private getDefaultLang(): string {
    return this.translateService.getDefaultLang();
  }
}
