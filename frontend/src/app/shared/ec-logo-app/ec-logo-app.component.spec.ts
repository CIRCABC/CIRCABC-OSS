import { TestBed } from '@angular/core/testing';
import { TranslocoService } from '@jsverse/transloco';
import { vi } from 'vitest';
import { environment } from '../../../environments/environment';
import { EcLogoAppComponent } from './ec-logo-app.component';

describe('EcLogoAppComponent', () => {
  let component: EcLogoAppComponent;
  const mockTranslocoService = { getActiveLang: vi.fn().mockReturnValue('EN') };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [EcLogoAppComponent],
      providers: [
        { provide: TranslocoService, useValue: mockTranslocoService },
      ],
    });
    component = TestBed.createComponent(EcLogoAppComponent).componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should have circabcRelease from environment', () => {
    expect(component.circabcRelease).toBe(environment.circabcRelease);
  });

  it('should return active language in lowercase', () => {
    mockTranslocoService.getActiveLang.mockReturnValue('FR');
    expect(component.getLang()).toBe('fr');
  });
});
