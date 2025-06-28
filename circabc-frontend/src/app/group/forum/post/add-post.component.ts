import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  output,
  input,
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  type Comment,
  Node as ModelNode,
  PostService,
  TopicService,
  UserService,
} from 'app/core/generated/circabc';
import { type Quote } from 'app/core/ui-model/index';
import { getUserFullName } from 'app/core/util';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { SharedModule } from 'primeng/api';
import { EditorModule } from 'primeng/editor';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-post',
  templateUrl: './add-post.component.html',
  styleUrl: './add-post.component.scss',
  preserveWhitespaces: true,
  imports: [
    ReactiveFormsModule,
    EditorModule,
    SharedModule,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class AddPostComponent implements OnInit, OnChanges {
  readonly topic = input.required<ModelNode>();
  readonly futureQuote = input.required<Quote>();
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  editPost: ModelNode | undefined;
  readonly postedComment = output<ActionEmitterResult>();

  public addPostForm!: FormGroup;
  public showForm = false;
  public posting = false;

  constructor(
    private fb: FormBuilder,
    private postService: PostService,
    private topicService: TopicService,
    private translateService: TranslocoService,
    private userService: UserService
  ) {}

  ngOnInit() {
    this.addPostForm = this.fb.group(
      {
        text: ['', Validators.required],
      },
      {
        updateOn: 'change',
      }
    );
  }

  async ngOnChanges(changes: SimpleChanges) {
    if (changes.futureQuote) {
      if (changes.futureQuote.currentValue !== undefined) {
        const quoting = this.translateService.translate('label.quoting');
        const authorFullName = await getUserFullName(
          changes.futureQuote.currentValue.author as string,
          this.userService
        );
        const text = `<br/><br/><p>${quoting}: ${authorFullName}
        </p><blockquote>${changes.futureQuote.currentValue.text}</blockquote>`;
        if (this.addPostForm) {
          this.addPostForm.patchValue({ text });
          this.showForm = true;
        }
      }
    } else if (changes.editPost) {
      if (changes.editPost.currentValue !== undefined) {
        const text = changes.editPost.currentValue.properties.message;
        if (this.addPostForm) {
          this.addPostForm.patchValue({ text });
          this.showForm = true;
        }
      }
    } else if (changes.topic) {
      this.resetForm();
    }
  }

  public resetForm() {
    this.showForm = false;
    this.editPost = undefined;
    this.addPostForm.reset();
  }

  public async addPost() {
    if (this.addPostForm.valid) {
      this.posting = true;
      const body: Comment = {
        ...this.addPostForm.value,
      };

      const result: ActionEmitterResult = {};
      result.type = ActionType.CREATE_POST;

      try {
        const topic = this.topic();
        if (topic.id) {
          await firstValueFrom(
            this.topicService.postReply(topic.id, true, body)
          );
          result.result = ActionResult.SUCCEED;
          this.addPostForm.reset();
          this.showForm = false;
        }
      } catch (_exception) {
        result.result = ActionResult.FAILED;
      }
      this.posting = false;
      this.postedComment.emit(result);
    }
  }

  public async updatePost() {
    if (this.addPostForm.valid) {
      this.posting = true;

      const result: ActionEmitterResult = {};
      result.type = ActionType.EDIT_POST;

      if (this.editPost?.properties) {
        this.editPost.properties.message = this.addPostForm.value.text;

        try {
          if (this.editPost.id) {
            await firstValueFrom(
              this.postService.putPost(this.editPost.id, false, this.editPost)
            );
            result.result = ActionResult.SUCCEED;
            this.addPostForm.reset();
            this.showForm = false;
          }
        } catch (_exception) {
          result.result = ActionResult.FAILED;
        }
        this.posting = false;
        this.postedComment.emit(result);
      }
    }
  }
}
