import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  output,
  input,
  OnDestroy,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { TranslocoModule } from '@jsverse/transloco';
import { ActionEmitterResult, ActionResult } from 'app/action-result';
import {
  ContentService,
  Node as ModelNode,
  SpaceService,
} from 'app/core/generated/circabc';
import { setupCalendarDateHandling } from 'app/core/util/date-calendar-util';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { DatePicker } from 'primeng/datepicker';
import { firstValueFrom, Subscription } from 'rxjs';

@Component({
  selector: 'cbc-update-expired-date',
  templateUrl: './update-expired-date.component.html',
  styleUrl: './update-expired-date.component.scss',
  preserveWhitespaces: true,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    MatSlideToggleModule,
    DatePicker,
    TranslocoModule,
  ],
})
export class UpdateExpiredDateComponent
  implements OnInit, OnChanges, OnDestroy
{
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public showModal = false;
  public readonly nodeSelected = input.required<ModelNode>();
  public readonly showModalChange = output();
  public readonly modalHide = output<ActionEmitterResult>();

  public editExpiedDateNodeForm!: FormGroup;
  public defaultValues = [7, 15, 30];
  public processing = false;
  private dateSubscription!: Subscription;

  constructor(
    private fb: FormBuilder,
    private contentService: ContentService,
    private spaceService: SpaceService
  ) {}

  public ngOnInit() {
    this.editExpiedDateNodeForm = this.fb.group(
      {
        name: [''],
        expirationDate: [],
        expirationDateActived: [],
      },
      {
        updateOn: 'change',
      }
    );

    this.dateSubscription = setupCalendarDateHandling(
      this.editExpiedDateNodeForm.controls.expirationDate
    );
  }

  ngOnDestroy() {
    if (this.dateSubscription) {
      this.dateSubscription.unsubscribe();
    }
  }

  async ngOnChanges(changes: SimpleChanges) {
    if (changes.nodeSelected) {
      this.loadForm(changes.nodeSelected.currentValue);
    }
  }

  loadForm(node: ModelNode): void {
    if (this.nodeSelected() && node.properties) {
      this.editExpiedDateNodeForm.controls.name.setValue(node.name);

      this.editExpiedDateNodeForm.controls.expirationDate.setValue(
        node.properties.expiration_date ? new Date() : undefined
      );

      if (
        node.properties.expiration_date === '' ||
        node.properties.expiration_date === undefined
      ) {
        this.editExpiedDateNodeForm.controls.expirationDateActived.setValue(
          false
        );
      } else {
        this.editExpiedDateNodeForm.controls.expirationDateActived.setValue(
          true
        );
      }
    }
  }

  public async saveConfiguration() {
    const tmpNode: ModelNode = {
      id: this.nodeSelected().id,
      name: this.nodeSelected().name,
      title: this.nodeSelected().title,
      description: this.nodeSelected().description,
      properties: {},
    };

    const isFile = this.isFile();

    const nodeSelected = this.nodeSelected();
    if (!isFile) {
      tmpNode.properties = {
        expiration_date:
          this.editExpiedDateNodeForm.value.expirationDate === undefined ||
          this.editExpiedDateNodeForm.value.expirationDate === '' ||
          !this.editExpiedDateNodeForm.value.expirationDateActived
            ? null
            : this.editExpiedDateNodeForm.value.expirationDate,
      };
    } else if (nodeSelected.properties) {
      tmpNode.properties = {
        expiration_date:
          this.editExpiedDateNodeForm.value.expirationDate === undefined ||
          this.editExpiedDateNodeForm.value.expirationDate === '' ||
          !this.editExpiedDateNodeForm.value.expirationDateActived
            ? 'null'
            : this.editExpiedDateNodeForm.value.expirationDate,
        issue_date: nodeSelected.properties.issueDate,
        encoding: nodeSelected.properties.encoding,
        mimetype: nodeSelected.properties.mimetype,
        reference: nodeSelected.properties.reference,
        author: nodeSelected.properties.author,
        url: nodeSelected.properties.url,
        status: nodeSelected.properties.status,
        security: nodeSelected.properties.security_ranking,
      };
    }

    if (isFile) {
      await firstValueFrom(
        this.contentService.putContent(nodeSelected.id as string, tmpNode)
      );
    } else {
      await firstValueFrom(
        this.spaceService.putSpace(nodeSelected.id as string, tmpNode)
      );
    }

    this.showModal = false;
    this.showModalChange.emit();
    this.modalHide.emit({ result: ActionResult.SUCCEED });
  }

  public cancel() {
    this.showModal = false;
    this.showModalChange.emit();
  }

  public isExpired() {
    return this.editExpiedDateNodeForm.value.expirationDate < Date.now();
  }

  public isFile(): boolean {
    let result = false;
    const nodeSelected = this.nodeSelected();
    if (nodeSelected?.type !== undefined) {
      result = nodeSelected.type.indexOf('folder') === -1;
    }
    return result;
  }

  public getCurrentDate() {
    return new Date();
  }
}
