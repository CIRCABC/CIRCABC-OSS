import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  linkedSignal,
  output,
  resource,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  GroupConfiguration,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { ModalComponent } from 'app/shared/modal/modal.component';

/**
 * Modal component that renders a reactive form for configuring the "new topic"
 * and "new forum" flagging behaviour of an interest group's forum (newsgroups)
 * service.
 *
 * The form lets an administrator toggle whether new topics and new forums are
 * flagged as "new" and, when enabled, for how many days (age) they remain
 * flagged. The current {@link GroupConfiguration} for the group is loaded via
 * {@link InterestGroupService} through a {@link resource} keyed on
 * {@link groupId}, and the form is patched with the loaded newsgroups settings.
 * Saving persists the updated configuration and notifies the parent through
 * the output events.
 *
 * Key collaborators:
 * - {@link FormBuilder} to build the reactive configuration form.
 * - {@link InterestGroupService} to read and persist the group configuration.
 * - {@link ModalComponent} to render the modal shell.
 */
@Component({
  selector: 'cbc-configure-forum-service',
  templateUrl: './configure-forum-service.component.html',
  styleUrl: './configure-forum-service.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ModalComponent, ReactiveFormsModule, TranslocoModule],
})
export class ConfigureForumServiceComponent {
  /** Reactive forms builder used to construct the configuration form. */
  private readonly fb = inject(FormBuilder);
  /** API client used to read and persist the group's configuration. */
  private readonly groupsService = inject(InterestGroupService);

  /**
   * Input (aliased as `showModal`) controlling the initial visibility of the
   * modal. Provided by the parent component.
   */
  // eslint-disable-next-line @angular-eslint/no-input-rename
  public showModalInput = input(false, { alias: 'showModal' });
  /**
   * Writable signal mirroring {@link showModalInput}. Used internally to open
   * or close the modal (e.g. on save or cancel) while staying in sync with the
   * parent-provided input.
   */
  public showModal = linkedSignal(this.showModalInput);
  /** Required input holding the identifier of the group being configured. */
  public readonly groupId = input.required<string>();
  /** Output emitted when the modal visibility changes (open/close). */
  public readonly showModalChange = output();
  /**
   * Output emitted when the modal closes, carrying the outcome of the
   * configuration action as an {@link ActionEmitterResult}.
   */
  public readonly modalHide = output<ActionEmitterResult>();

  /** Reactive form holding the newsgroups flagging configuration controls. */
  public readonly configurationForm: FormGroup = this.fb.group({
    enableFlagNewTopic: [false],
    enableFlagNewForum: [false],
    ageFlagNewTopic: ['7'],
    ageFlagNewForum: ['7'],
  });
  /** Selectable age values (in days) offered for the flagging controls. */
  public defaultValues = [7, 15, 30];
  /**
   * Resource loading the current group configuration for {@link groupId}. Idle
   * while `groupId` is empty. Load failures are caught internally (no
   * dedicated error UI) so the resource never enters the `error` state.
   */
  private readonly configurationResource = resource({
    params: () => this.groupId() || undefined,
    loader: async ({ params: groupId }) => {
      try {
        return await this.groupsService.getGroupConfigurationAsync({
          id: groupId,
        });
      } catch (error) {
        console.error(error);
        return undefined;
      }
    },
  });
  /** Current group configuration loaded from and saved to the backend. */
  public readonly configuration = computed(() =>
    this.configurationResource.hasValue()
      ? this.configurationResource.value()
      : undefined
  );
  /** Whether a save operation is currently in progress. */
  public readonly processing = signal(false);

  constructor() {
    this.configurationForm.controls.ageFlagNewTopic.disable();
    this.configurationForm.controls.ageFlagNewForum.disable();

    this.configurationForm.controls.enableFlagNewTopic.valueChanges.subscribe(
      (value) => {
        if (value) {
          this.configurationForm.controls.ageFlagNewTopic.enable();
        } else {
          this.configurationForm.controls.ageFlagNewTopic.disable();
        }
      }
    );

    this.configurationForm.controls.enableFlagNewForum.valueChanges.subscribe(
      (value) => {
        if (value) {
          this.configurationForm.controls.ageFlagNewForum.enable();
        } else {
          this.configurationForm.controls.ageFlagNewForum.disable();
        }
      }
    );

    // Patches the form with the newsgroups settings once the configuration
    // resource resolves. Syncing loaded data into a non-signal FormGroup is a
    // genuine imperative side effect, so `effect` is used rather than a
    // computed/linkedSignal.
    effect(() => {
      const configuration = this.configuration();
      if (configuration) {
        this.configurationForm.patchValue(configuration.newsgroups);
      }
    });
  }

  /**
   * Persists the current form values as the group's newsgroups configuration.
   *
   * The string age values from the form are converted to numbers before being
   * sent. On success the form is patched with the returned configuration, the
   * modal is closed and {@link showModalChange} is emitted. Regardless of the
   * outcome, {@link modalHide} is emitted with an {@link ActionEmitterResult}
   * describing whether the update succeeded or failed.
   *
   * @returns A promise that resolves once the save attempt has completed and
   * the result has been emitted.
   */
  public async saveConfiguration() {
    const res: ActionEmitterResult = {};
    res.type = ActionType.UPDATE_GROUP_CONFIGURATION;
    this.processing.set(true);

    try {
      // must be used to convert ageFlagNewTopic and ageFlagNewForum from string to number
      const newsgroupConf: GroupConfiguration = {
        newsgroups: {
          enableFlagNewTopic: this.configurationForm.value.enableFlagNewTopic,
          enableFlagNewForum: this.configurationForm.value.enableFlagNewForum,
          ageFlagNewTopic: +this.configurationForm.value.ageFlagNewTopic,
          ageFlagNewForum: +this.configurationForm.value.ageFlagNewForum,
        },
      };

      const updatedConfiguration =
        await this.groupsService.putGroupConfigurationAsync({
          id: this.groupId(),
          groupConfiguration: newsgroupConf,
        });
      this.configurationResource.value.set(updatedConfiguration);
      this.configurationForm.patchValue(updatedConfiguration.newsgroups);
      res.result = ActionResult.SUCCEED;
      this.showModal.set(false);
      this.showModalChange.emit();
    } catch (error) {
      console.error(error);
      res.result = ActionResult.FAILED;
    }

    this.processing.set(false);
    this.modalHide.emit(res);
  }

  /**
   * Closes the modal without saving and notifies the parent by emitting
   * {@link showModalChange}.
   */
  public cancel() {
    this.showModal.set(false);
    this.showModalChange.emit();
  }
}
