import { Injectable, signal, inject } from '@angular/core';
import { ArticleViewerService } from './article-viewer.service';

@Injectable({
  providedIn: 'root',
})
export class AccordionStateService {
  private readonly articleViewerService = inject(ArticleViewerService);

  private readonly expandedCategoryId = signal<string | null>(null);
  private readonly expandedSubcategoryMap = signal<Map<string, Set<string>>>(
    new Map()
  );

  toggleCategory(categoryId: string): void {
    const currentExpanded = this.expandedCategoryId();

    if (currentExpanded === categoryId) {
      this.expandedCategoryId.set(null);
      this.collapseAllSubcategoriesInCategory(categoryId);
    } else {
      this.expandedCategoryId.set(categoryId);
    }

    this.articleViewerService.closeArticle();
  }

  toggleSubcategory(categoryId: string, subcategoryId: string): void {
    const map = this.expandedSubcategoryMap();
    const expandedSet = map.get(categoryId) || new Set<string>();

    if (expandedSet.has(subcategoryId)) {
      expandedSet.delete(subcategoryId);
    } else {
      expandedSet.add(subcategoryId);
    }

    map.set(categoryId, expandedSet);
    this.expandedSubcategoryMap.set(new Map(map));

    this.articleViewerService.closeArticle();
  }

  expandSubcategory(categoryId: string, subcategoryId: string): void {
    const map = this.expandedSubcategoryMap();
    const expandedSet = map.get(categoryId) || new Set<string>();

    expandedSet.add(subcategoryId);
    map.set(categoryId, expandedSet);
    this.expandedSubcategoryMap.set(new Map(map));
  }

  isCategoryExpanded(categoryId: string): boolean {
    return this.expandedCategoryId() === categoryId;
  }

  isSubcategoryExpanded(categoryId: string, subcategoryId: string): boolean {
    const expandedSet = this.expandedSubcategoryMap().get(categoryId);
    return expandedSet ? expandedSet.has(subcategoryId) : false;
  }

  private collapseAllSubcategoriesInCategory(categoryId: string): void {
    const map = this.expandedSubcategoryMap();
    map.set(categoryId, new Set<string>());
    this.expandedSubcategoryMap.set(new Map(map));
  }
}
