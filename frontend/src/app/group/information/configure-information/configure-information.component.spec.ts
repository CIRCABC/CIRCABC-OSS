import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult } from 'app/action-result';
import { InformationService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ConfigureInformationComponent } from './configure-information.component';

const mockInfoPage = {
  url: 'http://example.com',
  adapt: false,
  displayOldInformation: true,
};

const mockInformationService = {
  getInformationDefinitionsAsync: vi.fn().mockResolvedValue(mockInfoPage),
  putInformationDefinitions: vi.fn().mockReturnValue(of(mockInfoPage)),
  putInformationDefinitionsAsync: vi.fn().mockResolvedValue(mockInfoPage),
};

describe('ConfigureInformationComponent', () => {
  let component: ConfigureInformationComponent;
  let fixture: ComponentFixture<ConfigureInformationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConfigureInformationComponent, ReactiveFormsModule],
      providers: [
        { provide: InformationService, useValue: mockInformationService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ConfigureInformationComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('groupId', 'group1');
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load information definitions on init and patch form', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(
      mockInformationService.getInformationDefinitionsAsync
    ).toHaveBeenCalledWith({ id: 'group1' });
    expect(component.configurationForm.value.displayOldInformation).toBe(true);
    expect(component.infPage()).toEqual(mockInfoPage);
  });

  it('should emit CANCELED result and reset form on cancel', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    const emitSpy = vi.spyOn(component.modalHide, 'emit');

    component.configurationForm.patchValue({ displayOldInformation: false });
    component.cancel();

    expect(emitSpy).toHaveBeenCalledWith({
      result: ActionResult.CANCELED,
      type: 'information.configuration.update',
    });
    expect(component.configurationForm.value.displayOldInformation).toBe(true);
  });

  it('should save configuration and emit SUCCEED', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    const emitSpy = vi.spyOn(component.modalHide, 'emit');

    component.configurationForm.patchValue({ displayOldInformation: false });
    await component.save();

    expect(
      mockInformationService.putInformationDefinitionsAsync
    ).toHaveBeenCalledWith({
      id: 'group1',
      informationPage: expect.objectContaining({
        displayOldInformation: false,
      }),
    });
    expect(emitSpy).toHaveBeenCalledWith({
      result: ActionResult.SUCCEED,
      type: 'information.configuration.update',
    });
  });

  it('should emit FAILED result when save throws', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    mockInformationService.putInformationDefinitionsAsync.mockRejectedValueOnce(
      new Error('fail')
    );
    const emitSpy = vi.spyOn(component.modalHide, 'emit');

    await component.save();

    expect(emitSpy).toHaveBeenCalledWith({
      result: ActionResult.FAILED,
      type: 'information.configuration.update',
    });
  });

  it('getDisplayValue should return form value when form exists', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    component.configurationForm.patchValue({ displayOldInformation: true });

    expect(component.getDisplayValue()).toBe(true);
  });

  it('getDisplayValue should return false when form is not initialized', () => {
    // Before change detection runs, configurationForm still holds its default value
    expect(component.getDisplayValue()).toBe(false);
  });
});
