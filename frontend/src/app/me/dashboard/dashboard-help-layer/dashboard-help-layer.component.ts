import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { OverlayerComponent } from 'app/shared/overlayer/overlayer.component';

/**
 * Overlay component that renders a guided, multi-step help walkthrough for the
 * user dashboard.
 *
 * It displays an {@link OverlayerComponent}-based overlay the first time a user
 * visits their dashboard, guiding them through a sequence of steps. Whether the
 * overlay is shown is derived from `sessionStorage` and `localStorage` flags so
 * that returning users (or users who opted out) are not shown the help again.
 */
@Component({
  selector: 'cbc-dashboard-help-layer',
  templateUrl: './dashboard-help-layer.component.html',
  styleUrl: './dashboard-help-layer.component.scss',
  imports: [OverlayerComponent, TranslocoModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardHelpLayerComponent implements OnInit {
  /**
   * Whether the help overlay is currently displayed. Initialised in
   * {@link ngOnInit} based on the persisted "dashboard viewed" flags.
   */
  public showOverlayHelp = true;
  /** Index of the currently active walkthrough step (starting at 0). */
  public step = 0;
  /** The last step index of the walkthrough. */
  public maxStep = 3;
  /**
   * Whether the close control is shown. Becomes `true` once the user reaches
   * the final step of the walkthrough.
   */
  public showClose = false;

  /**
   * Angular lifecycle hook. Determines whether the help overlay should be shown
   * by checking the session and local storage "viewed" flags, then marks the
   * dashboard as viewed for the current session.
   */
  ngOnInit() {
    this.showOverlayHelp = !(
      sessionStorage.getItem('session-user-dashboard-viewed') === 'yes' ||
      localStorage.getItem('user-dashboard-viewed') === 'yes'
    );

    sessionStorage.setItem('session-user-dashboard-viewed', 'yes');
  }

  /**
   * Persists the user's preference for showing the dashboard help in the
   * future.
   *
   * @param doNotShow When `true`, records that the user opted out of seeing the
   * dashboard help again (stored as `'yes'`); when `false`, records that the
   * help may be shown again (stored as `'no'`).
   */
  public saveAsViewed(doNotShow: boolean) {
    localStorage.setItem('user-dashboard-viewed', doNotShow ? 'yes' : 'no');
  }

  /**
   * Navigates the walkthrough to the given step. When the final step (3) is
   * reached, the close control is revealed.
   *
   * @param step The step index to navigate to.
   */
  public toStep(step: number) {
    this.step = step;
    if (step === 3) {
      this.showClose = true;
    }
  }
}
