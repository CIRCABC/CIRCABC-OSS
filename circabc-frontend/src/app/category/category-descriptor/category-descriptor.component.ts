import {
  Component,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
  input,
} from '@angular/core';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { ActionService } from 'app/action-result/action.service';
import { Node as ModelNode, NodesService } from 'app/core/generated/circabc';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { Subscription, firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-category-descriptor',
  templateUrl: './category-descriptor.component.html',
  styleUrl: './category-descriptor.component.scss',
  preserveWhitespaces: true,
  imports: [DownloadPipe, SecurePipe],
})
export class CategoryDescriptorComponent
  implements OnInit, OnChanges, OnDestroy
{
  readonly categoryId = input.required<string>();
  public category!: ModelNode;
  private actionFinishedSubscription$!: Subscription;

  constructor(
    private i18nPipe: I18nPipe,
    private nodesService: NodesService,
    private actionService: ActionService
  ) {}

  private subscribe() {
    this.actionFinishedSubscription$ =
      this.actionService.actionFinished$.subscribe(
        async (actionResult: ActionEmitterResult) => {
          if (
            (actionResult.type === ActionType.UPDATE_CATEGORY ||
              actionResult.type === ActionType.ADD_CATEGORY_LOGO) &&
            actionResult.result === ActionResult.SUCCEED &&
            actionResult.node &&
            actionResult.node.id
          ) {
            await this.loadCategoryNode(actionResult.node.id);
          }
        }
      );
  }

  async ngOnInit() {
    this.subscribe();
    await this.loadCategoryNode(this.categoryId());
  }

  async ngOnChanges(changes: SimpleChanges) {
    if (changes.categoryId) {
      await this.loadCategoryNode(changes.categoryId.currentValue);
    }
  }

  ngOnDestroy(): void {
    this.actionFinishedSubscription$.unsubscribe();
  }

  private async loadCategoryNode(categoryId: string) {
    if (categoryId) {
      this.category = await firstValueFrom(
        this.nodesService.getNode(categoryId)
      );
    }
  }

  public hasModelNodeLogo(): boolean {
    if (this.category?.properties) {
      return !(
        this.category.properties.logoRef === undefined ||
        this.category.properties.logoRef === ''
      );
    }
    return false;
  }

  public getLogoRef(): string {
    if (this.category?.properties) {
      const parts = this.category.properties.logoRef.split('/');
      return parts[parts.length - 1];
    }
    return '';
  }

  public getCategoryGroupDescription(): string {
    let result = '';
    if (this.category?.title !== undefined) {
      result = this.i18nPipe.transform(this.category.title);
      if (result === '') {
        result = this.i18nPipe.transform(this.category.title);
      }
    }

    if (this.category && result === '' && this.category.name) {
      result = this.category.name;
    }

    return result;
  }
}
