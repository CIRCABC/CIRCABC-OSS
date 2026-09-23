import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { type User } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { UserResultBoxComponent } from './user-result-box.component';

describe('UserResultBoxComponent', () => {
  let component: UserResultBoxComponent;
  let componentRef: ComponentRef<UserResultBoxComponent>;
  let fixture: ComponentFixture<UserResultBoxComponent>;

  const mockUser: User = {
    userId: 'user1',
    firstname: 'John',
    lastname: 'Doe',
    email: 'john@example.com',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserResultBoxComponent],
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

    fixture = TestBed.createComponent(UserResultBoxComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('user', mockUser);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('toggleFocus', () => {
    it('should set focusedUserId to user.userId when not focused', () => {
      component.toggleFocus();
      expect(component.focusedUserId()).toBe('user1');
    });

    it('should clear focusedUserId when already focused on same user', () => {
      componentRef.setInput('focusedUserId', 'user1');
      fixture.detectChanges();

      component.toggleFocus();
      expect(component.focusedUserId()).toBe('');
    });

    it('should emit focusedUserIdChange', () => {
      const spy = vi.fn();
      component.focusedUserIdChange.subscribe(spy);

      component.toggleFocus();
      expect(spy).toHaveBeenCalledWith('user1');
    });
  });

  describe('triggerSelect', () => {
    it('should emit selectionTriggered with user and stop propagation', () => {
      const spy = vi.fn();
      component.selectionTriggered.subscribe(spy);
      const event = new Event('click');
      vi.spyOn(event, 'stopPropagation');

      component.triggerSelect(event);

      expect(spy).toHaveBeenCalledWith(mockUser);
      expect(event.stopPropagation).toHaveBeenCalled();
    });
  });

  describe('triggerRemove', () => {
    it('should emit removeTriggered with user and stop propagation', () => {
      const spy = vi.fn();
      component.removeTriggered.subscribe(spy);
      const event = new Event('click');
      vi.spyOn(event, 'stopPropagation');

      component.triggerRemove(event);

      expect(spy).toHaveBeenCalledWith(mockUser);
      expect(event.stopPropagation).toHaveBeenCalled();
    });
  });
});
