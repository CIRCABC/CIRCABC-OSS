import { Component, OnInit } from '@angular/core';
import { LoginService } from 'app/core/login.service';
import { interval } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

@Component({
  selector: 'cbc-ticket-validator',
  templateUrl: './ticket-validator.component.html',
})
export class TicketValidatorComponent implements OnInit {
  constructor(private loginService: LoginService) {}

  ngOnInit() {
    interval(10 * 60 * 1000)
      .pipe(mergeMap(async () => this.loginService.validateTicket()))
      .subscribe((data) =>
        // eslint-disable-next-line no-console
        console.log(data ? 'Ticket is valid' : 'Ticket is invalid')
      );
  }
}
