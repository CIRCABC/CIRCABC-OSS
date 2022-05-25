import { CommonModule, I18nSelectPipe } from '@angular/common';
import { NgModule } from '@angular/core';
import { BreadcrumbModule } from 'app/group/breadcrumb/breadcrumb.module';
import { ForumModule } from 'app/group/forum/forum.module';
import { NodeAccessGuard } from 'app/group/guards/access-guard.service';
import { AdminGuard } from 'app/group/guards/admin-guard.service';
import { NodeEditGuard } from 'app/group/guards/edit-guard.service';
import { LeaveFileUploadGuard } from 'app/group/guards/leave-file-upload-guard';
import { KeywordsModule } from 'app/group/keywords/keywords.module';
import { AddContentComponent } from 'app/group/library/add-content/add-content.component';
import { FileDetailsComponent } from 'app/group/library/add-content/file-details/file-details.component';
import { AddSharedSpaceLinkComponent } from 'app/group/library/add-shared-space-link/add-shared-space-link.component';
import { AddSpaceComponent } from 'app/group/library/add-space/add-space.component';
import { AddTranslationComponent } from 'app/group/library/add-translation/add-translation.component';
import { AddUrlComponent } from 'app/group/library/add-url/add-url.component';
import { AutoUploadLibraryComponent } from 'app/group/library/auto-upload-library/auto-upload-library.component';
import { LibraryBrowserComponent } from 'app/group/library/browser/library-browser.component';
import { ClipboardComponent } from 'app/group/library/clipboard/clipboard.component';
import { ClipboardService } from 'app/group/library/clipboard/clipboard.service';
import { NgxExtendedPdfViewerModule } from 'ngx-extended-pdf-viewer';
import { ContentPreviewExtendedComponent } from 'app/group/library/content-preview-ext/content-preview-ext.component';
import { DeleteActionComponent } from 'app/group/library/delete/delete-action.component';
import { DeleteMultipleComponent } from 'app/group/library/delete/delete-multiple.component';
import { CancelCheckoutComponent } from 'app/group/library/details/cancel-checkout/cancel-checkout.component';
import { CheckinComponent } from 'app/group/library/details/checkin/checkin.component';
import { DetailsComponent } from 'app/group/library/details/details.component';
import { EnableMultilingualComponent } from 'app/group/library/details/enable-multilingual/enable-multilingual.component';
import { AddDropdownComponent } from 'app/group/library/dropdown/add-dropdown.component';
import { EditNodeComponent } from 'app/group/library/edit/edit-node.component';
import { FavouriteSwitchComponent } from 'app/group/library/favourite-switch/favourite-switch.component';
import { FolderTreeViewComponent } from 'app/group/library/folder-tree-view/folder-tree-view.component';
import { ImportComponent } from 'app/group/library/import/import.component';
import { EncodingInputComponent } from 'app/group/library/input/encoding-input.component';
import { MimetypeInputComponent } from 'app/group/library/input/mimetype-input.component';
import { LibraryRoutingModule } from 'app/group/library/library-routing.module';
import { LibraryComponent } from 'app/group/library/library.component';
import { ManageSpaceSharingComponent } from 'app/group/library/manage-space-sharing/manage-space-sharing.component';
import { ShareSpaceComponent } from 'app/group/library/manage-space-sharing/share-space/share-space.component';
import { BulkDownloadPipe } from 'app/group/library/pipes/bulk-download.pipe';
import { SnackbarComponent } from 'app/group/library/snackbar/snackbar.component';
import { UpdateContentComponent } from 'app/group/library/update-content/update-content.component';
import { FileInputComponent } from 'app/group/library/upload-form/file-input/file-input.component';
import { FileListComponent } from 'app/group/library/upload-form/file-list/file-list.component';
import { FileMetadataComponent } from 'app/group/library/upload-form/file-metadata/file-metadata.component';
import { UploadFormComponent } from 'app/group/library/upload-form/upload-form.component';
import { PrimengComponentsModule } from 'app/primeng-components/primeng-components.module';
import { SharedModule } from 'app/shared/shared.module';
import { MaterialComponentsModule } from 'app/material-components/material-components.module';

@NgModule({
  imports: [
    LibraryRoutingModule,
    BreadcrumbModule,
    SharedModule,
    KeywordsModule,
    ForumModule,
    PrimengComponentsModule,
    NgxExtendedPdfViewerModule,
    CommonModule,
    MaterialComponentsModule,
  ],
  exports: [ShareSpaceComponent],
  declarations: [
    LibraryComponent,
    DetailsComponent,
    EncodingInputComponent,
    AddContentComponent,
    LibraryBrowserComponent,
    AddSpaceComponent,
    BulkDownloadPipe,
    AddDropdownComponent,
    DeleteActionComponent,
    FileDetailsComponent,
    EditNodeComponent,
    UpdateContentComponent,
    DeleteMultipleComponent,
    CancelCheckoutComponent,
    CheckinComponent,
    ClipboardComponent,
    MimetypeInputComponent,
    SnackbarComponent,
    AddUrlComponent,
    AddTranslationComponent,
    EnableMultilingualComponent,
    AutoUploadLibraryComponent,
    FavouriteSwitchComponent,
    ImportComponent,
    ManageSpaceSharingComponent,
    ShareSpaceComponent,
    AddSharedSpaceLinkComponent,
    ContentPreviewExtendedComponent,
    FolderTreeViewComponent,
    UploadFormComponent,
    FileInputComponent,
    FileListComponent,
    FileMetadataComponent,
  ],
  providers: [
    BulkDownloadPipe,
    ClipboardService,
    NodeAccessGuard,
    I18nSelectPipe,
    NodeEditGuard,
    AdminGuard,
    LeaveFileUploadGuard,
  ],
})
export class LibraryModule {}
