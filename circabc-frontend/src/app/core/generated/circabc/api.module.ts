import { NgModule, ModuleWithProviders, SkipSelf, Optional } from '@angular/core';
import { Configuration } from './configuration';
import { HttpClient } from '@angular/common/http';


import { AppMessageService } from './api/appMessage.service';
import { ArchiveService } from './api/archive.service';
import { AuditService } from './api/audit.service';
import { AutoUploadService } from './api/autoUpload.service';
import { CategoryService } from './api/category.service';
import { ContentService } from './api/content.service';
import { DashboardService } from './api/dashboard.service';
import { DynamicPropertiesService } from './api/dynamicProperties.service';
import { EmailService } from './api/email.service';
import { EventsService } from './api/events.service';
import { ExpiredService } from './api/expired.service';
import { ExternalRepositoryService } from './api/externalRepository.service';
import { FTPService } from './api/fTP.service';
import { FavouritesService } from './api/favourites.service';
import { FileService } from './api/file.service';
import { ForumService } from './api/forum.service';
import { GuardsService } from './api/guards.service';
import { HeaderService } from './api/header.service';
import { HelpService } from './api/help.service';
import { HistoryService } from './api/history.service';
import { InformationService } from './api/information.service';
import { InterestGroupService } from './api/interestGroup.service';
import { KeywordsService } from './api/keywords.service';
import { LoginService } from './api/login.service';
import { MembersService } from './api/members.service';
import { NodesService } from './api/nodes.service';
import { NotificationService } from './api/notification.service';
import { PermissionService } from './api/permission.service';
import { PostService } from './api/post.service';
import { ProfileService } from './api/profile.service';
import { SearchService } from './api/search.service';
import { SpaceService } from './api/space.service';
import { TopicService } from './api/topic.service';
import { TranslationsService } from './api/translations.service';
import { UserService } from './api/user.service';

@NgModule({
  imports:      [],
  declarations: [],
  exports:      [],
  providers: []
})
export class ApiModule {
    public static forRoot(configurationFactory: () => Configuration): ModuleWithProviders<ApiModule> {
        return {
            ngModule: ApiModule,
            providers: [ { provide: Configuration, useFactory: configurationFactory } ]
        };
    }

    constructor( @Optional() @SkipSelf() parentModule: ApiModule,
                 @Optional() http: HttpClient) {
        if (parentModule) {
            throw new Error('ApiModule is already loaded. Import in your base AppModule only.');
        }
        if (!http) {
            throw new Error('You need to import the HttpClientModule in your AppModule! \n' +
            'See also https://github.com/angular/angular/issues/20575');
        }
    }
}
