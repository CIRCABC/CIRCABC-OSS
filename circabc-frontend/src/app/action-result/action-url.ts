import { ActionType } from 'app/action-result/action-type';

interface ActionUrlRegexMethod {
  urlRegex: RegExp;
  method: 'GET' | 'PUT' | 'POST' | 'DELETE';
  actionType: ActionType;
  show: boolean;
}

export class ActionUrl {
  private static availableActions: ActionUrlRegexMethod[] = [
    {
      urlRegex: /\/autoupload/,
      method: 'POST',
      actionType: ActionType.ADD_AUTOUPLOAD,
      show: false,
    },
    {
      urlRegex: /\/categories\/[a-f0-9-]{36}\/logos/,
      method: 'POST',
      actionType: ActionType.ADD_CATEGORY_LOGO,
      show: true,
    },
    {
      urlRegex: /\/groups\/[a-f0-9-]{36}\/logos/,
      method: 'POST',
      actionType: ActionType.ADD_GROUP_LOGO,
      show: true,
    },
    {
      urlRegex: /\/groups\/[a-f0-9-]{36}\/logos/,
      method: 'DELETE',
      actionType: ActionType.DELETE_GROUP_LOGO,
      show: true,
    },
    {
      urlRegex: /\/groups\/[a-f0-9-]{36}\/repositories/,
      method: 'POST',
      actionType: ActionType.ADD_GROUP_EXTERNAL_REPO,
      show: true,
    },

    {
      urlRegex: /\/groups\/[a-f0-9-]{36}\/repositories/,
      method: 'DELETE',
      actionType: ActionType.DELETE_GROUP_EXTERNAL_REPO,
      show: true,
    },
    {
      urlRegex: /\/groups\/[a-f0-9-]{36}\?purgedata=/,
      method: 'DELETE',
      actionType: ActionType.DELETE_GROUP,
      show: true,
    },
    {
      urlRegex: /\/headers\/[a-f0-9-]{36}\/categories/,
      method: 'POST',
      actionType: ActionType.ADD_CATEGORY,
      show: true,
    },
    {
      urlRegex: /\/headers/,
      method: 'POST',
      actionType: ActionType.ADD_HEADERS,
      show: true,
    },
    {
      urlRegex: /\/help\/categories\/[a-f0-9-]{36}\/articles/,
      method: 'POST',
      actionType: ActionType.ADD_HELP_ARTICLE,
      show: true,
    },
    {
      urlRegex: /\/help\/categories/,
      method: 'POST',
      actionType: ActionType.ADD_HELP_CATEGORY,
      show: true,
    },
    {
      urlRegex: /\/help\/links/,
      method: 'POST',
      actionType: ActionType.ADD_HELP_LINK,
      show: true,
    },
    {
      urlRegex: /\/keywords/,
      method: 'POST',
      actionType: ActionType.ADD_KEYWORD,
      show: false,
    },
    {
      urlRegex: /\/nodes\/[a-f0-9-]{36}\/notifications/,
      method: 'POST',
      actionType: ActionType.ADD_NOTIFICATIONS,
      show: true,
    },
    {
      urlRegex: /\/permissions/,
      method: 'PUT',
      actionType: ActionType.ADD_PERMISSIONS,
      show: false,
    },
    {
      urlRegex: /\/exportedshares/,
      method: 'POST',
      actionType: ActionType.ADD_SHARED_SPACE_LINK,
      show: true,
    },
    {
      urlRegex: /\/translations$/,
      method: 'POST',
      actionType: ActionType.ADD_TRANSLATION,
      show: true,
    },
    {
      urlRegex: /\/url/,
      method: 'POST',
      actionType: ActionType.ADD_URL,
      show: true,
    },
    {
      urlRegex: /\/groups\/[a-f0-9-]{36}\/members$/,
      method: 'DELETE',
      actionType: ActionType.REMOVE_MEMBERSHIP,
      show: false,
    },
    {
      urlRegex: /\/groups\/[a-f0-9-]{36}\/members$/,
      method: 'PUT',
      actionType: ActionType.CHANGE_PROFILE,
      show: true,
    },
    {
      urlRegex: /\/members$/,
      method: 'POST',
      actionType: ActionType.ADD_MEMBERSHIPS,
      show: false,
    },
    {
      urlRegex: /\/groups\/[a-f0-9-]{36}\/members\/applicants/,
      method: 'POST',
      actionType: ActionType.APPLY_FOR_MEMBERSHIP,
      show: true,
    },
    {
      urlRegex: /\/check/,
      method: 'DELETE',
      actionType: ActionType.CANCEL_CHECKOUT,
      show: true,
    },
    {
      urlRegex: /\/nodes\/[a-f0-9-]{36}\/notifications/,
      method: 'PUT',
      actionType: ActionType.CHANGE_SUBSCRIPTION,
      show: true,
    },
    {
      urlRegex: /\/check/,
      method: 'PUT',
      actionType: ActionType.CHECKIN,
      show: true,
    },
    {
      urlRegex: /\/check/,
      method: 'POST',
      actionType: ActionType.CHECKOUT,
      show: true,
    },
    {
      urlRegex: /\/nodes\/[a-f0-9-]{36}\/paste/,
      method: 'POST',
      actionType: ActionType.CLIPBOARD_COPY_NODE,
      show: true,
    },
    {
      urlRegex: /\/nodes\/[a-f0-9-]{36}\/link/,
      method: 'POST',
      actionType: ActionType.CLIPBOARD_LINK_NODE,
      show: true,
    },
    {
      urlRegex: /\/nodes\/[a-f0-9-]{36}\/paste/,
      method: 'PUT',
      actionType: ActionType.CLIPBOARD_MOVE_NODE,
      show: true,
    },
    {
      urlRegex: /\/headers\/[a-f0-9-]{36}\/categories/,
      method: 'POST',
      actionType: ActionType.CREATE_CATEGORY,
      show: true,
    },
    {
      urlRegex: /\/groups\/[a-f0-9-]{36}\/dynprops/,
      method: 'POST',
      actionType: ActionType.CREATE_DYNAMIC_PROPERTY,
      show: true,
    },
    {
      urlRegex: /\/groups\/[a-f0-9-]{36}\/events/,
      method: 'POST',
      actionType: ActionType.CREATE_EVENT,
      show: true,
    },
    {
      urlRegex: /\/groups\/[a-f0-9-]{36}\/information/,
      method: 'PUT',
      actionType: ActionType.UPDATE_INFORMATION_CONFIGURATION,
      show: true,
    },
    {
      urlRegex: /\/subforums/,
      method: 'POST',
      actionType: ActionType.CREATE_FORUM,
      show: true,
    },
    {
      urlRegex: /\/news\/[a-f0-9-]{36}/,
      method: 'PUT',
      actionType: ActionType.UPDATE_INFORMATION_NEWS,
      show: true,
    },
    {
      urlRegex: /\/information\/news/,
      method: 'POST',
      actionType: ActionType.CREATE_INFORMATION_NEWS,
      show: true,
    },
    {
      urlRegex: /\/categories\/[a-f0-9-]{36}\/groups/,
      method: 'POST',
      actionType: ActionType.CREATE_INTEREST_GROUP,
      show: false,
    },
    {
      urlRegex: /\/groups\/[a-f0-9-]{36}\/keywords/,
      method: 'POST',
      actionType: ActionType.CREATE_KEYWORD,
      show: true,
    },
    {
      urlRegex: /\/topics\/[a-f0-9-]{36}\/replies/,
      method: 'POST',
      actionType: ActionType.CREATE_POST,
      show: true,
    },
    {
      urlRegex: /\/groups\/[a-f0-9-]{36}\/profiles/,
      method: 'POST',
      actionType: ActionType.CREATE_PROFILE,
      show: true,
    },
    {
      urlRegex: /\/spaces\/[a-f0-9-]{36}\/subspaces/,
      method: 'POST',
      actionType: ActionType.CREATE_SPACE,
      show: true,
    },
    {
      urlRegex: /\/forums\/[a-f0-9-]{36}\/content/,
      method: 'POST',
      actionType: ActionType.CREATE_TOPIC,
      show: true,
    },
    {
      urlRegex: /\/content\/[a-f0-9-]{36}\/topics/,
      method: 'POST',
      actionType: ActionType.CREATE_TOPIC,
      show: true,
    },
    {
      urlRegex: /\/groups\/[a-f0-9-]{36}\/members\/applicants\?action=clean/,
      method: 'PUT',
      actionType: ActionType.ACCEPT_APPLICANT,
      show: true,
    },
    {
      urlRegex: /\/groups\/[a-f0-9-]{36}\/members\/applicants\?action=decline/,
      method: 'PUT',
      actionType: ActionType.DECLINE_APPLICANT,
      show: true,
    },
    {
      urlRegex: /\/DELETE_ALL\//,
      method: 'DELETE',
      actionType: ActionType.DELETE_ALL,
      show: false,
    },
    {
      urlRegex: /\/categories\/[a-f0-9-]{36}\/admins\/[a-f0-9-]{36}/,
      method: 'DELETE',
      actionType: ActionType.DELETE_CATEGORY_ADMIN,
      show: true,
    },
    {
      urlRegex: /\/categories\/[a-f0-9-]{36}\/logos\/[a-f0-9-]{36}/,
      method: 'DELETE',
      actionType: ActionType.DELETE_CATEGORY_LOGO,
      show: true,
    },
    {
      urlRegex: /\/categories\/[a-f0-9-]{36}\/contact/,
      method: 'POST',
      actionType: ActionType.CONTACT_CATEGORY_ADMINS,
      show: true,
    },
    {
      urlRegex: /\/content\/[a-f0-9-]{36}/,
      method: 'DELETE',
      actionType: ActionType.DELETE_CONTENT,
      show: false,
    },
    {
      urlRegex: /\/dynprops\//,
      method: 'DELETE',
      actionType: ActionType.DELETE_DYNAMIC_PROPERTY,
      show: true,
    },
    {
      urlRegex: /\/events\//,
      method: 'DELETE',
      actionType: ActionType.DELETE_EVENT,
      show: true,
    },
    {
      urlRegex: /\/forums\//,
      method: 'DELETE',
      actionType: ActionType.DELETE_FORUM,
      show: true,
    },
    {
      urlRegex: /\/help\/articles/,
      method: 'DELETE',
      actionType: ActionType.DELETE_HELP_ARTICLE,
      show: true,
    },
    {
      urlRegex: /\/help\/categories/,
      method: 'DELETE',
      actionType: ActionType.DELETE_HELP_CATEGORY,
      show: true,
    },
    {
      urlRegex: /\/help\/links/,
      method: 'DELETE',
      actionType: ActionType.DELETE_HELP_LINK,
      show: true,
    },
    {
      urlRegex: /\/news\//,
      method: 'DELETE',
      actionType: ActionType.DELETE_INFORMATION_NEWS,
      show: true,
    },
    {
      urlRegex: /\/keywords\//,
      method: 'DELETE',
      actionType: ActionType.DELETE_KEYWORD,
      show: false,
    },
    {
      urlRegex: /\/notifications\//,
      method: 'DELETE',
      actionType: ActionType.DELETE_NOTIFICATION,
      show: true,
    },
    {
      urlRegex: /\/posts\/[a-f0-9-]{36}\/abuse/,
      method: 'DELETE',
      actionType: ActionType.DELETE_ABUSE,
      show: true,
    },
    {
      urlRegex: /\/posts\//,
      method: 'DELETE',
      actionType: ActionType.DELETE_POST,
      show: false,
    },
    {
      urlRegex: /\/profiles\//,
      method: 'DELETE',
      actionType: ActionType.DELETE_PROFILE,
      show: true,
    },
    {
      urlRegex: /\/spaces\//,
      method: 'DELETE',
      actionType: ActionType.DELETE_SPACE,
      show: false,
    },
    {
      urlRegex: /\/topics\//,
      method: 'DELETE',
      actionType: ActionType.DELETE_TOPIC,
      show: true,
    },
    {
      urlRegex: /\/posts\/[a-f0-9-]{36}\/verify\?approve=true/,
      method: 'PUT',
      actionType: ActionType.APPROVE_POST,
      show: true,
    },
    {
      urlRegex: /\/posts\/[a-f0-9-]{36}\/verify\?approve=false/,
      method: 'PUT',
      actionType: ActionType.REJECT_POST,
      show: true,
    },
    {
      urlRegex: /\/posts\//,
      method: 'PUT',
      actionType: ActionType.EDIT_POST,
      show: true,
    },
    {
      urlRegex: /\/profiles\//,
      method: 'PUT',
      actionType: ActionType.EDIT_PROFILE,
      show: true,
    },
    {
      urlRegex: /\/topics\//,
      method: 'PUT',
      actionType: ActionType.EDIT_TOPIC,
      show: true,
    },
    {
      urlRegex: /\/content\/[a-f0-9-]{36}\/email/,
      method: 'POST',
      actionType: ActionType.EMAIL_CONTENT,
      show: false,
    },
    {
      urlRegex: /\/content\/[a-f0-9-]{36}\/multilingual\/aspect/,
      method: 'POST',
      actionType: ActionType.ENABLE_MULTILINGUAL,
      show: true,
    },
    {
      urlRegex: /\/imported\/profiles/,
      method: 'POST',
      actionType: ActionType.IMPORT_PROFILE,
      show: true,
    },
    {
      urlRegex: /\/groups\/import\//,
      method: 'POST',
      actionType: ActionType.IMPORT_ZIP,
      show: true,
    },
    {
      urlRegex: /\/categories\/[a-f0-9-]{36}\/admins/,
      method: 'POST',
      actionType: ActionType.INVITE_CATEGORY_ADMIN,
      show: true,
    },
    {
      urlRegex: /\/groups\/[a-f0-9-]{36}\/email/,
      method: 'POST',
      actionType: ActionType.MAIL_TO_MEMBERS,
      show: true,
    },
    {
      urlRegex: /\/moderation\//,
      method: 'POST',
      actionType: ActionType.MODERATE_FORUM,
      show: true,
    },
    {
      urlRegex: /\/documents\/deleted\//,
      method: 'DELETE',
      actionType: ActionType.PURGE_CONTENT,
      show: false,
    },
    {
      urlRegex: /\/verify/,
      method: 'PUT',
      actionType: ActionType.REJECT_POST,
      show: true,
    },
    {
      urlRegex: /\/autoupload/,
      method: 'DELETE',
      actionType: ActionType.REMOVE_AUTOUPLOAD,
      show: true,
    },
    {
      urlRegex: /\/nodes\/[a-f0-9-]{36}\/permissions\//,
      method: 'POST',
      actionType: ActionType.REMOVE_PERMISSION,
      show: true,
    },
    {
      urlRegex: /\/requests\/group\/[a-f0-9-]*\/approval/,
      method: 'POST',
      actionType: ActionType.APPROVE_REQUEST_INTEREST_GROUP,
      show: true,
    },
    {
      urlRegex: /\/requests\/group\/[a-f0-9-]*/,
      method: 'PUT',
      actionType: ActionType.UPDATE_REQUEST_INTEREST_GROUP,
      show: true,
    },
    {
      urlRegex: /\/requests\/group/,
      method: 'POST',
      actionType: ActionType.REQUEST_INTEREST_GROUP,
      show: true,
    },
    {
      urlRegex: /\/machinetranslation\//,
      method: 'POST',
      actionType: ActionType.REQUEST_MACHINE_TRANSLATION,
      show: true,
    },
    {
      urlRegex: /\/groups\/[a-f0-9-]{36}\/documents\/deleted/,
      method: 'POST',
      actionType: ActionType.RESTORE_CONTENT,
      show: false,
    },
    {
      urlRegex: /\/notifications\/status/,
      method: 'POST',
      actionType: ActionType.SAVE_PASTE_NOTIFICATIONS,
      show: true,
    },
    {
      urlRegex: /\/categories\/[a-f0-9-]{36}\/logos\/[a-f0-9-]{36}/,
      method: 'PUT',
      actionType: ActionType.SELECT_CATEGORY_LOGO,
      show: true,
    },
    {
      urlRegex: /\/spaces\/[a-f0-9-]{36}\/shares/,
      method: 'POST',
      actionType: ActionType.SHARE_SPACE,
      show: true,
    },
    {
      urlRegex: /\/spaces\/[a-f0-9-]{36}\/shares/,
      method: 'PUT',
      actionType: ActionType.SHARE_SPACE_CHANGE_PERMISSION,
      show: true,
    },
    {
      urlRegex: /\/posts\/[a-f0-9-]{36}\/abuse/,
      method: 'POST',
      actionType: ActionType.SIGNAL_ABUSE_POST,
      show: true,
    },
    {
      urlRegex: /\/nodes\/[a-f0-9-]{36}\/ownership/,
      method: 'PUT',
      actionType: ActionType.TAKE_OWNERSHIP,
      show: true,
    },
    {
      urlRegex: /\/groups\/[a-f0-9-]{36}\/autoupload/,
      method: 'PUT',
      actionType: ActionType.TOGGLE_AUTOUPLOAD,
      show: true,
    },
    {
      urlRegex: /\/nodes\/[a-f0-9-]{36}\/permissions/,
      method: 'PUT',
      actionType: ActionType.TOGGLE_INHERITANCE,
      show: true,
    },
    {
      urlRegex: /\/users\//,
      method: 'PUT',
      actionType: ActionType.UPDATE_ACCOUNT,
      show: false,
    },
    {
      urlRegex: /\/categories\/[a-f0-9-]{36}/,
      method: 'PUT',
      actionType: ActionType.UPDATE_CATEGORY,
      show: true,
    },
    {
      urlRegex: /\/content\/[a-f0-9-]{36}/,
      method: 'PUT',
      actionType: ActionType.UPDATE_CONTENT_PROPERTIES,
      show: true,
    },
    {
      urlRegex: /\/dynprops\/[a-f0-9-]{36}/,
      method: 'PUT',
      actionType: ActionType.UPDATE_DYNAMIC_PROPERTIES,
      show: true,
    },
    {
      urlRegex: /\/events\/[a-f0-9-]{36}/,
      method: 'PUT',
      actionType: ActionType.UPDATE_EVENT,
      show: true,
    },
    {
      urlRegex: /\/UPDATE_FILE_CONTENT\//,
      method: 'PUT',
      actionType: ActionType.UPDATE_FILE_CONTENT,
      show: false,
    },
    {
      urlRegex: /\/forums\/[a-f0-9-]{36}/,
      method: 'PUT',
      actionType: ActionType.UPDATE_FORUM,
      show: true,
    },
    {
      urlRegex: /\/groups\/[a-f0-9-]{36}\/configuration/,
      method: 'PUT',
      actionType: ActionType.UPDATE_GROUP_CONFIGURATION,
      show: true,
    },
    {
      urlRegex: /\/help\/articles/,
      method: 'PUT',
      actionType: ActionType.UPDATE_HELP_ARTICLE,
      show: true,
    },
    {
      urlRegex: /\/help\/categories/,
      method: 'PUT',
      actionType: ActionType.UPDATE_HELP_CATEGORY,
      show: true,
    },
    {
      urlRegex: /\/help\/links/,
      method: 'PUT',
      actionType: ActionType.UPDATE_HELP_LINK,
      show: true,
    },
    {
      urlRegex: /\/groups\/[a-f0-9-]{36}$/,
      method: 'PUT',
      actionType: ActionType.UPDATE_INTEREST_GROUP,
      show: true,
    },
    {
      urlRegex: /\/keywords\/[a-f0-9-]{36}/,
      method: 'PUT',
      actionType: ActionType.UPDATE_KEYWORD,
      show: true,
    },
    {
      urlRegex: /\/spaces\/[a-f0-9-]{36}/,
      method: 'PUT',
      actionType: ActionType.UPDATE_SPACE_PROPERTIES,
      show: true,
    },
    {
      urlRegex: /\/topics\/[a-f0-9-]{36}/,
      method: 'PUT',
      actionType: ActionType.UPDATE_TOPIC,
      show: true,
    },
    {
      urlRegex: /\/app\/messages\/templates\/[a-f0-9-]*/,
      method: 'PUT',
      actionType: ActionType.UPDATE_APP_MESSAGE_TEMPLATE,
      show: true,
    },
    {
      urlRegex: /\/app\/messages\/templates\/[a-f0-9-]*/,
      method: 'DELETE',
      actionType: ActionType.DELETE_APP_MESSAGE_TEMPLATE,
      show: true,
    },
    {
      urlRegex: /\/app\/messages\/templates/,
      method: 'POST',
      actionType: ActionType.CREATE_APP_MESSAGE_TEMPLATE,
      show: true,
    },
    {
      urlRegex: /\/app\/messages\/old\/config/,
      method: 'PUT',
      actionType: ActionType.UPDATE_OLD_APP_MESSAGE_CONFIG,
      show: true,
    },
    {
      urlRegex: /\/app\/messages\/config$/,
      method: 'PUT',
      actionType: ActionType.UPDATE_SHOW_OLD_MESSAGE_CONFIG,
      show: true,
    },
    {
      urlRegex: /\/app\/messages\/old$/,
      method: 'PUT',
      actionType: ActionType.UPDATE_OLD_APP_MESSAGE,
      show: true,
    },
    {
      urlRegex: /\/help\/support\/mail/,
      method: 'POST',
      actionType: ActionType.CONTACT_HELPDESK,
      show: true,
    },
    {
      urlRegex:
        /\/history\/.*\/groups\/[a-f0-9-]*\/recoverable\/profiles\/[a-f0-9-]*/,
      method: 'POST',
      actionType: ActionType.RESTORE_PREVIOUS_MEMBERSHIP,
      show: true,
    },
    {
      urlRegex: /\/history\/job\/memberships\/revocation$/,
      method: 'POST',
      actionType: ActionType.SCHEDULE_REVOCATION_MEMBERSHIP,
      show: true,
    },
  ];

  public static getActionType(
    url: string,
    method: 'GET' | 'PUT' | 'POST' | 'DELETE'
  ) {
    // eslint-disable-next-line

    const result = ActionUrl.availableActions.find(
      (item: ActionUrlRegexMethod) =>
        item.method === method && item.urlRegex.test(url)
    );
    if (result !== undefined && result.show === true) {
      return result.actionType;
    } else {
      return undefined;
    }
  }
}
