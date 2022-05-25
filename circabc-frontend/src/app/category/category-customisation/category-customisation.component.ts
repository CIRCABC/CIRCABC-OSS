import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ActionService } from 'app/action-result/action.service';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result/index';
import {
  CategoryService,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-category-customisation',
  templateUrl: './category-customisation.component.html',
  styleUrls: ['./category-customisation.component.scss'],
  preserveWhitespaces: true,
})
export class CategoryCustomisationComponent implements OnInit {
  public logos: ModelNode[] = [];
  public loading = false;
  public showUploadModal = false;
  public categoryId!: string;
  public category!: ModelNode;

  constructor(
    private route: ActivatedRoute,
    private categoryService: CategoryService,
    private nodesService: NodesService,
    private actionService: ActionService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(async (params) => {
      await this.loadCategory(params.id);
      await this.listLogos(params.id);
    });
  }

  private async loadCategory(id: string) {
    this.categoryId = id;
    this.loading = true;
    try {
      this.category = await firstValueFrom(this.nodesService.getNode(id));
    } catch (error) {
      console.error('impossible to get the category');
    }

    this.loading = false;
  }

  private async listLogos(id: string) {
    this.logos = [];
    this.categoryId = id;
    this.loading = true;
    try {
      this.logos = await firstValueFrom(
        this.categoryService.getCategoryLogoByCategoryId(id)
      );
    } catch (error) {
      console.error('impossible to get the logos');
    }
    this.loading = false;
  }

  public async refresh(result: ActionEmitterResult) {
    if (result.result === ActionResult.SUCCEED) {
      await this.listLogos(this.categoryId);
    }
    this.showUploadModal = false;
  }

  public isSelected(id: string | undefined): boolean {
    if (
      id &&
      this.category &&
      this.category.properties &&
      this.category.properties.logoRef
    ) {
      if (this.category.properties.logoRef.indexOf(id) !== -1) {
        return true;
      }
    }

    return false;
  }

  public async select(id: string | undefined) {
    if (id && this.categoryId) {
      try {
        const result: ActionEmitterResult = {
          type: ActionType.ADD_CATEGORY_LOGO,
          node: { id: this.categoryId },
          result: ActionResult.SUCCEED,
        };
        await firstValueFrom(
          this.categoryService.selectCategoryLogoByLogoId(this.categoryId, id)
        );
        this.actionService.propagateActionFinished(result);
        await this.loadCategory(this.categoryId);
      } catch (error) {
        console.error('impossible to select the image');
      }
    }
  }

  public async delete(id: string | undefined) {
    if (this.categoryId && id) {
      try {
        this.logos = await firstValueFrom(
          this.categoryService.deleteCategoryLogoByLogoId(this.categoryId, id)
        );
      } catch (error) {
        console.error('impossible to select the image');
      }
    }
  }
}
