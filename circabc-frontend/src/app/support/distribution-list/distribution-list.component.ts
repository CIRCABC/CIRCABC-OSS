import { Component, OnInit } from '@angular/core';
import {
  AppMessageService,
  DistributionMail,
} from 'app/core/generated/circabc';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import {
  SelectableDistributionMail,
  SelectablePagedDistributionMails,
} from 'app/support/distribution-list/selectable-paged-distribution-mails';
import { saveAs } from 'file-saver';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-distribution-list',
  templateUrl: './distribution-list.component.html',
  styleUrls: ['./distribution-list.component.scss'],
})
export class DistributionListComponent implements OnInit {
  public distributionEmails: SelectablePagedDistributionMails = {
    data: [],
    total: 0,
  };
  public listingOptions: ListingOptions = { page: 1, limit: 25, sort: '' };
  public showModal = false;
  public deletingAll = false;
  public allSelected = false;

  constructor(private appMessageService: AppMessageService) {}

  ngOnInit() {
    this.loadDistributionEmails();
  }

  public async loadDistributionEmails() {
    const distribs = await firstValueFrom(
      this.appMessageService.getDistributionEmails(
        this.listingOptions.sort,
        this.listingOptions.limit,
        this.listingOptions.page
      )
    );

    this.distributionEmails = {
      data: [],
      total: distribs.total ? distribs.total : 0,
    };
    if (distribs.data) {
      for (const distribMail of distribs.data) {
        this.distributionEmails.data.push(
          distribMail as SelectableDistributionMail
        );
      }
    }
  }

  public goToPage(page: number) {
    this.listingOptions.page = page;
    this.loadDistributionEmails();
  }

  public async unsubscribeEmail(distribMail: DistributionMail) {
    if (distribMail.id) {
      try {
        await firstValueFrom(
          this.appMessageService.deleteDistributionEmails(distribMail.id)
        );
        this.listingOptions = {
          page: 1,
          limit: 25,
          sort: this.listingOptions.sort,
        };
        this.loadDistributionEmails();
      } catch (error) {
        console.error(error);
      }
    }
  }

  public hasSelectedDistribution() {
    return this.countSelectedDistribution() > 0;
  }

  public countSelectedDistribution() {
    let nbSelection = 0;
    this.distributionEmails.data.forEach((distrib) => {
      if (distrib.selected) {
        nbSelection += 1;
      }
    });

    return nbSelection;
  }

  public tickDistributionMail(distrib: SelectableDistributionMail) {
    if (distrib.selected) {
      distrib.selected = false;
    } else {
      distrib.selected = true;
    }
  }

  public async unsubscribeSelectedEmail() {
    const toRemove: DistributionMail[] = [];
    this.distributionEmails.data.forEach((distrib) => {
      if (distrib.selected) {
        toRemove.push({ id: distrib.id, emailAddress: distrib.emailAddress });
      }
    });

    if (toRemove.length > 0) {
      this.deletingAll = true;
      for (const distrib of toRemove) {
        try {
          if (distrib.id) {
            await firstValueFrom(
              this.appMessageService.deleteDistributionEmails(distrib.id)
            );
          }
        } catch (error) {
          console.error(error);
        }
      }
      this.listingOptions = {
        page: 1,
        limit: 25,
        sort: this.listingOptions.sort,
      };
      this.loadDistributionEmails();
      this.deletingAll = false;
    }
  }

  public selectAll() {
    this.allSelected = !this.allSelected;
    this.distributionEmails.data.forEach((distrib) => {
      distrib.selected = this.allSelected;
    });
  }

  public async downloadExport() {
    this.appMessageService
      .getDistributionEmailsExport()
      .subscribe((response) => {
        saveAs(response, 'distribution-list.xlsx');
      });
  }
  public trackById(_index: number, item: { id?: string | number }) {
    return item.id;
  }
}
