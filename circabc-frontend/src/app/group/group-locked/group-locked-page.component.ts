import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  OnInit,
  DestroyRef,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { GroupLockInfo, GroupLockService } from 'app/core/generated/circabc';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-group-locked-page',
  templateUrl: './group-locked-page.component.html',
  styleUrl: './group-locked-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [HorizontalLoaderComponent],
})
export class GroupLockedPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly groupLockService = inject(GroupLockService);
  private readonly destroyRef = inject(DestroyRef);

  readonly loading = signal(true);
  readonly lockInfo = signal<GroupLockInfo | null>(null);

  ngOnInit(): void {
    this.route.params
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((params) => {
        const groupId = params['id'];
        if (groupId) {
          this.loadLockInfo(groupId);
        }
      });
  }

  private async loadLockInfo(groupId: string): Promise<void> {
    this.loading.set(true);
    try {
      const info = await firstValueFrom(
        this.groupLockService.getGroupLockInfo(groupId)
      );
      this.lockInfo.set(info);
    } catch (error) {
      console.error('Failed to load group lock info', error);
      this.lockInfo.set({ locked: true });
    } finally {
      this.loading.set(false);
    }
  }
}
