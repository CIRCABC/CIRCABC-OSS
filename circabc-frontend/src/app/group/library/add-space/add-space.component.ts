import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { TranslocoService } from '@ngneat/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { ActionService } from 'app/action-result/action.service';
import {
  Node as ModelNode,
  PagedNodes,
  SpaceService,
  PermissionDefinition,
  PermissionService,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getSuccessTranslation, removeNulls } from 'app/core/util';
import { ValidationService } from 'app/core/validation.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-space',
  templateUrl: './add-space.component.html',
  styleUrls: ['./add-space.component.scss'],
  preserveWhitespaces: true,
})
export class AddSpaceComponent implements OnInit {
  @ViewChild('permitedToogle') permitedToogle!: ElementRef;

  public checked = false;
  @Input()
  public showWizard!: boolean;
  @Input()
  public parentNode!: ModelNode;
  @Output()
  public readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public contents!: PagedNodes;
  public createSpaceForm!: FormGroup;
  public creating = false;
  public perms!: PermissionDefinition;

  public constructor(
    private fb: FormBuilder,
    private spaceService: SpaceService,
    private translateService: TranslocoService,
    private actionService: ActionService,
    private router: Router,
    private route: ActivatedRoute,
    private permissionService: PermissionService,
    private uiMessageService: UiMessageService
  ) {}

  public async ngOnInit() {
    this.contents = await firstValueFrom(
      this.spaceService.getChildren(
        this.parentNode.id as string,
        this.translateService.getActiveLang(),
        false,
        -1,
        1,
        'modified_DESC',
        false,
        false
      )
    );

    if (this.contents !== undefined) {
      this.buildForm();
    }
  }

  public buildForm(): void {
    this.createSpaceForm = this.fb.group(
      {
        name: [
          '',
          [
            Validators.required,
            ValidationService.nameValidator,
            (control: AbstractControl) =>
              ValidationService.fileFolderExistsValidator(
                control,
                this.contents.data
              ),
            // more or less equivalent to:
            // function funcValidator(control: AbstractControl) { ValidationService.fileFolderExistsValidator(control, this.contents) }
            // the executor runs every validator of the array passing the control only,
            // so I encapsulate the control passing in a function to add the additional parameter (this.contents) as I need it
          ],
        ],
        description: [''],
        title: [''],
        managePermited: [true],
      },
      {
        updateOn: 'change',
      }
    );
  }

  public cancelWizard(_action: string): void {
    this.showWizard = false;

    this.createSpaceForm.reset();

    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.CREATE_SPACE;

    this.modalHide.emit(result);
  }

  public async createSpace() {
    this.creating = true;
    if (this.parentNode.id !== undefined) {
      if (this.createSpaceForm.valid) {
        const result: ActionEmitterResult = {};
        result.type = ActionType.CREATE_SPACE;
        try {
          const spaceNode: ModelNode = {
            id: '',
            name: this.createSpaceForm.value.name,
            description: removeNulls(this.createSpaceForm.value.description),
            title: removeNulls(this.createSpaceForm.value.title),
          };
          const response = await firstValueFrom(
            this.spaceService.postSubspace(this.parentNode.id, spaceNode)
          );

          if (!this.createSpaceForm.value.managePermited) {
            this.managerPermission(response);
          } else {
            result.node = response;
            result.result = ActionResult.SUCCEED;
          }
          this.showWizard = false;
        } catch (error) {
          result.result = ActionResult.FAILED;
        }
        this.modalHide.emit(result);
        this.actionService.propagateActionFinished(result);
      }
    }
    this.creating = false;
    this.createSpaceForm.reset();
    this.createSpaceForm.controls.managePermited.setValue(true);
  }

  public setPermissionMsg() {
    const text = this.translateService.translate(
      getSuccessTranslation(ActionType.SET_PERMISSION)
    );
    if (text) {
      this.uiMessageService.addSuccessMessage(text, true);
    }
  }

  public async managerPermission(newFolderNode: ModelNode) {
    const body: PermissionDefinition = {
      inherited: false,
      permissions: {
        profiles: [],
        users: [],
      },
    };

    if (this.parentNode && this.parentNode.id) {
      this.perms = await firstValueFrom(
        this.permissionService.putPermission(newFolderNode.id as string, body)
      );
    }

    this.router.navigate(['../../permissions', newFolderNode.id], {
      queryParams: { from: 'newFolder' },
      relativeTo: this.route,
    });
  }

  get nameControl(): AbstractControl {
    return this.createSpaceForm.controls.name;
  }
}
