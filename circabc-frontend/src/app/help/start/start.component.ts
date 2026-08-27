import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
  inject,
  signal,
  effect,
} from '@angular/core';

import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { ActionEmitterResult } from 'app/action-result';
import {
  HelpCategory,
  HelpLink,
  HelpService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { AddLinkComponent } from 'app/help/add-link/add-link.component';
import { ExportFaqButtonComponent } from 'app/help/export-faq/export-faq-button.component';
import { FaqHighlightsComponent } from 'app/help/faq-highlights/faq-highlights.component';
import { HelpLinksComponent } from 'app/help/help-links/help-links.component';
import { ImportFaqComponent } from 'app/help/import-faq/import-faq.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { firstValueFrom } from 'rxjs';
import { HelpAccordionComponent } from '../components/help-accordion/help-accordion.component';
import { ArticleViewerComponent } from '../components/article-viewer/article-viewer.component';
import { ArticleViewerService } from '../services/article-viewer.service';
import { HelpHierarchyService } from '../services/help-hierarchy.service';

@Component({
  selector: 'cbc-start',
  templateUrl: './start.component.html',
  styleUrl: './start.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    RouterLink,
    FaqHighlightsComponent,
    HelpLinksComponent,
    AddLinkComponent,
    ExportFaqButtonComponent,
    ImportFaqComponent,
    SetTitlePipe,
    TranslocoModule,
    HelpAccordionComponent,
    ArticleViewerComponent,
  ],
})
export class StartComponent implements OnInit, OnDestroy {
  public categories: HelpCategory[] = [];
  public links = signal<HelpLink[]>([]);
  public linkId = '';
  public loading = false;
  public showCreateModal = false;
  public showCreateLinkModal = false;

  categoriesWidth = signal(45);
  articleWidth = signal(55);

  private isResizing = false;
  private boundMouseMove = this.onMouseMove.bind(this);
  private boundMouseUp = this.stopResize.bind(this);

  private readonly articleViewerService = inject(ArticleViewerService);
  private readonly helpHierarchyService = inject(HelpHierarchyService);
  readonly isArticleViewerOpen = this.articleViewerService.isOpen;
  readonly currentArticleId = this.articleViewerService.articleId;

  constructor(
    private helpService: HelpService,
    private loginService: LoginService
  ) {
    effect(() => {
      const isOpen = this.isArticleViewerOpen();
      const articleId = this.currentArticleId();

      if (isOpen && articleId) {
        this.scrollToArticleViewer();
      }
    });
  }

  async ngOnInit() {
    this.loading = true;

    try {
      const fetchedLinks = await firstValueFrom(
        this.helpService.getHelpLinks()
      );
      this.links.set(fetchedLinks);
    } catch (error) {
      console.error(error);
    }

    try {
      this.categories = await firstValueFrom(
        this.helpService.getHelpCategories()
      );
    } catch (error) {
      console.error(error);
    }

    this.loading = false;
  }

  ngOnDestroy() {
    document.removeEventListener('mousemove', this.boundMouseMove);
    document.removeEventListener('mouseup', this.boundMouseUp);
    document.removeEventListener('touchmove', this.boundMouseMove);
    document.removeEventListener('touchend', this.boundMouseUp);
  }

  startResize(event: MouseEvent | TouchEvent) {
    event.preventDefault();
    this.isResizing = true;
    document.body.style.cursor = 'col-resize';
    document.body.style.userSelect = 'none';

    document.addEventListener('mousemove', this.boundMouseMove);
    document.addEventListener('mouseup', this.boundMouseUp);
    document.addEventListener('touchmove', this.boundMouseMove);
    document.addEventListener('touchend', this.boundMouseUp);
  }

  private onMouseMove(event: MouseEvent | TouchEvent) {
    if (!this.isResizing) return;

    const container = document.querySelector(
      '.main-content-wrapper'
    ) as HTMLElement;
    if (!container) return;

    const rect = container.getBoundingClientRect();
    const clientX =
      event instanceof MouseEvent ? event.clientX : event.touches[0].clientX;
    const offsetX = clientX - rect.left;
    const percentage = (offsetX / rect.width) * 100;

    const minWidth = 30;
    const maxWidth = 70;
    const clampedPercentage = Math.max(
      minWidth,
      Math.min(maxWidth, percentage)
    );

    this.categoriesWidth.set(clampedPercentage);
    this.articleWidth.set(100 - clampedPercentage);
  }

  private stopResize() {
    if (!this.isResizing) return;

    this.isResizing = false;
    document.body.style.cursor = '';
    document.body.style.userSelect = '';

    document.removeEventListener('mousemove', this.boundMouseMove);
    document.removeEventListener('mouseup', this.boundMouseUp);
    document.removeEventListener('touchmove', this.boundMouseMove);
    document.removeEventListener('touchend', this.boundMouseUp);
  }

  private scrollToArticleViewer() {
    setTimeout(() => {
      const mainContent = document.querySelector('.main-content-wrapper');
      if (mainContent) {
        const rect = mainContent.getBoundingClientRect();
        const scrollTop =
          window.pageYOffset || document.documentElement.scrollTop;
        const targetPosition = rect.top + scrollTop - 20;

        window.scrollTo({
          top: targetPosition,
          behavior: 'smooth',
        });
      }
    }, 450);
  }

  public isAdminOrSupport(): boolean {
    if (!this.loginService.isGuest()) {
      const user = this.loginService.getUser();
      return (
        user.properties !== undefined &&
        (user.properties.isAdmin === 'true' ||
          user.properties.isCircabcAdmin === 'true')
      );
    }

    return false;
  }

  public async refresh(_result: ActionEmitterResult) {
    this.loading = true;

    try {
      this.categories = await firstValueFrom(
        this.helpService.getHelpCategories()
      );
    } catch (error) {
      console.error(error);
    }

    this.loading = false;
  }

  public async refreshCategories() {
    this.loading = true;

    try {
      // Reload the hierarchy data in the accordion
      this.helpHierarchyService.loadHierarchy(true);

      // Also refresh the categories for backward compatibility
      this.categories = await firstValueFrom(
        this.helpService.getHelpCategories()
      );
    } catch (error) {
      console.error(error);
    }

    this.loading = false;
  }

  public async refreshLinks(_result?: ActionEmitterResult) {
    this.loading = true;
    this.showCreateLinkModal = false;

    try {
      const fetchedLinks = await firstValueFrom(
        this.helpService.getHelpLinks()
      );
      this.links.set(fetchedLinks);
    } catch (error) {
      console.error(error);
    }

    this.loading = false;
  }

  public openForEdit(linkId: string) {
    if (linkId) {
      this.linkId = linkId;
      this.showCreateLinkModal = true;
    }
  }
}
