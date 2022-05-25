import { Component, Input, Output, EventEmitter } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { HelpSearchResult } from 'app/core/generated/circabc';
import { Router } from '@angular/router';
import { assertDefined } from 'app/core/asserts';

@Component({
  selector: 'cbc-help-search-displayer',
  templateUrl: './help-search-displayer.component.html',
  styleUrls: ['./help-search-displayer.component.scss'],
})
export class HelpSearchDisplayerComponent {
  @Input()
  result: HelpSearchResult | undefined;
  @Input()
  searching = false;
  @Output()
  public readonly linkClick = new EventEmitter();

  constructor(private sanitizer: DomSanitizer, private router: Router) {}

  public hasResult() {
    if (this.result) {
      if (
        (this.result.articles && this.result.articles.length > 0) ||
        (this.result.categories && this.result.categories.length > 0) ||
        (this.result.links && this.result.links.length > 0)
      ) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public sanitizeLinkRef(href: string | undefined) {
    if (href) {
      return this.sanitizer.bypassSecurityTrustUrl(href);
    } else {
      return undefined;
    }
  }

  async goToCategoryLink(id: string | undefined) {
    assertDefined(id);
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    await this.router.navigate(['/help/category', id]);
    this.linkClick.emit();
  }

  async goToArticleLink(
    categoryId: string | undefined,
    articleId: string | undefined
  ) {
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    await this.router.navigate([
      '/help/category',
      categoryId,
      'article',
      articleId,
    ]);
    this.linkClick.emit();
  }
}
