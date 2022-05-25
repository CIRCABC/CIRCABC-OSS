import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { BreadcrumbComponent } from 'app/group/breadcrumb/breadcrumb.component';
import { SharedModule } from 'app/shared/shared.module';

@NgModule({
  imports: [CommonModule, RouterModule, SharedModule],
  exports: [BreadcrumbComponent],
  declarations: [BreadcrumbComponent],
  providers: [],
})
export class BreadcrumbModule {}
