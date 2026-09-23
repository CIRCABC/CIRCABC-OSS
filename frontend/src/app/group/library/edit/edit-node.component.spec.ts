import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  ContentService,
  DynamicPropertiesService,
  Node as ModelNode,
  NodesService,
  PermissionService,
  SpaceService,
} from 'app/core/generated/circabc';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { EditNodeComponent } from './edit-node.component';

const paramsSubject = new Subject<{ id: string; nodeId: string }>();

const mockRoute = {
  params: paramsSubject.asObservable(),
};

const mockRouter = {
  navigate: vi.fn().mockResolvedValue(true),
};

const mockNode: ModelNode = {
  id: 'node-1',
  name: 'test-file.pdf',
  type: 'content',
  title: { EN: 'Test Title' },
  description: { EN: 'Test Description' },
  properties: {
    author: 'Author',
    status: 'DRAFT',
    encoding: 'UTF-8',
    mimetype: 'application/pdf',
    reference: 'REF-001',
    security_ranking: 'NORMAL',
    url: '',
    issue_date: '2025-01-01',
    expiration_date: '',
  },
};

const mockFolderNode: ModelNode = {
  id: 'folder-1',
  name: 'test-folder',
  type: 'folder',
  title: { EN: 'Folder Title' },
  description: { EN: 'Folder Desc' },
  properties: {
    expiration_date: '',
  },
};

const mockNodesService = {
  getNodeAsync: vi.fn().mockResolvedValue(mockNode),
};

const mockSpaceService = {
  putSpace: vi.fn().mockReturnValue(of({})),
  putSpaceAsync: vi.fn().mockResolvedValue({}),
};

const mockContentService = {
  putContent: vi.fn().mockReturnValue(of({})),
  putContentAsync: vi.fn().mockResolvedValue({}),
};

const mockDynamicPropertiesService = {
  getDynamicPropertyDefinitionsAsync: vi.fn().mockResolvedValue([]),
};

const mockPermissionService = {
  putPermission: vi.fn().mockReturnValue(of({})),
  putPermissionAsync: vi.fn().mockResolvedValue({}),
};

const mockDialog = {
  open: vi.fn(),
};

describe('EditNodeComponent', () => {
  let component: EditNodeComponent;
  let fixture: ComponentFixture<EditNodeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditNodeComponent, ReactiveFormsModule],
      providers: [
        provideNativeDateAdapter(),
        { provide: ActivatedRoute, useValue: mockRoute },
        { provide: Router, useValue: mockRouter },
        { provide: NodesService, useValue: mockNodesService },
        { provide: SpaceService, useValue: mockSpaceService },
        { provide: ContentService, useValue: mockContentService },
        {
          provide: DynamicPropertiesService,
          useValue: mockDynamicPropertiesService,
        },
        { provide: PermissionService, useValue: mockPermissionService },
        { provide: MatDialog, useValue: mockDialog },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EditNodeComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('ngOnInit', () => {
    it('should initialize the form with expected controls', () => {
      fixture.detectChanges();
      const controls = component.editNodeForm.controls;
      expect(controls['name']).toBeDefined();
      expect(controls['title']).toBeDefined();
      expect(controls['description']).toBeDefined();
      expect(controls['author']).toBeDefined();
      expect(controls['expirationDate']).toBeDefined();
      expect(controls['expirationDateActived']).toBeDefined();
    });

    it('should load node when route params emit', async () => {
      fixture.detectChanges();
      paramsSubject.next({ id: 'group-1', nodeId: 'node-1' });

      await vi.waitFor(() => {
        expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({
          id: 'node-1',
        });
      });
    });

    it('should load dynamic properties model when route params emit', async () => {
      fixture.detectChanges();
      paramsSubject.next({ id: 'group-1', nodeId: 'node-1' });

      await vi.waitFor(() => {
        expect(
          mockDynamicPropertiesService.getDynamicPropertyDefinitionsAsync
        ).toHaveBeenCalledWith({ id: 'group-1' });
      });
    });
  });

  describe('loadNode', () => {
    it('should populate form fields from node properties', async () => {
      fixture.detectChanges();
      await component.loadNode('node-1');

      expect(component.editNodeForm.controls['name'].value).toBe(
        'test-file.pdf'
      );
      expect(component.editNodeForm.controls['title'].value).toEqual({
        EN: 'Test Title',
      });
      expect(component.editNodeForm.controls['author'].value).toBe('Author');
      expect(component.editNodeForm.controls['status'].value).toBe('DRAFT');
      expect(component.editNodeForm.controls['encoding'].value).toBe('UTF-8');
    });
  });

  describe('loadDynamicPropertiesModel', () => {
    it('should add form controls for each dynamic property', async () => {
      mockDynamicPropertiesService.getDynamicPropertyDefinitionsAsync.mockResolvedValue(
        [
          { index: 1, title: { EN: 'Prop1' }, propertyType: 'TEXT_FIELD' },
          { index: 2, title: { EN: 'Prop2' }, propertyType: 'DATE_FIELD' },
        ]
      );

      fixture.detectChanges();
      await component.loadDynamicPropertiesModel('group-1');

      expect(component.editNodeForm.controls['dynAttr1']).toBeDefined();
      expect(component.editNodeForm.controls['dynAttr2']).toBeDefined();
    });
  });

  describe('isFile', () => {
    it('should return true when node type does not include folder', () => {
      component.node = { ...mockNode };
      expect(component.isFile()).toBe(true);
    });

    it('should return false when node type includes folder', () => {
      component.node = { ...mockFolderNode };
      expect(component.isFile()).toBe(false);
    });
  });

  describe('isLink', () => {
    it('should return true when mimetype is text/html and url is not empty', () => {
      component.node = {
        type: 'content',
        properties: { mimetype: 'text/html', url: 'http://example.com' },
      };
      expect(component.isLink()).toBe(true);
    });

    it('should return false when mimetype is not text/html', () => {
      component.node = {
        type: 'content',
        properties: { mimetype: 'application/pdf', url: '' },
      };
      expect(component.isLink()).toBe(false);
    });
  });

  describe('tab management', () => {
    it('should default to GeneralInformation tab', () => {
      expect(component.isGeneralTab()).toBe(true);
      expect(component.isDetailsTab()).toBe(false);
      expect(component.isDynamicPropertiesTab()).toBe(false);
    });

    it('should switch tabs correctly', () => {
      component.setTab('Details');
      expect(component.isDetailsTab()).toBe(true);
      expect(component.isGeneralTab()).toBe(false);

      component.setTab('DynamicProperties');
      expect(component.isDynamicPropertiesTab()).toBe(true);
    });
  });

  describe('dynamic property type checks', () => {
    it('should identify DATE_FIELD', () => {
      const dpd = { title: { EN: '' }, propertyType: 'DATE_FIELD' as const };
      expect(component.isDateField(dpd)).toBe(true);
      expect(component.isTextField(dpd)).toBe(false);
    });

    it('should identify TEXT_FIELD', () => {
      const dpd = { title: { EN: '' }, propertyType: 'TEXT_FIELD' as const };
      expect(component.isTextField(dpd)).toBe(true);
    });

    it('should identify TEXT_AREA', () => {
      const dpd = { title: { EN: '' }, propertyType: 'TEXT_AREA' as const };
      expect(component.isTextArea(dpd)).toBe(true);
    });

    it('should identify SELECTION', () => {
      const dpd = { title: { EN: '' }, propertyType: 'SELECTION' as const };
      expect(component.isSelection(dpd)).toBe(true);
      expect(component.isSelectionOrMultiSelection(dpd)).toBe(true);
      expect(component.isMultiSelection(dpd)).toBe(false);
    });

    it('should identify MULTI_SELECTION', () => {
      const dpd = {
        title: { EN: '' },
        propertyType: 'MULTI_SELECTION' as const,
      };
      expect(component.isMultiSelection(dpd)).toBe(true);
      expect(component.isSelectionOrMultiSelection(dpd)).toBe(true);
    });
  });

  describe('compareFn', () => {
    it('should return true when both options are equal', () => {
      expect(component.compareFn('a', 'a')).toBe(true);
    });

    it('should return false when options differ', () => {
      expect(component.compareFn('a', 'b')).toBe(false);
    });

    it('should return false when either option is undefined', () => {
      expect(component.compareFn(undefined, 'b')).toBe(false);
      expect(component.compareFn('a', undefined)).toBe(false);
    });
  });

  describe('currentLanguage', () => {
    it('should return the active language from transloco', () => {
      expect(component.currentLanguage).toBe('en');
    });
  });

  describe('isExpired', () => {
    it('should return false when expiration date is not activated', () => {
      fixture.detectChanges();
      component.editNodeForm.controls['expirationDateActived'].setValue(false);
      expect(component.isExpired()).toBe(false);
    });

    it('should return true when expiration date is activated and in the past', () => {
      fixture.detectChanges();
      component.editNodeForm.controls['expirationDateActived'].setValue(true);
      component.editNodeForm.controls['expirationDate'].setValue(
        new Date('2020-01-01')
      );
      expect(component.isExpired()).toBe(true);
    });
  });

  describe('expirationDateRequired', () => {
    it('should return true when activated but no date set', () => {
      fixture.detectChanges();
      component.editNodeForm.controls['expirationDateActived'].setValue(true);
      component.editNodeForm.controls['expirationDate'].setValue(null);
      expect(component.expirationDateRequired()).toBe(true);
    });

    it('should return false when not activated', () => {
      fixture.detectChanges();
      component.editNodeForm.controls['expirationDateActived'].setValue(false);
      expect(component.expirationDateRequired()).toBe(false);
    });
  });

  describe('updateProperties', () => {
    beforeEach(async () => {
      fixture.detectChanges();
      await component.loadNode('node-1');
    });

    it('should call putContent for file nodes and navigate', async () => {
      component.editNodeForm.controls['expirationDateActived'].setValue(false);
      await component.updateProperties();

      expect(mockContentService.putContentAsync).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['../details'], {
        relativeTo: mockRoute,
      });
    });

    it('should call putSpace for folder nodes', async () => {
      mockNodesService.getNodeAsync.mockResolvedValue(mockFolderNode);
      await component.loadNode('folder-1');
      component.editNodeForm.controls['expirationDateActived'].setValue(false);

      await component.updateProperties();

      expect(mockSpaceService.putSpaceAsync).toHaveBeenCalled();
    });

    it('should not save when expired', async () => {
      component.editNodeForm.controls['expirationDateActived'].setValue(true);
      component.editNodeForm.controls['expirationDate'].setValue(
        new Date('2020-01-01')
      );

      await component.updateProperties();

      expect(mockContentService.putContentAsync).not.toHaveBeenCalled();
    });
  });

  describe('getIndex', () => {
    it('should return the index of a dynamic property definition', () => {
      const dpd = {
        index: 3,
        title: { EN: '' },
        propertyType: 'TEXT_FIELD' as const,
      };
      expect(component.getIndex(dpd)).toBe(3);
    });
  });

  describe('ngOnDestroy', () => {
    it('should unsubscribe from date subscription', () => {
      fixture.detectChanges();
      expect(() => component.ngOnDestroy()).not.toThrow();
    });
  });
});
