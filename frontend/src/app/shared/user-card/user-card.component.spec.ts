import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { User } from '../../core/generated/circabc/model/user';
import { UserCacheService } from '../../core/user-cache.service';
import { UserCardComponent } from './user-card.component';

describe('UserCardComponent', () => {
  let component: UserCardComponent;
  let componentRef: ComponentRef<UserCardComponent>;
  let fixture: ComponentFixture<UserCardComponent>;
  let mockUserCacheService: { getUser: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    mockUserCacheService = {
      getUser: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [UserCardComponent],
      providers: [
        { provide: UserCacheService, useValue: mockUserCacheService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UserCardComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should set fullName to "unknown" when userId is null', async () => {
    componentRef.setInput('userId', null);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(component.fullName()).toBe('unknown');
  });

  it('should set fullName to "unknown" when userId is undefined', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    expect(component.fullName()).toBe('unknown');
  });

  it('should set fullName to "System" when userId is "System"', async () => {
    componentRef.setInput('userId', 'System');
    fixture.detectChanges();
    await fixture.whenStable();
    expect(component.fullName()).toBe('System');
  });

  it('should set fullName from user firstname and lastname', async () => {
    const mockUser: User = { firstname: 'John', lastname: 'Doe' };
    mockUserCacheService.getUser.mockResolvedValue(mockUser);
    componentRef.setInput('userId', 'user123');

    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockUserCacheService.getUser).toHaveBeenCalledWith('user123');
    expect(component.fullName()).toBe('John Doe');
    expect(component.user()).toBe(mockUser);
  });

  it('should set fullName to bracketed userId when firstname is empty', async () => {
    mockUserCacheService.getUser.mockResolvedValue({
      firstname: '',
      lastname: 'Doe',
    } as User);
    componentRef.setInput('userId', 'user123');

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.fullName()).toBe('[user123]');
  });

  it('should set fullName to bracketed userId when lastname is empty', async () => {
    mockUserCacheService.getUser.mockResolvedValue({
      firstname: 'John',
      lastname: '',
    } as User);
    componentRef.setInput('userId', 'user123');

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.fullName()).toBe('[user123]');
  });

  it('should set fullName to userId on error', async () => {
    mockUserCacheService.getUser.mockRejectedValue(new Error('not found'));
    componentRef.setInput('userId', 'user123');

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.fullName()).toBe('user123');
  });

  it('should toggle visible when not disabled', () => {
    componentRef.setInput('disabled', false);
    fixture.detectChanges();

    component.toggleVisible();
    expect(component.visible).toBe(true);

    component.toggleVisible();
    expect(component.visible).toBe(false);
  });

  it('should not toggle visible when disabled', () => {
    componentRef.setInput('disabled', true);
    fixture.detectChanges();

    component.toggleVisible();
    expect(component.visible).toBe(false);
  });

  it('should set visible to false on outside click', () => {
    component.visible = true;
    const event = new MouseEvent('click');
    const outsideElement = document.createElement('div');
    document.body.appendChild(outsideElement);

    component.onClick(event, outsideElement);

    expect(component.visible).toBe(false);
    document.body.removeChild(outsideElement);
  });

  it('should not change visible on inside click', () => {
    component.visible = true;
    const event = new MouseEvent('click');
    const insideElement = fixture.nativeElement;

    component.onClick(event, insideElement);

    expect(component.visible).toBe(true);
  });

  it('should do nothing when targetElement is null', () => {
    component.visible = true;
    const event = new MouseEvent('click');

    component.onClick(event, null);

    expect(component.visible).toBe(true);
  });
});
