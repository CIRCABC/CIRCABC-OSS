import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { PagerComponent } from './pager.component';

describe('PagerComponent', () => {
  let component: PagerComponent;
  let fixture: ComponentFixture<PagerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PagerComponent],
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

    fixture = TestBed.createComponent(PagerComponent);
    component = fixture.componentInstance;
  });

  describe('canPreviousPage', () => {
    it('should return false when page is 1', () => {
      component.page.set(1);
      expect(component.canPreviousPage()).toBe(false);
    });

    it('should return true when page is greater than 1', () => {
      component.page.set(2);
      expect(component.canPreviousPage()).toBe(true);
    });
  });

  describe('canNextPage', () => {
    it('should return false when on last page', () => {
      fixture.componentRef.setInput('length', 10);
      fixture.componentRef.setInput('pageSize', 5);
      component.page.set(2);
      expect(component.canNextPage()).toBe(false);
    });

    it('should return true when not on last page', () => {
      fixture.componentRef.setInput('length', 10);
      fixture.componentRef.setInput('pageSize', 5);
      component.page.set(1);
      expect(component.canNextPage()).toBe(true);
    });
  });

  describe('previousPage', () => {
    it('should decrement page when canPreviousPage is true', () => {
      component.page.set(3);
      component.previousPage();
      expect(component.page()).toBe(2);
    });

    it('should not decrement page when on first page', () => {
      component.page.set(1);
      component.previousPage();
      expect(component.page()).toBe(1);
    });
  });

  describe('nextPage', () => {
    it('should increment page when canNextPage is true', () => {
      fixture.componentRef.setInput('length', 10);
      fixture.componentRef.setInput('pageSize', 5);
      component.page.set(1);
      component.nextPage();
      expect(component.page()).toBe(2);
    });

    it('should not increment page when on last page', () => {
      fixture.componentRef.setInput('length', 10);
      fixture.componentRef.setInput('pageSize', 5);
      component.page.set(2);
      component.nextPage();
      expect(component.page()).toBe(2);
    });
  });

  describe('goToPage', () => {
    it('should set page to the given value', () => {
      component.goToPage('5');
      expect(component.page()).toBe(5);
    });
  });

  describe('getPages', () => {
    it('should return an array of page numbers', () => {
      fixture.componentRef.setInput('length', 25);
      fixture.componentRef.setInput('pageSize', 10);
      expect(component.getPages()).toEqual([1, 2, 3]);
    });

    it('should return empty array when length is 0', () => {
      fixture.componentRef.setInput('length', 0);
      fixture.componentRef.setInput('pageSize', 10);
      expect(component.getPages()).toEqual([]);
    });
  });
});
