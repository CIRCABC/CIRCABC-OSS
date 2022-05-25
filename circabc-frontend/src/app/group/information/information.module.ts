import { NgModule } from '@angular/core';
import { ServiceAccessGuard } from 'app/group/guards/service-guard.service';
import { AddNewsComponent } from 'app/group/information/add-news/add-news.component';
import { CardComponent } from 'app/group/information/card/card.component';
import { ConfigureInformationComponent } from 'app/group/information/configure-information/configure-information.component';
import { InformationRoutingModule } from 'app/group/information/information-routing.module';
import { InformationComponent } from 'app/group/information/information.component';
import { NewsCardComponent } from 'app/group/information/news-card/news-card.component';
import { PrimengComponentsModule } from 'app/primeng-components/primeng-components.module';
import { SharedModule } from 'app/shared/shared.module';
import { NgxExtendedPdfViewerModule } from 'ngx-extended-pdf-viewer';

@NgModule({
  imports: [
    InformationRoutingModule,
    SharedModule,
    PrimengComponentsModule,
    NgxExtendedPdfViewerModule,
  ],
  exports: [],
  declarations: [
    InformationComponent,
    CardComponent,
    NewsCardComponent,
    AddNewsComponent,
    ConfigureInformationComponent,
  ],
  providers: [ServiceAccessGuard],
})
export class InformationModule {}
