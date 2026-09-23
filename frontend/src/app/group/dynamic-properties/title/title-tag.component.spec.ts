import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TitleTagComponent } from './title-tag.component';

describe('TitleTagComponent', () => {
  let fixture: ComponentFixture<TitleTagComponent>;
  let componentRef: ComponentRef<TitleTagComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TitleTagComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TitleTagComponent);
    componentRef = fixture.componentRef;
    componentRef.setInput('entry', { lang: 'en', value: 'Test Title' });
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should display the lang', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.titleTagLang')?.textContent).toBe('en');
  });

  it('should display the value', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.titleTagValue')?.textContent).toBe('Test Title');
  });

  it('should update when input changes', () => {
    componentRef.setInput('entry', { lang: 'fr', value: 'Titre' });
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.titleTagLang')?.textContent).toBe('fr');
    expect(el.querySelector('.titleTagValue')?.textContent).toBe('Titre');
  });
});
