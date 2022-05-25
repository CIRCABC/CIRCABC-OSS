import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'cbc-cookie-disclaimer',
  templateUrl: './cookie-disclaimer.component.html',
  styleUrls: ['./cookie-disclaimer.component.scss'],
})
export class CookieDisclaimerComponent implements OnInit {
  public display = true;

  ngOnInit() {
    if (localStorage.getItem('agree-with-cookie')) {
      this.display = localStorage.getItem('agree-with-cookie') !== 'yes';
    }
  }

  public agree() {
    localStorage.setItem('agree-with-cookie', 'yes');
    this.display = false;
  }
}
