import { Component, Inject, OnInit, Optional, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  BASE_PATH,
  EventItemDefinition,
  EventsService,
  InterestGroup,
  InterestGroupService,
  Node as ModelNode,
  NodesService,
  PagedEventItemDefinition,
} from 'app/core/generated/circabc';
import { SaveAsService } from 'app/core/save-as.service';
import { changeSort, getFormattedDate, getFullDate } from 'app/core/util';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-agenda-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss'],
  preserveWhitespaces: true,
})
export class ListComponent implements OnInit {
  @ViewChild('datePicker')
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  datePicker: any;

  public appointments: EventItemDefinition[] = [];
  public igId!: string;
  public listingOptions: ListingOptions = { page: 1, limit: 10, sort: '' };
  public totalItems = 10;
  // eslint-disable-next-line no-magic-numbers

  public loading = false;

  public eventRootNode!: ModelNode;

  public exportForm!: FormGroup;
  public changeDateForm!: FormGroup;
  // properties for the exporter (format to export the file and the file id)
  public exportFormats = [
    { code: 'csv', name: 'CSV' },
    { code: 'xml', name: 'XML' },
    { code: 'xls', name: 'Excel' },
  ];

  private basePath!: string;
  public filter: 'Exact' | 'Future' | 'Previous' = 'Exact';

  public deleteEventShowModal = false;
  public eventToDelete!: EventItemDefinition;

  constructor(
    private route: ActivatedRoute,
    private eventsService: EventsService,
    private formBuilder: FormBuilder,
    private nodesService: NodesService,
    private permEvalService: PermissionEvaluatorService,
    private interestGroupService: InterestGroupService,
    private saveAsService: SaveAsService,
    @Optional()
    @Inject(BASE_PATH)
    basePath: string
  ) {
    if (basePath) {
      this.basePath = basePath;
    }
  }

  async ngOnInit() {
    this.exportForm = this.formBuilder.group(
      {
        export: [this.exportFormats[0]],
      },
      {
        updateOn: 'change',
      }
    );

    this.changeDateForm = this.formBuilder.group(
      {
        date: [new Date()],
      },
      {
        updateOn: 'change',
      }
    );

    this.changeDateForm.controls.date.valueChanges.subscribe(async (_value) => {
      await this.changeFilter('Exact');
    });

    this.appointments = [];
    this.route.params.subscribe(
      async (params) => await this.listEvents(params)
    );
  }

  public isEveAdmin(): boolean {
    return this.permEvalService.isEveAdmin(this.eventRootNode);
  }

  // parameter setting

  public getFormattedTodaysDate() {
    const currentDate = new Date();
    return getFormattedDate(currentDate);
  }

  public async changeFilter(filter: 'Exact' | 'Future' | 'Previous') {
    this.filter = filter;
    this.listingOptions.page = 1;
    await this.changePage(this.listingOptions);
  }

  // events paging and listing

  private async listEvents(params: { [key: string]: string }) {
    this.igId = params.id;
    await this.changePage(this.listingOptions);
  }

  public async goToPage(page: number) {
    this.listingOptions.page = page;
    await this.changePage(this.listingOptions);
  }

  public async changePage(listingOptions: ListingOptions) {
    this.loading = true;
    if (this.filter === 'Exact' || this.filter === 'Future') {
      listingOptions.sort = 'appointmentDate_DESC';
    } else {
      listingOptions.sort = 'appointmentDate_ASC';
    }
    this.listingOptions = listingOptions;
    if (this.igId !== undefined) {
      const result: PagedEventItemDefinition = await firstValueFrom(
        this.eventsService.getInterestGroupListEvents(
          this.igId,
          this.filter,
          getFullDate(this.changeDateForm.controls.date.value),
          this.listingOptions.limit,
          this.listingOptions.page,
          this.listingOptions.sort
        )
      );
      this.appointments = result.data;
      this.totalItems = result.total;

      if (this.eventRootNode === undefined) {
        const ig: InterestGroup = await firstValueFrom(
          this.interestGroupService.getInterestGroup(this.igId)
        );

        this.eventRootNode = await firstValueFrom(
          this.nodesService.getNode(ig.eventId as string)
        );
      }
    }
    this.loading = false;
  }

  public async changeSort(sort: string) {
    this.listingOptions.sort = changeSort(this.listingOptions.sort, sort);
    await this.changePage(this.listingOptions);
  }

  public async changeLimit(limit: number) {
    this.listingOptions.limit = limit;
    this.listingOptions.page = 1;
    await this.changePage(this.listingOptions);
  }

  // event deletion

  public popupDeleteEvent(event: EventItemDefinition) {
    if (event !== undefined) {
      this.eventToDelete = event;
      this.deleteEventShowModal = true;
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public async redisplayListAfterDelete() {
    await this.changePage(this.listingOptions);
  }

  // events export

  public export() {
    const exportCode: string = this.exportForm.value.export.code;
    const url = `${this.basePath}/groups/${this.igId}/events/export?filter=${
      this.filter
    }&format=${exportCode}&exactDate=${getFormattedDate(
      this.changeDateForm.controls.date.value
    )}`;
    const name = `Events.${exportCode}`;
    this.saveAsService.saveUrlAs(url, name);
    return false;
  }

  public trackById(_index: number, item: { id?: string | number }) {
    return item.id;
  }
}
