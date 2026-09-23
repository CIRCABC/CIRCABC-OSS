import { TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { Header, HeaderService } from 'app/core/generated/circabc';
import { HeaderReloadListenerService } from 'app/core/header-reload-listener.service';
import { LoginService } from 'app/core/login.service';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { HeadersComponent } from './headers.component';

const mockHeaders: Header[] = [
  {
    name: 'Header 1',
    id: 'h1',
    categories: [{ name: 'Cat1' }],
    description: { en: 'Desc 1' },
  },
  { name: 'Header 2', id: 'h2', description: { en: 'Desc 2' } },
];

describe('HeadersComponent', () => {
  const refreshSubject = new Subject<void>();

  const mockHeaderService = {
    getHeadersAsync: vi.fn().mockResolvedValue(mockHeaders),
    deleteHeaderAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockLoginService = {
    getUser: vi
      .fn()
      .mockReturnValue({ userId: 'admin', properties: { isAdmin: 'true' } }),
  };

  const mockI18nPipe = {
    transform: vi.fn((mltext: { [key: string]: string }) => mltext['en'] ?? ''),
  };

  const mockHeaderReloadListenerService = {
    refreshAnnounced$: refreshSubject.asObservable(),
  };

  let component: HeadersComponent;
  let fixture: ReturnType<typeof TestBed.createComponent<HeadersComponent>>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HeadersComponent],
      providers: [
        { provide: HeaderService, useValue: mockHeaderService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: I18nPipe, useValue: mockI18nPipe },
        {
          provide: HeaderReloadListenerService,
          useValue: mockHeaderReloadListenerService,
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

    fixture = TestBed.createComponent(HeadersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should load headers on init', () => {
    expect(component.headers()).toEqual(mockHeaders);
    expect(component.loading()).toBe(false);
  });

  it('should reload headers when refresh is announced', async () => {
    mockHeaderService.getHeadersAsync.mockResolvedValue([mockHeaders[0]]);
    refreshSubject.next();
    fixture.detectChanges();
    await fixture.whenStable();
    expect(mockHeaderService.getHeadersAsync).toHaveBeenCalledTimes(2);
  });

  it('should return true for hasCategories when categories exist', () => {
    expect(component.hasCategories(mockHeaders[0])).toBe(true);
  });

  it('should return false for hasCategories when categories is undefined', () => {
    expect(component.hasCategories(mockHeaders[1])).toBe(false);
  });

  it('should return false for hasCategories when categories is empty', () => {
    expect(component.hasCategories({ name: 'x', categories: [] })).toBe(false);
  });

  it('should delete header and reload', async () => {
    await component.deleteHeader(mockHeaders[0]);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(mockHeaderService.deleteHeaderAsync).toHaveBeenCalledWith({
      id: 'h1',
    });
    expect(mockHeaderService.getHeadersAsync).toHaveBeenCalled();
  });

  it('should return true for isAdmin when user is admin', () => {
    expect(component.isAdmin()).toBe(true);
  });

  it('should return false for isAdmin when user is not admin', () => {
    mockLoginService.getUser.mockReturnValue({
      userId: 'user',
      properties: { isAdmin: 'false' },
    });
    expect(component.isAdmin()).toBe(false);
  });

  it('should return description via i18nPipe', () => {
    const result = component.getDescription(mockHeaders[0]);
    expect(mockI18nPipe.transform).toHaveBeenCalledWith({ en: 'Desc 1' });
    expect(result).toBe('Desc 1');
  });

  it('should return "-" when description is undefined', () => {
    const header: Header = { name: 'No desc' };
    expect(component.getDescription(header)).toBe('-');
  });
});
