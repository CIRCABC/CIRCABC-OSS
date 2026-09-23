import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import { InterestGroup, MembersService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { MembersDashletComponent } from './members-dashlet.component';

const mockGroup: InterestGroup = {
  id: 'group-1',
  name: 'Test Group',
  permissions: { directory: 'DirAdmin' },
};

const mockMembersService = {
  getMemberCountAsync: vi.fn(),
  getApplicantAsync: vi.fn(),
};

const mockPermEvalService = {
  isDirAdmin: vi.fn(),
};

describe('MembersDashletComponent', () => {
  let component: MembersDashletComponent;
  let fixture: ComponentFixture<MembersDashletComponent>;
  let componentRef: ComponentRef<MembersDashletComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MembersDashletComponent],
      providers: [
        { provide: MembersService, useValue: mockMembersService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MembersDashletComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('group', mockGroup);
  });

  afterEach(() => {
    vi.resetAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load member count on init', async () => {
    mockMembersService.getMemberCountAsync.mockResolvedValue({ count: 42 });
    mockPermEvalService.isDirAdmin.mockReturnValue(false);

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.nbMembers()).toBe(42);
    expect(component.loading()).toBe(false);
    expect(component.restCallError()).toBe(false);
  });

  it('should load applicants when user is DirAdmin', async () => {
    mockMembersService.getMemberCountAsync.mockResolvedValue({ count: 10 });
    mockMembersService.getApplicantAsync.mockResolvedValue([
      { userId: 'u1' },
      { userId: 'u2' },
    ]);
    mockPermEvalService.isDirAdmin.mockReturnValue(true);

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.nbApplicants()).toBe(2);
  });

  it('should not load applicants when user is not DirAdmin', async () => {
    mockMembersService.getMemberCountAsync.mockResolvedValue({ count: 5 });
    mockPermEvalService.isDirAdmin.mockReturnValue(false);

    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockMembersService.getApplicantAsync).not.toHaveBeenCalled();
    expect(component.nbApplicants()).toBe(0);
  });

  it('should set restCallError on failure', async () => {
    mockMembersService.getMemberCountAsync.mockRejectedValue(new Error('fail'));
    mockPermEvalService.isDirAdmin.mockReturnValue(false);

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.restCallError()).toBe(true);
    expect(component.loading()).toBe(false);
  });

  it('should not call API when group has no id', async () => {
    componentRef.setInput('group', { name: 'No ID', permissions: {} });
    mockPermEvalService.isDirAdmin.mockReturnValue(false);

    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockMembersService.getMemberCountAsync).not.toHaveBeenCalled();
    expect(component.loading()).toBe(false);
  });
});
