/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.service.customisation.mail;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import org.alfresco.service.cmr.repository.TemplateService;
import org.springframework.extensions.surf.util.I18NUtil;

import java.text.MessageFormat;

/**
 * Define all email templates available.
 *
 * @author yanick Pignot
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 I18NUtil was moved to Spring. This class seems to be
 * developed for CircaBC
 */
public enum MailTemplate {
    MAIL_ME_CONTENT {
        public String getTemplateDirectoryName() {
            return "mailMeContent";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "mail_me_content_subject", "${location.name}", CircabcConfiguration.getApplicationName());
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    MAIL_ME_CONTENT_URL {
        public String getTemplateDirectoryName() {
            return "mailMeContentUrl";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "mail_me_content_url_subject",
                    "${location.name}",
                    CircabcConfiguration.getApplicationName());
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    MAIL_ME_DOSSIER {
        public String getTemplateDirectoryName() {
            return "mailMeDossier";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "mail_me_dossier_subject", "${location.name}", CircabcConfiguration.getApplicationName());
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    NOTIFY_DOC {
        public String getTemplateDirectoryName() {
            return "notifyDoc";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "notification_message_subject",
                    "${location.name}",
                    CircabcConfiguration.getApplicationName());
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    NOTIFY_POST {
        public String getTemplateDirectoryName() {
            return "notifyPost";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "notification_message_post_subject",
                    "${titleOrName(location.parent)}",
                    CircabcConfiguration.getApplicationName());
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    INVITE_USER {
        public String getTemplateDirectoryName() {
            return "inviteUser";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "invite_circabc_user_template_subject_new",
                    "${titleOrName(interestGroup)}",
                    CircabcConfiguration.getApplicationName());
        }

        public boolean multipleAllowed() {
            return true;
        }
    },
    UPDATE_USER_PROFILE {
        public String getTemplateDirectoryName() {
            return "updateUserProfile";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "update_circabc_user_profile_template_subject_new",
                    "${titleOrName(interestGroup)}",
                    CircabcConfiguration.getApplicationName());
        }

        public boolean multipleAllowed() {
            return true;
        }
    },
    APPLY_FOR_MEMBERSHIP {
        public String getTemplateDirectoryName() {
            return "applyForMembership";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "apply_application_mail_template_subject",
                    "${titleOrName(interestGroup)}",
                    CircabcConfiguration.getApplicationName());
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    REFUSE_APPLICATION {
        public String getTemplateDirectoryName() {
            return "refuseApplication";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "reject_application_mail_template_subject",
                    "${titleOrName(interestGroup)}",
                    CircabcConfiguration.getApplicationName());
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    SEND_TO_MEMBERS {
        public String getTemplateDirectoryName() {
            return "sendToMembers";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return "";
        }

        public boolean multipleAllowed() {
            return true;
        }
    },
    SHARE_SPACE_NOTIFICATION {
        public String getTemplateDirectoryName() {
            return "shareSpace";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "share_space_subject",
                    "${circabcPath(location)}",
                    CircabcConfiguration.getApplicationName());
        }

        public boolean multipleAllowed() {
            return true;
        }
    },
    SHARE_SPACE_PERMISSION_UPDATE_NOTIFICATION {
        public String getTemplateDirectoryName() {
            return "shareSpacePermissionUpdate";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "share_space_permission_update_subject",
                    "${circabcPath(location)}",
                    CircabcConfiguration.getApplicationName());
        }

        public boolean multipleAllowed() {
            return true;
        }
    },
    EVENT_CREATE_NOTIFICATION {
        public String getTemplateDirectoryName() {
            return "eventCreateNotification";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "create_event_audience_notification_subject",
                    "${appointment.title}",
                    CircabcConfiguration.getApplicationName());
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    EVENT_UPDATE_NOTIFICATION {
        public String getTemplateDirectoryName() {
            return "eventUpdateNotification";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "update_event_audience_notification_subject",
                    "${appointment.title}",
                    CircabcConfiguration.getApplicationName());
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    EVENT_DELETE_NOTIFICATION {
        public String getTemplateDirectoryName() {
            return "eventDeleteNotification";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "delete_event_audience_notification_subject",
                    "${appointment.title}",
                    CircabcConfiguration.getApplicationName());
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    EVENT_REMINDER {
        public String getTemplateDirectoryName() {
            return "eventReminder";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "event_author_reminder_subject",
                    "${appointment.title}",
                    CircabcConfiguration.getApplicationName());
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    EVENT_DELETE_REMINDER {
        public String getTemplateDirectoryName() {
            return "eventDeleteReminder";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "event_author_reminder_subject",
                    "${appointment.title}",
                    CircabcConfiguration.getApplicationName());
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    MEETING_REMINDER {
        public String getTemplateDirectoryName() {
            return "meetingReminder";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "meeting_author_reminder_subject",
                    "${appointment.title}",
                    CircabcConfiguration.getApplicationName());
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    MEETING_DELETE_REMINDER {
        public String getTemplateDirectoryName() {
            return "meetingDeleteReminder";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "meeting_author_reminder_subject",
                    "${appointment.title}",
                    CircabcConfiguration.getApplicationName());
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    REJECT_POST {
        public String getTemplateDirectoryName() {
            return "rejectPost";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("reject_post_mail_subject", CircabcConfiguration.getApplicationName());
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    SIGNAL_ABUSE {
        public String getTemplateDirectoryName() {
            return "signalAbuse";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("signal_abuse_mail_subject", CircabcConfiguration.getApplicationName());
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    SELF_REGISTRATION {
        public String getTemplateDirectoryName() {
            return "selfRegistration";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("self_registration_mail_subject");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    RESEND_OWN_PASSWORD {
        public String getTemplateDirectoryName() {
            return "resendOwnPwd";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("resend_password_mail_subject");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    RESEND_OTHER_PASSWORD {
        public String getTemplateDirectoryName() {
            return "resendOtherPwd";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("change_other_password_mail_subject");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    AUTO_UPLOAD_SUCCESS {
        public String getTemplateDirectoryName() {
            return "autoUploadSuccess";
        }

        public String getDefaultTemplateName() {
            return "success.ftl";
        }

        public String getDefaultSubject() {
            return translate("auto_upload_mail_template_success_title");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    AUTO_UPLOAD_ERROR {
        public String getTemplateDirectoryName() {
            return "autoUploadError";
        }

        public String getDefaultTemplateName() {
            return "error.ftl";
        }

        public String getDefaultSubject() {
            return translate("auto_upload_mail_template_error_title");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    AUTO_UPLOAD_FTP_PROBLEM {
        public String getTemplateDirectoryName() {
            return "autoUploadFtpProblem";
        }

        public String getDefaultTemplateName() {
            return "ftp-problem.ftl";
        }

        public String getDefaultSubject() {
            return translate("auto_upload_mail_template_ftp_problem_title");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    SUPPORT_REQUEST {
        public String getTemplateDirectoryName() {
            return "supportRequest";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("mail_template_support_contact_title");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    ADD_MEMBERSHIP_NOTIFICATION {
        public String getTemplateDirectoryName() {
            return "addMembershipNotification";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "add_membership_notification_subject",
                    "${fullName(person)}",
                    "${titleOrName(interestGroup)}");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    UPDATE_MEMBERSHIP_NOTIFICATION {
        public String getTemplateDirectoryName() {
            return "updateMembershipNotification";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "update_membership_notification_subject_new",
                    "${fullName(person)}",
                    "${titleOrName(interestGroup)}");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    REFUSE_APLICANT_NOTIFICATION {
        public String getTemplateDirectoryName() {
            return "refuseAplicantNotification";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "refuse_applicant_notification_subject",
                    "${fullName(person)}",
                    "${titleOrName(interestGroup)}");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    REMOVE_MEMBERSHIP_NOTIFICATION {
        public String getTemplateDirectoryName() {
            return "removeMembershipNotification";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "remove_membership_notification_subject",
                    "${fullName(person)}",
                    "${titleOrName(interestGroup)}");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    SUCCESSFUL_DOCUMENT_TRANSLATION {
        public String getTemplateDirectoryName() {
            return "successfulDocumentTranslation";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("machine_translate_doc_success_message_subject");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    SUCCESSFUL_PROPERTY_TRANSLATION {
        public String getTemplateDirectoryName() {
            return "successfulPropertyTranslation";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("machine_translate_prop_success_message_subject");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    UNSUCCESSFUL_DOCUMENT_TRANSLATION {
        public String getTemplateDirectoryName() {
            return "unsuccessfulDocumentTranslation";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("machine_translate_doc_error_message_subject");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    UNSUCCESSFUL_PROPERTY_TRANSLATION {
        public String getTemplateDirectoryName() {
            return "unsuccessfulPropertyTranslation";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("machine_translate_prop_error_message_subject");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    ADD_NEW_TRANSLATION_EDITION {
        public String getTemplateDirectoryName() {
            return "addNewTranslationEdition";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("add_new_translation_edition");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    CHANGE_PERMISSION {
        public String getTemplateDirectoryName() {
            return "changeUserOrGroupPermission";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("change_permission_group_user_template_mail_subject");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    GROUP_REQUEST {
        public String getTemplateDirectoryName() {
            return "requestInterestGroup";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("group_request_template_mail_subject");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    CATEGORY_ADMIN_CONTACT {
        public String getTemplateDirectoryName() {
            return "categoryAdminContact";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("category_admin_contact_template_mail_subject");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    CATEGORY_ADMIN_CONTACT_CONFIRMATION {
        public String getTemplateDirectoryName() {
            return "categoryAdminContactConfirmation";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("confirmation_message");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    NOTIFY_NEWS {
        public String getTemplateDirectoryName() {
            return "notifyNews";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate(
                    "notification_message_news_subject", CircabcConfiguration.getApplicationName());
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    GROUP_MEMBERS_CONTACT {
        public String getTemplateDirectoryName() {
            return "groupMembersContact";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("group_members_contact_template_mail_subject");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    GROUP_LEADERS_CONTACT {
        public String getTemplateDirectoryName() {
            return "groupLeadersContact";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("group_Leaders_contact_template_mail_subject");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    HELPDESK_CONTACT {
        public String getTemplateDirectoryName() {
            return "helpdeskContact";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("help_desk_contact_template_mail_subject");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    HELPDESK_CONTACT_CONFIRMATION {
        public String getTemplateDirectoryName() {
            return "helpdeskConfirmation";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("help_desk_contact_template_mail_confirmation_subject");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    CATEGORY_GROUP_REQUEST_REFUSE {
        public String getTemplateDirectoryName() {
            return "categoryGroupRequest";
        }

        public String getDefaultTemplateName() {
            return "refusal.ftl";
        }

        public String getDefaultSubject() {
            return translate("category_group_request_refusal_template_mail_subject");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    CATEGORY_GROUP_REQUEST_ACCEPT {
        public String getTemplateDirectoryName() {
            return "categoryGroupRequest";
        }

        public String getDefaultTemplateName() {
            return "acceptation.ftl";
        }

        public String getDefaultSubject() {
            return translate("category_group_request_acceptation_template_mail_subject");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    NOTIFY_DOC_BULK {
        public String getTemplateDirectoryName() {
            return "notifyDocumentsBulk";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("new_documents_bulk_template_mail_subject");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    NOTIFY_MOVE_BULK {
        public String getTemplateDirectoryName() {
            return "notifyMoveDocumentsBulk";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("move_documents_bulk_template_mail_subject");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    ADMIN_DISTRIBUTION_LIST {
        public String getTemplateDirectoryName() {
            return "adminDistributionList";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("distribution_list_mail_subject");
        }

        public boolean multipleAllowed() {
            return false;
        }
    },
    NOTIFY_SYSTEM_MESSAGE {
        public String getTemplateDirectoryName() {
            return "notifySystemMessage";
        }

        public String getDefaultTemplateName() {
            return "default.ftl";
        }

        public String getDefaultSubject() {
            return translate("notify_system_message_subject");
        }

        public boolean multipleAllowed() {
            return false;
        }
    };

    public static final String KEY_ME = "me";
    public static final String KEY_PERSON = TemplateService.KEY_PERSON;
    public static final String KEY_DATE = TemplateService.KEY_DATE;
    public static final String KEY_IMAGE_RESOLVER = TemplateService.KEY_IMAGE_RESOLVER;
    public static final String KEY_COMPANY_HOME = TemplateService.KEY_COMPANY_HOME;
    public static final String KEY_USER_HOME = TemplateService.KEY_USER_HOME;
    public static final String KEY_LOCATION = "location";
    public static final String KEY_SPACE = "space";
    public static final String KEY_DOCUMENT = "document";
    public static final String KEY_CIRCABC = "circabc";
    public static final String KEY_INTEREST_GROUP = "interestGroup";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_APPOINTMENT = "appointment";
    public static final String KEY_EVENT_SERVICE = "eventService";
    public static final String KEY_APPOINTMENT_FIRST_OCCURENCE = "appointmentFirstOccurence";
    public static final String KEY_APPOINTMENT_ID = "appointmentId";
    public static final String KEY_PERMISSION = "permission";
    public static final String KEY_REJECT_DATE = "rejectDate";
    public static final String KEY_REJECT_REASON = "rejectReason";
    public static final String KEY_REJECTED_CONTENT = "rejectedContent";
    public static final String KEY_ABUSE_DATE = "abuseDate";
    public static final String KEY_ABUSE_REASON = "abuseReason";

    public static MailTemplate getMailTemplateForFolderName(final String folder) {
        MailTemplate result = null;

        if (folder != null) {
            final MailTemplate[] templates = values();
            for (final MailTemplate template : templates) {
                if (template.getTemplateDirectoryName().equals(folder)) {
                    result = template;
                    break;
                }
            }
        }

        return result;
    }

    public static String translate(final String key, final Object... params) {
        if (params == null || params.length < 1) {
            return I18NUtil.getMessage(key);
        } else {
            return MessageFormat.format(I18NUtil.getMessage(key), params);
        }
    }

    public abstract String getTemplateDirectoryName();

    public abstract String getDefaultTemplateName();

    public abstract String getDefaultSubject();

    public abstract boolean multipleAllowed();
}
