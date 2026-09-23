import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { User } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { FocusedUserBoxComponent } from './focused-user-box.component';

describe('FocusedUserBoxComponent', () => {
  let fixture: ComponentFixture<FocusedUserBoxComponent>;
  let componentRef: ComponentRef<FocusedUserBoxComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FocusedUserBoxComponent],
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

    fixture = TestBed.createComponent(FocusedUserBoxComponent);
    componentRef = fixture.componentRef;
  });

  it('should create', () => {
    componentRef.setInput('user', undefined);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should display user details when user is provided', () => {
    const mockUser: User = {
      userId: 'jdoe',
      firstname: 'John',
      lastname: 'Doe',
      email: 'john@example.com',
    };
    componentRef.setInput('user', mockUser);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('jdoe');
    expect(el.textContent).toContain('John');
    expect(el.textContent).toContain('Doe');
    expect(el.textContent).toContain('john@example.com');
  });

  it('should not render details section when user is undefined', () => {
    componentRef.setInput('user', undefined);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('section')).toBeNull();
  });

  it('should display user properties when provided', () => {
    const mockUser: User = {
      userId: 'jdoe',
      properties: {
        address: '123 Main St',
        phone: '+123456',
        organisation: 'EC',
        title: 'Mr',
        description: 'A user',
        fax: '+654321',
        url: 'https://example.com',
      },
    };
    componentRef.setInput('user', mockUser);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('123 Main St');
    expect(el.textContent).toContain('+123456');
    expect(el.textContent).toContain('EC');
    expect(el.textContent).toContain('https://example.com');
  });

  it('should not render property rows when properties is undefined', () => {
    const mockUser: User = { userId: 'jdoe' };
    componentRef.setInput('user', mockUser);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    const rows = el.querySelectorAll('.user-details__row');
    // Only basic fields (userId, firstname, lastname, email) should render
    expect(rows).toHaveLength(4);
  });
});
