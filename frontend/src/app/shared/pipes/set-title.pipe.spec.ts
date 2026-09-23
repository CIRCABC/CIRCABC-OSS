import { TestBed } from '@angular/core/testing';
import { Title } from '@angular/platform-browser';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { vi } from 'vitest';

describe('SetTitlePipe', () => {
  let pipe: SetTitlePipe;
  let mockTitle: { setTitle: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    mockTitle = { setTitle: vi.fn() };
    TestBed.configureTestingModule({
      providers: [SetTitlePipe, { provide: Title, useValue: mockTitle }],
    });
    pipe = TestBed.inject(SetTitlePipe);
  });

  it('should set the document title when value is defined', () => {
    pipe.transform('Hello');
    expect(mockTitle.setTitle).toHaveBeenCalledWith('Hello');
  });

  it('should not set the document title when value is undefined', () => {
    pipe.transform(undefined);
    expect(mockTitle.setTitle).not.toHaveBeenCalled();
  });

  it('should return an empty string', () => {
    expect(pipe.transform('Test')).toBe('');
    expect(pipe.transform(undefined)).toBe('');
  });
});
