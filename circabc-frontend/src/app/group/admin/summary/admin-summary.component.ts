import { Component, Inject, OnInit, Optional } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import {
  BASE_PATH,
  IGStructure,
  IGTimelineElement,
  InterestGroupService,
  NameValue,
} from 'app/core/generated/circabc';
import { SaveAsService } from 'app/core/save-as.service';
import { StructureNode } from 'app/group/admin/summary/structure-tree/structure-node';
import { firstValueFrom } from 'rxjs';
interface PrettyProperty {
  name: string;
  value: string;
  icon: string;
}
interface PrettyProperties {
  section: string;
  data: PrettyProperty[];
}
@Component({
  selector: 'cbc-admin-ig-summary',
  templateUrl: './admin-summary.component.html',
  styleUrls: ['./admin-summary.component.scss'],
  preserveWhitespaces: true,
})
export class AdminSummaryComponent implements OnInit {
  public summaryForm!: FormGroup;

  public selectedStatistics = '1';

  public exportForm!: FormGroup;
  // properties for the exporter (format to export the file and the file id)
  public exportFormats = [
    { code: 'csv', name: 'CSV' },
    { code: 'xml', name: 'XML' },
    { code: 'xls', name: 'Excel' },
  ];

  public properties!: NameValue[];
  public activities!: IGTimelineElement[];
  public structureHolder!: IGStructure;

  public prettyProperties: PrettyProperties[] = [
    {
      section: 'label.ig.totals',
      data: [
        {
          name: 'summary.statistics.created.date',
          value: '',
          icon: 'calendar-with-a-clock-time-tools-155D79.png',
        },
        {
          name: 'summary.statistics.total.size',
          value: '',
          icon: 'size-155D79.png',
        },
      ],
    },
    {
      section: 'label.appointments',
      data: [
        {
          name: 'summary.statistics.meeting.count',
          value: '',
          icon: 'meeting-155D79.png',
        },
        {
          name: 'summary.statistics.event.count',
          value: '',
          icon: 'event-155D79.png',
        },
      ],
    },
    {
      section: 'label.information',
      data: [
        {
          name: 'summary.statistics.information.folder.count',
          value: '',
          icon: 'bigicon-folder.png',
        },
        {
          name: 'summary.statistics.information.document.count',
          value: '',
          icon: 'bigicon-file.png',
        },
        {
          name: 'summary.statistics.information.size',
          value: '',
          icon: 'size-155D79.png',
        },
      ],
    },
    {
      section: 'label.newsgroups',
      data: [
        {
          name: 'summary.statistics.forum.count',
          value: '',
          icon: 'group-155D79.png',
        },
        {
          name: 'summary.statistics.topic.count',
          value: '',
          icon: 'conversation-155D79.png',
        },
        {
          name: 'summary.statistics.post.count',
          value: '',
          icon: 'speech-bubble-with-text-lines.png',
        },
      ],
    },
    {
      section: 'label.library',
      data: [
        {
          name: 'summary.statistics.library.folder.count',
          value: '',
          icon: 'bigicon-folder.png',
        },
        {
          name: 'summary.statistics.library.document.count',
          value: '',
          icon: 'bigicon-file.png',
        },
        {
          name: 'summary.statistics.version.count',
          value: '',
          icon: 'bigicon-file.png',
        },
        {
          name: 'summary.statistics.library.size',
          value: '',
          icon: 'size-155D79.png',
        },
      ],
    },
    {
      section: 'label.members',
      data: [
        {
          name: 'summary.statistics.number.of.users',
          value: '',
          icon: 'bigicon-group.png',
        },
      ],
    },
  ];

  public loading = false;
  public igId!: string;
  private basePath!: string;

  constructor(
    private route: ActivatedRoute,
    private saveAsService: SaveAsService,
    private formBuilder: FormBuilder,
    private groupService: InterestGroupService,
    @Optional()
    @Inject(BASE_PATH)
    basePath: string
  ) {
    if (basePath) {
      this.basePath = basePath;
    }
  }

  async ngOnInit() {
    this.summaryForm = this.formBuilder.group(
      {
        selectedStatistics: [this.selectedStatistics],
      },
      {
        updateOn: 'change',
      }
    );

    this.exportForm = this.formBuilder.group(
      {
        export: [this.exportFormats[0]],
      },
      {
        updateOn: 'change',
      }
    );

    this.route.params.subscribe(async (params) => this.getIgId(params));
  }

  private async getIgId(params: { [key: string]: string }) {
    this.igId = params.id;
    await this.loadStatistics();
    if (!this.statisticsAvailable()) {
      await this.calculateStatistics();
    }
  }

  public async changeSummary() {
    this.selectedStatistics =
      this.summaryForm.controls.selectedStatistics.value;

    this.exportForm.controls.export.patchValue(this.exportFormats[0]);

    if (this.selectedStatistics === '1') {
      await this.loadStatistics();
      if (!this.statisticsAvailable()) {
        await this.calculateStatistics();
      }
    } else if (this.selectedStatistics === '2') {
      await this.loadTimeline();
    } else {
      await this.loadStructure();
    }
  }

  private toggleControls(enable: boolean) {
    if (enable) {
      this.summaryForm.controls.selectedStatistics.enable();
      this.exportForm.controls.export.enable();
    } else {
      this.summaryForm.controls.selectedStatistics.disable();
      this.exportForm.controls.export.disable();
    }
  }

  private async loadStatistics() {
    this.loading = true;
    this.toggleControls(false);
    this.properties = await firstValueFrom(
      this.groupService.getIGSummaryStatistics(this.igId, false)
    );
    this.prettifyProperties();
    this.toggleControls(true);
    this.loading = false;
  }

  public async calculateStatistics() {
    this.properties = await firstValueFrom(
      this.groupService.getIGSummaryStatistics(this.igId, true)
    );
    this.prettifyProperties();
  }

  public statisticsAvailable() {
    return this.properties !== undefined && this.properties.length !== 0;
  }

  public prettifyProperties() {
    for (const prettyProperties of this.prettyProperties) {
      for (const property of this.properties) {
        for (const prettyProperty of prettyProperties.data) {
          if (prettyProperty.name === property.name) {
            prettyProperty.value = property.value as string;
            break;
          }
        }
      }
    }
  }

  private async loadTimeline() {
    this.loading = true;
    this.toggleControls(false);
    this.activities = await firstValueFrom(
      this.groupService.getIGSummaryTimeline(this.igId)
    );
    this.toggleControls(true);
    this.loading = false;
  }

  private async loadStructure() {
    this.loading = true;
    this.toggleControls(false);
    this.structureHolder = await firstValueFrom(
      this.groupService.getIGSummaryStructure(this.igId)
    );
    this.toggleControls(true);
    this.loading = false;
  }

  public getStructure(index: number) {
    const root: StructureNode = JSON.parse(
      this.structureHolder.structure as string
    );
    return root.children[index];
  }

  public export(type: string, fileName: string) {
    const exportCode: string = this.exportForm.value.export.code;
    const url = `${this.basePath}/groups/${this.igId}/summary/export?format=${exportCode}&type=${type}`;
    const name = `${fileName}.${exportCode}`;
    this.saveAsService.saveUrlAs(url, name);
    return false;
  }
}
