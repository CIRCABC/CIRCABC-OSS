import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  InterestGroup,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { GroupReloadListenerService } from 'app/core/group-reload-listener.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { AdminGeneralComponent } from './admin-general.component';

const mockIg: InterestGroup = {
  id: '123',
  name: 'Test Group',
  title: { en: 'Test Title' },
  description: { en: 'Test Description' },
  contact: { en: 'test@example.com' },
  permissions: { library: 'Admin' },
};

describe('AdminGeneralComponent', () => {
  let component: AdminGeneralComponent;
  let fixture: ComponentFixture<AdminGeneralComponent>;
  let paramsSubject: Subject<{ [key: string]: string }>;

  const mockGroupsService = {
    getInterestGroupAsync: vi.fn().mockResolvedValue(mockIg),
    putInterestGroupAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockGroupReloadListenerService = {
    propagateGroupRefresh: vi.fn(),
  };

  beforeEach(async () => {
    paramsSubject = new Subject();

    await TestBed.configureTestingModule({
      imports: [AdminGeneralComponent],
      providers: [
        { provide: InterestGroupService, useValue: mockGroupsService },
        {
          provide: GroupReloadListenerService,
          useValue: mockGroupReloadListenerService,
        },
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminGeneralComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize the form with empty values', () => {
    expect(component.igForm).toBeDefined();
    expect(component.igForm.get('name')).toBeDefined();
    expect(component.igForm.get('title')).toBeDefined();
    expect(component.igForm.get('description')).toBeDefined();
    expect(component.igForm.get('contact')).toBeDefined();
  });

  it('should load interest group when route params emit', async () => {
    mockGroupsService.getInterestGroupAsync.mockResolvedValue(mockIg);
    paramsSubject.next({ id: '123' });
    await fixture.whenStable();

    expect(mockGroupsService.getInterestGroupAsync).toHaveBeenCalledWith({
      id: '123',
    });
    expect(component.ig).toEqual(mockIg);
    expect(component.igForm.value.name).toBe('Test Group');
  });

  it('should not load if params have no id', async () => {
    paramsSubject.next({});
    await fixture.whenStable();

    expect(mockGroupsService.getInterestGroupAsync).not.toHaveBeenCalled();
  });

  it('should reload on cancel', async () => {
    component.ig = { ...mockIg };
    mockGroupsService.getInterestGroupAsync.mockResolvedValue(mockIg);

    await component.cancel();

    expect(mockGroupsService.getInterestGroupAsync).toHaveBeenCalledWith({
      id: '123',
    });
  });

  it('should save and reload when form is valid', async () => {
    component.ig = { ...mockIg };
    component.igForm.patchValue({ id: '123', name: 'Updated' });
    mockGroupsService.putInterestGroupAsync.mockResolvedValue(undefined);
    mockGroupsService.getInterestGroupAsync.mockResolvedValue(mockIg);

    await component.save();

    expect(mockGroupsService.putInterestGroupAsync).toHaveBeenCalledWith({
      id: '123',
      interestGroup: expect.objectContaining({ id: '123', name: 'Updated' }),
    });
    expect(
      mockGroupReloadListenerService.propagateGroupRefresh
    ).toHaveBeenCalledWith('123');
    expect(component.saving()).toBe(false);
  });

  it('should not save when form is invalid', async () => {
    component.ig = { ...mockIg };
    component.igForm.patchValue({ name: '' });

    await component.save();

    expect(mockGroupsService.putInterestGroupAsync).not.toHaveBeenCalled();
  });

  it('should handle save error gracefully', async () => {
    component.ig = { ...mockIg };
    component.igForm.patchValue({ id: '123', name: 'Valid' });
    mockGroupsService.putInterestGroupAsync.mockRejectedValue(
      new Error('fail')
    );

    await component.save();

    expect(component.saving()).toBe(false);
  });

  it('should expose nameControl', () => {
    expect(component.nameControl).toBe(component.igForm.controls['name']);
  });
});
