import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Data, RouterOutlet } from '@angular/router';

import { TranslocoModule } from '@jsverse/transloco';
import { InterestGroup } from 'app/core/generated/circabc';
import { LibraryIdService } from 'app/core/libraryId.service';
import { LoginService } from 'app/core/login.service';
import { VisitedGroupService } from 'app/core/visited-groups/visited-group.service';
import { FlatMessageComponent } from 'app/shared/flat-message/flat-message.component';
import { HeaderComponent } from 'app/shared/header/header.component';
import { NavigatorComponent } from 'app/shared/navigator/navigator.component';
import { Subscription } from 'rxjs';

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

  private libraryIdSubscription$!: Subscription;

  public constructor(
    private route: ActivatedRoute,
    private loginService: LoginService,
    private visitedGroupService: VisitedGroupService,
    private libraryIdService: LibraryIdService
  ) {}

  public ngOnInit() {
    this.route.data.subscribe((value: Data) => {
      this.group = value.group;
      this.visitedGroupService.visitGroup(this.group);
    });
    this.subscribe();
  }

  public ngOnDestroy(): void {
    this.unsubscribe();
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
}
