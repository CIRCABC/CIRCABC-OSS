import { Component, Input, OnInit, output, input } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  CategoryService,
  Node as ModelNode,
  NodesService,
  Profile,
  ProfileService,
} from 'app/core/generated/circabc';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-import-profile',
  templateUrl: './import-profile.component.html',
  preserveWhitespaces: true,
  imports: [ModalComponent, ReactiveFormsModule, I18nPipe, TranslocoModule],
})
export class ImportProfileComponent implements OnInit {
  readonly igNodeId = input.required<string>();
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  readonly profileImported = output<ActionEmitterResult>();

  public importing = false;
  public ig!: ModelNode;
  public exportedProfiles!: Profile[];
  public importProfileForm!: FormGroup;

  constructor(
    private profileService: ProfileService,
    private nodesService: NodesService,
    private categoryService: CategoryService,
    private fb: FormBuilder
  ) {}

  async ngOnInit() {
    this.importProfileForm = this.fb.group(
      {
        selectedProfile: ['', Validators.required],
      },
      {
        updateOn: 'change',
      }
    );

    const igNodeId = this.igNodeId();
    if (igNodeId) {
      this.ig = await firstValueFrom(this.nodesService.getNode(igNodeId));

      if (this.ig.parentId) {
        this.exportedProfiles = await firstValueFrom(
          this.categoryService.getExportedProfiles(this.ig.parentId, this.ig.id)
        );
      }
    }
  }

  cancelWizard() {
    this.showModal = false;

    const result: ActionEmitterResult = {};
    result.type = ActionType.IMPORT_PROFILE;
    result.result = ActionResult.CANCELED;

    this.importProfileForm.reset();
    this.profileImported.emit(result);
  }

  async import() {
    this.importing = true;
    const result: ActionEmitterResult = {};
    result.type = ActionType.IMPORT_PROFILE;

    for (const profile of this.exportedProfiles) {
      if (this.importProfileForm.value.selectedProfile === profile.id) {
        const body: Profile = profile;
        try {
          await firstValueFrom(
            this.profileService.postImportedProfile(this.igNodeId(), body)
          );
          result.result = ActionResult.SUCCEED;
          this.importProfileForm.reset();
        } catch (_error) {
          result.result = ActionResult.FAILED;
        }
      }
    }
    this.profileImported.emit(result);
    this.importing = false;
  }
}
