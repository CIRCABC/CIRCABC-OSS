import { Component, input } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { PermissionEvaluator } from '../../core/evaluator/permission-evaluator';
import { AllPermission } from '../../core/evaluator/permissions';
import { Node as ModelNode } from '../../core/generated/circabc';
import { IfRoleGEDirective } from './ifrolege.directive';

@Component({
  imports: [IfRoleGEDirective],
  template: `<div *cbcIfRoleGE="cbcIfRoleGE()">visible</div>`,
})
class TestHostComponent {
  readonly cbcIfRoleGE = input<
    [ModelNode | undefined, AllPermission, AllPermission[]]
  >([undefined, 'LibAccess', []]);
}

describe('IfRoleGEDirective', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  const mockPermissionEvaluator = {
    hasStrongerPermission: vi.fn(),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TestHostComponent],
      providers: [
        { provide: PermissionEvaluator, useValue: mockPermissionEvaluator },
      ],
    });
    mockPermissionEvaluator.hasStrongerPermission.mockReset();
  });

  it('should render content when user has stronger permission', () => {
    mockPermissionEvaluator.hasStrongerPermission.mockReturnValue(true);
    fixture = TestBed.createComponent(TestHostComponent);
    fixture.componentRef.setInput('cbcIfRoleGE', [
      { permissions: { LibAdmin: 'ALLOWED' } } as ModelNode,
      'LibAccess',
      [],
    ]);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('visible');
    expect(mockPermissionEvaluator.hasStrongerPermission).toHaveBeenCalled();
  });

  it('should not render content when user lacks permission', () => {
    mockPermissionEvaluator.hasStrongerPermission.mockReturnValue(false);
    fixture = TestBed.createComponent(TestHostComponent);
    fixture.componentRef.setInput('cbcIfRoleGE', [
      { permissions: {} } as ModelNode,
      'LibAdmin',
      [],
    ]);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).not.toContain('visible');
  });

  it('should throw when node is undefined on init', () => {
    mockPermissionEvaluator.hasStrongerPermission.mockReturnValue(false);
    fixture = TestBed.createComponent(TestHostComponent);
    fixture.componentRef.setInput('cbcIfRoleGE', [undefined, 'LibAccess', []]);

    expect(() => fixture.detectChanges()).toThrow(
      'Must not be a nullable or undefined value'
    );
  });

  it('should update view when input changes', () => {
    mockPermissionEvaluator.hasStrongerPermission.mockReturnValue(true);
    fixture = TestBed.createComponent(TestHostComponent);
    fixture.componentRef.setInput('cbcIfRoleGE', [
      { permissions: { LibAdmin: 'ALLOWED' } } as ModelNode,
      'LibAccess',
      [],
    ]);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('visible');

    mockPermissionEvaluator.hasStrongerPermission.mockReturnValue(false);
    fixture.componentRef.setInput('cbcIfRoleGE', [
      { permissions: {} } as ModelNode,
      'LibAdmin',
      [],
    ]);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).not.toContain('visible');
  });
});
