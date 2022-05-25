import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, Params } from '@angular/router';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  ContentService,
  InformationService,
  News,
  NodesService,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { UploadService } from 'app/core/upload.service';
import { ValidationService } from 'app/core/validation.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-news',
  templateUrl: './add-news.component.html',
  styleUrls: ['./add-news.component.scss'],
  preserveWhitespaces: false,
})
export class AddNewsComponent implements OnInit {
  public newsForm!: FormGroup;
  public groupId!: string;
  public filesToUpload: File[] = [];
  public executing = false;
  public editableNews!: News;
  public newsId!: string;
  public defaultDate: Date = new Date();
  public infoId: string | undefined;
  public infoNode: ModelNode | undefined;
  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private informationService: InformationService,
    private router: Router,
    private uploadService: UploadService,
    private contentService: ContentService,
    private nodeService: NodesService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(
      async (params) => await this.loadPageData(params)
    );
  }

  private async loadPageData(params: Params) {
    this.groupId = params.id;
    this.newsId = params.newsId;
    this.infoId = params.infoId;
    this.newsForm = this.fb.group(
      {
        title: ['', [ValidationService.nonEmptyTitle]],
        size: [1],
        layout: ['normal'],
        pattern: ['text'],
        content: ['', Validators.required],
        date: [this.defaultDate],
        file: [],
        files: [],
        url: [],
      },
      {
        updateOn: 'change',
      }
    );
    if (this.infoId) {
      this.infoNode = await firstValueFrom(
        this.nodeService.getNode(this.infoId)
      );
    }
    await this.loadEditableNews(this.newsId);

    this.subscribeToPatternChanges();
  }

  async loadEditableNews(id: string) {
    if (id !== undefined) {
      this.editableNews = await firstValueFrom(
        this.informationService.getNews(this.newsId)
      );

      if (this.editableNews.pattern !== 'iframe') {
        this.newsForm.controls.title.patchValue(this.editableNews.title);
      }

      this.newsForm.controls.size.patchValue(this.editableNews.size);
      this.newsForm.controls.layout.patchValue(this.editableNews.layout);
      this.newsForm.controls.pattern.patchValue(this.editableNews.pattern);
      this.newsForm.controls.content.patchValue(this.editableNews.content);
      if (this.editableNews.date) {
        this.newsForm.controls.date.patchValue(
          new Date(this.editableNews.date)
        );
      }
      this.newsForm.controls.url.patchValue(this.editableNews.url);
      this.newsForm.controls.files.setValue(this.editableNews.files);
    }
  }

  subscribeToPatternChanges() {
    const patternChanges = this.newsForm.controls.pattern.valueChanges;

    // subscribe to changes in order to update the validator when a change happens
    patternChanges.subscribe((pattern) => {
      if (pattern === 'iframe') {
        this.newsForm.controls.title.setValidators(null);
        this.newsForm.controls.content.setValidators(null);
        this.newsForm.controls.url.setValidators([
          Validators.required,
          ValidationService.urlValidator,
        ]);
        this.newsForm.controls.title.updateValueAndValidity();
        this.newsForm.controls.content.updateValueAndValidity();
        this.newsForm.controls.url.updateValueAndValidity();
      } else {
        this.newsForm.controls.title.setValidators([
          ValidationService.nonEmptyTitle,
        ]);
        this.newsForm.controls.content.setValidators([Validators.required]);
        this.newsForm.controls.url.setValidators(null);
        this.newsForm.controls.title.updateValueAndValidity();
        this.newsForm.controls.content.updateValueAndValidity();
        this.newsForm.controls.url.updateValueAndValidity();
      }
    });
  }

  inEditMode(): boolean {
    return this.editableNews !== undefined;
  }

  getTypeOfCard() {
    return `${this.newsForm.value.pattern}-${this.newsForm.value.layout}-${this.newsForm.value.size}`;
  }

  async saveNews() {
    this.executing = true;
    const news: News = { ...this.newsForm.value };
    news.size = +this.newsForm.value.size;

    const result: ActionEmitterResult = {};
    result.type = ActionType.CREATE_INFORMATION_NEWS;

    try {
      if (news.date) {
        const date = new Date(news.date);
        news.date = `${date.getFullYear()}-${
          date.getMonth() + 1
        }-${date.getDate()}T00:00:00.000Z`;
      }

      const createdNews = await firstValueFrom(
        this.informationService.postInformationNews(this.groupId, news)
      );

      if (
        (createdNews.pattern === 'image' ||
          createdNews.pattern === 'document') &&
        createdNews.id
      ) {
        if (this.filesToUpload.length === 1) {
          await this.uploadService.uploadNewFile(
            this.filesToUpload[0],
            createdNews.id
          );
        }
      }

      result.result = ActionResult.SUCCEED;
    } catch (error) {
      result.result = ActionResult.FAILED;
    }

    if (result.result === ActionResult.SUCCEED) {
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.router.navigate(['../..'], { relativeTo: this.route });
    }

    this.executing = false;
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public fileChangeEvent(fileInput: any) {
    const filesList = fileInput.target.files as FileList;
    this.handleFiles(filesList);
  }

  private handleFiles(filesList: FileList) {
    this.filesToUpload = [];
    for (let i = 0; i < filesList.length; i += 1) {
      const fileItem = filesList.item(i);
      if (fileItem) {
        this.filesToUpload.push(fileItem);
      }
    }
  }

  public getFileName() {
    if (this.filesToUpload && this.filesToUpload.length > 0) {
      return this.filesToUpload[0].name;
    } else {
      return '';
    }
  }

  public getFile() {
    if (this.filesToUpload && this.filesToUpload.length > 0) {
      return this.filesToUpload[0];
    } else {
      return undefined;
    }
  }

  isDateSelected(): boolean {
    return this.newsForm.value.pattern === 'date';
  }

  isFileOrImageSelected(): boolean {
    return (
      this.newsForm.value.pattern === 'image' ||
      this.newsForm.value.pattern === 'document'
    );
  }

  isIFrameSelected(): boolean {
    return this.newsForm.value.pattern === 'iframe';
  }

  async updateNews() {
    this.executing = true;
    const news: News = { ...this.newsForm.value };
    news.size = +this.newsForm.value.size;

    const result: ActionEmitterResult = {};
    result.type = ActionType.UPDATE_INFORMATION_NEWS;

    try {
      if (news.date) {
        const date = new Date(news.date);
        news.date = `${date.getFullYear()}-${
          date.getMonth() + 1
        }-${date.getDate()}T00:00:00.000Z`;
      }

      const createdNews = await firstValueFrom(
        this.informationService.putNews(this.newsId, news)
      );

      if (
        (createdNews.pattern === 'image' ||
          createdNews.pattern === 'document') &&
        createdNews.id
      ) {
        if (this.filesToUpload.length === 1) {
          if (this.editableNews.files && this.editableNews.files.length > 0) {
            const fileId = this.editableNews.files[0].id;
            if (fileId) {
              await firstValueFrom(this.contentService.deleteContent(fileId));
            }
          }

          await this.uploadService.uploadNewFile(
            this.filesToUpload[0],
            createdNews.id
          );
        }
      }

      result.result = ActionResult.SUCCEED;
    } catch (error) {
      result.result = ActionResult.FAILED;
    }

    if (result.result === ActionResult.SUCCEED) {
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.router.navigate(['../..'], { relativeTo: this.route });
    }
    this.executing = false;
  }

  async cancel() {
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    this.router.navigate(['../..'], { relativeTo: this.route });
  }

  public isFormValid(): boolean {
    if (
      !this.inEditMode() &&
      (this.newsForm.value.pattern === 'document' ||
        this.newsForm.value.pattern === 'image')
    ) {
      return this.newsForm.valid && this.filesToUpload.length > 0;
    } else if (this.inEditMode() && this.newsForm.value.pattern === 'iframe') {
      return true;
    } else {
      return this.newsForm.valid;
    }
  }

  get titleControl(): AbstractControl {
    return this.newsForm.controls.title;
  }

  get contentControl(): AbstractControl {
    return this.newsForm.controls.content;
  }
}
