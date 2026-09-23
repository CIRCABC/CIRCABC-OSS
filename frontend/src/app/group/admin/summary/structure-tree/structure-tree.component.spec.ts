import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { type StructureNode } from './structure-node';
import { StructureTreeComponent } from './structure-tree.component';

describe('StructureTreeComponent', () => {
  let fixture: ComponentFixture<StructureTreeComponent>;
  let component: StructureTreeComponent;
  let componentRef: ComponentRef<StructureTreeComponent>;

  const mockTree: StructureNode = {
    name: 'Library',
    children: [{ name: 'SubFolder', children: [] }],
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StructureTreeComponent],
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

    fixture = TestBed.createComponent(StructureTreeComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('tree', mockTree);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should render root node as strong element', () => {
    const strong = (fixture.nativeElement as HTMLElement).querySelector(
      'strong'
    );
    expect(strong).toBeDefined();
  });

  it('should render children as list items', () => {
    const items = (fixture.nativeElement as HTMLElement).querySelectorAll('li');
    expect(items).toHaveLength(1);
  });

  it('should default isRoot to true', () => {
    expect(component.isRoot()).toBe(true);
  });

  it('should render span instead of strong when isRoot is false', () => {
    componentRef.setInput('isRoot', false);
    fixture.detectChanges();
    const strong = (fixture.nativeElement as HTMLElement).querySelector(
      'strong'
    );
    const span = (fixture.nativeElement as HTMLElement).querySelector('span');
    expect(strong).toBeNull();
    expect(span).toBeDefined();
  });

  it('should show block sign when node has no children and is root', () => {
    const emptyTree: StructureNode = { name: 'Empty', children: [] };
    componentRef.setInput('tree', emptyTree);
    fixture.detectChanges();
    const img = (fixture.nativeElement as HTMLElement).querySelector(
      'img[src="img/rounded-block-sign.png"]'
    );
    expect(img).toBeDefined();
  });

  it('should not show block sign when node has children', () => {
    const img = (fixture.nativeElement as HTMLElement).querySelector(
      'img[src="img/rounded-block-sign.png"]'
    );
    expect(img).toBeNull();
  });
});
