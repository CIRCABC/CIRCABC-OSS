import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { I18nService } from '../services/i18n.service';
import { I18nPipe } from './i18n.pipe';

describe('I18nPipe', () => {
  let pipe: I18nPipe;
  const mockI18nService = {
    getActiveLang: vi.fn().mockReturnValue('en'),
    getDefaultLang: vi.fn().mockReturnValue('en'),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        I18nPipe,
        { provide: I18nService, useValue: mockI18nService },
      ],
    });
    pipe = TestBed.inject(I18nPipe);
    mockI18nService.getActiveLang.mockReturnValue('en');
    mockI18nService.getDefaultLang.mockReturnValue('en');
  });

  it('should return empty string for undefined input', () => {
    expect(pipe.transform(undefined)).toBe('');
  });

  it('should return empty string for empty object', () => {
    expect(pipe.transform({})).toBe('');
  });

  it('should return the only non-empty value regardless of language', () => {
    mockI18nService.getActiveLang.mockReturnValue('fr');
    expect(pipe.transform({ en: 'Hello', fr: '' })).toBe('Hello');
  });

  it('should return value for active language when multiple values exist', () => {
    mockI18nService.getActiveLang.mockReturnValue('fr');
    expect(pipe.transform({ en: 'Hello', fr: 'Bonjour' })).toBe('Bonjour');
  });

  it('should fall back to default language when active language is missing', () => {
    mockI18nService.getActiveLang.mockReturnValue('de');
    mockI18nService.getDefaultLang.mockReturnValue('en');
    expect(pipe.transform({ en: 'Hello', fr: 'Bonjour' })).toBe('Hello');
  });

  it('should return first valid value when neither active nor default language exists', () => {
    mockI18nService.getActiveLang.mockReturnValue('de');
    mockI18nService.getDefaultLang.mockReturnValue('de');
    expect(pipe.transform({ fr: 'Bonjour', es: 'Hola' })).toBe('Bonjour');
  });

  it('should handle locale with dash prefix (e.g. en-US keys)', () => {
    mockI18nService.getActiveLang.mockReturnValue('en');
    expect(pipe.transform({ 'en-US': 'Color', 'fr-FR': 'Couleur' })).toBe(
      'Color'
    );
  });

  it('should match short locale to key with dash suffix', () => {
    mockI18nService.getActiveLang.mockReturnValue('fr');
    expect(pipe.transform({ 'en-US': 'Color', 'fr-': 'Couleur' })).toBe(
      'Couleur'
    );
  });
});
