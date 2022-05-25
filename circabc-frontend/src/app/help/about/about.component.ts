import { Component } from '@angular/core';

@Component({
  selector: 'cbc-about',
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.scss'],
})
export class AboutComponent {
  public step = 'privacy';
}
