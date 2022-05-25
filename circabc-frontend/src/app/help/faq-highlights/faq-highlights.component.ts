import { Component, OnInit } from '@angular/core';
import { HelpArticle, HelpService } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-faq-highlights',
  templateUrl: './faq-highlights.component.html',
  styleUrls: ['./faq-highlights.component.scss'],
})
export class FaqHighlightsComponent implements OnInit {
  public highlightedArticles: HelpArticle[] = [];
  public displayedArticles: HelpArticle[] = [];

  constructor(private helpService: HelpService) {}

  public async ngOnInit() {
    try {
      this.highlightedArticles = await firstValueFrom(
        this.helpService.getHelpHighlightedArticles()
      );
      if (this.highlightedArticles.length >= 3) {
        this.displayedArticles = this.highlightedArticles.slice(0, 3);
      } else {
        this.displayedArticles = this.highlightedArticles;
      }
    } catch (error) {
      console.error(error);
    }
  }

  public previous() {
    const item = this.highlightedArticles.pop();
    if (item) {
      this.highlightedArticles = [item].concat(this.highlightedArticles);
      this.displayedArticles = this.highlightedArticles.slice(0, 3);
    }
  }

  public next() {
    const item = this.highlightedArticles.shift();
    if (item) {
      this.highlightedArticles = this.highlightedArticles.concat([item]);
      this.displayedArticles = this.highlightedArticles.slice(0, 3);
    }
  }
}
