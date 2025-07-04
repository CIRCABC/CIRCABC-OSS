import { Component, OnInit } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { OverlayerComponent } from 'app/shared/overlayer/overlayer.component';

@Component({
  selector: 'cbc-dashboard-help-layer',
  templateUrl: './dashboard-help-layer.component.html',
  styleUrl: './dashboard-help-layer.component.scss',
  imports: [OverlayerComponent, TranslocoModule],
})
export class DashboardHelpLayerComponent implements OnInit {
  public showOverlayHelp = true;
  public step = 0;
  public maxStep = 3;
  public showClose = false;

  ngOnInit() {
    this.showOverlayHelp = !(
      sessionStorage.getItem('session-user-dashboard-viewed') === 'yes' ||
      localStorage.getItem('user-dashboard-viewed') === 'yes'
    );

    sessionStorage.setItem('session-user-dashboard-viewed', 'yes');
  }

  public saveAsViewed(doNotShow: boolean) {
    localStorage.setItem('user-dashboard-viewed', doNotShow ? 'yes' : 'no');
  }

  public toStep(step: number) {
    this.step = step;
    if (step === 3) {
      this.showClose = true;
    }
  }
}
