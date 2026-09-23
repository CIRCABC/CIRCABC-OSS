import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { type User } from 'app/core/generated/circabc';
import { SERVER_URL } from 'app/core/variables';
import { DownloadUtilService } from 'app/shared/services/download-util.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { MemberCardComponent } from './member-card.component';

describe('MemberCardComponent', () => {
  let component: MemberCardComponent;
  let componentRef: ComponentRef<MemberCardComponent>;
  let fixture: ComponentFixture<MemberCardComponent>;

  const mockUser: User = {
    userId: 'testuser',
    firstname: 'John',
    lastname: 'Doe',
    visibility: true,
    avatar: 'avatar-id',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MemberCardComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: SERVER_URL, useValue: 'http://localhost/' },
        {
          provide: DownloadUtilService,
          useValue: { getDownloadUrl: vi.fn().mockReturnValue('') },
        },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MemberCardComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('user', mockUser);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should return true when user visibility is true', () => {
    componentRef.setInput('user', { ...mockUser, visibility: true });
    expect(component.getUserVisibility()).toBe(true);
  });

  it('should return false when user visibility is false', () => {
    componentRef.setInput('user', { ...mockUser, visibility: false });
    expect(component.getUserVisibility()).toBe(false);
  });

  it('should return false when user visibility is undefined', () => {
    componentRef.setInput('user', { userId: 'testuser' } as User);
    expect(component.getUserVisibility()).toBe(false);
  });
});
