import { ComponentRef, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { Node, NodesService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { BreadcrumbComponent } from './breadcrumb.component';

describe('BreadcrumbComponent', () => {
  let component: BreadcrumbComponent;
  let componentRef: ComponentRef<BreadcrumbComponent>;
  let fixture: ComponentFixture<BreadcrumbComponent>;

  const mockNodesService = {
    getPathAsync: vi.fn().mockResolvedValue([]),
  };

  const mockRouter = {
    url: '/group/library',
  };

  function setup(url = '/group/library') {
    (mockRouter as { url: string }).url = url;
    mockNodesService.getPathAsync.mockResolvedValue([]);

    TestBed.configureTestingModule({
      imports: [BreadcrumbComponent],
      providers: [
        { provide: NodesService, useValue: mockNodesService },
        { provide: Router, useValue: mockRouter },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(BreadcrumbComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });

    fixture = TestBed.createComponent(BreadcrumbComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    fixture.detectChanges();
  }

  describe('ngOnInit', () => {
    it('should set isInDetails when URL contains details', () => {
      setup('/group/library/details/123');
      expect(component.isInDetails).toBe(true);
      expect(component.isInTopic).toBe(false);
    });

    it('should set isInTopic when URL contains topic', () => {
      setup('/group/forum/topic/456');
      expect(component.isInTopic).toBe(true);
      expect(component.isInDetails).toBe(false);
    });

    it('should set both flags to false for normal URLs', () => {
      setup('/group/library');
      expect(component.isInDetails).toBe(false);
      expect(component.isInTopic).toBe(false);
    });
  });

  describe('ngOnChanges', () => {
    it('should return early if node is undefined', async () => {
      setup();
      await component.ngOnChanges();
      expect(mockNodesService.getPathAsync).not.toHaveBeenCalled();
    });

    it('should set lastElement when path has one item', async () => {
      setup();
      const singleNode: Node = { id: '1', name: 'Root' };
      mockNodesService.getPathAsync.mockResolvedValue([singleNode]);
      componentRef.setInput('node', '1');
      fixture.detectChanges();
      await component.ngOnChanges();
      expect(component.clickablePath()).toEqual([]);
      expect(component.lastElement()).toEqual([singleNode]);
    });

    it('should split path into clickable and last element for multiple items', async () => {
      setup();
      const nodes: Node[] = [
        { id: '1', name: 'Root' },
        { id: '2', name: 'Child' },
        { id: '3', name: 'Leaf' },
      ];
      mockNodesService.getPathAsync.mockResolvedValue(nodes);
      componentRef.setInput('node', '3');
      fixture.detectChanges();
      await component.ngOnChanges();
      expect(component.clickablePath()).toEqual([nodes[0], nodes[1]]);
      expect(component.lastElement()).toEqual([nodes[2]]);
    });

    it('should handle error from getPath gracefully', async () => {
      setup();
      mockNodesService.getPathAsync.mockRejectedValue(
        new Error('Network error')
      );
      componentRef.setInput('node', '999');
      fixture.detectChanges();
      await component.ngOnChanges();
      expect(component.clickablePath()).toEqual([]);
      expect(component.lastElement()).toEqual([]);
    });
  });

  describe('isRoot', () => {
    it('should return true for the first element in path', async () => {
      setup();
      const nodes: Node[] = [
        { id: '1', name: 'Root' },
        { id: '2', name: 'Child' },
      ];
      mockNodesService.getPathAsync.mockResolvedValue(nodes);
      componentRef.setInput('node', '2');
      fixture.detectChanges();
      await component.ngOnChanges();
      expect(component.isRoot(nodes[0])).toBe(true);
    });

    it('should return false for non-root elements', async () => {
      setup();
      const nodes: Node[] = [
        { id: '1', name: 'Root' },
        { id: '2', name: 'Child' },
      ];
      mockNodesService.getPathAsync.mockResolvedValue(nodes);
      componentRef.setInput('node', '2');
      fixture.detectChanges();
      await component.ngOnChanges();
      expect(component.isRoot(nodes[1])).toBe(false);
    });
  });

  describe('getStyle', () => {
    it('should return empty object when textColor is undefined', () => {
      setup();
      expect(component.getStyle()).toEqual({});
    });

    it('should return color style with hash prefix', () => {
      setup();
      componentRef.setInput('textColor', 'FF0000');
      fixture.detectChanges();
      expect(component.getStyle()).toEqual({ color: '#FF0000' });
    });
  });

  describe('prepareArrayParts', () => {
    it('should return ../ for normal URLs', () => {
      setup('/group/library');
      expect(component.prepareArrayParts({ id: 'abc' })).toEqual([
        '../',
        'abc',
      ]);
    });

    it('should return ../../ when in details', () => {
      setup('/group/library/details/123');
      expect(component.prepareArrayParts({ id: 'abc' })).toEqual([
        '../../',
        'abc',
      ]);
    });

    it('should return ../../ when in topic', () => {
      setup('/group/forum/topic/456');
      expect(component.prepareArrayParts({ id: 'abc' })).toEqual([
        '../../',
        'abc',
      ]);
    });
  });

  describe('isForumNewsgroupsName', () => {
    it('should return true for Newsgroups name with forums type', () => {
      setup();
      expect(component.isForumNewsgroupsName('Newsgroups', 'forums')).toBe(
        true
      );
    });

    it('should return false for non-Newsgroups name', () => {
      setup();
      expect(component.isForumNewsgroupsName('Library', 'forums')).toBe(false);
    });

    it('should return false for Newsgroups name with non-forums type', () => {
      setup();
      expect(component.isForumNewsgroupsName('Newsgroups', 'library')).toBe(
        false
      );
    });
  });
});
