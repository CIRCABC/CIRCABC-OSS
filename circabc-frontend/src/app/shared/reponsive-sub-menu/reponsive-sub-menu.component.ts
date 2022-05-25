import { Component } from '@angular/core';

@Component({
  selector: 'cbc-reponsive-sub-menu',
  templateUrl: './reponsive-sub-menu.component.html',
  styleUrls: ['./reponsive-sub-menu.component.scss'],
  preserveWhitespaces: true,
})
export class ReponsiveSubMenuComponent {
  public className = 'sub-menu';

  public toggle() {
    if (this.className === 'sub-menu') {
      this.className += ' responsive';
    } else {
      this.className = 'sub-menu';
    }
  }
}
