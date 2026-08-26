import {
  Component,
  Inject,
  Input,
  Optional,
  output,
  input,
} from '@angular/core';

import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { assertDefined } from 'app/core/asserts';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  AbuseReport,
  Attachment,
  BASE_PATH,
  Node as ModelNode,
  NodesService,
  PostService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { SaveAsService } from 'app/core/save-as.service';
import { Quote } from 'app/core/ui-model/index';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { HintComponent } from 'app/shared/hint/hint.component';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { IfRoleGePipe } from 'app/shared/pipes/if-role-ge.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';
import { firstValueFrom } from 'rxjs';
import { RejectPostComponent } from './reject-post/reject-post.component';
import { SignalAbuseComponent } from './signal-abuse/signal-abuse.component';

@Component({
  selector: 'cbc-post',
  templateUrl: './post.component.html',
  styleUrl: './post.component.scss',
  preserveWhitespaces: true,
  imports: [
    HintComponent,
    UserCardComponent,
    InlineDeleteComponent,
    RejectPostComponent,
    SignalAbuseComponent,
    DatePipe,
    DownloadPipe,
    IfRoleGePipe,
    SecurePipe,
    TranslocoModule,
  ],
})
export class PostComponent {
  public readonly topic = input<ModelNode>();
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  public post!: ModelNode;
  public readonly igId = input<string>();
  public readonly replyClicked = output<Quote>();
  public readonly editClicked = output<ModelNode>();
  public readonly deleted = output<ActionEmitterResult>();
  public readonly verified = output<ActionEmitterResult>();

  public abuses!: AbuseReport[];

  public showRejectModal = false;

  public showSignalModal = false;
  public showSignaledAbuses = false;
  public showModifiedDate = false;

  public attachments: File[] = [];

  private basePath!: string;

  public constructor(
    private postService: PostService,
    private nodesService: NodesService,
    private permEvalService: PermissionEvaluatorService,
    private loginService: LoginService,
    private saveAsService: SaveAsService,
    private router: Router,
    @Optional()
    @Inject(BASE_PATH)
    basePath: string
  ) {
    if (basePath) {
      this.basePath = basePath;
    }
  }

  public fireReply() {
    if (this.post?.properties) {
      const allQuotesExpression =
        /<p><br><\/p><p>(\w)+: (((\w)+\s)*)(\w)+<\/p>(<p><br><\/p>)?<blockquote>(.)+<\/blockquote>$/;
      const message =
        allQuotesExpression.test(this.post.properties.message) !== null
          ? this.post.properties.message.replace(allQuotesExpression, '')
          : this.post.properties.message;
      const quote: Quote = {
        author: this.post.properties.creator,
        text: message,
      };
      this.replyClicked.emit(quote);
    }
  }

  public fireEdit() {
    if (this.post?.properties) {
      this.editClicked.emit(this.post);
    }
  }

  public async deletePost() {
    const result: ActionEmitterResult = {};
    result.type = ActionType.DELETE_POST;

    try {
      if (this.post.id) {
        await firstValueFrom(this.postService.deletePost(this.post.id));
        result.result = ActionResult.SUCCEED;
      }
    } catch (_error) {
      result.result = ActionResult.FAILED;
    }

    this.deleted.emit(result);
  }

  public canDeleteComment(): boolean {
    return (
      this.permEvalService.isLibAdmin(this.post) ||
      this.permEvalService.isNewsgroupAdmin(this.post) ||
      this.permEvalService.isNewsgroupModerate(this.post) ||
      this.isOwner()
    );
  }

  public canPostComment(): boolean {
    return (
      this.permEvalService.isLibAccess(this.post) ||
      this.permEvalService.isNewsgroupPost(this.post) ||
      this.permEvalService.isNewsgroupModerate(this.post)
    );
  }

  public canEditComment(): boolean {
    return (
      this.permEvalService.isLibAdminOrFullEdit(this.post) ||
      this.permEvalService.isNewsgroupModerate(this.post) ||
      this.isOwner()
    );
  }

  public isOwner(): boolean {
    if (this.post?.properties) {
      return (
        this.loginService.getCurrentUsername() === this.post.properties.owner
      );
    }
    return false;
  }

  public async approve() {
    if (this.post.properties) {
      await firstValueFrom(
        this.postService.putVerify(this.post.id as string, true)
      );
      this.post.properties.waitingForApproval = 'false';

      const result: ActionEmitterResult = {};
      result.result = ActionResult.SUCCEED;
      result.type = ActionType.APPROVE_POST;

      this.verified.emit(result);
    }
  }

  public reject() {
    this.showRejectModal = true;
  }

  public refreshPostAfterReject(result: ActionEmitterResult) {
    this.showRejectModal = false;
    this.verified.emit(result);
  }

  public refreshPostAfterAbuse(result: ActionEmitterResult) {
    this.showSignalModal = false;
    this.verified.emit(result);
  }

  public waitingForApproval(): boolean {
    if (this.post.properties) {
      return this.post.properties.waitingForApproval === 'true';
    }
    return false;
  }

  public isModerator(): boolean {
    const topic = this.topic();
    assertDefined(topic);
    if (topic.permissions) {
      return (
        topic.permissions.NwsModerate === 'ALLOWED' ||
        topic.permissions.NwsAdmin === 'ALLOWED'
      );
    }
    return false;
  }

  public hasBeenEdited(): boolean {
    if (this.post.properties) {
      return this.post.properties.versionLabel !== '1.0';
    }
    return false;
  }

  public hasBeenRejected(): boolean {
    if (this.post.properties) {
      return this.post.properties.rejectedOn !== undefined;
    }
    return false;
  }

  public abuseSignaled(): boolean {
    if (!this.post.properties) {
      return false;
    }
    return this.post.properties.messages?.includes('Abuse Report:');
  }

  public signalAbuse() {
    this.showSignalModal = true;
  }

  public async removeAbuses() {
    await firstValueFrom(this.postService.deleteAbuse(this.post.id as string));
    const result: ActionEmitterResult = {};
    result.result = ActionResult.SUCCEED;
    result.type = ActionType.DELETE_ABUSE;
    this.verified.emit(result);
  }

  public async toggleShowAbuses() {
    if (!this.showSignaledAbuses) {
      await this.getAbuses();
    }
    this.showSignaledAbuses = !this.showSignaledAbuses;
  }

  private async getAbuses() {
    this.abuses = await firstValueFrom(
      this.postService.getAbuses(this.post.id as string)
    );
  }

  public toggleShowModifiedDate() {
    this.showModifiedDate = !this.showModifiedDate;
  }

  public displayName(text: string | undefined): string {
    if (text === undefined) {
      return '';
    }
    return text.length > 7 ? `${text.substring(0, 7)}...` : text;
  }

  public async accessAttachment(attachment: Attachment) {
    if (attachment.isLink) {
      const attachmentNode = await firstValueFrom(
        this.nodesService.getNode(attachment.id as string)
      );
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.router.navigate([
        `/group/${this.igId()}/library/${attachment.id}${
          (attachmentNode.type as string).endsWith('folder') ? '' : '/details'
        }`,
      ]);
    } else {
      this.downloadAttachment(attachment);
    }
  }

  public downloadAttachment(attachment: Attachment) {
    const url = `${this.basePath}/posts/${attachment.id}/attachment/download`;
    this.saveAsService.saveUrlAs(url, attachment.name as string);
  }

  get avatar(): string | null {
    if (this.post.properties) {
      return this.post.properties.avatar;
    }
    return null;
  }

  get creator(): string | null {
    if (this.post.properties) {
      return this.post.properties.creator;
    }
    return null;
  }

  get modifier(): string | null {
    if (this.post.properties) {
      return this.post.properties.modifier;
    }
    return null;
  }

  get created(): string | null {
    if (this.post.properties) {
      return this.post.properties.created;
    }
    return null;
  }

  get modified(): string | null {
    if (this.post.properties) {
      return this.post.properties.modified;
    }
    return null;
  }

  get rejectedBy(): string | null {
    if (this.post.properties) {
      return this.post.properties.rejectedBy;
    }
    return null;
  }

  get rejectedOn(): string | null {
    if (this.post.properties) {
      return this.post.properties.rejectedOn;
    }
    return null;
  }

  get message(): string | null {
    if (this.post.properties) {
      return this.post.properties.message;
    }
    return null;
  }
}
