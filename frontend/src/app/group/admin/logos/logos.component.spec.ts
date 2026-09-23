import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionEmitterResult, ActionResult } from 'app/action-result';
import {
  InterestGroup,
  InterestGroupService,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { LogosComponent } from './logos.component';

const mockGroup: InterestGroup = {
  id: 'group1',
  name: 'Test Group',
  permissions: {},
  logoUrl: '/logo/logo1',
};

const mockLogos: ModelNode[] = [
  { id: 'logo1', name: 'logo1.png' } as ModelNode,
  { id: 'logo2', name: 'logo2.png' } as ModelNode,
];

describe('LogosComponent', () => {
  let component: LogosComponent;
  let fixture: ComponentFixture<LogosComponent>;
  let mockGroupsService: {
    getInterestGroupAsync: ReturnType<typeof vi.fn>;
    getGroupLogosAsync: ReturnType<typeof vi.fn>;
    selectGroupLogoAsync: ReturnType<typeof vi.fn>;
    deleteGroupLogoAsync: ReturnType<typeof vi.fn>;
  };
  let paramsSubject: Subject<Record<string, string>>;

  beforeEach(async () => {
    paramsSubject = new Subject();
    mockGroupsService = {
      getInterestGroupAsync: vi.fn().mockResolvedValue(mockGroup),
      getGroupLogosAsync: vi.fn().mockResolvedValue(mockLogos),
      selectGroupLogoAsync: vi.fn().mockResolvedValue(undefined),
      deleteGroupLogoAsync: vi.fn().mockResolvedValue(undefined),
    };

    await TestBed.configureTestingModule({
      imports: [LogosComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        { provide: InterestGroupService, useValue: mockGroupsService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LogosComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load group and logos on route params', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'group1' });
    await new Promise((resolve) => setTimeout(resolve));

    expect(mockGroupsService.getInterestGroupAsync).toHaveBeenCalledWith({
      id: 'group1',
    });
    expect(mockGroupsService.getGroupLogosAsync).toHaveBeenCalledWith({
      id: 'group1',
    });
    expect(component.group()).toEqual(mockGroup);
    expect(component.logos()).toEqual(mockLogos);
  });

  it('should not load group when params.id is missing', async () => {
    fixture.detectChanges();
    paramsSubject.next({});
    await new Promise((resolve) => setTimeout(resolve));

    expect(mockGroupsService.getInterestGroupAsync).not.toHaveBeenCalled();
  });

  describe('isSelected', () => {
    beforeEach(() => {
      component.group.set({ ...mockGroup });
    });

    it('should return true when logo id is in logoUrl', () => {
      expect(component.isSelected('logo1')).toBe(true);
    });

    it('should return false when logo id is not in logoUrl', () => {
      expect(component.isSelected('logo3')).toBe(false);
    });

    it('should return false when id is undefined', () => {
      expect(component.isSelected(undefined)).toBe(false);
    });

    it('should return false when group has no logoUrl', () => {
      component.group.set({ ...mockGroup, logoUrl: undefined });
      expect(component.isSelected('logo1')).toBe(false);
    });
  });

  describe('select', () => {
    beforeEach(() => {
      component.group.set({ ...mockGroup });
    });

    it('should call selectGroupLogo and reload', async () => {
      await component.select('logo2');

      expect(mockGroupsService.selectGroupLogoAsync).toHaveBeenCalledWith({
        id: 'group1',
        logoId: 'logo2',
      });
      expect(mockGroupsService.getInterestGroupAsync).toHaveBeenCalledWith({
        id: 'group1',
      });
    });

    it('should not call service when id is undefined', async () => {
      await component.select(undefined);

      expect(mockGroupsService.selectGroupLogoAsync).not.toHaveBeenCalled();
    });
  });

  describe('delete', () => {
    beforeEach(() => {
      component.group.set({ ...mockGroup });
    });

    it('should deselect then delete when logo is selected', async () => {
      await component.delete('logo1');

      expect(mockGroupsService.selectGroupLogoAsync).toHaveBeenCalledWith({
        id: 'group1',
        logoId: 'logo1',
      });
      expect(mockGroupsService.deleteGroupLogoAsync).toHaveBeenCalledWith({
        id: 'group1',
        logoId: 'logo1',
      });
    });

    it('should delete without deselecting when logo is not selected', async () => {
      await component.delete('logo2');

      expect(mockGroupsService.selectGroupLogoAsync).not.toHaveBeenCalled();
      expect(mockGroupsService.deleteGroupLogoAsync).toHaveBeenCalledWith({
        id: 'group1',
        logoId: 'logo2',
      });
    });

    it('should not call service when id is undefined', async () => {
      await component.delete(undefined);

      expect(mockGroupsService.deleteGroupLogoAsync).not.toHaveBeenCalled();
    });
  });

  describe('refresh', () => {
    beforeEach(() => {
      component.group.set({ ...mockGroup });
      component.showUploadModal.set(true);
    });

    it('should reload group on success and hide modal', async () => {
      const res: ActionEmitterResult = { result: ActionResult.SUCCEED };
      await component.refresh(res);

      expect(mockGroupsService.getInterestGroupAsync).toHaveBeenCalledWith({
        id: 'group1',
      });
      expect(component.showUploadModal()).toBe(false);
    });

    it('should hide modal without reloading on cancel', async () => {
      const res: ActionEmitterResult = { result: ActionResult.CANCELED };
      await component.refresh(res);

      expect(mockGroupsService.getInterestGroupAsync).not.toHaveBeenCalled();
      expect(component.showUploadModal()).toBe(false);
    });
  });
});
