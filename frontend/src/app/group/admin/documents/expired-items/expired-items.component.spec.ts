import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import {
  ContentService,
  ExpiredService,
  InterestGroup,
  InterestGroupService,
  Node as ModelNode,
  PagedNodes,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { ExpiredItemsComponent } from './expired-items.component';

// Wrapper to provide required input
@Component({
  template: '<cbc-expired-items [groupId]="groupId" />',
  imports: [ExpiredItemsComponent],
})
class TestHostComponent {
  groupId = '123';
}

const mockIg: InterestGroup = {
  name: 'Test Group',
  permissions: { library: 'Admin' },
};

const mockNode: ModelNode = {
  id: 'node1',
  name: 'test.pdf',
  type: 'content',
  properties: {
    modifier: 'user1',
    expiration_date: '2025-01-01',
    mimetype: 'application/pdf',
    url: '',
  },
};

const mockPagedNodes: PagedNodes = {
  data: [mockNode],
  total: 1,
};

describe('ExpiredItemsComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let component: ExpiredItemsComponent;

  const mockGroupService = {
    getInterestGroupAsync: vi.fn().mockResolvedValue(mockIg),
  };
  const mockExpiredService = {
    getExpiredDocumentsAsync: vi.fn().mockResolvedValue(mockPagedNodes),
  };
  const mockContentService = {
    deleteContentAsync: vi.fn().mockResolvedValue(undefined),
  };
  const mockUiMessageService = {
    addSuccessMessage: vi.fn(),
    addErrorMessage: vi.fn(),
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
      providers: [
        provideRouter([]),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: InterestGroupService, useValue: mockGroupService },
        { provide: ExpiredService, useValue: mockExpiredService },
        { provide: ContentService, useValue: mockContentService },
        { provide: UiMessageService, useValue: mockUiMessageService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    component = fixture.debugElement.children[0].componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load interest group and expired nodes on init', () => {
    expect(mockGroupService.getInterestGroupAsync).toHaveBeenCalledWith({
      id: '123',
    });
    expect(mockExpiredService.getExpiredDocumentsAsync).toHaveBeenCalledWith({
      id: '123',
      limit: 10,
      page: 1,
      order: 'expirationDate_DESC',
    });
    expect(component.expiredNodes()).toEqual(mockPagedNodes);
    expect(component.totalItems()).toBe(1);
    expect(component.loading()).toBe(false);
  });

  it('should change page', async () => {
    component.goToPage(2);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(component.listingOptions().page).toBe(2);
    expect(mockExpiredService.getExpiredDocumentsAsync).toHaveBeenCalledWith({
      id: '123',
      limit: 10,
      page: 2,
      order: 'expirationDate_DESC',
    });
  });

  it('should change limit and reset page to 1', async () => {
    component.changeLimit(25);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(component.listingOptions().limit).toBe(25);
    expect(component.listingOptions().page).toBe(1);
  });

  it('should identify file nodes', () => {
    expect(component.isFile({ type: 'content' } as ModelNode)).toBe(true);
    expect(component.isFile({ type: 'folder' } as ModelNode)).toBe(false);
  });

  it('should identify folder nodes', () => {
    expect(component.isFolder({ type: 'folder' } as ModelNode)).toBe(true);
    expect(component.isFolder({ type: 'content' } as ModelNode)).toBe(false);
  });

  it('should identify link nodes', () => {
    const link = {
      properties: { mimetype: 'text/html', url: 'http://example.com' },
    } as unknown as ModelNode;
    expect(component.isLink(link)).toBe(true);

    const notLink = {
      properties: { mimetype: 'application/pdf', url: '' },
    } as unknown as ModelNode;
    expect(component.isLink(notLink)).toBe(false);
  });

  it('should toggle node preparation', () => {
    component.prepareNode(mockNode);
    expect(component.isPrepared(mockNode)).toBe(true);

    component.prepareNode(mockNode);
    expect(component.isPrepared(mockNode)).toBe(false);
  });

  it('should toggle all prepared', () => {
    component.toggleAllPrepared();
    expect(component.allSelected).toBe(true);
    expect(component.deletableNodes).toContain(mockNode);

    component.toggleAllPrepared();
    expect(component.allSelected).toBe(false);
    expect(component.deletableNodes).toHaveLength(0);
  });

  it('should delete a single node', async () => {
    await component.deleteNode(mockNode);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(mockContentService.deleteContentAsync).toHaveBeenCalledWith({
      id: 'node1',
    });
    expect(mockUiMessageService.addSuccessMessage).toHaveBeenCalled();
  });

  it('should handle error when deleting a single node', async () => {
    mockContentService.deleteContentAsync.mockRejectedValueOnce(
      new Error('fail')
    );
    await component.deleteNode(mockNode);
    expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
  });

  it('should delete multiple prepared nodes', async () => {
    component.deletableNodes = [mockNode];
    await component.deleteNodes();
    fixture.detectChanges();
    await fixture.whenStable();
    expect(mockContentService.deleteContentAsync).toHaveBeenCalledWith({
      id: 'node1',
    });
    expect(mockUiMessageService.addSuccessMessage).toHaveBeenCalled();
    expect(component.deleting()).toBe(false);
    expect(component.mustConfirm()).toBe(false);
  });

  it('should show error when batch delete fails', async () => {
    mockContentService.deleteContentAsync.mockRejectedValueOnce(
      new Error('fail')
    );
    component.deletableNodes = [mockNode];
    await component.deleteNodes();
    expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
  });

  it('should handle onDeletedElement for DELETE_SPACE', async () => {
    component.onDeletedElement({
      result: ActionResult.SUCCEED,
      type: ActionType.DELETE_SPACE,
    });
    fixture.detectChanges();
    await fixture.whenStable();
    expect(mockUiMessageService.addSuccessMessage).toHaveBeenCalled();
  });

  it('should handle onDeletedElement for DELETE_CONTENT', async () => {
    component.onDeletedElement({
      result: ActionResult.SUCCEED,
      type: ActionType.DELETE_CONTENT,
    });
    fixture.detectChanges();
    await fixture.whenStable();
    expect(mockUiMessageService.addSuccessMessage).toHaveBeenCalled();
  });

  it('should get modifier from node properties', () => {
    expect(component.getModifier(mockNode)).toBe('user1');
    expect(component.getModifier({} as ModelNode)).toBe('');
  });

  it('should get expiration date from node properties', () => {
    expect(component.getExpirationDate(mockNode)).toBe('2025-01-01');
    expect(component.getExpirationDate({} as ModelNode)).toBeNull();
  });

  it('should open update expired date modal', () => {
    component.updateExpiredDate(mockNode);
    expect(component.nodeSelected).toBe(mockNode);
    expect(component.showUpdateExpirateDateModal).toBe(true);
  });
});
