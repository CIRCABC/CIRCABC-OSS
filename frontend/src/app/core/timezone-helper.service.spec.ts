import { TestBed } from '@angular/core/testing';
import { EventItemDefinition } from 'app/core/generated/circabc';
import { TimeZoneHelperService } from './timezone-helper.service';

describe('TimeZoneHelperService', () => {
  let service: TimeZoneHelperService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TimeZoneHelperService);
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  it('should convert event times from UTC to local time', () => {
    const events: EventItemDefinition[] = [
      {
        appointmentDate: '2026-06-15',
        startTime: '10:00',
        endTime: '11:00',
        timeZone: 'GMT',
      },
    ];

    const result = service.toLocalDateTime(events);

    expect(result).toHaveLength(1);
    expect(result[0].appointmentDate).toBeDefined();
    expect(result[0].startTime).toBeDefined();
    expect(result[0].endTime).toBeDefined();
  });

  it('should handle positive timezone offset', () => {
    const events: EventItemDefinition[] = [
      {
        appointmentDate: '2026-01-15',
        startTime: '10:00',
        endTime: '11:00',
        timeZone: 'GMT+2',
      },
    ];

    const result = service.toLocalDateTime(events);

    expect(result).toHaveLength(1);
    expect(result[0].startTime).toMatch(/^\d{2}:\d{2}$/);
    expect(result[0].endTime).toMatch(/^\d{2}:\d{2}$/);
    expect(result[0].appointmentDate).toMatch(/^\d{4}-\d{2}-\d{2}$/);
  });

  it('should handle negative timezone offset', () => {
    const events: EventItemDefinition[] = [
      {
        appointmentDate: '2026-01-15',
        startTime: '10:00',
        endTime: '11:00',
        timeZone: 'GMT-5',
      },
    ];

    const result = service.toLocalDateTime(events);

    expect(result).toHaveLength(1);
    expect(result[0].startTime).toMatch(/^\d{2}:\d{2}$/);
    expect(result[0].endTime).toMatch(/^\d{2}:\d{2}$/);
  });

  it('should handle multiple events', () => {
    const events: EventItemDefinition[] = [
      {
        appointmentDate: '2026-03-10',
        startTime: '09:00',
        endTime: '10:00',
        timeZone: 'GMT+1',
      },
      {
        appointmentDate: '2026-03-11',
        startTime: '14:00',
        endTime: '15:30',
        timeZone: 'GMT-3',
      },
    ];

    const result = service.toLocalDateTime(events);

    expect(result).toHaveLength(2);
    expect(result[0].appointmentDate).toMatch(/^\d{4}-\d{2}-\d{2}$/);
    expect(result[1].appointmentDate).toMatch(/^\d{4}-\d{2}-\d{2}$/);
  });

  it('should return an empty array when given no events', () => {
    const result = service.toLocalDateTime([]);
    expect(result).toEqual([]);
  });
});
