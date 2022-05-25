import { NgModule } from '@angular/core';

import { BreadcrumbModule } from 'app/group/breadcrumb/breadcrumb.module';
import { ForumBrowserComponent } from 'app/group/forum/browser/forum-browser.component';
import { ConfigureForumServiceComponent } from 'app/group/forum/configure-forum-service/configure-forum-service.component';
import { CreateForumComponent } from 'app/group/forum/create-forum/create-forum.component';
import { DeleteForumComponent } from 'app/group/forum/delete-forum.component';
import { ForumDropdownComponent } from 'app/group/forum/dropdown/forum-dropdown.component';
import { ForumRoutingModule } from 'app/group/forum/forum-routing.module';
import { ForumComponent } from 'app/group/forum/forum.component';
import { ModerateComponent } from 'app/group/forum/moderate/moderate.component';
import { AddPostComponent } from 'app/group/forum/post/add-post.component';
import { PostComponent } from 'app/group/forum/post/post.component';
import { RejectPostComponent } from 'app/group/forum/post/reject-post/reject-post.component';
import { SignalAbuseComponent } from 'app/group/forum/post/signal-abuse/signal-abuse.component';
import { CreateTopicComponent } from 'app/group/forum/topic/create-topic.component';
import { CreateDetailsTopicComponent } from 'app/group/forum/topic/create-topic/create-details-topic.component';
import { DeleteTopicComponent } from 'app/group/forum/topic/delete-topic.component';
import { EditTopicComponent } from 'app/group/forum/topic/edit-topic/edit-topic.component';
import { TopicComponent } from 'app/group/forum/topic/topic.component';
import { ViewEditDetailsTopicComponent } from 'app/group/forum/topic/view-edit-details-topic/view-edit-details-topic.component';
import { ViewEditDetailsForumComponent } from 'app/group/forum/view-edit-details-forum/view-edit-details-forum.component';
import { NodeAccessGuard } from 'app/group/guards/access-guard.service';
import { PrimengComponentsModule } from 'app/primeng-components/primeng-components.module';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SharedModule } from 'app/shared/shared.module';

@NgModule({
  imports: [
    BreadcrumbModule,
    ForumRoutingModule,
    SharedModule,
    PrimengComponentsModule,
  ],
  declarations: [
    AddPostComponent,
    CreateForumComponent,
    CreateTopicComponent,
    CreateDetailsTopicComponent,
    DeleteTopicComponent,
    DeleteForumComponent,
    ForumBrowserComponent,
    ForumComponent,
    ForumDropdownComponent,
    PostComponent,
    TopicComponent,
    ViewEditDetailsForumComponent,
    ViewEditDetailsTopicComponent,
    ModerateComponent,
    RejectPostComponent,
    SignalAbuseComponent,
    EditTopicComponent,
    ConfigureForumServiceComponent,
  ],
  exports: [
    AddPostComponent,
    CreateTopicComponent,
    CreateDetailsTopicComponent,
    DeleteTopicComponent,
    PostComponent,
    EditTopicComponent,
  ],
  providers: [NodeAccessGuard, I18nPipe],
})
export class ForumModule {}
