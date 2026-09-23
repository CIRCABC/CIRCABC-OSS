import { Component, input } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { PermissionEvaluator } from '../../core/evaluator/permission-evaluator';
import { AllPermission } from '../../core/evaluator/permissions';
import { Node as ModelNode } from '../../core/generated/circabc';
import { IfRolesDirective } from './ifroles.directive';

@Component({
  imports: [IfRolesDirective],
  template: `<div *cbcIfRoles="value()">visible</div>`,
})
class TestHostComponent {
  readonly value = input<[ModelNode, AllPermission[], AllPermission[]]>([
    { permissions: { LibAdmin: 'ALLOWED' } },
    ['LibAdmin'],
    [],
  ]);
}

describe('IfRolesDirective', () => {
  const mockEvaluator = {
    hasAnyOfPermissions: vi.fn(),
  };

  beforeEach(() => {
    mockEvaluator.hasAnyOfPermissions.mockReset();
    TestBed.configureTestingModule({
      imports: [TestHostComponent],
      providers: [{ provide: PermissionEvaluator, useValue: mockEvaluator }],
    });
  });

  it('should render content when user has permission', () => {
    mockEvaluator.hasAnyOfPermissions.mockReturnValue(true);
    const fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('visible');
  });

  it('should not render content when user lacks permission', () => {
    mockEvaluator.hasAnyOfPermissions.mockReturnValue(false);
    const fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).not.toContain('visible');
  });

  it('should update view on input changes', () => {
    mockEvaluator.hasAnyOfPermissions.mockReturnValue(false);
    const fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).not.toContain('visible');

    mockEvaluator.hasAnyOfPermissions.mockReturnValue(true);
    fixture.componentRef.setInput('value', [
      { permissions: { LibAdmin: 'ALLOWED' } } as ModelNode,
      ['LibAdmin'] as AllPermission[],
      [] as AllPermission[],
    ]);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('visible');
  });
});
