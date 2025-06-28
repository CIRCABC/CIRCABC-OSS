import { Component } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { ExternalRepositoryHistoryComponent } from './ext-repo-history/cbc-external-repository-history.component';
import { ExternalRepositoryPropertiesComponent } from './ext-repo-properties/external-repository-properties.component';

@Component({
  selector: 'cbc-external-repository',
  templateUrl: './external-repository.component.html',
  styleUrl: './external-repository.component.scss',
  preserveWhitespaces: true,
  imports: [
    ExternalRepositoryPropertiesComponent,
    ExternalRepositoryHistoryComponent,
    SetTitlePipe,
    TranslocoModule,
  ],
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
