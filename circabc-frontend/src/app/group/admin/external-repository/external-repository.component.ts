import { Component } from '@angular/core';

@Component({
  selector: 'cbc-external-repository',
  templateUrl: './external-repository.component.html',
  styleUrls: ['./external-repository.component.scss'],
  preserveWhitespaces: true,
})
export class ExternalRepositoryComponent {
  public showPropertiesItems = true;
  public showHistoryItems = false;

  public isShowProperties(): boolean {
    return this.showPropertiesItems;
  }

  public isShowHistory(): boolean {
    return this.showHistoryItems;
  }

  public showProperties() {
    this.showPropertiesItems = true;
    this.showHistoryItems = false;
  }

  public showHistory() {
    this.showPropertiesItems = false;
    this.showHistoryItems = true;
  }
}
