import { Component, Input, OnInit, output, input } from '@angular/core';
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
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-configure-forum-service',
  templateUrl: './configure-forum-service.component.html',
  styleUrl: './configure-forum-service.component.scss',
  preserveWhitespaces: true,
  imports: [ModalComponent, ReactiveFormsModule, TranslocoModule],
})
export class ConfigureForumServiceComponent implements OnInit {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public showModal = false;
  public readonly groupId = input.required<string>();
  public readonly showModalChange = output();
  public readonly modalHide = output<ActionEmitterResult>();

  public configurationForm!: FormGroup;
  public defaultValues = [7, 15, 30];
  public configuration!: GroupConfiguration;
  public processing = false;

  constructor(
    private fb: FormBuilder,
    private groupsService: InterestGroupService
  ) {}

  async ngOnInit() {
    this.configurationForm = this.fb.group({
      enableFlagNewTopic: [false],
      enableFlagNewForum: [false],
      ageFlagNewTopic: ['7'],
      ageFlagNewForum: ['7'],
    });
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

    const groupId = this.groupId();
    if (groupId) {
      this.configuration = await firstValueFrom(
        this.groupsService.getGroupConfiguration(groupId)
      );
      this.configurationForm.patchValue(this.configuration.newsgroups);
    }
  }

  public async saveConfiguration() {
    const res: ActionEmitterResult = {};
    res.type = ActionType.UPDATE_GROUP_CONFIGURATION;
    this.processing = true;

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

      this.configuration = await firstValueFrom(
        this.groupsService.putGroupConfiguration(this.groupId(), newsgroupConf)
      );
      this.configurationForm.patchValue(this.configuration.newsgroups);
      res.result = ActionResult.SUCCEED;
      this.showModal = false;
      this.showModalChange.emit();
    } catch (_error) {
      res.result = ActionResult.FAILED;
    }

    this.processing = false;
    this.modalHide.emit(res);
  }

  public cancel() {
    this.showModal = false;
    this.showModalChange.emit();
  }
}
