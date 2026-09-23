import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { PagerConfigurationComponent } from './pager-configuration.component';

describe('PagerConfigurationComponent', () => {
  let component: PagerConfigurationComponent;
  let fixture: ComponentFixture<PagerConfigurationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PagerConfigurationComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PagerConfigurationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should emit pageSizeChanged when changePageSize is called', () => {
    const emitSpy = vi.spyOn(component.pageSizeChanged, 'emit');
    component.changePageSize(25);
    expect(emitSpy).toHaveBeenCalledWith(25);
  });

  it('should emit 0 for show all', () => {
    const emitSpy = vi.spyOn(component.pageSizeChanged, 'emit');
    component.changePageSize(0);
    expect(emitSpy).toHaveBeenCalledWith(0);
  });
});
