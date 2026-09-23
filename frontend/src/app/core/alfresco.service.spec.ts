import { TestBed } from '@angular/core/testing';
import { AlfrescoService } from 'app/core/alfresco.service';
import { LoginService } from 'app/core/login.service';
import { vi } from 'vitest';

const { mockCreateRendition, mockGetRendition } = vi.hoisted(() => ({
  mockCreateRendition: vi.fn(),
  mockGetRendition: vi.fn(),
}));

vi.mock('@alfresco/js-api', () => ({
  AlfrescoApi: class {},
  RenditionsApi: class {
    createRendition = mockCreateRendition;
    getRendition = mockGetRendition;
  },
}));

describe('AlfrescoService', () => {
  let service: AlfrescoService;
  const mockLoginService = {
    getTicket: vi.fn().mockReturnValue('test-ticket'),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [{ provide: LoginService, useValue: mockLoginService }],
    });
    service = TestBed.inject(AlfrescoService);
    mockCreateRendition.mockReset();
    mockGetRendition.mockReset();
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  describe('createRendition', () => {
    it('should call RenditionsApi.createRendition with correct params', async () => {
      mockCreateRendition.mockResolvedValue({});
      await service.createRendition('node-123', 'pdf');
      expect(mockCreateRendition).toHaveBeenCalledWith('node-123', {
        id: 'pdf',
      });
    });
  });

  describe('getRendition', () => {
    it('should call RenditionsApi.getRendition with correct params', async () => {
      const renditionEntry = { entry: { id: 'imgpreview', status: 'CREATED' } };
      mockGetRendition.mockResolvedValue(renditionEntry);
      const result = await service.getRendition('node-456', 'imgpreview');
      expect(mockGetRendition).toHaveBeenCalledWith('node-456', 'imgpreview');
      expect(result).toEqual(renditionEntry);
    });
  });
});
