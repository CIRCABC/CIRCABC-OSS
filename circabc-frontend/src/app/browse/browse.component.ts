import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, Data } from '@angular/router';
import {
  HeaderService,
  InterestGroup,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-browse',
  templateUrl: './browse.component.html',
})
export class BrowseComponent implements OnInit {
  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private nodesService: NodesService,
    private loginService: LoginService,
    private redirectionService: RedirectionService,
    private headerService: HeaderService
  ) {}

  private node!: ModelNode;
  async ngOnInit() {
    this.route.data.subscribe(async (value: Data) => {
      this.node = value.node;
      if (this.node.name === undefined) {
        return await this.nodeNameIsUndefined();
      }

      if (this.node.id) {
        let group = null;
        try {
          group = await firstValueFrom(
            this.nodesService.getGroup(this.node.id)
          );
        } catch (error) {
          console.error("Can't get group", error);
        }
        if (group !== null) {
          this.groupNavigation(group);
        } else if (
          this.node?.properties?.circaCategoryMasterGroup !== undefined
        ) {
          this.router.navigate(['/explore'], {
            queryParams: { categoryId: this.node.id },
          });
        }
      }
    });
  }

  private async nodeNameIsUndefined() {
    let header = null;
    try {
      header = await firstValueFrom(
        this.headerService.getHeader(this.node.id as string)
      );
    } catch (error) {
      console.error("Can't get header", error);
    }

    if (header === null) {
      if (this.loginService.isGuest()) {
        this.redirectionService.mustRedirect();
      }
      this.router.navigate(['/denied']);
    } else {
      this.router.navigate(['/explore'], {
        queryParams: { headerId: this.node.id },
      });
    }
  }

  private groupNavigation(group: InterestGroup) {
    if (group.id === this.node.id) {
      this.router.navigate(['/group', group.id]);
    } else if (group.eventId === this.node.id) {
      this.router.navigate(['/group', group.id, 'agenda']);
    } else if (group.informationId === this.node.id) {
      this.router.navigate(['/group', group.id, 'information']);
    } else if (group.newsgroupId === this.node.id) {
      this.router.navigate(['/group', group.id, 'forum']);
    } else if (group.libraryId === this.node.id) {
      this.router.navigate(['/group', group.id, 'library', group.libraryId]);
    } else if (this.node.service === 'library') {
      if (this.node.type?.endsWith('content')) {
        if (this.route.snapshot.queryParams.download === 'true') {
          this.router.navigate(
            ['/group', group.id, 'library', this.node.id, 'details'],
            {
              queryParams: {
                download: 'true',
              },
            }
          );
        } else {
          this.router.navigate([
            '/group',
            group.id,
            'library',
            this.node.id,
            'details',
          ]);
        }
      } else if (this.node.type?.endsWith('folder')) {
        this.router.navigate(['/group', group.id, 'library', this.node.id]);
      }
    } else if (this.node.service === 'events') {
      this.router.navigate([
        '/group',
        group.id,
        'agenda',
        this.node.id,
        'details',
      ]);
    } else if (this.node.service === 'information') {
      this.router.navigate(['/group', group.id, 'information'], {
        queryParams: { filterId: this.node.id },
      });
    } else if (this.node.service === 'newsgroups') {
      if (this.node.type?.endsWith('forum')) {
        this.router.navigate(['/group', group.id, 'forum', this.node.id]);
      } else if (this.node.type?.endsWith('topic')) {
        this.router.navigate([
          '/group',
          group.id,
          'forum',
          'topic',
          this.node.id,
        ]);
      }
    } else if (this.node.type?.endsWith('circaDirectoryRoot')) {
      this.router.navigate(['/group', group.id, 'members']);
    } else {
      this.router.navigate(['/group', group.id]);
    }
  }
}
