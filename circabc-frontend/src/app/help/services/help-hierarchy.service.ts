import { Injectable, signal, inject } from '@angular/core';
import { Observable, of, forkJoin } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import {
  HelpHierarchy,
  HelpCategory as HelpCategoryModel,
  HelpSubcategory as HelpSubcategoryModel,
  HelpArticle as HelpArticleModel,
} from '../models/help-hierarchy.model';
import {
  HelpService,
  HelpCategory,
  HelpSubcategory,
  HelpArticle,
} from 'app/core/generated/circabc';

@Injectable({
  providedIn: 'root',
})
export class HelpHierarchyService {
  private readonly helpService = inject(HelpService);

  readonly hierarchyData = signal<HelpHierarchy | null>(null);
  readonly isLoading = signal<boolean>(false);
  readonly error = signal<string | null>(null);

  loadHierarchy(forceReload = false): void {
    if (this.hierarchyData() && !forceReload) {
      return;
    }

    this.isLoading.set(true);
    this.error.set(null);

    this.helpService
      .getHelpCategories()
      .pipe(
        switchMap((categories) => {
          return this.buildHierarchy(categories);
        }),
        catchError((error) => {
          console.error(
            '[HelpHierarchyService] Failed to load help hierarchy:',
            error
          );
          console.error('[HelpHierarchyService] Error details:', {
            message: error.message,
            status: error.status,
            statusText: error.statusText,
            url: error.url,
          });
          this.error.set('Failed to load help content. Please try again.');
          this.isLoading.set(false);
          return of(null);
        })
      )
      .subscribe((hierarchy) => {
        this.hierarchyData.set(hierarchy);
        this.isLoading.set(false);
      });
  }

  private buildHierarchy(
    categories: HelpCategory[]
  ): Observable<HelpHierarchy> {
    if (!categories || categories.length === 0) {
      return of({ categories: [] });
    }

    // For each category, fetch its subcategories and articles
    const categoryObservables = categories.map((category) =>
      this.buildCategoryHierarchy(category)
    );

    return forkJoin(categoryObservables).pipe(
      map((processedCategories) => {
        // Sort categories by order
        const sortedCategories = processedCategories.sort(
          (a, b) => a.order - b.order
        );
        return { categories: sortedCategories };
      })
    );
  }

  private buildCategoryHierarchy(
    category: HelpCategory
  ): Observable<HelpCategoryModel> {
    const categoryId = category.id ?? '';
    const order = category.sortOrder ?? 0;

    // Fetch subcategories and category-level articles in parallel
    return forkJoin({
      subcategories: this.helpService.getCategorySubcategories(categoryId),
      articles: this.helpService.getCategoryArticles(categoryId, true),
    }).pipe(
      switchMap(({ subcategories, articles }) => {
        // Check for duplicate subcategories
        if (subcategories && subcategories.length > 0) {
          const subcatIds = subcategories.map((s) => s.id);
          const uniqueIds = new Set(subcatIds);

          if (subcatIds.length !== uniqueIds.size) {
            console.error(
              `[HelpHierarchyService] DUPLICATE SUBCATEGORIES DETECTED in category ${categoryId}!`,
              {
                total: subcatIds.length,
                unique: uniqueIds.size,
                ids: subcatIds,
              }
            );
          }
        }

        // For each subcategory, fetch its articles
        const subcategoryObservables =
          subcategories && subcategories.length > 0
            ? subcategories.map((subcat) =>
                this.buildSubcategoryHierarchy(subcat, categoryId)
              )
            : [];

        if (subcategoryObservables.length === 0) {
          return of({
            id: categoryId,
            name: this.extractTitle(category.title || {}),
            order: order,
            subcategories: [],
            articles: this.mapArticles(articles || [], categoryId).sort(
              (a, b) => a.order - b.order
            ),
          });
        }

        return forkJoin(subcategoryObservables).pipe(
          map((processedSubcategories) => {
            // Filter out any subcategory that has the same ID as the category (defensive check)
            const validSubcategories = processedSubcategories.filter(
              (sub) => sub.id !== categoryId
            );

            return {
              id: categoryId,
              name: this.extractTitle(category.title || {}),
              order: order,
              subcategories: validSubcategories.sort(
                (a, b) => a.order - b.order
              ),
              articles: this.mapArticles(articles || [], categoryId).sort(
                (a, b) => a.order - b.order
              ),
            };
          })
        );
      }),
      catchError((error) => {
        console.error(
          `[HelpHierarchyService] Failed to load data for category ${categoryId}:`,
          error
        );
        // Return category with empty subcategories and articles on error
        return of({
          id: categoryId,
          name: this.extractTitle(category.title || {}),
          order: order,
          subcategories: [],
          articles: [],
        });
      })
    );
  }

  private buildSubcategoryHierarchy(
    subcategory: HelpSubcategory,
    categoryId: string
  ): Observable<HelpSubcategoryModel> {
    const subcategoryId = subcategory.id ?? '';

    return this.helpService.getSubcategoryArticles(subcategoryId, true).pipe(
      map((articles) => ({
        id: subcategoryId,
        name: this.extractTitle(subcategory.title || {}),
        order: subcategory.sortOrder || 0,
        categoryId,
        articles: this.mapArticles(
          articles || [],
          categoryId,
          subcategoryId
        ).sort((a, b) => a.order - b.order),
      })),
      catchError((error) => {
        console.error(
          `Failed to load articles for subcategory ${subcategoryId}:`,
          error
        );
        // Return subcategory with empty articles on error
        return of({
          id: subcategoryId,
          name: this.extractTitle(subcategory.title || {}),
          order: subcategory.sortOrder || 0,
          categoryId,
          articles: [],
        });
      })
    );
  }

  private mapArticles(
    articles: HelpArticle[],
    categoryId: string,
    subcategoryId?: string
  ): HelpArticleModel[] {
    return articles.map((article) => ({
      id: article.id || '',
      title: this.extractTitle(article.title || {}),
      slug: article.id || '', // Using ID as slug since API doesn't provide slug
      order: article.sortOrder || 0,
      categoryId,
      subcategoryId,
      lastUpdate: article.lastUpdate,
    }));
  }

  private extractTitle(titleObj: { [key: string]: string }): string {
    if (typeof titleObj === 'string') {
      return titleObj;
    }

    // Try common languages first
    const preferredLangs = ['en', 'pt', 'es', 'fr', 'de'];
    for (const lang of preferredLangs) {
      if (titleObj[lang]) {
        return titleObj[lang];
      }
    }

    // Fallback to first available language
    const keys = Object.keys(titleObj);
    return keys.length > 0 ? titleObj[keys[0]] : '';
  }

  putCategorySubcategoriesOrder(
    categoryId: string,
    subcategoryIds: string[]
  ): Observable<void> {
    return this.helpService.putCategorySubcategoriesOrder(
      categoryId,
      subcategoryIds
    );
  }

  putCategoryArticlesOrder(
    categoryId: string,
    articleIds: string[]
  ): Observable<void> {
    return this.helpService.putCategoryArticlesOrder(categoryId, articleIds);
  }

  putSubcategoryArticlesOrder(
    subcategoryId: string,
    articleIds: string[]
  ): Observable<void> {
    return this.helpService.putSubcategoryArticlesOrder(
      subcategoryId,
      articleIds
    );
  }

  putCategoriesOrder(categoryIds: string[]): Observable<void> {
    return this.helpService.putCategoriesOrder(categoryIds);
  }
}
