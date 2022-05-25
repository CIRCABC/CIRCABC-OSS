import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ActionEmitterResult } from 'app/action-result';
import { ActionResult } from 'app/action-result/action-result';
import { ActionType } from 'app/action-result/action-type';
import { ActionService } from 'app/action-result/action.service';
import {
  Category,
  CategoryService,
  NodesService,
} from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-category-details',
  templateUrl: './category-details.component.html',
  styleUrls: ['./category-details.component.scss'],
  preserveWhitespaces: true,
})
export class CategoryDetailsComponent implements OnInit {
  public header!: string | null;
  public loading!: boolean;
  public category!: Category;
  public categoryForm!: FormGroup;

  constructor(
    private route: ActivatedRoute,
    private categoryService: CategoryService,
    private nodesService: NodesService,
    private fb: FormBuilder,
    private actionService: ActionService
  ) {}

  ngOnInit() {
    this.categoryForm = this.fb.group({
      name: ['', [Validators.required]],
      title: [''],
    });

    this.route.params.subscribe(async (params) => {
      await this.initCategory(params.id);
    });

    const headerSession = sessionStorage.getItem('currentHeader');
    if (headerSession) {
      this.header = JSON.parse(headerSession).name;
    }
  }

  private async initCategory(id: string) {
    this.loading = true;
    try {
      const categoryNode = await firstValueFrom(this.nodesService.getNode(id));
      if (categoryNode.name && categoryNode.title) {
        this.category = {
          id: categoryNode.id,
          name: categoryNode.name,
          title: categoryNode.title,
        };
      }

      if (this.categoryForm) {
        this.categoryForm.patchValue(this.category);
      }
    } catch (error) {
      console.error('impossible to get the category');
    }
    this.loading = false;
  }

  get nameControl(): AbstractControl {
    return this.categoryForm.controls.name;
  }

  public async cancel() {
    if (this.category.id) {
      await this.initCategory(this.category.id);
    }
  }

  public async update() {
    if (this.category && this.category.id) {
      try {
        const result: ActionEmitterResult = {
          type: ActionType.UPDATE_CATEGORY,
          node: { id: this.category.id },
          result: ActionResult.SUCCEED,
        };
        await firstValueFrom(
          this.categoryService.putCategory(
            this.category.id,
            this.categoryForm.value
          )
        );
        this.actionService.propagateActionFinished(result);
        await this.initCategory(this.category.id);
      } catch (error) {
        console.error(error);
      }
    }
  }
}
