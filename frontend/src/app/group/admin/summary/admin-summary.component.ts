import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  resource,
  signal,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { TranslocoModule } from '@jsverse/transloco';
import { BASE_PATH, InterestGroupService } from 'app/core/generated/circabc';
import { SaveAsService } from 'app/core/save-as.service';
import { StructureNode } from 'app/group/admin/summary/structure-tree/structure-node';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { StructureTreeComponent } from './structure-tree/structure-tree.component';

/**
 * A single, display-ready statistic row.
 *
 * Pairs a translation key with the resolved value and an icon used to render
 * the statistic in the interest group summary view.
 */
interface PrettyProperty {
  /** Translation key identifying the statistic (also used to match backend `NameValue` entries). */
  name: string;
  /** Human-readable value for the statistic, populated from the backend response. */
  value: string;
  /** File name of the icon asset displayed next to the statistic. */
  icon: string;
}
/**
 * A grouped section of {@link PrettyProperty} rows shown under a common heading
 * (for example totals, appointments, library, members).
 */
interface PrettyProperties {
  /** Translation key for the section heading. */
  section: string;
  /** Statistics belonging to this section. */
  data: PrettyProperty[];
}
/**
 * Administration summary view for an interest group.
 *
 * Renders the group's summary dashboard, which lets an administrator switch
 * between three views selected through {@link summaryForm}:
 * - statistics (grouped counts and sizes, see {@link prettyProperties}),
 * - a timeline of recent activities, and
 * - the group's node structure (rendered via {@link StructureTreeComponent}).
 *
 * It also exposes an export control ({@link exportForm}) that downloads the
 * current summary data in CSV, XML or Excel format.
 *
 * Key collaborators:
 * - {@link InterestGroupService} to fetch statistics, timeline and structure,
 * - {@link SaveAsService} to trigger file downloads for exports,
 * - {@link ActivatedRoute} to obtain the interest group id from the route,
 * - {@link FormBuilder} to build the reactive forms.
 */
@Component({
  selector: 'cbc-admin-ig-summary',
  templateUrl: './admin-summary.component.html',
  styleUrl: './admin-summary.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    SpinnerComponent,
    StructureTreeComponent,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class AdminSummaryComponent {
  /** Route used to read the interest group id from the URL parameters. */
  private readonly route = inject(ActivatedRoute);
  /** Service used to trigger browser downloads for exported summary files. */
  private readonly saveAsService = inject(SaveAsService);
  /** Builder used to create the reactive forms. */
  private readonly formBuilder = inject(FormBuilder);
  /** API client used to fetch summary statistics, timeline and structure. */
  private readonly groupService = inject(InterestGroupService);

  /** The current route params, as a signal. */
  private readonly routeParams = toSignal(this.route.params);

  /** Identifier of the interest group being summarized, read from the route. */
  public readonly igId = computed(() => this.routeParams()?.id ?? '');

  /** Reactive form holding the currently selected summary view (statistics/timeline/structure). */
  public summaryForm!: FormGroup;

  /**
   * Identifier of the currently selected summary view:
   * `'1'` = statistics, `'2'` = timeline, otherwise structure.
   */
  public selectedStatistics = signal('1');

  /** Reactive form holding the currently selected export format. */
  public exportForm!: FormGroup;
  // properties for the exporter (format to export the file and the file id)
  /** Available export formats offered by the export control. */
  public exportFormats = [
    { code: 'csv', name: 'CSV' },
    { code: 'xml', name: 'XML' },
    { code: 'xls', name: 'Excel' },
  ];

  /**
   * Bumped by {@link calculateStatistics} to force the statistics resource to
   * recompute the statistics on the backend (`calculate: true`) even though
   * {@link igId} did not change.
   */
  private readonly recalculate = signal(0);

  /**
   * Resource loading the raw name/value statistics for the current interest
   * group. Reactively re-fetches when {@link igId} changes or the statistics
   * view is (re)selected, and recomputes on the backend when the statistics
   * are empty or when {@link calculateStatistics} is called.
   */
  private readonly statisticsResource = resource({
    params: () => {
      const id = this.igId();
      return id && this.selectedStatistics() === '1'
        ? { id, recalculate: this.recalculate() }
        : undefined;
    },
    loader: async ({ params }) => {
      let properties = await this.groupService.getIGSummaryStatisticsAsync({
        id: params.id,
        calculate: params.recalculate > 0,
      });
      if (properties.length === 0) {
        properties = await this.groupService.getIGSummaryStatisticsAsync({
          id: params.id,
          calculate: true,
        });
      }
      return properties;
    },
  });

  /** Raw name/value statistics as returned by the backend. */
  public readonly properties = computed(
    () => this.statisticsResource.value() ?? []
  );

  /**
   * Resource loading the activity timeline for the current interest group.
   * Only active while the timeline view is selected.
   */
  private readonly timelineResource = resource({
    params: () => {
      const id = this.igId();
      return id && this.selectedStatistics() === '2' ? id : undefined;
    },
    loader: ({ params: id }) =>
      this.groupService.getIGSummaryTimelineAsync({ id }),
  });

  /** Timeline activity elements for the interest group. */
  public readonly activities = this.timelineResource.value;

  /**
   * Resource loading the node structure for the current interest group. Only
   * active while the structure view is selected.
   */
  private readonly structureResource = resource({
    params: () => {
      const id = this.igId();
      return id && this.selectedStatistics() === '3' ? id : undefined;
    },
    loader: ({ params: id }) =>
      this.groupService.getIGSummaryStructureAsync({ id }),
  });

  /** Holder for the serialized interest group node structure. */
  public readonly structureHolder = this.structureResource.value;

  /**
   * Statistics grouped into display sections, with each entry's `value`
   * populated from {@link properties}.
   */
  public readonly prettyProperties = computed<PrettyProperties[]>(() => {
    const properties = this.properties();
    return this.prettyPropertiesTemplate.map((prettyProperties) => ({
      ...prettyProperties,
      data: prettyProperties.data.map((prettyProperty) => {
        const match = properties.find(
          (property) => property.name === prettyProperty.name
        );
        return match
          ? { ...prettyProperty, value: match.value as string }
          : { ...prettyProperty };
      }),
    }));
  });

  /**
   * Static section/row layout used to derive {@link prettyProperties}. The
   * `value` of each entry is empty here and populated from {@link properties}.
   */
  private readonly prettyPropertiesTemplate: PrettyProperties[] = [
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

  /**
   * Whether an asynchronous data load is currently in progress for the
   * currently selected view.
   */
  public readonly loading = computed(
    () =>
      this.statisticsResource.isLoading() ||
      this.timelineResource.isLoading() ||
      this.structureResource.isLoading()
  );
  /** Base API path used to build export download URLs. */
  private readonly basePath!: string;

  /**
   * Resolves the base API path via dependency injection and stores it for
   * later use when building export URLs.
   */
  constructor() {
    const basePath = inject(BASE_PATH);

    if (basePath) {
      this.basePath = basePath;
    }

    this.summaryForm = this.formBuilder.group(
      {
        selectedStatistics: [this.selectedStatistics()],
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

    effect(() => {
      if (this.loading()) {
        this.disableControls();
      } else {
        this.enableControls();
      }
    });
  }

  /**
   * Reacts to a change of the selected summary view, resetting the export
   * format so the relevant {@link statisticsResource}, {@link timelineResource}
   * or {@link structureResource} reactively loads the newly selected view's
   * data.
   */
  public changeSummary() {
    this.selectedStatistics.set(
      this.summaryForm.controls.selectedStatistics.value
    );

    this.exportForm.controls.export.patchValue(this.exportFormats[0]);
  }

  /** Enables the summary view selector and export format controls. */
  private enableControls() {
    this.summaryForm.controls.selectedStatistics.enable();
    this.exportForm.controls.export.enable();
  }

  /** Disables the summary view selector and export format controls (e.g. while loading). */
  private disableControls() {
    this.summaryForm.controls.selectedStatistics.disable();
    this.exportForm.controls.export.disable();
  }

  /**
   * Forces a fresh computation of the interest group statistics on the
   * backend and refreshes the display sections.
   */
  public calculateStatistics() {
    this.recalculate.update((value) => value + 1);
  }

  /**
   * Indicates whether statistics have been loaded and are non-empty.
   *
   * @returns `true` when {@link properties} contains at least one entry.
   */
  public statisticsAvailable() {
    return this.properties().length !== 0;
  }

  /**
   * Parses the serialized structure held in {@link structureHolder} and returns
   * the root's child node at the given index.
   *
   * @param index Zero-based index of the desired top-level child node.
   * @returns The {@link StructureNode} child at the requested index.
   */
  public getStructure(index: number) {
    const root = JSON.parse(
      this.structureHolder()?.structure as string
    ) as StructureNode;
    return root.children[index];
  }

  /**
   * Builds the export download URL for the current interest group and triggers
   * the download through {@link SaveAsService}.
   *
   * @param type The type of summary data to export (e.g. statistics section).
   * @param fileName Base name for the downloaded file (the format extension is appended).
   * @returns `false` to prevent the default browser action of the triggering event.
   */
  public export(type: string, fileName: string) {
    const exportCode: string = this.exportForm.value.export.code;
    const url = `${this.basePath}/groups/${this.igId()}/summary/export?format=${exportCode}&type=${type}`;
    const name = `${fileName}.${exportCode}`;
    this.saveAsService.saveUrlAs(url, name);
    return false;
  }
}
