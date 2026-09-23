import { DatePipe } from '@angular/common';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, RouterLink } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import {
  EntryEvent,
  GroupDashboardEntry,
  type InterestGroup,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { TimelineComponent } from './timeline.component';

const mockI18nPipe = { transform: vi.fn().mockReturnValue('translated') };

describe('TimelineComponent', () => {
  let component: TimelineComponent;
  let fixture: ComponentFixture<TimelineComponent>;

  const mockGroup: InterestGroup = {
    name: 'Test Group',
    permissions: { library: 'LibAdmin' },
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TimelineComponent],
      providers: [
        provideRouter([]),
        { provide: I18nPipe, useValue: mockI18nPipe },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(TimelineComponent, {
        set: {
          imports: [TranslocoModule, DatePipe, RouterLink],
          schemas: [NO_ERRORS_SCHEMA],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(TimelineComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('group', mockGroup);
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('showNews / showLibrary', () => {
    it('should toggle panels when showNews is called', () => {
      component.showNews();
      expect(component.showNewsPanel).toBe(true);
      expect(component.showLibraryPanel).toBe(false);
    });

    it('should toggle panels when showLibrary is called', () => {
      component.showLibrary();
      expect(component.showNewsPanel).toBe(false);
      expect(component.showLibraryPanel).toBe(true);
    });
  });

  describe('isFile', () => {
    it('should return true for content types that are not folders', () => {
      expect(component.isFile('cm:content')).toBe(true);
    });

    it('should return false for folder types', () => {
      expect(component.isFile('cm:folder')).toBe(false);
    });

    it('should return false for non-content types', () => {
      expect(component.isFile('fm:topic')).toBe(false);
    });
  });

  describe('isTopic', () => {
    it('should return true when type includes topic', () => {
      expect(component.isTopic('fm:topic')).toBe(true);
    });

    it('should return false when type does not include topic', () => {
      expect(component.isTopic('cm:content')).toBe(false);
    });
  });

  describe('isForum', () => {
    it('should return true for forum type', () => {
      expect(component.isForum('fm:forum')).toBe(true);
    });

    it('should return false for post type', () => {
      expect(component.isForum('fm:post')).toBe(false);
    });

    it('should return false for topic type', () => {
      expect(component.isForum('fm:topic')).toBe(false);
    });
  });

  describe('isPost', () => {
    it('should return true when type includes post', () => {
      expect(component.isPost('fm:post')).toBe(true);
    });

    it('should return false when type does not include post', () => {
      expect(component.isPost('fm:forum')).toBe(false);
    });
  });

  describe('isNews', () => {
    it('should return true when type includes news', () => {
      expect(component.isNews('ci:news')).toBe(true);
    });

    it('should return false when type does not include news', () => {
      expect(component.isNews('cm:content')).toBe(false);
    });
  });

  describe('isFolder', () => {
    it('should return true when type includes folder', () => {
      expect(component.isFolder('cm:folder')).toBe(true);
    });

    it('should return false when type does not include folder', () => {
      expect(component.isFolder('cm:content')).toBe(false);
    });

    it('should return undefined for undefined type', () => {
      expect(component.isFolder(undefined)).toBeFalsy();
    });
  });

  describe('getDisplayProperty', () => {
    it('should return undefined for undefined node', () => {
      expect(component.getDisplayProperty(undefined)).toBeUndefined();
    });

    it('should return name when title is empty', () => {
      mockI18nPipe.transform.mockReturnValue('');
      const node: ModelNode = { name: 'test.pdf', title: { en: '' } };
      expect(component.getDisplayProperty(node)).toBe('test.pdf');
    });

    it('should return translated title when available', () => {
      mockI18nPipe.transform.mockReturnValue('My Title');
      const node: ModelNode = {
        name: 'test.pdf',
        title: { en: 'My Title' },
        properties: {},
      };
      expect(component.getDisplayProperty(node)).toBe('My Title');
    });

    it('should extract event title from MLText format', () => {
      mockI18nPipe.transform.mockReturnValue('{en=My Event}');
      const node: ModelNode = {
        name: 'event1',
        title: { en: 'My Event' },
        properties: { kindOfEvent: 'Event' },
      };
      expect(component.getDisplayProperty(node)).toBe('My Event');
    });

    it('should format post name correctly', () => {
      mockI18nPipe.transform.mockReturnValue('');
      const node: ModelNode = {
        name: 'posted-01-01-2024-14-30.html',
      };
      expect(component.getDisplayProperty(node)).toBe('Post 01-01-2024 14:30');
    });
  });

  describe('getDestination', () => {
    it('should return UNDEFINED when properties is undefined', () => {
      const node: ModelNode = {};
      expect(component.getDestination(node)).toBe('UNDEFINED');
    });

    it('should return agenda for events', () => {
      const node: ModelNode = { properties: { kindOfEvent: 'Event' } };
      expect(component.getDestination(node)).toBe('agenda');
    });

    it('should return agenda for meetings', () => {
      const node: ModelNode = { properties: { meetingType: 'Meeting' } };
      expect(component.getDestination(node)).toBe('agenda');
    });

    it('should return forum/topic for posts', () => {
      const node: ModelNode = {
        name: 'posted-01-01-2024-14-30.html',
        properties: {},
      };
      expect(component.getDestination(node)).toBe('forum/topic');
    });

    it('should return library by default', () => {
      const node: ModelNode = { name: 'doc.pdf', properties: {} };
      expect(component.getDestination(node)).toBe('library');
    });
  });

  describe('getRouterLinkParts', () => {
    it('should return empty array when groupId is empty', () => {
      const node: ModelNode = { id: '1', type: 'cm:content', parentId: 'p1' };
      expect(component.getRouterLinkParts('', node)).toEqual([]);
    });

    it('should return empty array when node is undefined', () => {
      expect(component.getRouterLinkParts('g1', undefined)).toEqual([]);
    });

    it('should return library details route for file nodes', () => {
      const node: ModelNode = {
        id: 'n1',
        type: 'cm:content',
        parentId: 'p1',
        service: 'library',
      };
      expect(component.getRouterLinkParts('g1', node)).toEqual([
        '/group',
        'g1',
        'library',
        'n1',
        'details',
      ]);
    });

    it('should return library route for folder nodes', () => {
      const node: ModelNode = {
        id: 'n1',
        type: 'cm:folder',
        parentId: 'p1',
        service: 'library',
      };
      expect(component.getRouterLinkParts('g1', node)).toEqual([
        '/group',
        'g1',
        'library',
        'n1',
      ]);
    });

    it('should return information route for information service', () => {
      const node: ModelNode = {
        id: 'n1',
        type: 'ci:news',
        parentId: 'p1',
        service: 'information',
      };
      expect(component.getRouterLinkParts('g1', node)).toEqual([
        '/group',
        'g1',
        'information',
      ]);
    });

    it('should return forum route for newsgroups forum', () => {
      const node: ModelNode = {
        id: 'n1',
        type: 'fm:forum',
        parentId: 'p1',
        service: 'newsgroups',
      };
      expect(component.getRouterLinkParts('g1', node)).toEqual([
        '/group',
        'g1',
        'forum',
        'n1',
      ]);
    });

    it('should return forum topic route for newsgroups topic', () => {
      const node: ModelNode = {
        id: 'n1',
        type: 'fm:topic',
        parentId: 'p1',
        service: 'newsgroups',
      };
      expect(component.getRouterLinkParts('g1', node)).toEqual([
        '/group',
        'g1',
        'forum',
        'topic',
        'n1',
      ]);
    });

    it('should return forum topic route with parentId for newsgroups post', () => {
      const node: ModelNode = {
        id: 'n1',
        type: 'fm:post',
        parentId: 'p1',
        service: 'newsgroups',
      };
      expect(component.getRouterLinkParts('g1', node)).toEqual([
        '/group',
        'g1',
        'forum',
        'topic',
        'p1',
      ]);
    });
  });

  describe('getLibraryEntries', () => {
    it('should return at most 15 entries when showAll is false', () => {
      component.libraryEntries = Array.from({ length: 20 }, (_, i) => ({
        date: `2024-01-${i + 1}`,
      }));
      component.showAll = false;
      expect(component.getLibraryEntries()).toHaveLength(15);
    });

    it('should return all entries when showAll is true', () => {
      component.libraryEntries = Array.from({ length: 20 }, (_, i) => ({
        date: `2024-01-${i + 1}`,
      }));
      component.showAll = true;
      expect(component.getLibraryEntries()).toHaveLength(20);
    });
  });

  describe('showAllFeed', () => {
    it('should set showAll to true', () => {
      component.showAll = false;
      component.showAllFeed();
      expect(component.showAll).toBe(true);
    });
  });

  describe('ngOnChanges - getValidNews', () => {
    it('should categorize library entries from dashboard entries', () => {
      const entries: GroupDashboardEntry[] = [
        {
          news: [
            {
              node: {
                id: 'n1',
                type: 'cm:content',
                service: 'library',
                name: 'doc.pdf',
              },
            },
            {
              node: {
                id: 'n2',
                type: 'ci:news',
                service: 'information',
                name: 'News Item',
              },
            },
          ],
        },
      ];

      fixture.componentRef.setInput('entries', entries);
      fixture.detectChanges();

      expect(component.libraryEntries).toHaveLength(1);
      expect(component.informationEntries).toHaveLength(1);
    });

    it('should exclude library posts and topics', () => {
      const entries: GroupDashboardEntry[] = [
        {
          news: [
            {
              node: {
                id: 'n1',
                type: 'fm:post',
                service: 'library',
                name: 'post1',
              },
            },
            {
              node: {
                id: 'n2',
                type: 'fm:topic',
                service: 'library',
                name: 'topic1',
              },
            },
          ],
        },
      ];

      fixture.componentRef.setInput('entries', entries);
      fixture.detectChanges();

      expect(component.libraryEntries).toHaveLength(0);
    });

    it('should set hasMoreThan15 when library entries exceed 15', () => {
      const news: EntryEvent[] = Array.from({ length: 20 }, (_, i) => ({
        node: {
          id: `n${i}`,
          type: 'cm:content',
          service: 'library' as ModelNode.ServiceEnum,
          name: `file${i}.pdf`,
        },
      }));

      const entries: GroupDashboardEntry[] = [{ news }];

      fixture.componentRef.setInput('entries', entries);
      fixture.detectChanges();

      expect(component.hasMoreThan15).toBe(true);
    });

    it('should show news panel when no library entries but information entries exist', () => {
      const entries: GroupDashboardEntry[] = [
        {
          news: [
            {
              node: {
                id: 'n1',
                type: 'ci:news',
                service: 'information',
                name: 'News',
              },
            },
          ],
        },
      ];

      fixture.componentRef.setInput('entries', entries);
      fixture.detectChanges();

      expect(component.showNewsPanel).toBe(true);
      expect(component.libraryEntries).toHaveLength(0);
      expect(component.informationEntries).toHaveLength(1);
    });

    it('should limit information entries to 3', () => {
      const news: EntryEvent[] = Array.from({ length: 5 }, (_, i) => ({
        node: {
          id: `n${i}`,
          type: 'ci:news',
          service: 'information' as ModelNode.ServiceEnum,
          name: `News ${i}`,
        },
      }));

      const entries: GroupDashboardEntry[] = [{ news }];

      fixture.componentRef.setInput('entries', entries);
      fixture.detectChanges();

      expect(component.informationEntries).toHaveLength(3);
    });
  });
});
