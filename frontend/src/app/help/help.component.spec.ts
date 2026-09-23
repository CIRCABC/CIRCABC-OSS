import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { HelpComponent } from './help.component';

describe('HelpComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HelpComponent],
    }).overrideComponent(HelpComponent, {
      set: { imports: [], schemas: [NO_ERRORS_SCHEMA] },
    });
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(HelpComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeDefined();
  });
});
