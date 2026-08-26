import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Data, RouterOutlet } from '@angular/router';

import { TranslocoModule } from '@jsverse/transloco';
import { GroupLockService, InterestGroup } from 'app/core/generated/circabc';
import { GroupLockInfo } from 'app/core/generated/circabc/model/groupLockInfo';
import { LibraryIdService } from 'app/core/libraryId.service';
import { LoginService } from 'app/core/login.service';
import { GroupLockStateService } from 'app/core/group-lock-state.service';
import { ReadOnlyStateService } from 'app/core/read-only-state.service';
import { VisitedGroupService } from 'app/core/visited-groups/visited-group.service';
import { FlatMessageComponent } from 'app/shared/flat-message/flat-message.component';
import { HeaderComponent } from 'app/shared/header/header.component';
import { NavigatorComponent } from 'app/shared/navigator/navigator.component';
import { Subscription, firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-group',
  templateUrl: './group.component.html',
  preserveWhitespaces: true,
  imports: [
    HeaderComponent,
    NavigatorComponent,
    FlatMessageComponent,
    RouterOutlet,
    TranslocoModule,
  ],
})
export class GroupComponent implements OnInit, OnDestroy {
  public group!: InterestGroup;

  /** Driven by GroupLockStateService so GroupAdminComponent updates it reactively */
  public get isGroupLocked(): boolean {
    return this.groupLockStateService.isLocked();
  }

  private libraryIdSubscription$!: Subscription;

  public constructor(
    private route: ActivatedRoute,
    private loginService: LoginService,
    private visitedGroupService: VisitedGroupService,
    private libraryIdService: LibraryIdService,
    private groupLockService: GroupLockService,
    private readOnlyStateService: ReadOnlyStateService,
    private groupLockStateService: GroupLockStateService
  ) {}

  public ngOnInit() {
    this.route.data.subscribe((value: Data) => {
      this.group = value.group;
      this.visitedGroupService.visitGroup(this.group);
      this.checkGroupLock();
    });
    this.subscribe();
  }

  public ngOnDestroy(): void {
    this.unsubscribe();
    this.readOnlyStateService.reset();
    this.groupLockStateService.reset();
  }

  private subscribe() {
    this.libraryIdSubscription$ =
      this.libraryIdService.libraryIdSubject$.subscribe((libraryId: string) => {
        this.group.libraryId = libraryId;
      });
  }

  private unsubscribe() {
    this.libraryIdSubscription$.unsubscribe();
  }

  public isAccessingAsVisitor(): boolean {
    return this.loginService.isGuest();
  }

  private async checkGroupLock(): Promise<void> {
    if (!this.group?.id) {
      return;
    }

    try {
      const lockInfo: GroupLockInfo = await firstValueFrom(
        this.groupLockService.getGroupLockInfo(this.group.id)
      );

      if (lockInfo?.locked) {
        this.group.lockInfo = lockInfo;
        // The guard already redirected non-admins to the locked page.
        // Here we only need to activate the locked/read-only UI state for admins.
        this.groupLockStateService.setLocked(true);
        this.readOnlyStateService.setReadOnly(lockInfo.readOnly === true);
      } else {
        this.group.lockInfo = lockInfo;
        this.groupLockStateService.setLocked(false);
        this.readOnlyStateService.setReadOnly(false);
      }
    } catch {
      this.groupLockStateService.setLocked(false);
      this.readOnlyStateService.setReadOnly(false);
    }
  }
}
