import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  InterestGroup,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { AdminSecurityComponent } from './admin-security.component';

function createMockIg(overrides: Partial<InterestGroup> = {}): InterestGroup {
  return {
    id: 'ig-1',
    name: 'Test IG',
    permissions: {},
    isPublic: false,
    isRegistered: false,
    allowApply: false,
    ...overrides,
  };
}

describe('AdminSecurityComponent', () => {
  let component: AdminSecurityComponent;
  let paramsSubject: Subject<{ [key: string]: string }>;
  let mockInterestGroupService: {
    getInterestGroupAsync: ReturnType<typeof vi.fn>;
    putInterestGroupAsync: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    paramsSubject = new Subject();
    mockInterestGroupService = {
      getInterestGroupAsync: vi.fn().mockResolvedValue(createMockIg()),
      putInterestGroupAsync: vi.fn().mockResolvedValue(undefined),
    };

    TestBed.configureTestingModule({
      imports: [AdminSecurityComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        { provide: InterestGroupService, useValue: mockInterestGroupService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    });

    const fixture = TestBed.createComponent(AdminSecurityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load interest group on route params change', async () => {
    const ig = createMockIg({
      isPublic: true,
      isRegistered: true,
      allowApply: true,
    });
    mockInterestGroupService.getInterestGroupAsync.mockResolvedValue(ig);

    paramsSubject.next({ id: 'ig-1' });
    await vi.waitFor(() => {
      expect(
        mockInterestGroupService.getInterestGroupAsync
      ).toHaveBeenCalledWith({
        id: 'ig-1',
      });
    });

    expect(component.ig()).toEqual(ig);
    expect(component.securityForm.controls['visibility'].value).toBe(2);
    expect(component.securityForm.controls['applicants'].value).toBe(true);
  });

  it('should set visibility to 1 for registered-only group', async () => {
    const ig = createMockIg({ isPublic: false, isRegistered: true });
    mockInterestGroupService.getInterestGroupAsync.mockResolvedValue(ig);

    paramsSubject.next({ id: 'ig-1' });
    await vi.waitFor(() => {
      expect(component.securityForm.controls['visibility'].value).toBe(1);
    });
  });

  it('should set visibility to 0 for private group', async () => {
    const ig = createMockIg({ isPublic: false, isRegistered: false });
    mockInterestGroupService.getInterestGroupAsync.mockResolvedValue(ig);

    paramsSubject.next({ id: 'ig-1' });
    await vi.waitFor(() => {
      expect(component.securityForm.controls['visibility'].value).toBe(0);
    });
  });

  it('should save with public visibility when visibility is 2', async () => {
    const ig = createMockIg({ id: 'ig-1' });
    mockInterestGroupService.getInterestGroupAsync.mockResolvedValue(ig);

    paramsSubject.next({ id: 'ig-1' });
    await vi.waitFor(() => {
      expect(component.ig()).toBeDefined();
    });

    component.securityForm.controls['visibility'].setValue(2);
    component.securityForm.controls['applicants'].setValue(true);

    await component.save();

    expect(mockInterestGroupService.putInterestGroupAsync).toHaveBeenCalledWith(
      {
        id: 'ig-1',
        interestGroup: expect.objectContaining({
          isPublic: true,
          isRegistered: true,
          allowApply: true,
        }),
      }
    );
  });

  it('should save with registered visibility when visibility is 1', async () => {
    const ig = createMockIg({ id: 'ig-1' });
    mockInterestGroupService.getInterestGroupAsync.mockResolvedValue(ig);

    paramsSubject.next({ id: 'ig-1' });
    await vi.waitFor(() => {
      expect(component.ig()).toBeDefined();
    });

    component.securityForm.controls['visibility'].setValue(1);
    await component.save();

    expect(mockInterestGroupService.putInterestGroupAsync).toHaveBeenCalledWith(
      {
        id: 'ig-1',
        interestGroup: expect.objectContaining({
          isPublic: false,
          isRegistered: true,
        }),
      }
    );
  });

  it('should save with private visibility when visibility is 0', async () => {
    const ig = createMockIg({ id: 'ig-1' });
    mockInterestGroupService.getInterestGroupAsync.mockResolvedValue(ig);

    paramsSubject.next({ id: 'ig-1' });
    await vi.waitFor(() => {
      expect(component.ig()).toBeDefined();
    });

    component.securityForm.controls['visibility'].setValue(0);
    await component.save();

    expect(mockInterestGroupService.putInterestGroupAsync).toHaveBeenCalledWith(
      {
        id: 'ig-1',
        interestGroup: expect.objectContaining({
          isPublic: false,
          isRegistered: false,
        }),
      }
    );
  });

  it('should reset form on cancel', async () => {
    const ig = createMockIg({ id: 'ig-1', isPublic: true, isRegistered: true });
    mockInterestGroupService.getInterestGroupAsync.mockResolvedValue(ig);

    paramsSubject.next({ id: 'ig-1' });
    await vi.waitFor(() => {
      expect(component.ig()).toBeDefined();
    });

    component.securityForm.controls['visibility'].setValue(0);
    await component.cancel();

    expect(component.securityForm.controls['visibility'].value).toBe(2);
    expect(component.saving()).toBe(false);
  });

  describe('helper methods', () => {
    it('isGuestVisible returns true when visibility is 2', () => {
      component.securityForm.controls['visibility'].setValue(2);
      expect(component.isGuestVisible()).toBe(true);
    });

    it('isRegisteredVisible returns true when visibility is 1', () => {
      component.securityForm.controls['visibility'].setValue(1);
      expect(component.isRegisteredVisible()).toBe(true);
    });

    it('isPrivateVisible returns true when visibility is 0', () => {
      component.securityForm.controls['visibility'].setValue(0);
      expect(component.isPrivateVisible()).toBe(true);
    });

    it('isApplicationAllowed returns true when applicants is true', () => {
      component.securityForm.controls['applicants'].setValue(true);
      expect(component.isApplicationAllowed()).toBe(true);
    });

    it('getVisibilityLabel returns correct labels', () => {
      component.securityForm.controls['visibility'].setValue(2);
      expect(component.getVisibilityLabel()).toBe('label.public');

      component.securityForm.controls['visibility'].setValue(1);
      expect(component.getVisibilityLabel()).toBe('label.users');

      component.securityForm.controls['visibility'].setValue(0);
      expect(component.getVisibilityLabel()).toBe('label.private');
    });

    it('getApplicantsYesNoLabel returns correct labels', () => {
      component.securityForm.controls['applicants'].setValue(true);
      expect(component.getApplicantsYesNoLabel()).toBe('label.yes');

      component.securityForm.controls['applicants'].setValue(false);
      expect(component.getApplicantsYesNoLabel()).toBe('label.no');
    });
  });
});
