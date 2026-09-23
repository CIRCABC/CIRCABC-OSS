import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import {
  AppMessageService,
  PagedDistributionMails,
} from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { DistributionListComponent } from './distribution-list.component';
import { SelectableDistributionMail } from './selectable-paged-distribution-mails';

const mockPagedMails: PagedDistributionMails = {
  data: [
    { id: 1, emailAddress: 'a@test.com' },
    { id: 2, emailAddress: 'b@test.com' },
  ],
  total: 2,
};

const mockAppMessageService = {
  getDistributionEmailsAsync: vi.fn().mockResolvedValue(mockPagedMails),
  deleteDistributionEmails: vi.fn().mockReturnValue(of(undefined)),
  deleteDistributionEmailsAsync: vi.fn().mockResolvedValue(undefined),
  getDistributionEmailsExport: vi.fn().mockReturnValue(of(new Blob())),
};

describe('DistributionListComponent', () => {
  let component: DistributionListComponent;

  beforeEach(() => {
    vi.clearAllMocks();
    mockAppMessageService.getDistributionEmailsAsync.mockResolvedValue(
      mockPagedMails
    );
    mockAppMessageService.deleteDistributionEmails.mockReturnValue(
      of(undefined)
    );
    mockAppMessageService.deleteDistributionEmailsAsync.mockResolvedValue(
      undefined
    );

    TestBed.configureTestingModule({
      imports: [DistributionListComponent],
      providers: [
        { provide: AppMessageService, useValue: mockAppMessageService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(DistributionListComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });

    const fixture = TestBed.createComponent(DistributionListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should load distribution emails on init', () => {
    expect(
      mockAppMessageService.getDistributionEmailsAsync
    ).toHaveBeenCalledWith({
      search: '',
      limit: 25,
      page: 1,
    });
    expect(component.distributionEmails().total).toBe(2);
    expect(component.distributionEmails().data).toHaveLength(2);
  });

  it('should handle empty response', async () => {
    mockAppMessageService.getDistributionEmailsAsync.mockResolvedValue({
      data: undefined,
      total: undefined,
    } as PagedDistributionMails);
    await component.loadDistributionEmails();
    expect(component.distributionEmails().total).toBe(0);
    expect(component.distributionEmails().data).toHaveLength(0);
  });

  it('should go to page', () => {
    component.goToPage(3);
    expect(component.listingOptions().page).toBe(3);
    expect(
      mockAppMessageService.getDistributionEmailsAsync
    ).toHaveBeenCalledWith({
      search: '',
      limit: 25,
      page: 3,
    });
  });

  it('should unsubscribe email', async () => {
    await component.unsubscribeEmail({ id: 1, emailAddress: 'a@test.com' });
    expect(
      mockAppMessageService.deleteDistributionEmailsAsync
    ).toHaveBeenCalledWith({ id: 1 });
    expect(component.listingOptions().page).toBe(1);
  });

  it('should not call delete if email has no id', async () => {
    await component.unsubscribeEmail({ emailAddress: 'no-id@test.com' });
    expect(
      mockAppMessageService.deleteDistributionEmailsAsync
    ).not.toHaveBeenCalled();
  });

  it('should handle delete error gracefully', async () => {
    mockAppMessageService.deleteDistributionEmailsAsync.mockRejectedValue(
      new Error('fail')
    );
    const spy = vi.spyOn(console, 'error').mockImplementation(() => {});
    await component.unsubscribeEmail({ id: 1, emailAddress: 'a@test.com' });
    expect(spy).toHaveBeenCalled();
    spy.mockRestore();
  });

  it('should toggle selection on tickDistributionMail', () => {
    const mail: SelectableDistributionMail = {
      id: 1,
      emailAddress: 'a@test.com',
      selected: false,
    };
    component.tickDistributionMail(mail);
    expect(mail.selected).toBe(true);
    component.tickDistributionMail(mail);
    expect(mail.selected).toBe(false);
  });

  it('should count selected distributions', () => {
    component.distributionEmails.set({
      data: [
        { id: 1, emailAddress: 'a@test.com', selected: true },
        { id: 2, emailAddress: 'b@test.com', selected: false },
      ],
      total: 2,
    });
    expect(component.countSelectedDistribution()).toBe(1);
    expect(component.hasSelectedDistribution()).toBe(true);
  });

  it('should return false when no distributions selected', () => {
    component.distributionEmails.set({
      data: [{ id: 1, emailAddress: 'a@test.com', selected: false }],
      total: 1,
    });
    expect(component.hasSelectedDistribution()).toBe(false);
  });

  it('should select all and deselect all', () => {
    component.distributionEmails.set({
      data: [
        { id: 1, emailAddress: 'a@test.com', selected: false },
        { id: 2, emailAddress: 'b@test.com', selected: false },
      ],
      total: 2,
    });
    component.selectAll();
    expect(component.allSelected).toBe(true);
    expect(component.distributionEmails().data.every((d) => d.selected)).toBe(
      true
    );
    component.selectAll();
    expect(component.allSelected).toBe(false);
    expect(component.distributionEmails().data.every((d) => !d.selected)).toBe(
      true
    );
  });

  it('should unsubscribe selected emails', async () => {
    component.distributionEmails.set({
      data: [
        { id: 1, emailAddress: 'a@test.com', selected: true },
        { id: 2, emailAddress: 'b@test.com', selected: false },
      ],
      total: 2,
    });
    await component.unsubscribeSelectedEmail();
    expect(
      mockAppMessageService.deleteDistributionEmailsAsync
    ).toHaveBeenCalledWith({ id: 1 });
    expect(
      mockAppMessageService.deleteDistributionEmailsAsync
    ).toHaveBeenCalledTimes(1);
    expect(component.deletingAll()).toBe(false);
  });

  it('should not delete when no emails selected', async () => {
    component.distributionEmails.set({
      data: [{ id: 1, emailAddress: 'a@test.com', selected: false }],
      total: 1,
    });
    await component.unsubscribeSelectedEmail();
    expect(
      mockAppMessageService.deleteDistributionEmailsAsync
    ).not.toHaveBeenCalled();
  });
});
