import { NgModule } from '@angular/core';

import { ContactCategoryComponent } from 'app/explorer/contact-category/contact-category.component';
import { ExplorerDropdownComponent } from 'app/explorer/explorer-dropdown/explorer-dropdown.component';
import { ExplorerRoutingModule } from 'app/explorer/explorer-routing.module';
import { ExplorerComponent } from 'app/explorer/explorer.component';
import { GroupCardComponent } from 'app/explorer/group-card/group-card.component';
import { LeaderCardComponent } from 'app/explorer/request-group/leader-card/leader-card.component';
import { RequestGroupComponent } from 'app/explorer/request-group/request-group.component';
import { PrimengComponentsModule } from 'app/primeng-components/primeng-components.module';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SharedModule } from 'app/shared/shared.module';

@NgModule({
  imports: [ExplorerRoutingModule, SharedModule, PrimengComponentsModule],
  exports: [],
  declarations: [
    ExplorerComponent,
    GroupCardComponent,
    RequestGroupComponent,
    LeaderCardComponent,
    ExplorerDropdownComponent,
    ContactCategoryComponent,
  ],
  providers: [I18nPipe],
})
export class ExplorerModule {}
