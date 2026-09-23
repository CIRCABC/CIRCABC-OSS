import { Location } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  output,
  resource,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
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
  ForumService,
  NodesService,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import {
  fileNameValidator,
  maxLengthTitleValidator,
  titleValidator,
} from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Component that renders the details view/edit form for a single forum
 * (newsgroup) node.
 *
 * It loads the forum node identified by the `forumId` route parameter, exposes
 * a reactive form ({@link ViewEditDetailsForumComponent.updateForumForm}) with
 * the multilingual title, description and technical name fields, and resolves
 * the creator/modifier users so their information can be displayed alongside
 * versioning metadata. Users with forum-admin permissions can edit these
 * fields and persist changes back to the backend.
 *
 * Key collaborators:
 * - {@link NodesService} to fetch the forum node.
 * - {@link ForumService} to persist forum updates.
 * - {@link UserService} to resolve creator/modifier user details.
 * - {@link PermissionEvaluatorService} to determine admin capabilities.
 * - {@link UiMessageService} to surface error messages.
 *
 * @remarks The template imports {@link MultilingualInputComponent},
 * {@link ControlMessageComponent} and {@link SpinnerComponent} for rendering.
 */
@Component({
  selector: 'cbc-view-edit-details-forum',
  templateUrl: './view-edit-details-forum.component.html',
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
export class ViewEditDetailsForumComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly nodesService = inject(NodesService);
  private readonly userService = inject(UserService);
  private readonly forumService = inject(ForumService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly location = inject(Location);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly permEvalService = inject(PermissionEvaluatorService);

  /**
   * Output event emitted after a forum has been successfully updated.
   *
   * Consumers can listen to this event to refresh the forum display once
   * changes are persisted.
   */
  public readonly forumUpdated = output();

  /** The current route params, as a signal. */
  private readonly routeParams = toSignal(this.route.params);

  /** Identifier of the forum node, resolved from the `forumId` route parameter. */
  public readonly forumId = computed(() => this.routeParams()?.forumId ?? '');

  /**
   * Resource that loads the forum node together with its creator/modifier
   * users, reactively re-fetching whenever {@link forumId} changes.
   *
   * @remarks There is no dedicated error UI for this component: backend
   * errors are caught inside the loader and surfaced via
   * {@link UiMessageService} (as the previous imperative code did), and the
   * resource resolves to `undefined` in that case instead of entering the
   * error state.
   */
  private readonly forumResource = resource({
    params: () => this.forumId() || undefined,
    loader: async ({ params: id }) => {
      try {
        const forum = await this.nodesService.getNodeAsync({ id });

        let creator: User | undefined;
        let modifier: User | undefined;
        // get creator and modifier users to retrieve their names for display
        if (forum.properties) {
          creator = await this.userService.getUserAsync({
            userId: forum.properties.creator,
          });
          modifier = await this.userService.getUserAsync({
            userId: forum.properties.modifier,
          });
        }

        return { forum, creator, modifier };
      } catch (error: unknown) {
        const body = (error as { _body?: string })?._body;
        if (body) {
          try {
            const jsonError = JSON.parse(body) as Record<string, string>;
            if ('message' in jsonError) {
              this.uiMessageService.addErrorMessage(jsonError.message);
            }
          } catch {
            // body is not valid JSON — nothing to extract
          }
        }
        return undefined;
      }
    },
  });

  /** The forum node currently being viewed or edited. */
  public readonly forum = computed(() => this.forumResource.value()?.forum);
  /** User who created the forum, resolved for display purposes. */
  public readonly creator = computed(() => this.forumResource.value()?.creator);
  /** User who last modified the forum, resolved for display purposes. */
  public readonly modifier = computed(
    () => this.forumResource.value()?.modifier
  );

  // viewing variable has been disabled because of request https://webgate.ec.europa.eu/CITnet/jira/browse/DIGITCIRCABC-3489
  /**
   * Whether the form is in read-only viewing mode.
   *
   * @remarks Currently kept `false`; view-only behaviour was disabled per
   * request DIGITCIRCABC-3489.
   */
  public viewing = false;
  /** Whether an update request is currently in progress (used to gate the UI). */
  public processing = false;

  /** Reactive form backing the forum title, description and technical name fields. */
  public readonly updateForumForm: FormGroup = this.formBuilder.group(
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
    },
    {
      updateOn: 'change',
    }
  );

  constructor() {
    // The form is patched imperatively from the loaded resource value;
    // syncing a signal into the non-signal, imperative FormGroup API is a
    // legitimate use of `effect`.
    effect(() => {
      const forum = this.forum();
      if (forum === undefined) {
        return;
      }
      // `titleValidator` expects `null` (not `undefined`) for an empty value;
      // `fileNameValidator` requires a string and has no undefined guard.
      this.updateForumForm.controls.title.patchValue(forum.title ?? null);
      this.updateForumForm.controls.description.patchValue(forum.description);
      this.updateForumForm.controls.name.patchValue(forum.name ?? '');
    });
  }

  /**
   * Truncates a date-time string to its date portion (first 10 characters).
   *
   * @param dateString - The ISO date-time string to trim.
   * @returns The `YYYY-MM-DD` prefix, or an empty string when the input is
   * `undefined`.
   */
  public cutDate(dateString: string) {
    return dateString === undefined ? '' : dateString.substring(0, 10);
  }

  /** Navigates back to the previous location in the browser history. */
  public goBack() {
    this.location.back();
  }

  /**
   * Enables all controls of the update form so the forum details can be edited
   * and switches the component out of viewing mode.
   */
  public enableEdit() {
    // enable all form controls for edit
    Object.keys(this.updateForumForm.controls).forEach((key) => {
      (this.updateForumForm.get(key) as AbstractControl).enable();
    });

    this.viewing = false;
  }

  /**
   * Discards pending edits by reloading the forum data into the form and then
   * navigating back.
   */
  public cancel() {
    this.forumResource.reload();
    this.goBack();
  }

  /**
   * Persists the edited title, description and name to the backend forum node.
   *
   * On success, emits {@link ViewEditDetailsForumComponent.forumUpdated} and
   * navigates back. The `processing` flag is always cleared when finished.
   *
   * @returns A promise that resolves once the update attempt completes.
   * @throws Error if the forum node has not been loaded (`forum` is undefined).
   */
  public async update() {
    try {
      this.processing = true;

      const forum = this.forum();
      if (forum === undefined) {
        throw new Error('"forum" is undefined.');
      }

      forum.title = this.updateForumForm.controls.title.value;
      forum.description = this.updateForumForm.controls.description.value;
      forum.name = this.updateForumForm.controls.name.value;

      await this.forumService.putForumAsync({
        id: forum.id as string,
        node: forum,
      });

      // emit an event to signal that a forum has been updated
      // will be used to redisplay the view
      this.forumUpdated.emit();
      this.goBack();
    } finally {
      this.processing = false;
    }
  }

  /**
   * Determines whether the current user has newsgroup (forum) admin rights on
   * the loaded forum.
   *
   * @returns `true` if the user is a newsgroup admin, otherwise `false`.
   */
  public isForumAdmin(): boolean {
    const forum = this.forum();
    return forum !== undefined && this.permEvalService.isNewsgroupAdmin(forum);
  }

  /** The reactive form control backing the forum technical name field. */
  get nameControl(): AbstractControl {
    return this.updateForumForm.controls.name;
  }

  /** The forum version label, or an empty string when properties are absent. */
  get versionLabel(): string {
    const forum = this.forum();
    if (forum?.properties) {
      return forum.properties.versionLabel;
    }
    return '';
  }

  /**
   * The forum creation date, truncated to `YYYY-MM-DD`, or an empty string when
   * properties are absent.
   */
  get created(): string {
    const forum = this.forum();
    if (forum?.properties) {
      return this.cutDate(forum.properties.created);
    }
    return '';
  }

  /**
   * The forum last-modified date, truncated to `YYYY-MM-DD`, or an empty string
   * when properties are absent.
   */
  get modified(): string {
    const forum = this.forum();
    if (forum?.properties) {
      return this.cutDate(forum.properties.modified);
    }
    return '';
  }

  /**
   * Whether the forum is moderated, as a string flag.
   *
   * @returns The `ismoderated` property value, or `'false'` when properties are
   * absent.
   */
  get ismoderated(): string {
    const forum = this.forum();
    if (forum?.properties) {
      return forum.properties.ismoderated;
    }
    return 'false';
  }
}
