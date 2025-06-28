import { Component } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { environment } from 'environments/environment';

@Component({
  selector: 'cbc-about',
  templateUrl: './about.component.html',
  styleUrl: './about.component.scss',
  imports: [TranslocoModule],
})
export class AboutComponent {
  public circabcRelease = environment.circabcRelease;
  public step = 'privacy';
}
