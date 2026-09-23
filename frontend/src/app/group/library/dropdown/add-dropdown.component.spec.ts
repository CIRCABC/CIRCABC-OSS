import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import { Node as ModelNode, SpaceService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { AddDropdownComponent } from './add-dropdown.component';

describe('AddDropdownComponent', () => {
  let component: AddDropdownComponent;
  let fixture: ComponentFixture<AddDropdownComponent>;

  const paramsSubject = new Subject<{ [key: string]: string }>();

  const mockSpaceService = {
    getExportedSharedSpacesAsync: vi.fn().mockResolvedValue([]),
  };

  const mockPermEvalService = {
    isLibManageOwnOrHigher: vi.fn().mockReturnValue(true),
  };

  const mockLoginService = {
    isGuest: vi.fn().mockReturnValue(false),
  };

  const mockNode = { id: 'node-1', name: 'Test Node' } as ModelNode;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddDropdownComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        { provide: SpaceService, useValue: mockSpaceService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        { provide: LoginService, useValue: mockLoginService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(AddDropdownComponent, {
        set: { template: '', schemas: [NO_ERRORS_SCHEMA] },
      })
      .compileComponents();

    fixture = TestBed.createComponent(AddDropdownComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('currentNode', mockNode);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should set isGuest on init', () => {
    expect(mockLoginService.isGuest).toHaveBeenCalled();
    expect(component.isGuest).toBe(false);
  });

  it('should load shared space items when route params emit', async () => {
    const items = [{ id: 's1', path: '/shared' }];
    mockSpaceService.getExportedSharedSpacesAsync.mockResolvedValue(items);

    paramsSubject.next({ id: 'space-123' });
    await fixture.whenStable();

    expect(mockSpaceService.getExportedSharedSpacesAsync).toHaveBeenCalledWith({
      id: 'space-123',
    });
    expect(component.sharedSpaceItems()).toEqual(items);
    expect(component.spaceId()).toBe('space-123');
  });

  it('should not call spaceService if id is not in params', async () => {
    mockSpaceService.getExportedSharedSpacesAsync.mockClear();
    paramsSubject.next({});
    await fixture.whenStable();

    expect(
      mockSpaceService.getExportedSharedSpacesAsync
    ).not.toHaveBeenCalled();
  });

  it('hasSharedSpaceItems returns true when items exist', async () => {
    mockSpaceService.getExportedSharedSpacesAsync.mockResolvedValue([
      { id: '1', path: '/p' },
    ]);
    paramsSubject.next({ id: 'space-1' });
    await fixture.whenStable();

    expect(component.hasSharedSpaceItems()).toBe(true);
  });

  it('hasSharedSpaceItems returns false when items are empty', async () => {
    mockSpaceService.getExportedSharedSpacesAsync.mockResolvedValue([]);
    paramsSubject.next({ id: 'space-1' });
    await fixture.whenStable();

    expect(component.hasSharedSpaceItems()).toBe(false);
  });

  it('hasSharedSpaceItems returns false when no space id is present', () => {
    expect(component.hasSharedSpaceItems()).toBe(false);
  });

  it('toggleAddDropdown should toggle showAddDropdown when target has dropdown-trigger class', () => {
    const event = {
      target: { classList: { contains: vi.fn().mockReturnValue(true) } },
    };
    component.showAddDropdown = false;
    component.toggleAddDropdown(event);
    expect(component.showAddDropdown).toBe(true);
    component.toggleAddDropdown(event);
    expect(component.showAddDropdown).toBe(false);
  });

  it('toggleAddDropdown should not toggle when target lacks dropdown-trigger class', () => {
    const event = {
      target: { classList: { contains: vi.fn().mockReturnValue(false) } },
    };
    component.showAddDropdown = false;
    component.toggleAddDropdown(event);
    expect(component.showAddDropdown).toBe(false);
  });

  it('launchCreateSpaceWizard should hide dropdown and toggle launchCreateSpace', () => {
    component.showAddDropdown = true;
    component.launchCreateSpace = false;
    component.launchCreateSpaceWizard();
    expect(component.showAddDropdown).toBe(false);
    expect(component.launchCreateSpace).toBe(true);
  });

  it('launchAddWizardStep1 should hide dropdown and toggle launchAddContent', () => {
    component.showAddDropdown = true;
    component.launchAddContent = false;
    component.launchAddWizardStep1();
    expect(component.showAddDropdown).toBe(false);
    expect(component.launchAddContent).toBe(true);
  });

  it('launchAddUrlModal should hide dropdown and set launchAddUrl to true', () => {
    component.showAddDropdown = true;
    component.launchAddUrlModal();
    expect(component.showAddDropdown).toBe(false);
    expect(component.launchAddUrl).toBe(true);
  });

  it('launchSharedSpaceLinkModal should hide dropdown and set launchAddSharedSpaceLink to true', () => {
    component.showAddDropdown = true;
    component.launchSharedSpaceLinkModal();
    expect(component.showAddDropdown).toBe(false);
    expect(component.launchAddSharedSpaceLink).toBe(true);
  });

  it('showImport should hide dropdown and set showModalImport to true', () => {
    component.showAddDropdown = true;
    component.showImport();
    expect(component.showAddDropdown).toBe(false);
    expect(component.showModalImport).toBe(true);
  });

  it('isLibManageOwn should delegate to permEvalService', () => {
    const result = component.isLibManageOwn();
    expect(mockPermEvalService.isLibManageOwnOrHigher).toHaveBeenCalledWith(
      mockNode
    );
    expect(result).toBe(true);
  });

  it('propagateCreateSpaceClosure should reset flag and emit result', () => {
    const emitSpy = vi.spyOn(component.actionFinished, 'emit');
    component.launchCreateSpace = true;
    const result = { type: undefined, result: undefined };
    component.propagateCreateSpaceClosure(result);
    expect(component.launchCreateSpace).toBe(false);
    expect(emitSpy).toHaveBeenCalledWith(result);
  });

  it('propagateUploadFilesClosure should reset flag and emit result', () => {
    const emitSpy = vi.spyOn(component.actionFinished, 'emit');
    component.launchAddContent = true;
    const result = { type: undefined, result: undefined };
    component.propagateUploadFilesClosure(result);
    expect(component.launchAddContent).toBe(false);
    expect(emitSpy).toHaveBeenCalledWith(result);
  });

  it('propagateAddUrlClosure should reset flag and emit result', () => {
    const emitSpy = vi.spyOn(component.actionFinished, 'emit');
    component.launchAddUrl = true;
    const result = { type: undefined, result: undefined };
    component.propagateAddUrlClosure(result);
    expect(component.launchAddUrl).toBe(false);
    expect(emitSpy).toHaveBeenCalledWith(result);
  });

  it('propagateAddLinkClosure should reset flag and emit result', () => {
    const emitSpy = vi.spyOn(component.actionFinished, 'emit');
    component.launchAddSharedSpaceLink = true;
    const result = { type: undefined, result: undefined };
    component.propagateAddLinkClosure(result);
    expect(component.launchAddSharedSpaceLink).toBe(false);
    expect(emitSpy).toHaveBeenCalledWith(result);
  });

  it('propagateAfterImportClosure should reset flag and emit result', () => {
    const emitSpy = vi.spyOn(component.actionFinished, 'emit');
    component.showModalImport = true;
    const result = { type: undefined, result: undefined };
    component.propagateAfterImportClosure(result);
    expect(component.showModalImport).toBe(false);
    expect(emitSpy).toHaveBeenCalledWith(result);
  });

  it('onClick should hide dropdown and emit clickOutside when clicking outside', () => {
    const emitSpy = vi.spyOn(component.clickOutside, 'emit');
    component.showAddDropdown = true;
    const event = new MouseEvent('click');
    const externalElement = document.createElement('div');
    component.onClick(event, externalElement);
    expect(component.showAddDropdown).toBe(false);
    expect(emitSpy).toHaveBeenCalledWith(event);
  });

  it('onClick should not hide dropdown when clicking inside', () => {
    component.showAddDropdown = true;
    const event = new MouseEvent('click');
    const nativeEl = fixture.nativeElement as HTMLElement;
    component.onClick(event, nativeEl);
    expect(component.showAddDropdown).toBe(true);
  });

  it('onClick should return early if targetElement is null', () => {
    const emitSpy = vi.spyOn(component.clickOutside, 'emit');
    component.showAddDropdown = true;
    const event = new MouseEvent('click');
    component.onClick(event, null);
    expect(component.showAddDropdown).toBe(true);
    expect(emitSpy).not.toHaveBeenCalled();
  });
});
