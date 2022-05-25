import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { LoginRoutingModule } from 'app/login/login-routing.module';
import { LoginComponent } from 'app/login/login.component';
import { LogoutComponent } from 'app/login/logout.component';
import { SharedModule } from 'app/shared/shared.module';

@NgModule({
  imports: [CommonModule, LoginRoutingModule, SharedModule],
  exports: [],
  declarations: [LogoutComponent, LoginComponent],
  providers: [],
})
export class LoginModule {}
