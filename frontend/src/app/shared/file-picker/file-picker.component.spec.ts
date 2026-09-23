import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  Node as ModelNode,
  NodesService,
  PagedNodes,
  SpaceService,
} from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { FilePickerComponent } from './file-picker.component';

const mockNode: ModelNode = {
  id: 'node-1',
  name: 'Library',
  type: 'folder',
};

const mockPagedNodes: PagedNodes = {
  data: [
    { id: 'child-1', name: 'file.txt', type: 'content' },
    { id: 'child-2', name: 'subfolder', type: 'folder' },
  ],
  total: 2,
};

const mockNodesService = {
  getNodeAsync: vi.fn().mockResolvedValue(mockNode),
};

const mockSpaceService = {
  getChildrenAsync: vi.fn().mockResolvedValue(mockPagedNodes),
};

describe('FilePickerComponent', () => {
  let component: FilePickerComponent;
  let fixture: ComponentFixture<FilePickerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FilePickerComponent],
      providers: [
        { provide: NodesService, useValue: mockNodesService },
        { provide: SpaceService, useValue: mockSpaceService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    vi.clearAllMocks();
    mockNodesService.getNodeAsync.mockResolvedValue(mockNode);
    mockSpaceService.getChildrenAsync.mockResolvedValue(mockPagedNodes);

    fixture = TestBed.createComponent(FilePickerComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('loading content on init', () => {
    it('should load node and content when nodeId is set', async () => {
      fixture.componentRef.setInput('nodeId', 'node-1');
      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({
        id: 'node-1',
      });
      expect(mockSpaceService.getChildrenAsync).toHaveBeenCalled();
      expect(component.content()).toEqual(mockPagedNodes);
      expect(component.currentNode()).toEqual(mockNode);
    });

    it('should not load anything when nodeId is undefined', async () => {
      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockNodesService.getNodeAsync).not.toHaveBeenCalled();
      expect(mockSpaceService.getChildrenAsync).not.toHaveBeenCalled();
    });
  });

  describe('getContent', () => {
    it('should fetch node and children for given nodeId', async () => {
      component.getContent('node-1');
      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({
        id: 'node-1',
      });
      expect(mockSpaceService.getChildrenAsync).toHaveBeenCalledWith({
        id: 'node-1',
        language: 'en',
        guest: false,
        limit: -1,
        page: -1,
        order: 'modified_DESC',
        folderOnly: false,
        fileOnly: false,
        skipExpiredItems: true,
      });
      expect(component.loading()).toBe(false);
      expect(component.content()).toEqual(mockPagedNodes);
    });

    it('should not fetch when nodeId is undefined', async () => {
      component.getContent(undefined);
      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockNodesService.getNodeAsync).not.toHaveBeenCalled();
    });
  });

  describe('isLibrary', () => {
    it('should return true when currentNode matches nodeId and name is Library', async () => {
      mockNodesService.getNodeAsync.mockResolvedValue({
        id: 'node-1',
        name: 'Library',
      });
      fixture.componentRef.setInput('nodeId', 'node-1');
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.isLibrary()).toBe(true);
    });

    it('should return false when name is not Library', async () => {
      mockNodesService.getNodeAsync.mockResolvedValue({
        id: 'node-1',
        name: 'Other',
      });
      fixture.componentRef.setInput('nodeId', 'node-1');
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.isLibrary()).toBe(false);
    });

    it('should return false when nodeId is not set', () => {
      expect(component.isLibrary()).toBe(false);
    });
  });

  describe('isFolder', () => {
    it('should return true when type includes folder', () => {
      expect(component.isFolder({ type: 'folder' })).toBe(true);
      expect(component.isFolder({ type: 'cm:folder' })).toBe(true);
    });

    it('should return false when type does not include folder', () => {
      expect(component.isFolder({ type: 'content' })).toBe(false);
    });

    it('should return false when type is undefined', () => {
      expect(component.isFolder({})).toBe(false);
    });
  });

  describe('isEmpty', () => {
    it('should return true when total is 0', async () => {
      mockSpaceService.getChildrenAsync.mockResolvedValue({
        data: [],
        total: 0,
      });
      fixture.componentRef.setInput('nodeId', 'node-1');
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.isEmpty()).toBe(true);
    });

    it('should return false when total is greater than 0', async () => {
      mockSpaceService.getChildrenAsync.mockResolvedValue({
        data: [{ id: '1' }],
        total: 1,
      });
      fixture.componentRef.setInput('nodeId', 'node-1');
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.isEmpty()).toBe(false);
    });
  });

  describe('isSelected', () => {
    it('should return true when id is in selection', () => {
      fixture.componentRef.setInput('selection', ['id-1', 'id-2']);
      expect(component.isSelected('id-1')).toBe(true);
    });

    it('should return false when id is not in selection', () => {
      fixture.componentRef.setInput('selection', ['id-1']);
      expect(component.isSelected('id-3')).toBe(false);
    });

    it('should return false when id is undefined', () => {
      expect(component.isSelected(undefined)).toBe(false);
    });
  });

  describe('toggleSelect', () => {
    it('should add id to selection', () => {
      fixture.componentRef.setInput('selection', []);
      component.toggleSelect('id-1');
      expect(component.selection()).toEqual(['id-1']);
    });

    it('should remove id from selection if already selected', () => {
      fixture.componentRef.setInput('selection', ['id-1', 'id-2']);
      component.toggleSelect('id-1');
      expect(component.selection()).toEqual(['id-2']);
    });

    it('should do nothing when id is undefined', () => {
      fixture.componentRef.setInput('selection', ['id-1']);
      component.toggleSelect(undefined);
      expect(component.selection()).toEqual(['id-1']);
    });

    it('should replace selection in targetFolderMode', () => {
      fixture.componentRef.setInput('targetFolderMode', true);
      fixture.componentRef.setInput('selection', ['id-1']);
      component.toggleSelect('id-2');
      expect(component.selection()).toEqual(['id-2']);
    });
  });

  describe('getModified', () => {
    it('should return modified property', () => {
      const node: ModelNode = { properties: { modified: '2024-01-01' } };
      expect(component.getModified(node)).toBe('2024-01-01');
    });

    it('should return null when properties is undefined', () => {
      expect(component.getModified({})).toBeNull();
    });
  });
});
