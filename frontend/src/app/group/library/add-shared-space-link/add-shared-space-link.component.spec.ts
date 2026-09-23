import { TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { SpaceService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AddSharedSpaceLinkComponent } from './add-shared-space-link.component';

const mockSpaceService = {
  postExportedSharedSpace: vi.fn().mockReturnValue(of(undefined)),
  postExportedSharedSpaceAsync: vi.fn().mockResolvedValue(undefined),
};

describe('AddSharedSpaceLinkComponent', () => {
  let component: AddSharedSpaceLinkComponent;
  let fixture: ReturnType<
    typeof TestBed.createComponent<AddSharedSpaceLinkComponent>
  >;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddSharedSpaceLinkComponent],
      providers: [
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

    fixture = TestBed.createComponent(AddSharedSpaceLinkComponent);
    component = fixture.componentInstance;

    fixture.componentRef.setInput('parentNode', { id: 'node-1' });
    fixture.componentRef.setInput('sharedSpaceItems', [
      { id: 'space-1', path: '/path' },
    ]);

    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with empty values', () => {
    expect(component.createSharedSpaceLinkForm.value).toEqual({
      title: '',
      description: '',
      sharedSpaceId: '',
    });
  });

  it('should have invalid form when title and sharedSpaceId are empty', () => {
    expect(component.createSharedSpaceLinkForm.valid).toBe(false);
  });

  it('should have valid form when title and sharedSpaceId are filled', () => {
    component.titleControl.setValue('Test Title');
    component.sharedSpaceIdControl.setValue('space-1');
    expect(component.createSharedSpaceLinkForm.valid).toBe(true);
  });

  it('should call spaceService and emit success on createSharedSpaceLink', async () => {
    const emitSpy = vi.spyOn(component.modalHide, 'emit');
    component.titleControl.setValue('Link Title');
    component.descriptionControl.setValue('Desc');
    component.sharedSpaceIdControl.setValue('space-1');

    await component.createSharedSpaceLink();

    expect(mockSpaceService.postExportedSharedSpaceAsync).toHaveBeenCalledWith({
      id: 'space-1',
      parentId: 'node-1',
      title: 'Link Title',
      description: 'Desc',
    });
    expect(emitSpy).toHaveBeenCalledWith({
      type: ActionType.ADD_SHARED_SPACE_LINK,
      result: ActionResult.SUCCEED,
    });
    expect(component.processing()).toBe(false);
  });

  it('should emit failed result when spaceService throws', async () => {
    mockSpaceService.postExportedSharedSpaceAsync.mockRejectedValue(
      new Error('fail')
    );
    const emitSpy = vi.spyOn(component.modalHide, 'emit');
    component.titleControl.setValue('Title');
    component.sharedSpaceIdControl.setValue('space-1');

    await component.createSharedSpaceLink();

    expect(emitSpy).toHaveBeenCalledWith({
      type: ActionType.ADD_SHARED_SPACE_LINK,
      result: ActionResult.FAILED,
    });
    expect(component.processing()).toBe(false);
  });

  it('should emit canceled result and reset form on cancel', () => {
    const emitSpy = vi.spyOn(component.modalHide, 'emit');
    component.titleControl.setValue('Something');

    component.cancel();

    expect(emitSpy).toHaveBeenCalledWith({
      type: ActionType.ADD_SHARED_SPACE_LINK,
      result: ActionResult.CANCELED,
    });
    expect(component.titleControl.value).toBeNull();
  });
});
