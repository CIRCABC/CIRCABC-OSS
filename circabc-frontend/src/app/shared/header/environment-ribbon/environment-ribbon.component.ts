import { Component } from '@angular/core';
import { environment } from 'environments/environment';

// to keep in mind
// http://www.cssportal.com/css-ribbon-generator/

@Component({
  selector: 'cbc-environment-ribbon',
  templateUrl: './environment-ribbon.component.html',
  styleUrls: ['./environment-ribbon.component.scss'],
})
export class EnvironmentRibbonComponent {
  public environmentType = environment.environmentType;
}
