import { Injectable, signal, computed } from '@angular/core';

export interface ArticleViewerState {
  isOpen: boolean;
  articleId: string | null;
  categoryId: string | null;
  subcategoryId: string | null;
  categoryName?: string;
  subcategoryName?: string;
}

@Injectable({
  providedIn: 'root',
})
export class ArticleViewerService {
  private readonly state = signal<ArticleViewerState>({
    isOpen: false,
    articleId: null,
    categoryId: null,
    subcategoryId: null,
  });

  readonly isOpen = computed(() => this.state().isOpen);
  readonly articleId = computed(() => this.state().articleId);
  readonly categoryId = computed(() => this.state().categoryId);
  readonly subcategoryId = computed(() => this.state().subcategoryId);
  readonly categoryName = computed(() => this.state().categoryName);
  readonly subcategoryName = computed(() => this.state().subcategoryName);

  openArticle(
    articleId: string,
    categoryId: string,
    subcategoryId: string | null,
    categoryName?: string,
    subcategoryName?: string
  ): void {
    this.state.set({
      isOpen: true,
      articleId,
      categoryId,
      subcategoryId,
      categoryName,
      subcategoryName,
    });
  }

  closeArticle(): void {
    this.state.set({
      isOpen: false,
      articleId: null,
      categoryId: null,
      subcategoryId: null,
    });
  }
}
