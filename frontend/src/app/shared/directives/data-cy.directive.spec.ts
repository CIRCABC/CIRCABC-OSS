import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';

import { DataCyDirective } from './data-cy.directive';

vi.mock('environments/environment', () => ({
  environment: {
    environmentType: 'local',
  },
}));

import { environment } from 'environments/environment';

@Component({
  template: `<div data-cy="test-element"></div>`,
  imports: [DataCyDirective],
})
class HostComponent {}

describe('DataCyDirective', () => {
  afterEach(() => {
    (environment as { environmentType: string }).environmentType = 'local';
    TestBed.resetTestingModule();
  });

  it('should keep data-cy attribute in non-prod environment', () => {
    const fixture = TestBed.configureTestingModule({
      imports: [HostComponent],
    }).createComponent(HostComponent);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement.querySelector('div');
    expect(el.getAttribute('data-cy')).toBe('test-element');
  });

  it('should remove data-cy attribute in prod environment', () => {
    (environment as { environmentType: string }).environmentType = 'prod';

    const fixture = TestBed.configureTestingModule({
      imports: [HostComponent],
    }).createComponent(HostComponent);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement.querySelector('div');
    expect(el.hasAttribute('data-cy')).toBe(false);
  });
});
