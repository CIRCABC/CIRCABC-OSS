import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AresBridgeHelperService } from 'app/core/ares-bridge-helper.service';
import { ExternalRepositoryData } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { environment } from 'environments/environment';

@Component({
  selector: 'cbc-external-repository-history',
  templateUrl: './cbc-external-repository-history.component.html',
  preserveWhitespaces: true,
})
export class ExternalRepositoryHistoryComponent implements OnInit {
  public groupLog: ExternalRepositoryData[] = [];
  private groupId = '';
  public isExternalUser = false;

  constructor(
    private route: ActivatedRoute,
    private aresBridgeHelperService: AresBridgeHelperService,
    private loginService: LoginService
  ) {}

  async ngOnInit() {
    this.isExternalUser =
      this.loginService.getUser().properties?.domain === 'external';
    this.route.params.subscribe((params) => {
      if (params.id) {
        this.groupId = params.id;
        this.LoadGroupExternalRepoLog(params.id);
      }
    });
  }

  public async LoadGroupExternalRepoLog(id: string) {
    this.groupLog = await this.aresBridgeHelperService.groupLog(id);
  }

  public aresDocumentLink(id: string | undefined): string {
    return (
      `${environment.aresBridgeServer}/Ares/document/show.do?documentId=` + id
    );
  }

  public circabcDocumentLink(nodeId: string): string {
    return `${environment.serverURL}${environment.baseHref.substring(1)}group/${
      this.groupId
    }/library/${nodeId}/details`;
  }
}
