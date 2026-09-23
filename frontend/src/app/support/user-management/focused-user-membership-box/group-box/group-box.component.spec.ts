import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { MembersService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { GroupBoxComponent } from './group-box.component';

describe('GroupBoxComponent', () => {
  let component: GroupBoxComponent;
  let fixture: ComponentFixture<GroupBoxComponent>;
  const mockMembersService = {
    deleteMember: vi.fn(),
    deleteMemberAsync: vi.fn(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GroupBoxComponent],
      providers: [
        provideRouter([]),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: MembersService, useValue: mockMembersService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(GroupBoxComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('membership', {
      interestGroup: { id: 'group1' },
      profile: {},
    });
    fixture.componentRef.setInput('userId', 'user1');
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.resetAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call deleteMember and emit userUninvited on success', async () => {
    mockMembersService.deleteMemberAsync.mockResolvedValue(undefined);
    const emitSpy = vi.spyOn(component.userUninvited, 'emit');

    await component.uninviteUSer();

    expect(mockMembersService.deleteMemberAsync).toHaveBeenCalledWith({
      id: 'group1',
      userId: 'user1',
    });
    expect(emitSpy).toHaveBeenCalled();
    expect(component.uninviting).toBe(false);
  });

  it('should set uninviting to false and log error on failure', async () => {
    const error = new Error('fail');
    mockMembersService.deleteMemberAsync.mockRejectedValue(error);
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    await component.uninviteUSer();

    expect(consoleSpy).toHaveBeenCalledWith(error);
    expect(component.uninviting).toBe(false);
  });

  it('should not call deleteMember if groupId is missing', async () => {
    fixture.componentRef.setInput('membership', {
      interestGroup: {},
      profile: {},
    });
    fixture.detectChanges();

    await component.uninviteUSer();

    expect(mockMembersService.deleteMemberAsync).not.toHaveBeenCalled();
  });
});
