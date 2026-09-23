import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { MeComponent } from './me.component';

describe('MeComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [MeComponent],
    }).overrideComponent(MeComponent, {
      set: { imports: [], schemas: [NO_ERRORS_SCHEMA] },
    });
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(MeComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeDefined();
  });
});
