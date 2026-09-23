import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { BrowseComponent } from 'app/browse/browse.component';
import {
  HeaderService,
  InterestGroup,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';
import { of } from 'rxjs';
import { vi } from 'vitest';

describe('BrowseComponent', () => {
  let fixture: ComponentFixture<BrowseComponent>;
  let mockRouter: { navigate: ReturnType<typeof vi.fn> };
  let mockNodesService: { getGroupAsync: ReturnType<typeof vi.fn> };
  let mockLoginService: { isGuest: ReturnType<typeof vi.fn> };
  let mockRedirectionService: { mustRedirect: ReturnType<typeof vi.fn> };
  let mockHeaderService: { getHeaderAsync: ReturnType<typeof vi.fn> };

  function setup(node: ModelNode, queryParams: Record<string, string> = {}) {
    TestBed.configureTestingModule({
      imports: [BrowseComponent],
      providers: [
        { provide: Router, useValue: mockRouter },
        {
          provide: ActivatedRoute,
          useValue: {
            data: of({ node }),
            snapshot: { queryParams },
          },
        },
        { provide: NodesService, useValue: mockNodesService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: RedirectionService, useValue: mockRedirectionService },
        { provide: HeaderService, useValue: mockHeaderService },
      ],
    });

    fixture = TestBed.createComponent(BrowseComponent);
  }

  beforeEach(() => {
    mockRouter = { navigate: vi.fn() };
    mockNodesService = { getGroupAsync: vi.fn() };
    mockLoginService = { isGuest: vi.fn().mockReturnValue(false) };
    mockRedirectionService = { mustRedirect: vi.fn() };
    mockHeaderService = { getHeaderAsync: vi.fn() };
  });

  describe('when group is found', () => {
    it('should navigate to group dashboard when groupId equals nodeId', async () => {
      const group: InterestGroup = { id: 'g1', name: 'G', permissions: {} };
      mockNodesService.getGroupAsync.mockResolvedValue(group);
      setup({ id: 'g1', name: 'Test' });

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockRouter.navigate).toHaveBeenCalledWith(['/group', 'g1']);
    });

    it('should navigate to agenda when node matches eventId', async () => {
      const group: InterestGroup = {
        id: 'g1',
        name: 'G',
        permissions: {},
        eventId: 'e1',
      };
      mockNodesService.getGroupAsync.mockResolvedValue(group);
      setup({ id: 'e1', name: 'Test' });

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockRouter.navigate).toHaveBeenCalledWith([
        '/group',
        'g1',
        'agenda',
      ]);
    });

    it('should navigate to information when node matches informationId', async () => {
      const group: InterestGroup = {
        id: 'g1',
        name: 'G',
        permissions: {},
        informationId: 'i1',
      };
      mockNodesService.getGroupAsync.mockResolvedValue(group);
      setup({ id: 'i1', name: 'Test' });

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockRouter.navigate).toHaveBeenCalledWith([
        '/group',
        'g1',
        'information',
      ]);
    });

    it('should navigate to forum when node matches newsgroupId', async () => {
      const group: InterestGroup = {
        id: 'g1',
        name: 'G',
        permissions: {},
        newsgroupId: 'n1',
      };
      mockNodesService.getGroupAsync.mockResolvedValue(group);
      setup({ id: 'n1', name: 'Test' });

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockRouter.navigate).toHaveBeenCalledWith([
        '/group',
        'g1',
        'forum',
      ]);
    });

    it('should navigate to library when node matches libraryId', async () => {
      const group: InterestGroup = {
        id: 'g1',
        name: 'G',
        permissions: {},
        libraryId: 'l1',
      };
      mockNodesService.getGroupAsync.mockResolvedValue(group);
      setup({ id: 'l1', name: 'Test' });

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockRouter.navigate).toHaveBeenCalledWith([
        '/group',
        'g1',
        'library',
        'l1',
      ]);
    });

    it('should navigate to library details for content type', async () => {
      const group: InterestGroup = { id: 'g1', name: 'G', permissions: {} };
      mockNodesService.getGroupAsync.mockResolvedValue(group);
      setup({ id: 'n1', name: 'Test', service: 'library', type: 'cm:content' });

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockRouter.navigate).toHaveBeenCalledWith(
        ['/group', 'g1', 'library', 'n1', 'details'],
        {}
      );
    });

    it('should navigate to library details with download param', async () => {
      const group: InterestGroup = { id: 'g1', name: 'G', permissions: {} };
      mockNodesService.getGroupAsync.mockResolvedValue(group);
      setup(
        { id: 'n1', name: 'Test', service: 'library', type: 'cm:content' },
        { download: 'true' }
      );

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockRouter.navigate).toHaveBeenCalledWith(
        ['/group', 'g1', 'library', 'n1', 'details'],
        { queryParams: { download: 'true' } }
      );
    });

    it('should navigate to library folder for folder type', async () => {
      const group: InterestGroup = { id: 'g1', name: 'G', permissions: {} };
      mockNodesService.getGroupAsync.mockResolvedValue(group);
      setup({ id: 'n1', name: 'Test', service: 'library', type: 'cm:folder' });

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockRouter.navigate).toHaveBeenCalledWith([
        '/group',
        'g1',
        'library',
        'n1',
      ]);
    });

    it('should navigate to agenda details for events service', async () => {
      const group: InterestGroup = { id: 'g1', name: 'G', permissions: {} };
      mockNodesService.getGroupAsync.mockResolvedValue(group);
      setup({ id: 'n1', name: 'Test', service: 'events' });

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockRouter.navigate).toHaveBeenCalledWith([
        '/group',
        'g1',
        'agenda',
        'n1',
        'details',
      ]);
    });

    it('should navigate to information with filterId for information service', async () => {
      const group: InterestGroup = { id: 'g1', name: 'G', permissions: {} };
      mockNodesService.getGroupAsync.mockResolvedValue(group);
      setup({ id: 'n1', name: 'Test', service: 'information' });

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockRouter.navigate).toHaveBeenCalledWith(
        ['/group', 'g1', 'information'],
        { queryParams: { filterId: 'n1' } }
      );
    });

    it('should navigate to forum for newsgroups service with forum type', async () => {
      const group: InterestGroup = { id: 'g1', name: 'G', permissions: {} };
      mockNodesService.getGroupAsync.mockResolvedValue(group);
      setup({
        id: 'n1',
        name: 'Test',
        service: 'newsgroups',
        type: 'ci:forum',
      });

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockRouter.navigate).toHaveBeenCalledWith([
        '/group',
        'g1',
        'forum',
        'n1',
      ]);
    });

    it('should navigate to forum topic for newsgroups service with topic type', async () => {
      const group: InterestGroup = { id: 'g1', name: 'G', permissions: {} };
      mockNodesService.getGroupAsync.mockResolvedValue(group);
      setup({
        id: 'n1',
        name: 'Test',
        service: 'newsgroups',
        type: 'ci:topic',
      });

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockRouter.navigate).toHaveBeenCalledWith([
        '/group',
        'g1',
        'forum',
        'topic',
        'n1',
      ]);
    });

    it('should navigate to members for circaDirectoryRoot type', async () => {
      const group: InterestGroup = { id: 'g1', name: 'G', permissions: {} };
      mockNodesService.getGroupAsync.mockResolvedValue(group);
      setup({ id: 'n1', name: 'Test', type: 'ci:circaDirectoryRoot' });

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockRouter.navigate).toHaveBeenCalledWith([
        '/group',
        'g1',
        'members',
      ]);
    });

    it('should navigate to group dashboard as fallback', async () => {
      const group: InterestGroup = { id: 'g1', name: 'G', permissions: {} };
      mockNodesService.getGroupAsync.mockResolvedValue(group);
      setup({ id: 'n1', name: 'Test', type: 'unknown' });

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockRouter.navigate).toHaveBeenCalledWith(['/group', 'g1']);
    });
  });

  describe('when group is not found', () => {
    it('should navigate to explore with categoryId when circaCategoryMasterGroup is set', async () => {
      mockNodesService.getGroupAsync.mockRejectedValue(new Error());
      setup({
        id: 'cat1',
        name: 'Test',
        properties: { circaCategoryMasterGroup: 'true' },
      });

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockRouter.navigate).toHaveBeenCalledWith(['/explore'], {
        queryParams: { categoryId: 'cat1' },
      });
    });
  });

  describe('when node name is undefined', () => {
    it('should navigate to explore with headerId when header is found', async () => {
      mockHeaderService.getHeaderAsync.mockResolvedValue({ id: 'h1' });
      setup({ id: 'h1' });

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockRouter.navigate).toHaveBeenCalledWith(['/explore'], {
        queryParams: { headerId: 'h1' },
      });
    });

    it('should navigate to denied when header is not found and user is not guest', async () => {
      mockHeaderService.getHeaderAsync.mockRejectedValue(new Error());
      mockLoginService.isGuest.mockReturnValue(false);
      setup({ id: 'h1' });

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockRouter.navigate).toHaveBeenCalledWith(['/denied']);
      expect(mockRedirectionService.mustRedirect).not.toHaveBeenCalled();
    });

    it('should call mustRedirect and navigate to denied when user is guest', async () => {
      mockHeaderService.getHeaderAsync.mockRejectedValue(new Error());
      mockLoginService.isGuest.mockReturnValue(true);
      setup({ id: 'h1' });

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockRedirectionService.mustRedirect).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/denied']);
    });
  });
});
