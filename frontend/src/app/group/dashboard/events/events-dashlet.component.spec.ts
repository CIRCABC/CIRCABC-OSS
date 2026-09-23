import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { EventItemDefinition, EventsService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { EventsDashletComponent } from './events-dashlet.component';

const mockEvents: EventItemDefinition[] = [
  { id: '1', title: 'Event 1' },
  { id: '2', title: 'Event 2' },
  { id: '3', title: 'Event 3' },
  { id: '4', title: 'Event 4' },
  { id: '5', title: 'Event 5' },
  { id: '6', title: 'Event 6' },
  { id: '7', title: 'Event 7' },
  { id: '8', title: 'Event 8' },
  { id: '9', title: 'Event 9' },
  { id: '10', title: 'Event 10' },
];

const mockEventsService = {
  getInterestGroupEventsAsync: vi.fn().mockResolvedValue(mockEvents),
};

describe('EventsDashletComponent', () => {
  let component: EventsDashletComponent;
  let componentRef: ComponentRef<EventsDashletComponent>;
  let fixture: ComponentFixture<EventsDashletComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EventsDashletComponent],
      providers: [
        { provide: EventsService, useValue: mockEventsService },
        provideRouter([]),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    mockEventsService.getInterestGroupEventsAsync.mockResolvedValue(mockEvents);

    fixture = TestBed.createComponent(EventsDashletComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('igId', 'group-123');
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load events on init', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockEventsService.getInterestGroupEventsAsync).toHaveBeenCalledWith({
      id: 'group-123',
      startDate: expect.any(String),
      endDate: expect.any(String),
    });
    expect(component.events()).toEqual(mockEvents);
    expect(component.loading()).toBe(false);
    expect(component.restCallError()).toBe(false);
  });

  it('should set restCallError on service failure', async () => {
    mockEventsService.getInterestGroupEventsAsync.mockRejectedValue(
      new Error('fail')
    );

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.events()).toEqual([]);
    expect(component.restCallError()).toBe(true);
    expect(component.loading()).toBe(false);
  });

  it('should return at most 8 events when more is false', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.more = false;
    const result = component.getListOfEvents();
    expect(result).toHaveLength(8);
  });

  it('should return all events when more is true', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.more = true;
    const result = component.getListOfEvents();
    expect(result).toHaveLength(10);
  });
});
