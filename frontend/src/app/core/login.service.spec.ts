import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { TranslocoService } from '@jsverse/transloco';
import { User, UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { ALF_BASE_PATH, CBC_BASE_PATH } from 'app/core/variables';
import { vi } from 'vitest';

const mockUser: User = {
  userId: 'testuser',
  firstname: 'Test',
  lastname: 'User',
  email: 'test@example.com',
  phone: '123',
  uiLang: 'en',
  contentFilterLang: 'en',
  visibility: true,
  properties: { key: 'value' },
  avatar: 'avatar.png',
};

describe('LoginService', () => {
  let service: LoginService;
  let httpTesting: HttpTestingController;
  let mockUserService: { getUserAsync: ReturnType<typeof vi.fn> };
  let mockTransloco: { setActiveLang: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    mockUserService = { getUserAsync: vi.fn().mockResolvedValue(mockUser) };
    mockTransloco = { setActiveLang: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        LoginService,
        { provide: UserService, useValue: mockUserService },
        { provide: TranslocoService, useValue: mockTransloco },
        { provide: ALF_BASE_PATH, useValue: 'http://alf' },
        { provide: CBC_BASE_PATH, useValue: 'http://cbc' },
      ],
    });

    service = TestBed.inject(LoginService);
    httpTesting = TestBed.inject(HttpTestingController);
    sessionStorage.clear();
  });

  afterEach(() => {
    httpTesting.verify();
    sessionStorage.clear();
  });

  describe('isGuest', () => {
    it('should return true when no ticket in session', () => {
      expect(service.isGuest()).toBe(true);
    });

    it('should return false when ticket exists', () => {
      sessionStorage.setItem('ticket', 'abc123');
      expect(service.isGuest()).toBe(false);
    });
  });

  describe('getCurrentUsername', () => {
    it('should return guest when no userId', () => {
      expect(service.getCurrentUsername()).toBe('guest');
    });

    it('should return stored userId', () => {
      sessionStorage.setItem('user.userId', 'testuser');
      expect(service.getCurrentUsername()).toBe('testuser');
    });
  });

  describe('getTicket', () => {
    it('should return empty string when no ticket', () => {
      expect(service.getTicket()).toBe('');
    });

    it('should return valid ticket', () => {
      sessionStorage.setItem('ticket', 'TICKET_abc123');
      expect(service.getTicket()).toBe('TICKET_abc123');
    });

    it('should return empty for ticket with invalid characters', () => {
      sessionStorage.setItem('ticket', 'bad ticket!@#');
      expect(service.getTicket()).toBe('');
    });
  });

  describe('cleanAuthentication', () => {
    it('should remove all session items', () => {
      sessionStorage.setItem('ticket', 'abc');
      sessionStorage.setItem('user.userId', 'u');
      service.cleanAuthentication();
      expect(sessionStorage.getItem('ticket')).toBeNull();
      expect(sessionStorage.getItem('user.userId')).toBeNull();
    });
  });

  describe('getUser', () => {
    it('should return user from sessionStorage', () => {
      sessionStorage.setItem('user.userId', 'testuser');
      sessionStorage.setItem('user.email', 'test@example.com');
      sessionStorage.setItem('user.firstname', 'Test');
      sessionStorage.setItem('user.lastname', 'User');
      sessionStorage.setItem('user.phone', 'true');
      sessionStorage.setItem('user.uiLang', 'en');
      sessionStorage.setItem('user.contentFilterLang', 'en');
      sessionStorage.setItem('user.avatar', 'avatar.png');
      sessionStorage.setItem('user.properties', JSON.stringify({ k: 'v' }));

      const user = service.getUser();
      expect(user.userId).toBe('testuser');
      expect(user.email).toBe('test@example.com');
      expect(user.visibility).toBe(true);
    });
  });

  describe('loadUser', () => {
    it('should store user and return true on success', async () => {
      const result = await service.loadUser('testuser', 'TICKET_abc');
      expect(result).toBe(true);
      expect(sessionStorage.getItem('ticket')).toBe('TICKET_abc');
      expect(sessionStorage.getItem('user.userId')).toBe('testuser');
      expect(mockTransloco.setActiveLang).toHaveBeenCalledWith('en');
    });

    it('should return false on error', async () => {
      mockUserService.getUserAsync.mockRejectedValue(new Error('fail'));
      const result = await service.loadUser('bad', 'TICKET_x');
      expect(result).toBe(false);
    });
  });

  describe('validateTicket', () => {
    it('should return false when no ticket in session', async () => {
      const result = await service.validateTicket();
      expect(result).toBe(false);
    });

    it('should return true when ticket is valid', async () => {
      sessionStorage.setItem('ticket', 'TICKET_valid');
      const promise = service.validateTicket();
      const req = httpTesting.expectOne('http://alf/login/ticket/TICKET_valid');
      req.flush('OK');
      expect(await promise).toBe(true);
    });

    it('should return false when ticket validation fails', async () => {
      sessionStorage.setItem('ticket', 'TICKET_expired');
      const promise = service.validateTicket();
      const req = httpTesting.expectOne(
        'http://alf/login/ticket/TICKET_expired'
      );
      req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
      expect(await promise).toBe(false);
    });
  });

  describe('login', () => {
    it('should return true on successful login', async () => {
      const promise = service.login({
        username: 'testuser',
        password: 'pass',
      });
      const loginReq = httpTesting.expectOne('http://cbc/login');
      loginReq.flush({ data: { ticket: 'TICKET_new' } });
      expect(await promise).toBe(true);
      expect(sessionStorage.getItem('ticket')).toBe('TICKET_new');
    });

    it('should return false on failure', async () => {
      const promise = service.login({
        username: 'testuser',
        password: 'wrong',
      });
      const loginReq = httpTesting.expectOne('http://cbc/login');
      loginReq.flush('error', { status: 500, statusText: 'Server Error' });
      expect(await promise).toBe(false);
    });
  });

  describe('logout', () => {
    it('should clean auth and return true on success', async () => {
      sessionStorage.setItem('ticket', 'TICKET_valid');
      const promise = service.logout();
      const req = httpTesting.expectOne(
        'http://alf/login/ticket/TICKET_valid?format=json'
      );
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
      expect(await promise).toBe(true);
      expect(sessionStorage.getItem('ticket')).toBeNull();
    });

    it('should return false when no valid ticket', async () => {
      const result = await service.logout();
      expect(result).toBe(false);
    });

    it('should return false on HTTP error', async () => {
      sessionStorage.setItem('ticket', 'TICKET_valid');
      const promise = service.logout();
      const req = httpTesting.expectOne(
        'http://alf/login/ticket/TICKET_valid?format=json'
      );
      req.flush('error', { status: 500, statusText: 'Error' });
      expect(await promise).toBe(false);
    });
  });
});
