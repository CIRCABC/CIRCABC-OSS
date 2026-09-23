import { TestBed } from '@angular/core/testing';
import { TranslocoService } from '@jsverse/transloco';
import { AresBridgeHelperService } from 'app/core/ares-bridge-helper.service';
import { DownloadService } from 'app/core/download.service';
import { AresBridgeService } from 'app/core/generated/ares-bridge';
import {
  ExternalRepositoryData,
  ExternalRepositoryService,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { RedirectionService } from 'app/core/redirection.service';
import { environment } from 'environments/environment';
import { of } from 'rxjs';
import { vi } from 'vitest';

describe('AresBridgeHelperService', () => {
  let service: AresBridgeHelperService;

  const mockLoginService = {
    getUser: vi.fn().mockReturnValue({ userId: 'testuser' }),
  };

  const mockAresBridgeService = {
    configuration: { apiKeys: {} },
    hasUserAccess: vi.fn(),
    storeDocument: vi.fn(),
  };

  const mockDownloadService = {
    getNodeContent: vi.fn(),
  };

  const mockExternalRepositoryService = {
    getExternalRepositoriesAsync: vi.fn(),
    getExternalRepositoryNodeLogAsync: vi.fn(),
    getExternalRepositoryGroupLogAsync: vi.fn(),
    getExternalRepoTicket: vi.fn(),
    getExternalRepoTicketAsync: vi
      .fn()
      .mockResolvedValue({ ticket: 'ticket123' }),
    saveExternalRepoTransaction: vi.fn(),
    saveExternalRepoTransactionAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockUiMessageService = {
    addInfoMessage: vi.fn(),
  };

  const mockTranslocoService = {
    translate: vi.fn().mockReturnValue('translated'),
  };

  const mockRedirectionService = {
    mustRedirect: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();

    TestBed.configureTestingModule({
      providers: [
        AresBridgeHelperService,
        { provide: LoginService, useValue: mockLoginService },
        { provide: AresBridgeService, useValue: mockAresBridgeService },
        { provide: DownloadService, useValue: mockDownloadService },
        {
          provide: ExternalRepositoryService,
          useValue: mockExternalRepositoryService,
        },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: TranslocoService, useValue: mockTranslocoService },
        { provide: RedirectionService, useValue: mockRedirectionService },
      ],
    });

    service = TestBed.inject(AresBridgeHelperService);
  });

  describe('isAresBridgeEnabled', () => {
    it('should return false when aresBridgeEnabled is false in environment', async () => {
      environment.aresBridgeEnabled = false;
      const result = await service.isAresBridgeEnabled('group1');
      expect(result).toBe(false);
    });

    it('should return false when no repos exist', async () => {
      environment.aresBridgeEnabled = true;
      mockExternalRepositoryService.getExternalRepositoriesAsync.mockResolvedValue(
        []
      );
      const result = await service.isAresBridgeEnabled('group1');
      expect(result).toBe(false);
    });

    it('should return true when AresBridge repo exists', async () => {
      environment.aresBridgeEnabled = true;
      mockExternalRepositoryService.getExternalRepositoriesAsync.mockResolvedValue(
        [{ name: 'AresBridge' }]
      );
      const result = await service.isAresBridgeEnabled('group1');
      expect(result).toBe(true);
    });

    it('should return false when repos exist but none is AresBridge', async () => {
      environment.aresBridgeEnabled = true;
      mockExternalRepositoryService.getExternalRepositoriesAsync.mockResolvedValue(
        [{ name: 'OtherRepo' }]
      );
      const result = await service.isAresBridgeEnabled('group1');
      expect(result).toBe(false);
    });
  });

  describe('nodeLog', () => {
    it('should return empty array when aresBridgeEnabled is false', async () => {
      environment.aresBridgeEnabled = false;
      const result = await service.nodeLog('node1');
      expect(result).toEqual([]);
    });

    it('should return logs from external repository service', async () => {
      environment.aresBridgeEnabled = true;
      const logs: ExternalRepositoryData[] = [
        {
          nodeId: 'node1',
          nodeName: 'file.txt',
          versionLabel: '1.0',
          transactionId: 'tx1',
          saveNumber: 'S001',
        },
      ];
      mockExternalRepositoryService.getExternalRepositoryNodeLogAsync.mockResolvedValue(
        logs
      );
      const result = await service.nodeLog('node1');
      expect(result).toEqual(logs);
      expect(
        mockExternalRepositoryService.getExternalRepositoryNodeLogAsync
      ).toHaveBeenCalledWith({ id: 'node1', repoId: 'AresBridge' });
    });
  });

  describe('groupLog', () => {
    it('should return empty array when aresBridgeEnabled is false', async () => {
      environment.aresBridgeEnabled = false;
      const result = await service.groupLog('group1');
      expect(result).toEqual([]);
    });

    it('should return logs from external repository service', async () => {
      environment.aresBridgeEnabled = true;
      const logs: ExternalRepositoryData[] = [
        {
          nodeId: 'node1',
          nodeName: 'file.txt',
          versionLabel: '1.0',
          transactionId: 'tx1',
          registrationNumber: 'R001',
        },
      ];
      mockExternalRepositoryService.getExternalRepositoryGroupLogAsync.mockResolvedValue(
        logs
      );
      const result = await service.groupLog('group1');
      expect(result).toEqual(logs);
      expect(
        mockExternalRepositoryService.getExternalRepositoryGroupLogAsync
      ).toHaveBeenCalledWith({ id: 'group1', repoId: 'AresBridge' });
    });
  });

  describe('getAlreadySentToAresBridge', () => {
    it('should return empty array when no nodes have logs', async () => {
      mockExternalRepositoryService.getExternalRepositoryNodeLogAsync.mockResolvedValue(
        []
      );
      const nodes: ModelNode[] = [{ id: 'node1', name: 'file.txt' }];
      const result = await service.getAlreadySentToAresBridge(nodes);
      expect(result).toEqual([]);
    });

    it('should return logs that match node id and have saveNumber or registrationNumber', async () => {
      const logs: ExternalRepositoryData[] = [
        {
          nodeId: 'node1',
          nodeName: 'file.txt',
          versionLabel: '1.0',
          transactionId: 'tx1',
          saveNumber: 'S001',
        },
        {
          nodeId: 'node1',
          nodeName: 'file.txt',
          versionLabel: '1.0',
          transactionId: 'tx2',
        },
      ];
      mockExternalRepositoryService.getExternalRepositoryNodeLogAsync.mockResolvedValue(
        logs
      );
      const nodes: ModelNode[] = [{ id: 'node1', name: 'file.txt' }];
      const result = await service.getAlreadySentToAresBridge(nodes);
      expect(result).toEqual([logs[0]]);
    });

    it('should filter out logs with mismatched nodeId', async () => {
      const logs: ExternalRepositoryData[] = [
        {
          nodeId: 'other-node',
          nodeName: 'other.txt',
          versionLabel: '1.0',
          transactionId: 'tx1',
          saveNumber: 'S001',
        },
      ];
      mockExternalRepositoryService.getExternalRepositoryNodeLogAsync.mockResolvedValue(
        logs
      );
      const nodes: ModelNode[] = [{ id: 'node1', name: 'file.txt' }];
      const result = await service.getAlreadySentToAresBridge(nodes);
      expect(result).toEqual([]);
    });
  });

  describe('sendToAresBridge', () => {
    beforeEach(() => {
      environment.aresBridgeEnabled = true;
      environment.aresBridgeServer = 'https://intragate.test.ec.europa.eu';
      environment.aresBridgeURL =
        'https://intragate.test.ec.europa.eu/Ares_pg/services/v1';
      environment.aresBridgeKey = 'testkey';
      environment.aresBridgeUiURL =
        'https://intragate.test.ec.europa.eu/Ares_pg/bridge/ui';
    });

    it('should call mustRedirect then return early when passed an empty array', async () => {
      await service.sendToAresBridge([]);
      expect(mockRedirectionService.mustRedirect).toHaveBeenCalled();
      expect(
        mockExternalRepositoryService.getExternalRepoTicketAsync
      ).not.toHaveBeenCalled();
    });

    it('should show info message when user has no access', async () => {
      mockExternalRepositoryService.getExternalRepoTicket.mockReturnValue(
        of({ ticket: 'ticket123' })
      );
      mockAresBridgeService.hasUserAccess.mockReturnValue(
        of({ access: 'false', registrationRights: 'false' })
      );

      const node: ModelNode = {
        id: 'node1',
        name: 'file.txt',
        properties: { modified: '2024-01-01', locale: 'en' },
      };

      await service.sendToAresBridge(node);

      expect(mockUiMessageService.addInfoMessage).toHaveBeenCalledWith(
        'translated'
      );
      expect(mockTranslocoService.translate).toHaveBeenCalledWith(
        'error.ares.access.denied'
      );
    });

    it('should show info message when user has access but no registration rights', async () => {
      mockExternalRepositoryService.getExternalRepoTicket.mockReturnValue(
        of({ ticket: 'ticket123' })
      );
      mockAresBridgeService.hasUserAccess.mockReturnValue(
        of({ access: 'true', registrationRights: 'false' })
      );

      const node: ModelNode = {
        id: 'node1',
        name: 'file.txt',
        properties: { modified: '2024-01-01', locale: 'en' },
      };

      await service.sendToAresBridge(node);

      expect(mockUiMessageService.addInfoMessage).toHaveBeenCalled();
    });

    it('should store document and open ares bridge when user has full access', async () => {
      mockExternalRepositoryService.getExternalRepoTicket.mockReturnValue(
        of({ ticket: 'ticket123' })
      );
      mockAresBridgeService.hasUserAccess.mockReturnValue(
        of({ access: 'true', registrationRights: 'true' })
      );
      mockDownloadService.getNodeContent.mockResolvedValue(
        new Blob(['content'])
      );
      mockAresBridgeService.storeDocument.mockReturnValue(
        of({ transactionId: 'tx-abc' })
      );
      mockExternalRepositoryService.saveExternalRepoTransaction.mockReturnValue(
        of(undefined)
      );

      const openSpy = vi.spyOn(window, 'open').mockReturnValue(null);

      const node: ModelNode = {
        id: 'node1',
        name: 'file.txt',
        properties: { modified: '2024-01-01', locale: 'en' },
      };

      await service.sendToAresBridge(node);

      expect(mockAresBridgeService.storeDocument).toHaveBeenCalled();
      expect(
        mockExternalRepositoryService.saveExternalRepoTransactionAsync
      ).toHaveBeenCalled();
      expect(openSpy).toHaveBeenCalled();
    });

    it('should build correct attachment with language from locale', async () => {
      mockExternalRepositoryService.getExternalRepoTicket.mockReturnValue(
        of({ ticket: 'ticket123' })
      );
      mockAresBridgeService.hasUserAccess.mockReturnValue(
        of({ access: 'true', registrationRights: 'true' })
      );
      mockDownloadService.getNodeContent.mockResolvedValue(new Blob(['x']));
      mockAresBridgeService.storeDocument.mockReturnValue(
        of({ transactionId: 'tx1' })
      );
      mockExternalRepositoryService.saveExternalRepoTransaction.mockReturnValue(
        of(undefined)
      );
      vi.spyOn(window, 'open').mockReturnValue(null);

      const node: ModelNode = {
        id: 'n1',
        name: 'doc.pdf',
        properties: { modified: '2024-01-01', locale: 'fr_FR' },
      };

      await service.sendToAresBridge(node);

      const body = mockAresBridgeService.storeDocument.mock.calls[0][0];
      expect(body.document.attachments[0].language).toBe('FR');
      expect(body.document.attachments[0].filename).toBe('doc.pdf');
      expect(body.document.attachments[0].type).toBe('MAIN');
      expect(body.document.attachments[0].sequence).toBe(1);
    });

    it('should use NS language when locale is undefined', async () => {
      mockExternalRepositoryService.getExternalRepoTicket.mockReturnValue(
        of({ ticket: 'ticket123' })
      );
      mockAresBridgeService.hasUserAccess.mockReturnValue(
        of({ access: 'true', registrationRights: 'true' })
      );
      mockDownloadService.getNodeContent.mockResolvedValue(new Blob(['x']));
      mockAresBridgeService.storeDocument.mockReturnValue(
        of({ transactionId: 'tx1' })
      );
      mockExternalRepositoryService.saveExternalRepoTransaction.mockReturnValue(
        of(undefined)
      );
      vi.spyOn(window, 'open').mockReturnValue(null);

      const node: ModelNode = {
        id: 'n1',
        name: 'doc.pdf',
        properties: { modified: '2024-01-01' },
      };

      await service.sendToAresBridge(node);

      const body = mockAresBridgeService.storeDocument.mock.calls[0][0];
      expect(body.document.attachments[0].language).toBe('NS');
    });

    it('should use NS language for unknown locale', async () => {
      mockExternalRepositoryService.getExternalRepoTicket.mockReturnValue(
        of({ ticket: 'ticket123' })
      );
      mockAresBridgeService.hasUserAccess.mockReturnValue(
        of({ access: 'true', registrationRights: 'true' })
      );
      mockDownloadService.getNodeContent.mockResolvedValue(new Blob(['x']));
      mockAresBridgeService.storeDocument.mockReturnValue(
        of({ transactionId: 'tx1' })
      );
      mockExternalRepositoryService.saveExternalRepoTransaction.mockReturnValue(
        of(undefined)
      );
      vi.spyOn(window, 'open').mockReturnValue(null);

      const node: ModelNode = {
        id: 'n1',
        name: 'doc.pdf',
        properties: { modified: '2024-01-01', locale: 'xx_XX' },
      };

      await service.sendToAresBridge(node);

      const body = mockAresBridgeService.storeDocument.mock.calls[0][0];
      expect(body.document.attachments[0].language).toBe('NS');
    });

    it('should handle multiple nodes', async () => {
      mockExternalRepositoryService.getExternalRepoTicket.mockReturnValue(
        of({ ticket: 'ticket123' })
      );
      mockAresBridgeService.hasUserAccess.mockReturnValue(
        of({ access: 'true', registrationRights: 'true' })
      );
      mockDownloadService.getNodeContent.mockResolvedValue(new Blob(['x']));
      mockAresBridgeService.storeDocument.mockReturnValue(
        of({ transactionId: 'tx1' })
      );
      mockExternalRepositoryService.saveExternalRepoTransaction.mockReturnValue(
        of(undefined)
      );
      vi.spyOn(window, 'open').mockReturnValue(null);

      const nodes: ModelNode[] = [
        {
          id: 'n1',
          name: 'a.pdf',
          properties: { modified: '2024-01-01', locale: 'fr' },
        },
        {
          id: 'n2',
          name: 'b.pdf',
          properties: { modified: '2024-01-01', locale: 'de' },
        },
      ];

      await service.sendToAresBridge(nodes);

      const body = mockAresBridgeService.storeDocument.mock.calls[0][0];
      expect(body.document.attachments).toHaveLength(2);
      expect(body.document.attachments[0].sequence).toBe(1);
      expect(body.document.attachments[1].sequence).toBe(2);
      expect(body.document.attachments[0].language).toBe('FR');
      expect(body.document.attachments[1].language).toBe('DE');
    });
  });
});
