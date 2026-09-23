import { TestBed } from '@angular/core/testing';
import { environment } from 'environments/environment';
import { EnvironmentRibbonComponent } from './environment-ribbon.component';

describe('EnvironmentRibbonComponent', () => {
  it('should create and expose environmentType from environment config', () => {
    TestBed.configureTestingModule({ imports: [EnvironmentRibbonComponent] });
    const fixture = TestBed.createComponent(EnvironmentRibbonComponent);
    fixture.detectChanges();

    expect(fixture.componentInstance.environmentType).toBe(
      environment.environmentType
    );
  });

  it('should render ribbon when environmentType is not prod', () => {
    TestBed.configureTestingModule({ imports: [EnvironmentRibbonComponent] });
    const fixture = TestBed.createComponent(EnvironmentRibbonComponent);
    fixture.detectChanges();

    if (environment.environmentType === 'prod') {
      const ribbon = fixture.nativeElement.querySelector('.ribbon');
      expect(ribbon).toBeNull();
    } else {
      const ribbon = fixture.nativeElement.querySelector(
        '.ribbon'
      ) as HTMLElement;
      expect(ribbon).toBeDefined();
      expect(ribbon.textContent?.trim()).toBe(environment.environmentType);
    }
  });

  it('should hide ribbon when environmentType is prod', () => {
    TestBed.configureTestingModule({ imports: [EnvironmentRibbonComponent] });
    const fixture = TestBed.createComponent(EnvironmentRibbonComponent);
    fixture.componentInstance.environmentType = 'prod';
    fixture.detectChanges();

    const ribbon = fixture.nativeElement.querySelector('.ribbon');
    expect(ribbon).toBeNull();
  });

  it('should apply correct CSS class for each environment type', () => {
    const classMap: Record<string, string> = {
      stress: 'ribbon-red',
      local: 'ribbon-violet',
      dev: 'ribbon-orange',
      test: 'ribbon-blue',
      acc: 'ribbon-green',
    };

    for (const [envType, cssClass] of Object.entries(classMap)) {
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({ imports: [EnvironmentRibbonComponent] });
      const fixture = TestBed.createComponent(EnvironmentRibbonComponent);
      fixture.componentInstance.environmentType =
        envType as typeof environment.environmentType;
      fixture.detectChanges();

      const ribbon = fixture.nativeElement.querySelector(
        '.ribbon'
      ) as HTMLElement;
      expect(ribbon.classList.contains(cssClass)).toBe(true);
    }
  });
});
