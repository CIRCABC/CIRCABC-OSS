import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { EventItemDefinition, UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { UserEventsComponent } from './user-events.component';

const mockEvents: EventItemDefinition[] = [
  { id: '1', title: 'Test Event', appointmentType: 'Event' },
];

const mockUserService = {
  getUserEventsPeriodAsync: vi.fn().mockResolvedValue(mockEvents),
};

const mockLoginService = {
  getCurrentUsername: vi.fn().mockReturnValue('testuser'),
};

const mockUiMessageService = {
  addErrorMessage: vi.fn(),
};

describe('UserEventsComponent', () => {
  let component: UserEventsComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserEventsComponent],
      providers: [
        { provide: UserService, useValue: mockUserService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: UiMessageService, useValue: mockUiMessageService },
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

    vi.clearAllMocks();
    mockUserService.getUserEventsPeriodAsync.mockResolvedValue(mockEvents);
    mockLoginService.getCurrentUsername.mockReturnValue('testuser');
  });

  function createComponent(): {
    component: UserEventsComponent;
    fixture: ComponentFixture<UserEventsComponent>;
  } {
    const fixture = TestBed.createComponent(UserEventsComponent);
    fixture.detectChanges();
    return { component: fixture.componentInstance, fixture };
  }

  it('should fetch events on init', async () => {
    const { component: comp, fixture } = createComponent();
    component = comp;
    await fixture.whenStable();

    expect(mockUserService.getUserEventsPeriodAsync).toHaveBeenCalledWith({
      userId: 'testuser',
      exactDate: expect.any(String),
      period: 'Future',
    });
    expect(component.events()).toEqual(mockEvents);
  });

  it('should display error message on failure', async () => {
    const errorBody = JSON.stringify({ message: 'Something went wrong' });
    mockUserService.getUserEventsPeriodAsync.mockRejectedValue({
      _body: errorBody,
    });

    const { component: comp, fixture } = createComponent();
    component = comp;
    await fixture.whenStable();

    expect(mockUiMessageService.addErrorMessage).toHaveBeenCalledWith(
      'Something went wrong'
    );
  });
});
