import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from 'app/shared/header/header.component';
import { NavigatorComponent } from 'app/shared/navigator/navigator.component';

@Component({
  selector: 'cbc-me',
  templateUrl: './me.component.html',
  preserveWhitespaces: true,
  imports: [HeaderComponent, NavigatorComponent, RouterOutlet],
})
export class MeComponent {}
