import { Component, Input, OnChanges, output, input } from '@angular/core';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';

import { TranslocoModule } from '@jsverse/transloco';
import { ForumService, Node as ModelNode } from 'app/core/generated/circabc';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-moderate-forum',
  templateUrl: './moderate.component.html',
  preserveWhitespaces: true,
  imports: [SpinnerComponent, TranslocoModule],
})
export class ModerateComponent implements OnChanges {
  readonly forum = input.required<ModelNode>();
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  readonly modalHide = output<ActionEmitterResult>();

  public executing = false;

  public moderationEnabled = false;
  public acceptPostValidation = false;

  constructor(private forumService: ForumService) {}

  ngOnChanges() {
    this.moderationEnabled = this.forumModerated();
  }

  public forumModerated(): boolean {
    const forum = this.forum();
    if (forum.properties !== undefined) {
      return forum.properties.ismoderated === 'true';
    }
    return false;
  }

  public toggleModeration() {
    this.moderationEnabled = !this.moderationEnabled;
  }

  public togglePostValidation() {
    this.acceptPostValidation = !this.acceptPostValidation;
  }

  public async accept() {
    this.executing = true;
    const forum = this.forum();
    if (forum.id && forum.properties) {
      await firstValueFrom(
        this.forumService.putModeration(
          forum.id,
          this.moderationEnabled,
          this.acceptPostValidation
        )
      );
      forum.properties.ismoderated = this.moderationEnabled ? 'true' : 'false';
    }

    this.showModal = false;

    const result: ActionEmitterResult = {};
    result.result = ActionResult.SUCCEED;
    result.type = ActionType.MODERATE_FORUM;
    this.modalHide.emit(result);

    this.executing = false;
  }

  public cancel(_action: string): void {
    this.showModal = false;
    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.MODERATE_FORUM;
    this.modalHide.emit(result);
  }
}
