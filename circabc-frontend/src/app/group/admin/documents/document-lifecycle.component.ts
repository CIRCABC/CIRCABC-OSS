import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'cbc-document-lifecycle',
  templateUrl: './document-lifecycle.component.html',
  styleUrls: ['./document-lifecycle.component.scss'],
  preserveWhitespaces: true,
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
