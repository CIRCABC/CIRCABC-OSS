import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
} from '@angular/core';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';

import { ForumService, Node as ModelNode } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-moderate-forum',
  templateUrl: './moderate.component.html',
  preserveWhitespaces: true,
})
export class ModerateComponent implements OnChanges {
  @Input()
  forum!: ModelNode;
  @Input()
  showModal = false;
  @Output()
  readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public executing = false;

  public moderationEnabled = false;
  public acceptPostValidation = false;

  constructor(private forumService: ForumService) {}

  ngOnChanges() {
    this.moderationEnabled = this.forumModerated();
  }

  public forumModerated(): boolean {
    if (this.forum.properties !== undefined) {
      return this.forum.properties.ismoderated === 'true';
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
    if (this.forum.id && this.forum.properties) {
      await firstValueFrom(
        this.forumService.putModeration(
          this.forum.id,
          this.moderationEnabled,
          this.acceptPostValidation
        )
      );
      this.forum.properties.ismoderated = this.moderationEnabled
        ? 'true'
        : 'false';
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
