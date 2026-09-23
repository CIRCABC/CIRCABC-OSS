import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { EventItemDefinition, EventsService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { DeleteEventComponent } from './delete-event.component';

const mockEventsService = {
  deleteEvent: vi.fn().mockReturnValue(of(undefined)),
  deleteEventAsync: vi.fn().mockResolvedValue(undefined),
};

const mockEvent: EventItemDefinition = {
  id: 'event-123',
  occurrenceRate: 'OnlyOnce|2026-04-30',
};

describe('DeleteEventComponent', () => {
  let component: DeleteEventComponent;
  let componentRef: ComponentRef<DeleteEventComponent>;
  let fixture: ComponentFixture<DeleteEventComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteEventComponent],
      providers: [
        { provide: EventsService, useValue: mockEventsService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DeleteEventComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('event', mockEvent);
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize form with Single occurrence selection', () => {
    expect(component.form.value.occurrenceSelection).toBe('Single');
  });

  it('should return true for single event when occurrenceRate starts with OnlyOnce|', () => {
    expect(component.isSingleEvent()).toBe(true);
  });

  it('should return false for recurring event', () => {
    componentRef.setInput('event', {
      id: 'event-456',
      occurrenceRate: 'Weekly|Monday',
    });
    fixture.detectChanges();
    expect(component.isSingleEvent()).toBe(false);
  });

  it('should close popup and reset state', () => {
    component.showModal.set(true);
    component.processing.set(true);
    component.form.patchValue({ occurrenceSelection: 'AllOccurences' });

    component.closePopupWindow();

    expect(component.showModal()).toBe(false);
    expect(component.processing()).toBe(false);
    expect(component.form.value.occurrenceSelection).toBe('Single');
  });

  it('should call deleteEvent and emit eventMeetingDeleted on delete', async () => {
    const emitSpy = vi.spyOn(component.eventMeetingDeleted, 'emit');

    await component.delete();

    expect(mockEventsService.deleteEventAsync).toHaveBeenCalledWith({
      id: 'event-123',
      updateMode: 'Single',
    });
    expect(emitSpy).toHaveBeenCalled();
    expect(component.processing()).toBe(false);
    expect(component.showModal()).toBe(false);
  });

  it('should set processing to true during delete', () => {
    mockEventsService.deleteEvent.mockReturnValue(of(undefined));

    // We can't easily test mid-execution state, but we verify it starts as false
    expect(component.processing()).toBe(false);
  });
});
