import { TestBed } from '@angular/core/testing';
import { ActionEmitterResult } from 'app/action-result';
import { ActionService } from 'app/action-result/action.service';

describe('ActionService', () => {
  let service: ActionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ActionService);
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  it('should emit action result when propagateActionFinished is called', () => {
    const mockResult: ActionEmitterResult = { type: 'category.add', result: 1 };
    let emitted: ActionEmitterResult | undefined;

    service.actionFinished$.subscribe((result: ActionEmitterResult) => {
      emitted = result;
    });

    service.propagateActionFinished(mockResult);

    expect(emitted).toEqual(mockResult);
  });

  it('should not emit before propagateActionFinished is called', () => {
    let called = false;

    service.actionFinished$.subscribe(() => {
      called = true;
    });

    expect(called).toBe(false);
  });
});
