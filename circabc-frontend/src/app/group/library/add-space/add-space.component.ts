import {
  Component,
  ElementRef,
  Input,
  OnInit,
  viewChild,
  output,
  input,
  OnDestroy,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { ActionService } from 'app/action-result/action.service';
import {
  Node as ModelNode,
  PagedNodes,
  PermissionDefinition,
  PermissionService,
  SpaceService,
} from 'app/core/generated/circabc';
import { removeNulls } from 'app/core/util';
import {
  fileFolderExistsValidator,
  nameValidator,
} from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { HintComponent } from 'app/shared/hint/hint.component';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { firstValueFrom, Subscription } from 'rxjs';
import { DatePicker } from 'primeng/datepicker';
import { setupCalendarDateHandling } from 'app/core/util/date-calendar-util';

@Component({
  selector: 'cbc-add-space',
  templateUrl: './add-space.component.html',
  styleUrl: './add-space.component.scss',
  preserveWhitespaces: true,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    ControlMessageComponent,
    MultilingualInputComponent,
    HintComponent,
    MatSlideToggleModule,
    TranslocoModule,
    DatePicker,
  ],
})
export class AddSpaceComponent implements OnInit, OnDestroy {
  readonly permitedToogle = viewChild.required<ElementRef>('permitedToogle');

  public checked = false;
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public showWizard!: boolean;
  public readonly parentNode = input.required<ModelNode>();
  public readonly modalHide = output<ActionEmitterResult>();

  public contents!: PagedNodes;
  public createSpaceForm!: FormGroup;
  public creating = false;
  public perms!: PermissionDefinition;
  public dateRequired = false;

  private dateSubscription!: Subscription;

  public constructor(
    private fb: FormBuilder,
    private spaceService: SpaceService,
    private translateService: TranslocoService,
    private actionService: ActionService,
    private router: Router,
    private route: ActivatedRoute,
    private permissionService: PermissionService
  ) {}

  public async ngOnInit() {
    this.contents = await firstValueFrom(
      this.spaceService.getChildren(
        this.parentNode().id as string,
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
            nameValidator,
            (control: AbstractControl) =>
              fileFolderExistsValidator(control, this.contents.data),
            // more or less equivalent to:
            // function funcValidator(control: AbstractControl) { fileFolderExistsValidator(control, this.contents) }
            // the executor runs every validator of the array passing the control only,
            // so I encapsulate the control passing in a function to add the additional parameter (this.contents) as I need it
          ],
        ],
        description: [''],
        title: [''],
        managePermited: [true],
        expirationDate: [],
        expirationDateActived: [false],
      },
      {
        updateOn: 'change',
      }
    );

    this.dateSubscription = setupCalendarDateHandling(
      this.createSpaceForm.controls.expirationDate
    );

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    this.createSpaceForm.valueChanges.subscribe((value: any) => {
      if (
        value.expirationDateActived &&
        this.createSpaceForm.controls.expirationDate.value === null
      ) {
        this.dateRequired = true;
      } else {
        this.dateRequired = false;
      }
    });
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
    const parentNode = this.parentNode();
    if (parentNode.id !== undefined) {
      if (this.createSpaceForm.valid) {
        const result: ActionEmitterResult = {};
        result.type = ActionType.CREATE_SPACE;
        try {
          const spaceNode: ModelNode = {
            id: '',
            name: this.createSpaceForm.value.name,
            description: removeNulls(this.createSpaceForm.value.description),
            title: removeNulls(this.createSpaceForm.value.title),
            properties: {
              expiration_date: this.createSpaceForm.value.expirationDateActived
                ? this.createSpaceForm.value.expirationDate
                : '',
            },
          };
          const response = await firstValueFrom(
            this.spaceService.postSubspace(parentNode.id, spaceNode)
          );

          if (this.createSpaceForm.value.managePermited) {
            result.node = response;
            result.result = ActionResult.SUCCEED;
          } else {
            this.managerPermission(response);
          }
          this.showWizard = false;
        } catch (_error) {
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

  public async managerPermission(newFolderNode: ModelNode) {
    const body: PermissionDefinition = {
      inherited: false,
      permissions: {
        profiles: [],
        users: [],
      },
    };

    if (this.parentNode()?.id) {
      this.perms = await firstValueFrom(
        this.permissionService.putPermission(newFolderNode.id as string, body)
      );
    }

    this.router.navigate(['../../permissions', newFolderNode.id], {
      queryParams: { from: 'library' },
      relativeTo: this.route,
    });
  }

  get nameControl(): AbstractControl {
    return this.createSpaceForm.controls.name;
  }

  public isExpired() {
    return this.createSpaceForm.value.expirationDate < Date.now();
  }

  ngOnDestroy() {
    if (this.dateSubscription) {
      this.dateSubscription.unsubscribe();
    }
  }
}
