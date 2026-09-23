import { Location } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  output,
  resource,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { TranslocoModule } from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  Node as ModelNode,
  NodesService,
  TopicService,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import {
  fileNameValidator,
  maxLengthTitleValidator,
  pastDateValidator,
  titleValidator,
} from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Standalone Angular component that renders the details view/edit form for a
 * forum topic.
 *
 * It displays a topic's metadata (title, description, technical name, security
 * ranking and expiration date) together with read-only audit information such
 * as the version label, creation/modification dates and the names of the
 * creator and modifier users. When the current user has sufficient
 * permissions, the form controls can be edited and the changes persisted back
 * to the backend through {@link TopicService}.
 *
 * The component loads the topic identified by the `topicId` route parameter,
 * populates a reactive {@link FormGroup} and, on submit, updates the topic and
 * emits {@link ViewEditDetailsTopicComponent.topicUpdated} so the parent view
 * can refresh.
 *
 * Key collaborators:
 * - {@link NodesService} to fetch the topic node.
 * - {@link TopicService} to persist topic updates.
 * - {@link UserService} to resolve the creator/modifier display data.
 * - {@link PermissionEvaluatorService} and {@link LoginService} to determine
 *   whether the current user may administer the topic.
 */
@Component({
  selector: 'cbc-view-edit-details-topic',
  templateUrl: './view-edit-details-topic.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    MultilingualInputComponent,
    ControlMessageComponent,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class ViewEditDetailsTopicComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly nodesService = inject(NodesService);
  private readonly userService = inject(UserService);
  private readonly topicService = inject(TopicService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly location = inject(Location);
  private readonly permEvalService = inject(PermissionEvaluatorService);
  private readonly loginService = inject(LoginService);

  /**
   * Emitted after the topic has been successfully updated so that the parent
   * view can refresh its display.
   */
  public readonly topicUpdated = output();

  /** Identifier of the topic being viewed/edited, taken from the route params. */
  private readonly topicIdSignal = signal<string | undefined>(undefined);
  /** Identifier of the topic being viewed/edited, taken from the route params. */
  public get topicId(): string {
    return this.topicIdSignal() ?? '';
  }
  public set topicId(value: string) {
    this.topicIdSignal.set(value);
  }

  /**
   * Loads the topic node identified by {@link topicId}, together with the
   * creator and modifier users, whenever the topic id changes.
   */
  private readonly topicResource = resource({
    params: () => this.topicIdSignal() || undefined,
    loader: async ({ params: topicId }) => {
      const topic = await this.nodesService.getNodeAsync({ id: topicId });

      let creator: User | undefined;
      let modifier: User | undefined;
      if (topic.properties) {
        creator = await this.userService.getUserAsync({
          userId: topic.properties.creator,
        });
        modifier = await this.userService.getUserAsync({
          userId: topic.properties.modifier,
        });
      }

      return { topic, creator, modifier };
    },
  });

  /** The topic node loaded from the backend and bound to the form. */
  public readonly topic = computed(() => this.topicResource.value()?.topic);
  /** User who created the topic, used to display the creator's name. */
  public readonly creator = computed(() => this.topicResource.value()?.creator);
  /** User who last modified the topic, used to display the modifier's name. */
  public readonly modifier = computed(
    () => this.topicResource.value()?.modifier
  );

  // viewing variable has been disabled because of request https://webgate.ec.europa.eu/CITnet/jira/browse/DIGITCIRCABC-3489
  /**
   * Whether the component is in read-only viewing mode. Currently kept `false`
   * (editing enabled) following request DIGITCIRCABC-3489.
   */
  public viewing = false;
  /** Flags that an update request is in progress, used to disable the UI. */
  public readonly processing = signal(false);

  /** Reactive form holding the editable topic details. */
  public readonly updateTopicForm: FormGroup = this.formBuilder.group(
    {
      title: [
        '',
        [
          Validators.required,
          (control: AbstractControl) => titleValidator(control),
          (control: AbstractControl) => maxLengthTitleValidator(control, 50),
        ],
      ],
      description: [''],
      name: ['', [Validators.required, fileNameValidator]],
      securityRanking: [''],
      expirationDate: ['', pastDateValidator],
    },
    {
      updateOn: 'change',
    }
  );

  constructor() {
    this.route.params.subscribe((params) => this.getParams(params));

    effect(() => {
      const topic = this.topic();
      if (topic) {
        this.populateForm(topic);
      }
    });
  }

  /**
   * Stores the topic id extracted from the resolved route parameters.
   *
   * @param params The route parameter map expected to contain `topicId`.
   */
  private getParams(params: { [key: string]: string }) {
    this.topicId = params.topicId;
  }

  /**
   * Populates the reactive form with the given topic's values, including
   * title, description, name, security ranking and expiration date.
   *
   * @param topic The topic node whose data is patched into the form.
   */
  private populateForm(topic: ModelNode) {
    this.updateTopicForm.controls.title.patchValue(topic.title);
    this.updateTopicForm.controls.description.patchValue(topic.description);
    this.updateTopicForm.controls.name.patchValue(topic.name);
    if (topic.properties !== undefined) {
      this.updateTopicForm.controls.securityRanking.patchValue(
        topic.properties.security_ranking
      );
      const expirationDate: string = topic.properties.expiration_date;
      if (expirationDate !== undefined && expirationDate.length >= 10) {
        this.updateTopicForm.controls.expirationDate.patchValue(
          this.adaptDateString(expirationDate)
        );
      }
    }
  }

  /**
   * Returns the date portion (first 10 characters) of an ISO-like date string.
   *
   * @param dateString The raw date string; may be `undefined`.
   * @returns The `YYYY-MM-DD` prefix, or an empty string when input is undefined.
   */
  private cutDate(dateString: string) {
    return dateString === undefined ? '' : dateString.substring(0, 10);
  }

  /**
   * Converts an ISO-like date string (`YYYY-MM-DD...`) into the display format
   * `DD/MM/YYYY`.
   *
   * @param dateString The raw date string; may be `undefined` or empty.
   * @returns The formatted `DD/MM/YYYY` string, or an empty string when the
   *   input is undefined or empty.
   */
  private adaptDateString(dateString: string) {
    if (dateString === undefined || dateString.length === 0) {
      return '';
    }
    const year = dateString.substring(0, 4);
    const month = dateString.substring(5, 7);
    const day = dateString.substring(8, 10);
    return `${day}/${month}/${year}`;
  }

  /** Navigates back to the previous location in the browser history. */
  public goBack() {
    this.location.back();
  }

  /**
   * Enables every control in the update form and switches the component out of
   * read-only viewing mode.
   */
  public enableEdit() {
    // enable all form controls for edit
    Object.keys(this.updateTopicForm.controls).forEach((key) => {
      (this.updateTopicForm.get(key) as AbstractControl).enable();
    });

    this.viewing = false;
  }

  /** Clears the expiration date form control. */
  public clearExpirationDate() {
    this.updateTopicForm.controls.expirationDate.patchValue('');
  }

  /**
   * Discards pending changes by reloading the topic into the form and then
   * navigates back.
   */
  public cancel() {
    this.topicResource.reload();
    this.goBack();
  }

  /**
   * Persists the edited topic details to the backend, emits
   * {@link ViewEditDetailsTopicComponent.topicUpdated} and navigates back.
   *
   * The `processing` flag is set while the request is in flight and reset in a
   * `finally` block.
   *
   * @returns A promise that resolves once the update completes.
   * @throws Error If the loaded topic or its properties are undefined.
   */
  public async update() {
    try {
      this.processing.set(true);

      const topic = this.topic();
      if (topic?.properties === undefined) {
        throw new Error('"topic" is undefined.');
      }

      topic.title = this.updateTopicForm.controls.title.value;
      topic.description = this.updateTopicForm.controls.description.value;
      topic.name = this.updateTopicForm.controls.name.value;
      topic.properties.security_ranking =
        this.updateTopicForm.controls.securityRanking.value;
      topic.properties.expiration_date =
        this.updateTopicForm.controls.expirationDate.value;

      await this.topicService.putTopicAsync({
        id: topic.id as string,
        node: topic,
      });

      // emit an event to signal that a topic has been updated
      // will be used to redisplay the view
      this.topicUpdated.emit();

      this.goBack();
    } finally {
      this.processing.set(false);
    }
  }

  /**
   * Determines whether the current user is allowed to administer the topic,
   * i.e. whether they are a newsgroup administrator or the topic owner.
   *
   * @returns `true` if the current user may administer the topic.
   */
  public isTopicAdmin(): boolean {
    const topic = this.topic();
    if (topic === undefined) {
      return false;
    }
    return (
      this.permEvalService.isNewsgroupAdmin(topic) ||
      this.permEvalService.isOwner(
        topic,
        this.loginService.getCurrentUsername()
      )
    );
  }

  /** Convenience accessor for the expiration date form control. */
  get expirationDateControl(): AbstractControl {
    return this.updateTopicForm.controls.expirationDate;
  }
  /** Convenience accessor for the technical name form control. */
  get nameControl(): AbstractControl {
    return this.updateTopicForm.controls.name;
  }

  /**
   * The topic's version label.
   *
   * @returns The version label from the topic properties, or an empty string.
   */
  get versionLabel(): string {
    const topic = this.topic();
    if (topic?.properties) {
      return topic.properties.versionLabel;
    }
    return '';
  }

  /**
   * The topic's creation date, truncated to `YYYY-MM-DD`.
   *
   * @returns The formatted creation date, or an empty string when unavailable.
   */
  get created(): string {
    const topic = this.topic();
    if (topic?.properties) {
      return this.cutDate(topic.properties.created);
    }
    return '';
  }

  /**
   * The topic's last modification date, truncated to `YYYY-MM-DD`.
   *
   * @returns The formatted modification date, or an empty string when unavailable.
   */
  get modified(): string {
    const topic = this.topic();
    if (topic?.properties) {
      return this.cutDate(topic.properties.modified);
    }
    return '';
  }

  /**
   * The topic's moderation flag value.
   *
   * @returns The `ismoderated` property (truncated to 10 characters), or the
   *   string `'false'` when unavailable.
   */
  get ismoderated(): string {
    const topic = this.topic();
    if (topic?.properties) {
      return this.cutDate(topic.properties.ismoderated);
    }
    return 'false';
  }
}
