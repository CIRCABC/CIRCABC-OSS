import { TestBed } from '@angular/core/testing';
import { CategoryActionsComponent } from 'app/category/category-actions/category-actions.component';

describe('CategoryActionsComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CategoryActionsComponent],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(CategoryActionsComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeTruthy();
  });
});
