export const ActionType = {
  ACCEPT_APPLICANT: 'applicants.accept',
  ADD_AUTOUPLOAD: 'auto.upload.add',
  ADD_CATEGORY: 'category.add',
  ADD_CATEGORY_LOGO: 'category.admin.logo.upload',
  ADD_GROUP_EXTERNAL_REPO: 'admin.external.repo.add',
  ADD_GROUP_LOGO: 'group.admin.logo.upload',
  ADD_HEADERS: 'headers.add',
  ADD_HELP_ARTICLE: 'help.add.article',
  ADD_HELP_SECTION: 'help.add.section',
  ADD_HELP_LINK: 'help.add.link',
  ADD_KEYWORD: 'keyword.add',
  ADD_MEMBERSHIPS: 'memberships.add',
  ADD_NOTIFICATIONS: 'notifications.subscription.add',
  ADD_PERMISSIONS: 'permissions.configuration.add',
  ADD_SHARED_SPACE_LINK: 'shared.space.link.add',
  ADD_TRANSLATION: 'translations.add.translation',
  ADD_URL: 'url.add',
  APPLY_FOR_MEMBERSHIP: 'members.apply.membership',
  APPROVE_POST: 'post.approve',
  APPROVE_REQUEST_INTEREST_GROUP: 'group.request.approval.send',
  CANCEL_CHECKOUT: 'cancel.checkout',
  CHANGE_PROFILE: 'members.change.profile',
  CHANGE_PROFILES: 'members.change.profiles',
  CHANGE_SUBSCRIPTION: 'notifications.subscription.change',
  CHECKIN: 'checkin',
  CHECKOUT: 'checkout',
  CLIPBOARD_COPY_NODE: 'clipboard.copy.node',
  CLIPBOARD_LINK_NODE: 'clipboard.link.node',
  CLIPBOARD_MOVE_NODE: 'clipboard.move.node',
  CONTACT_LEADERS: 'members.leaders.contact',
  CONTACT_CATEGORY_ADMINS: 'global.category.admin.contact',
  CONTACT_HELPDESK: 'help.support.contact',
  CREATE_APP_MESSAGE_TEMPLATE: 'admin.system.message.create',
  CREATE_CATEGORY: 'admin.category.create',
  CREATE_DYNAMIC_PROPERTY: 'dynamic-property.create',
  CREATE_EVENT: 'event.create',
  CREATE_FORUM: 'forum.create',
  CREATE_INFORMATION_NEWS: 'information.news.create',
  CREATE_INTEREST_GROUP: 'admin.group.create',
  CREATE_INTEREST_GROUP_EXISTS: 'admin.group.create.exists',
  CREATE_KEYWORD: 'keyword.create',
  CREATE_POST: 'post.create',
  CREATE_PROFILE: 'profile.create',
  CREATE_SPACE: 'space.create',
  CREATE_TOPIC: 'topic.create',
  CREATE_USER: 'user.create',
  DECLINE_APPLICANT: 'applicants.decline',
  DELETE_APP_MESSAGE_TEMPLATE: 'admin.system.message.delete',
  DELETE_ALL: 'action.delete-all',
  DELETE_CATEGORY_LOGO: 'category.admin.logo.delete',
  DELETE_CATEGORY_ADMIN: 'admin.category.admin.delete',
  DELETE_CONTENT: 'content.delete',
  DELETE_DYNAMIC_PROPERTY: 'dynamic-property.delete',
  DELETE_EVENT: 'event.delete',
  DELETE_FORUM: 'forum.delete',
  DELETE_GROUP: 'admin.delete.group',
  DELETE_GROUP_LOGO: 'group.admin.logo.delete',
  DELETE_GROUP_EXTERNAL_REPO: 'admin.external.repo.delete',
  DELETE_HELP_ARTICLE: 'help.delete.article',
  DELETE_HELP_SECTION: 'help.delete.section',
  DELETE_HELP_LINK: 'help.delete.link',
  DELETE_INFORMATION_NEWS: 'information.news.delete',
  DELETE_KEYWORD: 'keyword.delete',
  DELETE_NOTIFICATION: 'notifications.subscription.delete',
  DELETE_POST: 'post.delete',
  DELETE_ABUSE: 'post.abuse.delete',
  DELETE_PERMISSION: 'permissions.action.delete',
  DELETE_PROFILE: 'profile.delete',
  DELETE_SPACE: 'space.delete',
  DELETE_TOPIC: 'topic.delete',
  EDIT_EXPIRATION: 'expiration.edit',
  EDIT_POST: 'post.edit',
  EDIT_PROFILE: 'profile.edit',
  EDIT_TOPIC: 'forums.topic.edit',
  EMAIL_CONTENT: 'email.content',
  EMAIL_CONTENTS: 'email.contents',
  ENABLE_MULTILINGUAL: 'translations.make.multilingual',
  IMPORT_KEYWORD: 'keyword.import',
  IMPORT_PROFILE: 'profile.import',
  IMPORT_ZIP: 'import.zip',
  IMPORT_ZIP_EXISTS: 'import.zip.exists',
  IMPORT_ZIP_BIG: 'import.zip.big',
  INVITE_CATEGORY_ADMIN: 'admin.category.invite.admins',
  INVITE_CIRCABC_ADMIN: 'admin.invite.circabc.admin',
  MAIL_TO_MEMBERS: 'members.mail.send',
  MODERATE_FORUM: 'forum.moderate',
  PURGE_CONTENT: 'admin.deleted.items.purge',
  REJECT_POST: 'post.reject',
  REMOVE_AUTOUPLOAD: 'auto.upload.remove',
  REMOVE_MEMBERSHIP: 'memberships.remove-member',
  REMOVE_MEMBERSHIPS: 'memberships.remove-members',
  REMOVE_PERMISSION: 'permissions.configuration.delete',
  REQUEST_INTEREST_GROUP: 'group.request',
  REQUEST_DELETE_GROUP: 'request.delete.group',
  REQUEST_MACHINE_TRANSLATION: 'translations.request.machine.translation',
  RESTORE_CONTENT: 'admin.deleted.items.restore',
  RESTORE_PREVIOUS_MEMBERSHIP: 'memberships.restore.user',
  SAVE_PASTE_NOTIFICATIONS: 'admin.paste.notifications.save',
  SCHEDULE_REVOCATION_MEMBERSHIP: 'memberships.revocation.schedule',
  SELECT_CATEGORY_LOGO: 'admin.logo.selection',
  SET_PERMISSION: 'set.permission',
  SHARE_SPACE: 'spaces.sharing.share',
  SHARE_SPACE_CHANGE_PERMISSION: 'spaces.sharing.share.change.permission',
  SIGNAL_ABUSE_POST: 'post.post.signal.abuse',
  TAKE_OWNERSHIP: 'content.take.ownership',
  TOGGLE_AUTOUPLOAD: 'auto.upload.toggle',
  TOGGLE_INHERITANCE: 'permissions.inheritance.toggle',
  UPDATE_APP_MESSAGE_TEMPLATE: 'admin.system.message.update',
  UPDATE_ACCOUNT: 'account.update',
  UPDATE_AUTOUPLOAD: 'auto.upload.update',
  UPDATE_CATEGORY: 'admin.category.update',
  UPDATE_CONTENT_PROPERTIES: 'content.update-properties',
  UPDATE_DYNAMIC_PROPERTIES: 'dynamic-property.udpate',
  UPDATE_EVENT: 'event.update',
  UPDATE_FILE_CONTENT: 'content.update-file',
  UPDATE_FORUM: 'forum.update',
  UPDATE_GROUP_CONFIGURATION: 'group.update.configuration',
  UPDATE_HELP_ARTICLE: 'help.update.article',
  UPDATE_HELP_SECTION: 'help.update.section',
  UPDATE_HELP_LINK: 'help.update.link',
  UPDATE_INFORMATION_CONFIGURATION: 'information.configuration.update',
  UPDATE_INFORMATION_NEWS: 'information.news.update',
  UPDATE_INTEREST_GROUP: 'group.update',
  UPDATE_KEYWORD: 'keyword.update',
  UPDATE_SHOW_OLD_MESSAGE_CONFIG:
    'admin.system.message.old.display.new.ui.config',
  UPDATE_OLD_APP_MESSAGE_CONFIG: 'admin.system.message.old.display.config',
  UPDATE_OLD_APP_MESSAGE: 'admin.system.message.old.update',
  UPDATE_REQUEST_INTEREST_GROUP: 'group.request.update',
  UPDATE_SPACE_PROPERTIES: 'space.update-properties',
  UPDATE_TOPIC: 'topic.update',
  UPLOAD_FILE: 'action.upload-files',
} as const;

export type ActionType = (typeof ActionType)[keyof typeof ActionType];
