import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { Node as ModelNode, SpaceService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { AddUrlComponent } from './add-url.component';

describe('AddUrlComponent', () => {
  let component: AddUrlComponent;
  let componentRef: ComponentRef<AddUrlComponent>;
  let fixture: ComponentFixture<AddUrlComponent>;

  const mockSpaceService = {
    postURL: vi.fn(),
    postURLAsync: vi.fn(),
  };

  const parentNode: ModelNode = { id: 'node-123', name: 'parent' };

  beforeEach(async () => {
    mockSpaceService.postURL.mockReturnValue(of({} as ModelNode));
    mockSpaceService.postURLAsync.mockResolvedValue({} as ModelNode);

    await TestBed.configureTestingModule({
      imports: [AddUrlComponent],
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

    fixture = TestBed.createComponent(AddUrlComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('parentNode', parentNode);
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize form with empty values', () => {
    expect(component.createUrlForm.value).toEqual({ name: '', url: '' });
  });

  it('should have invalid form when empty', () => {
    expect(component.createUrlForm.valid).toBe(false);
  });

  it('should have valid form when name and url are provided', () => {
    component.createUrlForm.setValue({
      name: 'test-link',
      url: 'http://example.com',
    });
    expect(component.createUrlForm.valid).toBe(true);
  });

  it('should call spaceService.postURL on createUrl and emit success', async () => {
    mockSpaceService.postURLAsync.mockResolvedValue({} as ModelNode);
    component.createUrlForm.setValue({
      name: 'my-url',
      url: 'http://test.com',
    });
    vi.spyOn(component.createUrlForm, 'reset').mockImplementation(() => {});

    const emitSpy = vi.spyOn(component.modalHide, 'emit');

    await component.createUrl();

    expect(mockSpaceService.postURLAsync).toHaveBeenCalledWith({
      id: 'node-123',
      node: {
        name: 'my-url',
        properties: { url: 'http://test.com' },
      },
    });
    expect(emitSpy).toHaveBeenCalledWith(
      expect.objectContaining({
        type: ActionType.ADD_URL,
        result: ActionResult.SUCCEED,
      })
    );
    expect(component.processing()).toBe(false);
  });

  it('should emit FAILED result when postURL throws', async () => {
    mockSpaceService.postURLAsync.mockRejectedValue(new Error('fail'));
    component.createUrlForm.setValue({
      name: 'my-url',
      url: 'http://test.com',
    });

    const emitSpy = vi.spyOn(component.modalHide, 'emit');

    await component.createUrl();

    expect(emitSpy).toHaveBeenCalledWith(
      expect.objectContaining({
        type: ActionType.ADD_URL,
        result: ActionResult.FAILED,
      })
    );
    expect(component.processing()).toBe(false);
  });

  it('should emit CANCELED result and reset form on cancel', () => {
    component.createUrlForm.setValue({ name: 'test', url: 'http://x.com' });

    const emitSpy = vi.spyOn(component.modalHide, 'emit');

    component.cancel();

    expect(emitSpy).toHaveBeenCalledWith({
      result: ActionResult.CANCELED,
      type: ActionType.ADD_URL,
    });
    expect(component.createUrlForm.value).toEqual({ name: '', url: '' });
  });

  it('should expose nameControl and urlControl getters', () => {
    expect(component.nameControl).toBe(
      component.createUrlForm.controls['name']
    );
    expect(component.urlControl).toBe(component.createUrlForm.controls['url']);
  });
});
