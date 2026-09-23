import { Component, viewChild } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { AuditService, PagedLogSearchResult } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { HistoryComponent } from './history.component';

const mockAuditService = {
  getHistoryAsync: vi.fn(),
};

@Component({
  template: `<cbc-history
    [(showModal)]="show"
    [itemId]="itemId"
    [historyText]="text"
    (modalHide)="hidden = true"
  />`,
  imports: [HistoryComponent],
})
class TestHostComponent {
  show = false;
  itemId = '123';
  text = 'History';
  hidden = false;
  child = viewChild.required(HistoryComponent);
}

describe('HistoryComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;

  beforeEach(async () => {
    mockAuditService.getHistoryAsync.mockReset();

    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
      providers: [
        { provide: AuditService, useValue: mockAuditService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(host.child()).toBeDefined();
  });

  it('should load history when showModal is true', async () => {
    const response: PagedLogSearchResult = {
      data: [
        {
          logDate: '2026-01-01',
          activityDescription: 'Upload',
          userId: 'user1',
          information: 'info',
          path: '/path',
          success: true,
        },
      ],
      total: 1,
    };
    mockAuditService.getHistoryAsync.mockResolvedValue(response);

    host.show = true;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockAuditService.getHistoryAsync).toHaveBeenCalledWith({
      id: '123',
      limit: 0,
      page: 1,
    });
    expect(host.child().historyEntries()).toEqual(response.data);
  });

  it('should not load history when showModal is false', async () => {
    host.show = false;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockAuditService.getHistoryAsync).not.toHaveBeenCalled();
    expect(host.child().historyEntries()).toEqual([]);
  });

  it('should set historyEntries to empty array when data is undefined', async () => {
    const response = {
      data: undefined,
      total: 0,
    } as unknown as PagedLogSearchResult;
    mockAuditService.getHistoryAsync.mockResolvedValue(response);

    host.show = true;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(host.child().historyEntries()).toEqual([]);
  });

  it('should close modal and emit modalHide', () => {
    mockAuditService.getHistoryAsync.mockResolvedValue({ data: [], total: 0 });
    host.show = true;
    fixture.detectChanges();

    host.child().close();
    fixture.detectChanges();

    expect(host.show).toBe(false);
    expect(host.hidden).toBe(true);
  });
});
