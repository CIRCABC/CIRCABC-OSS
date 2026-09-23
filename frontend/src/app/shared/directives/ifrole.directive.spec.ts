import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { vi } from 'vitest';
import { PermissionEvaluator } from '../../core/evaluator/permission-evaluator';
import { AllPermission } from '../../core/evaluator/permissions';
import { Node as ModelNode } from '../../core/generated/circabc';
import { IfRoleDirective } from './ifrole.directive';

@Component({
  imports: [IfRoleDirective],
  template: `<div *cbcIfRole="value()">visible</div>`,
})
class TestHostComponent {
  value = signal<[ModelNode, AllPermission, AllPermission[]]>([
    { id: '1' },
    'LibAdmin',
    [],
  ]);
}

describe('IfRoleDirective', () => {
  const mockPermissionEvaluator = {
    hasPermission: vi.fn(),
  };

  const node: ModelNode = {
    id: '1',
    permissions: { LibAdmin: 'ALLOWED' },
  };

  beforeEach(() => {
    mockPermissionEvaluator.hasPermission.mockReset();

    TestBed.configureTestingModule({
      imports: [TestHostComponent],
      providers: [
        { provide: PermissionEvaluator, useValue: mockPermissionEvaluator },
      ],
    });
  });

  it('should render the element when permission is granted', () => {
    mockPermissionEvaluator.hasPermission.mockReturnValue(true);

    const fixture = TestBed.createComponent(TestHostComponent);
    fixture.componentInstance.value.set([node, 'LibAdmin', []]);
    fixture.detectChanges();

    const el = fixture.debugElement.query(By.css('div'));
    expect(el).toBeTruthy();
    expect(el.nativeElement.textContent).toBe('visible');
  });

  it('should not render the element when permission is denied', () => {
    mockPermissionEvaluator.hasPermission.mockReturnValue(false);

    const fixture = TestBed.createComponent(TestHostComponent);
    fixture.componentInstance.value.set([node, 'LibAdmin', []]);
    fixture.detectChanges();

    const el = fixture.debugElement.query(By.css('div'));
    expect(el).toBeNull();
  });

  it('should update view when input changes', () => {
    mockPermissionEvaluator.hasPermission.mockReturnValue(true);

    const fixture = TestBed.createComponent(TestHostComponent);
    fixture.componentInstance.value.set([node, 'LibAdmin', []]);
    fixture.detectChanges();

    expect(fixture.debugElement.query(By.css('div'))).toBeTruthy();

    mockPermissionEvaluator.hasPermission.mockReturnValue(false);
    fixture.componentInstance.value.set([node, 'LibAccess', []]);
    fixture.detectChanges();

    expect(fixture.debugElement.query(By.css('div'))).toBeNull();
  });
});
