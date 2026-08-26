import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { DeletedItemsComponent } from './deleted-items/deleted-items.component';
import { ExpiredItemsComponent } from './expired-items/expired-items.component';

@Component({
  selector: 'cbc-document-lifecycle',
  templateUrl: './document-lifecycle.component.html',
  styleUrl: './document-lifecycle.component.scss',
  preserveWhitespaces: true,
  imports: [
    DeletedItemsComponent,
    ExpiredItemsComponent,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class DocumentLifecycleComponent implements OnInit {
  public showDeletedItems = false;
  public showExpiredItems = true;
  public groupId!: string;

  constructor(private route: ActivatedRoute) {}

  ngOnInit() {
    this.route.params.subscribe((params) => {
      this.groupId = params.id;
      if (params.expired === '1') {
        this.showExpired();
      }
    });
  }

  public isShowDeleted(): boolean {
    return this.showDeletedItems;
  }

  public isShowExpired(): boolean {
    return this.showExpiredItems;
  }

  public showDeleted() {
    this.showDeletedItems = true;
    this.showExpiredItems = false;
  }

  public showExpired() {
    this.showDeletedItems = false;
    this.showExpiredItems = true;
  }
}
