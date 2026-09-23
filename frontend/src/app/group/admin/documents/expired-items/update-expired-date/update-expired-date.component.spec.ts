import { ComponentRef, SimpleChange } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideNativeDateAdapter } from '@angular/material/core';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult } from 'app/action-result';
import {
  ContentService,
  Node as ModelNode,
  SpaceService,
} from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { UpdateExpiredDateComponent } from './update-expired-date.component';

const mockContentService = {
  putContentAsync: vi.fn().mockResolvedValue({}),
};

const mockSpaceService = {
  putSpaceAsync: vi.fn().mockResolvedValue({}),
};

function createNode(overrides: Partial<ModelNode> = {}): ModelNode {
  return {
    id: 'node-1',
    name: 'test-node',
    type: 'folder',
    title: { en: 'Test' },
    description: { en: 'Desc' },
    properties: { expiration_date: '2025-01-01' },
    ...overrides,
  };
}

describe('UpdateExpiredDateComponent', () => {
  let component: UpdateExpiredDateComponent;
  let componentRef: ComponentRef<UpdateExpiredDateComponent>;
  let fixture: ComponentFixture<UpdateExpiredDateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdateExpiredDateComponent, ReactiveFormsModule],
      providers: [
        provideNativeDateAdapter(),
        { provide: ContentService, useValue: mockContentService },
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

    fixture = TestBed.createComponent(UpdateExpiredDateComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('showModal', true);
    componentRef.setInput('nodeSelected', createNode());
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize the form on ngOnInit', () => {
    expect(component.editExpiedDateNodeForm).toBeDefined();
    expect(component.editExpiedDateNodeForm.get('name')).toBeDefined();
    expect(
      component.editExpiedDateNodeForm.get('expirationDate')
    ).toBeDefined();
    expect(
      component.editExpiedDateNodeForm.get('expirationDateActived')
    ).toBeDefined();
  });

  it('should load form values on ngOnChanges', async () => {
    const node = createNode({ name: 'changed-node' });
    await component.ngOnChanges({
      nodeSelected: new SimpleChange(null, node, false),
    });
    expect(component.editExpiedDateNodeForm.get('name')?.value).toBe(
      'changed-node'
    );
    expect(
      component.editExpiedDateNodeForm.get('expirationDateActived')?.value
    ).toBe(true);
  });

  it('should set expirationDateActived to false when expiration_date is empty', async () => {
    const node = createNode({ properties: { expiration_date: '' } });
    await component.ngOnChanges({
      nodeSelected: new SimpleChange(null, node, false),
    });
    expect(
      component.editExpiedDateNodeForm.get('expirationDateActived')?.value
    ).toBe(false);
  });

  it('should identify file vs folder via isFile()', () => {
    expect(component.isFile()).toBe(false);

    componentRef.setInput('nodeSelected', createNode({ type: 'content' }));
    expect(component.isFile()).toBe(true);
  });

  it('should call spaceService.putSpace for folders on save', async () => {
    componentRef.setInput('nodeSelected', createNode({ type: 'folder' }));
    fixture.detectChanges();
    component.editExpiedDateNodeForm.patchValue({
      expirationDateActived: true,
      expirationDate: '2026-06-01',
    });

    await component.saveConfiguration();

    expect(mockSpaceService.putSpaceAsync).toHaveBeenCalledWith({
      id: 'node-1',
      node: expect.objectContaining({ id: 'node-1' }),
    });
    expect(mockContentService.putContentAsync).not.toHaveBeenCalled();
  });

  it('should call contentService.putContent for files on save', async () => {
    const fileNode = createNode({
      type: 'content',
      properties: {
        expiration_date: '2025-01-01',
        issueDate: '2024-01-01',
        encoding: 'UTF-8',
        mimetype: 'application/pdf',
        reference: 'ref',
        author: 'author',
        url: '',
        status: 'draft',
        security_ranking: 'normal',
      },
    });
    componentRef.setInput('nodeSelected', fileNode);
    component.editExpiedDateNodeForm.patchValue({
      expirationDateActived: true,
      expirationDate: '2026-06-01',
    });

    await component.saveConfiguration();

    expect(mockContentService.putContentAsync).toHaveBeenCalledWith({
      id: 'node-1',
      node: expect.objectContaining({ id: 'node-1' }),
    });
    expect(mockSpaceService.putSpaceAsync).not.toHaveBeenCalled();
  });

  it('should set showModal to false and emit on cancel', () => {
    const emitSpy = vi.spyOn(component.showModalChange, 'emit');
    component.cancel();
    expect(component.showModal()).toBe(false);
    expect(emitSpy).toHaveBeenCalled();
  });

  it('should emit modalHide with SUCCEED result on save', async () => {
    const hideSpy = vi.spyOn(component.modalHide, 'emit');
    componentRef.setInput('nodeSelected', createNode({ type: 'folder' }));

    await component.saveConfiguration();

    expect(hideSpy).toHaveBeenCalledWith({ result: ActionResult.SUCCEED });
  });

  it('should return current date from getCurrentDate()', () => {
    const now = new Date();
    const result = component.getCurrentDate();
    expect(result.getFullYear()).toBe(now.getFullYear());
    expect(result.getMonth()).toBe(now.getMonth());
    expect(result.getDate()).toBe(now.getDate());
  });
});
