import { ComponentRef, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { type User } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { LeaderCardComponent } from './leader-card.component';

describe('LeaderCardComponent', () => {
  let fixture: ComponentFixture<LeaderCardComponent>;
  let componentRef: ComponentRef<LeaderCardComponent>;

  const mockUser: User = {
    userId: 'user1',
    firstname: 'John',
    lastname: 'Doe',
    email: 'john@example.com',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [LeaderCardComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(LeaderCardComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });

    fixture = TestBed.createComponent(LeaderCardComponent);
    componentRef = fixture.componentRef;
    componentRef.setInput('user', mockUser);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should have showDelete default to true', () => {
    expect(fixture.componentInstance.showDelete()).toBe(true);
  });

  it('should emit removeClicked when onRemoveClick is called', () => {
    const spy = vi.fn();
    fixture.componentInstance.removeClicked.subscribe(spy);
    fixture.componentInstance.onRemoveClick();
    expect(spy).toHaveBeenCalled();
  });

  it('should accept showDelete input as false', () => {
    componentRef.setInput('showDelete', false);
    fixture.detectChanges();
    expect(fixture.componentInstance.showDelete()).toBe(false);
  });
});
