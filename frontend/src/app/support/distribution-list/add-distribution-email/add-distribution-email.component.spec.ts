import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { AppMessageService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AddDistributionEmailComponent } from './add-distribution-email.component';

describe('AddDistributionEmailComponent', () => {
  let component: AddDistributionEmailComponent;
  let fixture: ComponentFixture<AddDistributionEmailComponent>;
  const mockAppMessageService = {
    addDistributionEmails: vi.fn().mockReturnValue(of(undefined)),
    addDistributionEmailsAsync: vi.fn().mockResolvedValue(undefined),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddDistributionEmailComponent, ReactiveFormsModule],
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
    }).compileComponents();

    fixture = TestBed.createComponent(AddDistributionEmailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the email form on init', () => {
    expect(component.emailForm).toBeDefined();
    expect(component.emailAddressControl).toBeDefined();
  });

  it('should reset state on cancel', () => {
    component.emailCreated.set([{ emailAddress: 'test@test.com' }]);
    component.cancel();
    expect(component.showModal()).toBe(false);
    expect(component.emailCreated()).toEqual([]);
  });

  it.each([
    ['empty', ''],
    ['invalid', 'invalid'],
  ])('should not call service when email is %s', async (_, email) => {
    component.emailForm.controls['emailAddress'].setValue(email);
    await component.addDistributionEmail();
    expect(
      mockAppMessageService.addDistributionEmailsAsync
    ).not.toHaveBeenCalled();
  });

  it('should call service and add to emailCreated on valid email', async () => {
    component.emailForm.controls['emailAddress'].setValue('user@example.com');
    await component.addDistributionEmail();
    expect(
      mockAppMessageService.addDistributionEmailsAsync
    ).toHaveBeenCalledWith({
      distributionMail: [{ emailAddress: 'user@example.com' }],
    });
    expect(component.emailCreated()).toHaveLength(1);
    expect(component.emailCreated()[0].emailAddress).toBe('user@example.com');
    expect(component.processing()).toBe(false);
  });

  it('should reset form control after successful add', async () => {
    component.emailForm.controls['emailAddress'].setValue('user@example.com');
    await component.addDistributionEmail();
    expect(component.emailForm.controls['emailAddress'].value).toBeNull();
  });

  it('should handle error and set processing to false', async () => {
    mockAppMessageService.addDistributionEmailsAsync.mockRejectedValue(
      new Error('fail')
    );
    component.emailForm.controls['emailAddress'].setValue('user@example.com');
    await component.addDistributionEmail();
    expect(component.processing()).toBe(false);
    expect(component.emailCreated()).toHaveLength(0);
  });

  it('should not call service when email is "null" string', async () => {
    component.emailForm.controls['emailAddress'].setValue('null');
    await component.addDistributionEmail();
    expect(
      mockAppMessageService.addDistributionEmailsAsync
    ).not.toHaveBeenCalled();
  });
});
