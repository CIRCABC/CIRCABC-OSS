import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { TranslocoModule } from '@ngneat/transloco';

import { PrimengComponentsModule } from 'app/primeng-components/primeng-components.module';
import { AddNotificationsComponent } from 'app/shared/add-notifications/add-notifications.component';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { ModalDeleteComponent } from 'app/shared/delete/modal-delete/modal-delete.component';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { FocusDirective } from 'app/shared/directives/focus.directive';
import { IfRoleDirective } from 'app/shared/directives/ifrole.directive';
import { IfRoleGEDirective } from 'app/shared/directives/ifrolege.directive';
import { IfRolesDirective } from 'app/shared/directives/ifroles.directive';
import { EcLogoAppComponent } from 'app/shared/ec-logo-app/ec-logo-app.component';
import { FileExtensionIconComponent } from 'app/shared/file-extension-icon/file-extension-icon.component';
import { FilePickerComponent } from 'app/shared/file-picker/file-picker.component';
import { FlatMessageComponent } from 'app/shared/flat-message/flat-message.component';
import { EnvironmentRibbonComponent } from 'app/shared/header/environment-ribbon/environment-ribbon.component';
import { HeaderComponent } from 'app/shared/header/header.component';
import { SearchBarComponent } from 'app/shared/header/search/search-bar.component';
import { HintComponent } from 'app/shared/hint/hint.component';
import { HistoryComponent } from 'app/shared/history/history.component';
import { InlineSelectComponent } from 'app/shared/inline-select/inline-select.component';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { ItemMultiselectorComponent } from 'app/shared/item-multiselector/item-multiselector.component';
import { LangSelectorComponent } from 'app/shared/lang/lang-selector.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { PersonalMenuComponent } from 'app/shared/menu/personal-menu.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { PreviewComponent } from 'app/shared/preview/preview.component';
import { NavigatorComponent } from 'app/shared/navigator/navigator.component';
import { NumberBadgeComponent } from 'app/shared/number-badge/number-badge.component';
import { OverlayerComponent } from 'app/shared/overlayer/overlayer.component';
import { CapitalizePipe } from 'app/shared/pipes/capitalize.pipe';
import { DatePipe } from 'app/shared/pipes/date.pipe';
import { TimePipe } from 'app/shared/pipes/time.pipe';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { IfOrRolesPipe } from 'app/shared/pipes/if-or-roles.pipe';
import { IfRoleGePipe } from 'app/shared/pipes/if-role-ge.pipe';
import { IfRolePipe } from 'app/shared/pipes/if-role.pipe';
import { IfRolesPipe } from 'app/shared/pipes/if-roles.pipe';
import { NodeIdPipe } from 'app/shared/pipes/nodeid.pipe';
import { NodeRefPipe } from 'app/shared/pipes/noderef.pipe';
import { OldDownloadPipe } from 'app/shared/pipes/old.download.pipe';
import { SafePipe } from 'app/shared/pipes/safe.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { SizePipe } from 'app/shared/pipes/size.pipe';
import { TaggedToPlainTextPipe } from 'app/shared/pipes/taggedtoplaintext.pipe';
import { ThumbnailPipe } from 'app/shared/pipes/thumbnail.pipe';
import { ReponsiveSubMenuComponent } from 'app/shared/reponsive-sub-menu/reponsive-sub-menu.component';
import { SaveAsComponent } from 'app/shared/save-as/save-as.component';
import { ShareComponent } from 'app/shared/share/share.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { SystemMessageIndicatorComponent } from 'app/shared/system-message-indicator/system-message-indicator.component';
import { TicketValidatorComponent } from 'app/shared/ticket-validator/ticket-validator.component';
import { TreeNodeComponent } from 'app/shared/treeview/tree-node.component';
import { TreeViewComponent } from 'app/shared/treeview/tree-view.component';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';
import { UsersPickerComponent } from 'app/shared/users/users-picker.component';
import { ClipboardModule } from 'ngx-clipboard';
import { NotificationMessageComponent } from 'app/shared/notification-message/notification-message.component';
import { CreateUserComponent } from 'app/shared/create-user-wizard/create-user.component';
import { PagerComponent } from 'app/shared//pager/pager.component';
import { PagerConfigurationComponent } from 'app/shared/pager-configuration/pager-configuration.component';
import { MaterialComponentsModule } from 'app/material-components/material-components.module';
import { ConfirmDialogComponent } from 'app/shared/confirm-dialog/confirm-dialog.component';

@NgModule({
  imports: [
    ClipboardModule,
    CommonModule,
    PrimengComponentsModule,
    MaterialComponentsModule,
    ReactiveFormsModule,
    RouterModule,
    TranslocoModule,
  ],
  declarations: [
    AddNotificationsComponent,
    CapitalizePipe,
    ControlMessageComponent,
    CreateUserComponent,
    DataCyDirective,
    DatePipe,
    DownloadPipe,
    FileExtensionIconComponent,
    FilePickerComponent,
    FlatMessageComponent,
    FocusDirective,
    HeaderComponent,
    HintComponent,
    HistoryComponent,
    HorizontalLoaderComponent,
    I18nPipe,
    IfOrRolesPipe,
    IfRoleDirective,
    IfRoleGEDirective,
    IfRoleGePipe,
    IfRolePipe,
    IfRolesDirective,
    IfRolesPipe,
    TimePipe,
    InlineDeleteComponent,
    InlineSelectComponent,
    ItemMultiselectorComponent,
    LangSelectorComponent,
    ModalComponent,
    PreviewComponent,
    MultilingualInputComponent,
    NavigatorComponent,
    NodeIdPipe,
    NodeRefPipe,
    NumberBadgeComponent,
    OldDownloadPipe,
    PersonalMenuComponent,
    ReponsiveSubMenuComponent,
    SafePipe,
    SaveAsComponent,
    SearchBarComponent,
    SecurePipe,
    SetTitlePipe,
    ShareComponent,
    SizePipe,
    SpinnerComponent,
    SystemMessageIndicatorComponent,
    TaggedToPlainTextPipe,
    ThumbnailPipe,
    TreeNodeComponent,
    TreeViewComponent,
    UserCardComponent,
    UsersPickerComponent,
    EnvironmentRibbonComponent,
    OverlayerComponent,
    EcLogoAppComponent,
    ModalDeleteComponent,
    TicketValidatorComponent,
    NotificationMessageComponent,
    PagerComponent,
    PagerConfigurationComponent,
    ConfirmDialogComponent,
  ],
  exports: [
    AddNotificationsComponent,
    CapitalizePipe,
    CommonModule,
    ControlMessageComponent,
    CreateUserComponent,
    DataCyDirective,
    DatePipe,
    DownloadPipe,
    FileExtensionIconComponent,
    FilePickerComponent,
    FlatMessageComponent,
    FocusDirective,
    HeaderComponent,
    HintComponent,
    HistoryComponent,
    HorizontalLoaderComponent,
    I18nPipe,
    IfOrRolesPipe,
    IfRoleDirective,
    IfRoleGEDirective,
    IfRoleGePipe,
    IfRolePipe,
    IfRolesDirective,
    IfRolesPipe,
    TimePipe,
    InlineDeleteComponent,
    InlineSelectComponent,
    ItemMultiselectorComponent,
    LangSelectorComponent,
    ModalComponent,
    PreviewComponent,
    MultilingualInputComponent,
    NavigatorComponent,
    NodeIdPipe,
    NodeRefPipe,
    NumberBadgeComponent,
    OldDownloadPipe,
    ReactiveFormsModule,
    ReponsiveSubMenuComponent,
    SafePipe,
    SaveAsComponent,
    SecurePipe,
    SetTitlePipe,
    ShareComponent,
    SizePipe,
    SpinnerComponent,
    SystemMessageIndicatorComponent,
    TaggedToPlainTextPipe,
    ThumbnailPipe,
    TranslocoModule,
    TreeNodeComponent,
    TreeViewComponent,
    UserCardComponent,
    UsersPickerComponent,
    OverlayerComponent,
    ModalDeleteComponent,
    TicketValidatorComponent,
    NotificationMessageComponent,
    PagerComponent,
    PagerConfigurationComponent,
  ],
  providers: [],
})
export class SharedModule {}
