import { TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { IgRequestsComponent } from './ig-requests.component';

describe('IgRequestsComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [IgRequestsComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    });

    TestBed.overrideComponent(IgRequestsComponent, {
      set: { template: '' },
    });
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(IgRequestsComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeDefined();
  });
});
