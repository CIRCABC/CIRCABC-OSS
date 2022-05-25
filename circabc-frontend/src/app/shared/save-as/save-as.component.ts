import { Component, Input, NgZone } from '@angular/core';
import { SaveAsService } from 'app/core/save-as.service';

@Component({
  selector: 'cbc-save-as',
  templateUrl: './save-as.component.html',
  styleUrls: ['./save-as.component.scss'],
  preserveWhitespaces: true,
})
export class SaveAsComponent {
  @Input()
  id: string | undefined;
  @Input()
  name: string | undefined;
  @Input()
  showIcon = true;

  constructor(private saveAsService: SaveAsService, private ngZone: NgZone) {}

  onClick() {
    this.ngZone.runOutsideAngular(() => {
      if (this.id && this.name) {
        this.saveAsService.saveAsDirect(this.id, this.name);
      }
    });
    return false;
  }
}
