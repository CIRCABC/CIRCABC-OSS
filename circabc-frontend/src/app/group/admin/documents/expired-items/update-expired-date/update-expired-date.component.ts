import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActionEmitterResult } from 'app/action-result';
import { Node as ModelNode, ContentService } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-update-expired-date',
  templateUrl: './update-expired-date.component.html',
  styleUrls: ['./update-expired-date.component.scss'],
  preserveWhitespaces: true,
})
export class UpdateExpiredDateComponent implements OnInit, OnChanges {
  @Input()
  public showModal = false;
  @Input()
  public nodeSelected!: ModelNode;
  @Output()
  public readonly showModalChange = new EventEmitter();
  @Output()
  public readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public editExpiedDateNodeForm!: FormGroup;
  public defaultValues = [7, 15, 30];
  public processing = false;

  constructor(
    private fb: FormBuilder,
    private contentService: ContentService
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
  }

  async ngOnChanges(changes: SimpleChanges) {
    if (changes.nodeSelected) {
      this.loadForm(changes.nodeSelected.currentValue);
    }
  }

  loadForm(node: ModelNode): void {
    if (this.nodeSelected && node.properties) {
      this.editExpiedDateNodeForm.controls.name.setValue(node.name);

      this.editExpiedDateNodeForm.controls.expirationDate.setValue(
        node.properties.expiration_date
          ? new Date(node.properties.expiration_date)
          : undefined
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
      id: this.nodeSelected.id,
      name: this.nodeSelected.name,
      title: this.nodeSelected.title,
      description: this.nodeSelected.description,
      properties: {},
    };

    const isFile = this.isFile();

    if (!isFile) {
      tmpNode.properties = {
        expiration_date:
          this.editExpiedDateNodeForm.value.expirationDate === undefined ||
          this.editExpiedDateNodeForm.value.expirationDate === '' ||
          !this.editExpiedDateNodeForm.value.expirationDateActived
            ? 'null'
            : this.editExpiedDateNodeForm.value.expirationDate,
      };
    } else if (this.nodeSelected.properties) {
      tmpNode.properties = {
        expiration_date:
          this.editExpiedDateNodeForm.value.expirationDate === undefined ||
          this.editExpiedDateNodeForm.value.expirationDate === '' ||
          !this.editExpiedDateNodeForm.value.expirationDateActived
            ? 'null'
            : this.editExpiedDateNodeForm.value.expirationDate,
        issue_date: this.nodeSelected.properties.issueDate,
        encoding: this.nodeSelected.properties.encoding,
        mimetype: this.nodeSelected.properties.mimetype,
        reference: this.nodeSelected.properties.reference,
        author: this.nodeSelected.properties.author,
        url: this.nodeSelected.properties.url,
        status: this.nodeSelected.properties.status,
        security: this.nodeSelected.properties.security_ranking,
      };
    }

    if (isFile) {
      await firstValueFrom(
        this.contentService.putContent(this.nodeSelected.id as string, tmpNode)
      );
    }

    this.showModal = false;
    this.showModalChange.emit();
    this.modalHide.emit();
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
    if (
      this.nodeSelected !== undefined &&
      this.nodeSelected.type !== undefined
    ) {
      result = this.nodeSelected.type.indexOf('folder') === -1;
    }
    return result;
  }
}
