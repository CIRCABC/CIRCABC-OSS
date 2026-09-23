import { DatePipe } from '@angular/common';
import type { ComponentRef } from '@angular/core';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import type { ComponentFixture } from '@angular/core/testing';
import { TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import type {
  InterestGroupFeed,
  Node as ModelNode,
  UserActionLog,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { GroupStepComponent } from './group-step.component';

describe('GroupStepComponent', () => {
  let component: GroupStepComponent;
  let componentRef: ComponentRef<GroupStepComponent>;
  let fixture: ComponentFixture<GroupStepComponent>;
  const mockI18nPipe = { transform: vi.fn((val) => val?.['en'] ?? '') };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GroupStepComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: I18nPipe, useValue: mockI18nPipe },
      ],
    })
      .overrideComponent(GroupStepComponent, {
        set: {
          imports: [TranslocoModule, DatePipe],
          schemas: [NO_ERRORS_SCHEMA],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(GroupStepComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
  });

  function setInputs(groupFeed: InterestGroupFeed, type = 'library') {
    componentRef.setInput('groupFeed', groupFeed);
    componentRef.setInput('type', type);
    fixture.detectChanges();
  }

  it('should create', () => {
    setInputs({ id: '1', name: 'Test', feed: [] });
    expect(component).toBeTruthy();
  });

  describe('ngOnChanges', () => {
    it('should set hasMoreThan15 to true when feed has more than 15 items', () => {
      const feed: UserActionLog[] = Array.from({ length: 16 }, () => ({
        action: 'test',
      }));
      setInputs({ id: '1', feed });
      expect(component.hasMoreThan15).toBe(true);
    });

    it('should set hasMoreThan15 to false when feed has 15 or fewer items', () => {
      setInputs({ id: '1', feed: Array.from({ length: 15 }, () => ({})) });
      expect(component.hasMoreThan15).toBe(false);
    });
  });

  describe('getGroupNameOrTitle', () => {
    it('should return transformed title when title exists', () => {
      mockI18nPipe.transform.mockReturnValue('Translated Title');
      setInputs({ id: '1', title: { en: 'Translated Title' }, feed: [] });
      expect(component.getGroupNameOrTitle()).toBe('Translated Title');
    });

    it('should return name when title is empty', () => {
      setInputs({ id: '1', name: 'GroupName', title: {}, feed: [] });
      expect(component.getGroupNameOrTitle()).toBe('GroupName');
    });

    it('should return empty string when no title and no name', () => {
      setInputs({ id: '1', feed: [] });
      expect(component.getGroupNameOrTitle()).toBe('');
    });
  });

  describe('getDisplayProperty', () => {
    beforeEach(() => {
      setInputs({ id: '1', feed: [] });
    });

    it('should return empty string for undefined node', () => {
      expect(component.getDisplayProperty(undefined)).toBe('');
    });

    it('should return title for node with title and event properties', () => {
      mockI18nPipe.transform.mockReturnValue('{en=Meeting Title}');
      const node: ModelNode = {
        name: 'event1',
        title: { en: 'Meeting Title' },
        properties: { kindOfEvent: 'meeting' },
      };
      expect(component.getDisplayProperty(node)).toBe('Meeting Title');
    });

    it('should return name when title transform is empty for event node', () => {
      mockI18nPipe.transform.mockReturnValue('');
      const node: ModelNode = {
        name: 'event1',
        title: { en: '' },
        properties: { eventType: 'event' },
      };
      expect(component.getDisplayProperty(node)).toBe('event1');
    });

    it('should format post name correctly', () => {
      mockI18nPipe.transform.mockReturnValue('');
      const node: ModelNode = {
        name: 'posted-01-05-2026-14-30.html',
      };
      expect(component.getDisplayProperty(node)).toBe('Post 01-05-2026 14:30');
    });

    it('should return title over post name when title exists', () => {
      mockI18nPipe.transform.mockReturnValue('Post Title');
      const node: ModelNode = {
        name: 'posted-01-05-2026-14-30.html',
      };
      expect(component.getDisplayProperty(node)).toBe('Post Title');
    });

    it('should return name as default when no title', () => {
      mockI18nPipe.transform.mockReturnValue('');
      const node: ModelNode = { name: 'document.pdf' };
      expect(component.getDisplayProperty(node)).toBe('document.pdf');
    });

    it('should return title when title is non-empty and no special properties', () => {
      mockI18nPipe.transform.mockReturnValue('Doc Title');
      const node: ModelNode = { name: 'doc.pdf', title: { en: 'Doc Title' } };
      expect(component.getDisplayProperty(node)).toBe('Doc Title');
    });
  });

  describe('getDisplayableFeed', () => {
    it('should return first 15 items when showAll is false', () => {
      const feed: UserActionLog[] = Array.from({ length: 20 }, (_, i) => ({
        action: `action${i}`,
      }));
      setInputs({ id: '1', feed });
      component.showAll = false;
      expect(component.getDisplayableFeed()!).toHaveLength(15);
    });

    it('should return all items when showAll is true', () => {
      const feed: UserActionLog[] = Array.from({ length: 20 }, (_, i) => ({
        action: `action${i}`,
      }));
      setInputs({ id: '1', feed });
      component.showAll = true;
      expect(component.getDisplayableFeed()!).toHaveLength(20);
    });

    it('should return undefined when feed is undefined', () => {
      setInputs({ id: '1' });
      expect(component.getDisplayableFeed()).toBeUndefined();
    });
  });

  describe('showAllFeed', () => {
    it('should set showAll to true', () => {
      setInputs({ id: '1', feed: [] });
      component.showAllFeed();
      expect(component.showAll).toBe(true);
    });
  });
});
