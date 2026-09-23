import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoService,
} from '@jsverse/transloco';
import { AnalyticsService } from 'app/core/analytics.service';
import { HelpService } from 'app/core/generated/circabc/api/help.service';
import { SearchService } from 'app/core/generated/circabc/api/search.service';
import { HelpSearchResult } from 'app/core/generated/circabc/model/helpSearchResult';
import { PagedSearchNodes } from 'app/core/generated/circabc/model/pagedSearchNodes';
import { SearchNode } from 'app/core/generated/circabc/model/searchNode';
import { ALF_BASE_PATH, CBC_BASE_PATH } from 'app/core/variables';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { SearchBarComponent } from './search-bar.component';

describe('SearchBarComponent', () => {
  let component: SearchBarComponent;
  let fixture: ComponentFixture<SearchBarComponent>;

  const mockSearchService = {
    getSearchAsync: vi.fn(),
  };

  const mockHelpService = {
    helpSearchAsync: vi.fn(),
  };

  const mockAnalyticsService = {
    trackSiteSearch: vi.fn(),
  };

  const mockRouter = {
    url: '/group/123',
    navigate: vi.fn(),
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    mockRouter.url = '/group/123';

    await TestBed.configureTestingModule({
      imports: [SearchBarComponent],
      providers: [
        { provide: SearchService, useValue: mockSearchService },
        { provide: HelpService, useValue: mockHelpService },
        { provide: AnalyticsService, useValue: mockAnalyticsService },
        { provide: Router, useValue: mockRouter },
        { provide: ALF_BASE_PATH, useValue: '' },
        { provide: CBC_BASE_PATH, useValue: '' },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(SearchBarComponent, {
        set: {
          template:
            '<form [formGroup]="searchForm"><input formControlName="searchString" /></form>',
          imports: [ReactiveFormsModule],
          schemas: [NO_ERRORS_SCHEMA],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(SearchBarComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('groupId', 'group-123');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize the search form on init', () => {
    expect(component.searchForm).toBeDefined();
    expect(component.searchForm.controls['searchString']).toBeDefined();
  });

  describe('clearResults', () => {
    it('should reset all result arrays', () => {
      component.fileResults.set([{ id: '1' } as SearchNode]);
      component.folderResults.set([{ id: '2' } as SearchNode]);

      component.clearResults();

      expect(component.fileResults()).toEqual([]);
      expect(component.folderResults()).toEqual([]);
      expect(component.postResults()).toEqual([]);
      expect(component.forumResults()).toEqual([]);
      expect(component.topicResults()).toEqual([]);
      expect(component.eventResults()).toEqual([]);
      expect(component.informationResults()).toEqual([]);
      expect(component.otherResults()).toEqual([]);
    });
  });

  describe('resetForm', () => {
    it('should clear results and reset search string', () => {
      component.searchForm.controls['searchString'].setValue('test');
      component.noResult.set(true);
      component.searching.set(true);

      component.resetForm();

      expect(component.searchForm.controls['searchString'].value).toBe('');
      expect(component.noResult()).toBe(false);
      expect(component.searching()).toBe(false);
    });
  });

  describe('splitResults', () => {
    it('should categorize search nodes by resultType', () => {
      const data: SearchNode[] = [
        { id: '1', resultType: 'file' },
        { id: '2', resultType: 'folder' },
        { id: '3', resultType: 'topic' },
        { id: '4', resultType: 'post' },
        { id: '5', resultType: 'forum' },
        { id: '6', resultType: 'information' },
      ];

      component.splitResults(data);

      expect(component.fileResults()).toHaveLength(1);
      expect(component.folderResults()).toHaveLength(1);
      expect(component.topicResults()).toHaveLength(1);
      expect(component.postResults()).toHaveLength(1);
      expect(component.forumResults()).toHaveLength(1);
      expect(component.informationResults()).toHaveLength(1);
    });

    it('should put events service nodes into eventResults', () => {
      component.splitResults([
        {
          id: '1',
          resultType: 'other',
          service: 'events' as SearchNode.ServiceEnum,
        },
      ]);

      expect(component.eventResults()).toHaveLength(1);
    });

    it('should put unknown types into otherResults', () => {
      component.splitResults([
        {
          id: '1',
          resultType: 'unknown',
          service: 'library' as SearchNode.ServiceEnum,
        },
      ]);

      expect(component.otherResults()).toHaveLength(1);
    });
  });

  describe('hasResults', () => {
    it('should return false when no results', () => {
      expect(component.hasResults()).toBe(false);
    });

    it('should return true when there are file results', () => {
      component.fileResults.set([{ id: '1' } as SearchNode]);
      expect(component.hasResults()).toBe(true);
    });
  });

  describe('hasText', () => {
    it('should return false when search string is empty', () => {
      component.searchForm.controls['searchString'].setValue('');
      expect(component.hasText()).toBe(false);
    });

    it('should return true when search string has value', () => {
      component.searchForm.controls['searchString'].setValue('test');
      expect(component.hasText()).toBe(true);
    });
  });

  describe('onValueChanged', () => {
    it('should perform group search when not on help route', async () => {
      const pagedResult: PagedSearchNodes = {
        data: [{ id: '1', resultType: 'file' }],
        total: 1,
      };
      mockSearchService.getSearchAsync.mockResolvedValue(pagedResult);

      await component.onValueChanged({ searchString: 'test' });

      expect(mockSearchService.getSearchAsync).toHaveBeenCalled();
      expect(component.fileResults()).toHaveLength(1);
      expect(component.noResult()).toBe(false);
    });

    it('should set noResult when group search returns zero total', async () => {
      mockSearchService.getSearchAsync.mockResolvedValue({
        data: [],
        total: 0,
      });

      await component.onValueChanged({ searchString: 'nothing' });

      expect(component.noResult()).toBe(true);
    });

    it('should set noResult on group search error', async () => {
      mockSearchService.getSearchAsync.mockRejectedValue(new Error('fail'));

      await component.onValueChanged({ searchString: 'error' });

      expect(component.noResult()).toBe(true);
      expect(component.searching()).toBe(false);
    });

    it('should not search when searchString is empty', async () => {
      await component.onValueChanged({ searchString: '' });

      expect(mockSearchService.getSearchAsync).not.toHaveBeenCalled();
    });

    it('should perform help search when on help route', async () => {
      mockRouter.url = '/help/articles';
      const helpResult: HelpSearchResult = {
        articles: [],
        categories: [],
        links: [],
      };
      mockHelpService.helpSearchAsync.mockResolvedValue(helpResult);

      await component.onValueChanged({ searchString: 'help query' });

      expect(mockHelpService.helpSearchAsync).toHaveBeenCalledWith({
        q: 'help query',
      });
      expect(component.helpResult()).toEqual(helpResult);
    });

    it('should set noResult on help search error', async () => {
      mockRouter.url = '/help/articles';
      mockHelpService.helpSearchAsync.mockRejectedValue(new Error('fail'));

      await component.onValueChanged({ searchString: 'error' });

      expect(component.noResult()).toBe(true);
      expect(component.searching()).toBe(false);
    });
  });

  describe('getModified', () => {
    it('should return modified property when properties exist', () => {
      const node: SearchNode = { properties: { modified: '2024-01-01' } };
      expect(component.getModified(node)).toBe('2024-01-01');
    });

    it('should return null when properties are undefined', () => {
      expect(component.getModified({})).toBeNull();
    });
  });

  describe('isRouteHelp', () => {
    it('should return true when URL contains /help', () => {
      mockRouter.url = '/help/category/1';
      expect(component.isRouteHelp()).toBe(true);
    });

    it('should return false when URL does not contain /help', () => {
      mockRouter.url = '/group/123';
      expect(component.isRouteHelp()).toBe(false);
    });
  });

  describe('isExpired', () => {
    it('should return true for past dates', () => {
      expect(component.isExpired('2000-01-01')).toBe(true);
    });

    it('should return false for future dates', () => {
      expect(component.isExpired('2099-01-01')).toBe(false);
    });
  });

  describe('cutBigText', () => {
    it('should return empty string for undefined', () => {
      expect(component.cutBigText(undefined)).toBe('');
    });

    it('should return text as-is when short enough', () => {
      expect(component.cutBigText('short')).toBe('short');
    });
  });

  describe('getTopicContext and getPostContext', () => {
    it('should return library for library service nodes', () => {
      const node: SearchNode = { service: 'library' as SearchNode.ServiceEnum };
      expect(component.getTopicContext(node)).toBe('library');
      expect(component.getPostContext(node)).toBe('library');
    });

    it('should return forum for topic context of non-library nodes', () => {
      const node: SearchNode = {
        service: 'newsgroups' as SearchNode.ServiceEnum,
      };
      expect(component.getTopicContext(node)).toBe('forum');
    });

    it('should return forum/topic for post context of non-library nodes', () => {
      const node: SearchNode = {
        service: 'newsgroups' as SearchNode.ServiceEnum,
      };
      expect(component.getPostContext(node)).toBe('forum/topic');
    });
  });

  describe('getPostEndContext', () => {
    it('should return /details for library service', () => {
      const node: SearchNode = { service: 'library' as SearchNode.ServiceEnum };
      expect(component.getPostEndContext(node)).toBe('/details');
    });

    it('should return empty string for non-library service', () => {
      const node: SearchNode = {
        service: 'newsgroups' as SearchNode.ServiceEnum,
      };
      expect(component.getPostEndContext(node)).toBe('');
    });
  });

  describe('goToCategoryLink', () => {
    it('should navigate to category', () => {
      component.goToCategoryLink('cat-1');
      expect(mockRouter.navigate).toHaveBeenCalledWith([
        '/help/category',
        'cat-1',
      ]);
    });
  });

  describe('goToArticleLink', () => {
    it('should navigate to article', () => {
      component.goToArticleLink('cat-1', 'art-1');
      expect(mockRouter.navigate).toHaveBeenCalledWith([
        '/help/category',
        'cat-1',
        'article',
        'art-1',
      ]);
    });
  });

  describe('hasResult (help)', () => {
    it('should return false when helpResult is undefined', () => {
      expect(component.hasResult()).toBe(false);
    });

    it('should return true when helpResult has articles', () => {
      component.helpResult.set({ articles: [{}] } as HelpSearchResult);
      expect(component.hasResult()).toBe(true);
    });

    it('should return false when helpResult has empty arrays', () => {
      component.helpResult.set({ articles: [], categories: [], links: [] });
      expect(component.hasResult()).toBe(false);
    });
  });

  describe('requestRefresh', () => {
    it('should emit renewSearchHit when text exists', () => {
      const emitSpy = vi.spyOn(component.renewSearchHit, 'emit');
      component.searchForm.controls['searchString'].setValue('query');

      component.requestRefresh();

      expect(emitSpy).toHaveBeenCalledWith('query');
    });

    it('should not emit when text is empty', () => {
      const emitSpy = vi.spyOn(component.renewSearchHit, 'emit');
      component.searchForm.controls['searchString'].setValue('');

      component.requestRefresh();

      expect(emitSpy).not.toHaveBeenCalled();
    });
  });

  describe('getName', () => {
    it('should return title in active language', () => {
      const translocoService = TestBed.inject(TranslocoService);
      vi.spyOn(translocoService, 'getActiveLang').mockReturnValue('en');

      const node: SearchNode = { title: { en: 'English Title' } };
      expect(component.getName(node)).toBe('English Title');
    });

    it('should fallback to English title', () => {
      const translocoService = TestBed.inject(TranslocoService);
      vi.spyOn(translocoService, 'getActiveLang').mockReturnValue('fr');

      const node: SearchNode = { title: { en: 'English Title' } };
      expect(component.getName(node)).toBe('English Title');
    });

    it('should handle news_ prefix in name', () => {
      const translocoService = TestBed.inject(TranslocoService);
      vi.spyOn(translocoService, 'getActiveLang').mockReturnValue('en');

      const node: SearchNode = { name: 'news_Some News Item' };
      expect(component.getName(node)).toBe('Some News Item');
    });
  });

  describe('sanitizeLinkRef', () => {
    it('should return undefined for undefined href', () => {
      expect(component.sanitizeLinkRef(undefined)).toBeUndefined();
    });

    it('should return a sanitized URL for valid href', () => {
      const result = component.sanitizeLinkRef('http://example.com');
      expect(result).toBeDefined();
    });
  });
});
