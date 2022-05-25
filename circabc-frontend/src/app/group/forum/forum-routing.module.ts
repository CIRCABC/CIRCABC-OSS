import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ForumComponent } from 'app/group/forum/forum.component';
import { TopicComponent } from 'app/group/forum/topic/topic.component';
import { ViewEditDetailsTopicComponent } from 'app/group/forum/topic/view-edit-details-topic/view-edit-details-topic.component';
import { ViewEditDetailsForumComponent } from 'app/group/forum/view-edit-details-forum/view-edit-details-forum.component';
import { NodeAccessGuard } from 'app/group/guards/access-guard.service';

const forumRoutes: Routes = [
  {
    path: ':nodeId',
    component: ForumComponent,
    canActivate: [NodeAccessGuard],
  },
  {
    path: 'topic/:nodeId',
    component: TopicComponent,
    canActivate: [NodeAccessGuard],
  },
  {
    path: ':forumId/details',
    component: ViewEditDetailsForumComponent,
    canActivate: [NodeAccessGuard],
  },
  {
    path: ':topicId/topic-details',
    component: ViewEditDetailsTopicComponent,
    canActivate: [NodeAccessGuard],
  },
];

@NgModule({
  imports: [RouterModule.forChild(forumRoutes)],
  exports: [RouterModule],
})
export class ForumRoutingModule {}
