import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  GroupConfiguration,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-configure-forum-service',
  templateUrl: './configure-forum-service.component.html',
  styleUrls: ['./configure-forum-service.component.scss'],
  preserveWhitespaces: true,
})
export class ConfigureForumServiceComponent implements OnInit {
  @Input()
  public showModal = false;
  @Input()
  public groupId!: string;
  @Output()
  public readonly showModalChange = new EventEmitter();
  @Output()
  public readonly modalHide = new EventEmitter<ActionEmitterResult>();

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

    if (this.groupId) {
      this.configuration = await firstValueFrom(
        this.groupsService.getGroupConfiguration(this.groupId)
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
        this.groupsService.putGroupConfiguration(this.groupId, newsgroupConf)
      );
      this.configurationForm.patchValue(this.configuration.newsgroups);
      res.result = ActionResult.SUCCEED;
      this.showModal = false;
      this.showModalChange.emit();
    } catch (error) {
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
