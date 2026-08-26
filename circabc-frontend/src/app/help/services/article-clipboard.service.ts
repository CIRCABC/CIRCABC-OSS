import { Injectable, signal, computed } from '@angular/core';

export interface ClipboardArticle {
  articleId: string;
  title: string;
  sourceCategoryId: string;
  sourceSubcategoryId: string | null;
}

@Injectable({
  providedIn: 'root',
})
export class ArticleClipboardService {
  private readonly clipboardItem = signal<ClipboardArticle | null>(null);

  readonly copiedArticle = computed(() => this.clipboardItem());
  readonly hasArticleCopied = computed(() => this.clipboardItem() !== null);

  copyArticle(article: ClipboardArticle): void {
    this.clipboardItem.set(article);
  }

  clear(): void {
    this.clipboardItem.set(null);
  }
}
