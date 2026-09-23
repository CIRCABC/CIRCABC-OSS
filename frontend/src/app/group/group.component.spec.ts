import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { InterestGroup } from 'app/core/generated/circabc';
import { LibraryIdService } from 'app/core/libraryId.service';
import { LoginService } from 'app/core/login.service';
import { VisitedGroupService } from 'app/core/visited-groups/visited-group.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { GroupComponent } from './group.component';

const mockGroup: InterestGroup = {
  name: 'Test Group',
  permissions: { library: 'LibAdmin' },
  id: 'group-1',
  libraryId: 'lib-1',
};

describe('GroupComponent', () => {
  let libraryIdSubject: Subject<string>;
  let mockLoginService: { isGuest: ReturnType<typeof vi.fn> };
  let mockVisitedGroupService: { visitGroup: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    libraryIdSubject = new Subject<string>();
    mockLoginService = { isGuest: vi.fn().mockReturnValue(false) };
    mockVisitedGroupService = { visitGroup: vi.fn() };

    TestBed.configureTestingModule({
      imports: [GroupComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { data: of({ group: mockGroup }) },
        },
        { provide: LoginService, useValue: mockLoginService },
        { provide: VisitedGroupService, useValue: mockVisitedGroupService },
        {
          provide: LibraryIdService,
          useValue: { libraryIdSubject$: libraryIdSubject.asObservable() },
        },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(GroupComponent, {
      set: { template: '' },
    });
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(GroupComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should set group from route data and visit it on init', () => {
    const fixture = TestBed.createComponent(GroupComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.group()).toEqual(mockGroup);
    expect(mockVisitedGroupService.visitGroup).toHaveBeenCalledWith(mockGroup);
  });

  it('should update group libraryId when libraryIdSubject emits', () => {
    const fixture = TestBed.createComponent(GroupComponent);
    fixture.detectChanges();
    libraryIdSubject.next('new-lib-id');
    expect(fixture.componentInstance.group()?.libraryId).toBe('new-lib-id');
  });

  it('should return true from isAccessingAsVisitor when user is guest', () => {
    const fixture = TestBed.createComponent(GroupComponent);
    fixture.detectChanges();
    mockLoginService.isGuest.mockReturnValue(true);
    expect(fixture.componentInstance.isAccessingAsVisitor()).toBe(true);
  });

  it('should return false from isAccessingAsVisitor when user is not guest', () => {
    const fixture = TestBed.createComponent(GroupComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.isAccessingAsVisitor()).toBe(false);
  });

  it('should unsubscribe from libraryId on destroy', () => {
    const fixture = TestBed.createComponent(GroupComponent);
    fixture.detectChanges();
    fixture.destroy();
    // After destroy, emitting should not update the group
    libraryIdSubject.next('after-destroy');
    expect(fixture.componentInstance.group()?.libraryId).not.toBe(
      'after-destroy'
    );
  });
});
