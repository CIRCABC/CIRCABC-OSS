import { TestBed } from '@angular/core/testing';
import { LoginService } from 'app/core/login.service';
import { vi } from 'vitest';
import { TicketValidatorComponent } from './ticket-validator.component';

describe('TicketValidatorComponent', () => {
  const mockLoginService = {
    validateTicket: vi.fn().mockResolvedValue(true),
  };

  beforeEach(() => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({
      imports: [TicketValidatorComponent],
      providers: [{ provide: LoginService, useValue: mockLoginService }],
    });
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.restoreAllMocks();
    mockLoginService.validateTicket.mockReset().mockResolvedValue(true);
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(TicketValidatorComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should call validateTicket after 10 minutes', async () => {
    const fixture = TestBed.createComponent(TicketValidatorComponent);
    fixture.detectChanges();

    expect(mockLoginService.validateTicket).not.toHaveBeenCalled();

    vi.advanceTimersByTime(10 * 60 * 1000);
    await Promise.resolve();

    expect(mockLoginService.validateTicket).toHaveBeenCalled();
  });

  it('should log "Ticket is valid" when validateTicket returns true', async () => {
    const consoleSpy = vi.spyOn(console, 'log');
    const fixture = TestBed.createComponent(TicketValidatorComponent);
    fixture.detectChanges();

    await vi.advanceTimersByTimeAsync(10 * 60 * 1000);

    expect(consoleSpy).toHaveBeenCalledWith('Ticket is valid');
  });

  it('should log "Ticket is invalid" when validateTicket returns false', async () => {
    mockLoginService.validateTicket.mockResolvedValue(false);
    const consoleSpy = vi.spyOn(console, 'log');
    const fixture = TestBed.createComponent(TicketValidatorComponent);
    fixture.detectChanges();

    await vi.advanceTimersByTimeAsync(10 * 60 * 1000);

    expect(consoleSpy).toHaveBeenCalledWith('Ticket is invalid');
  });
});
