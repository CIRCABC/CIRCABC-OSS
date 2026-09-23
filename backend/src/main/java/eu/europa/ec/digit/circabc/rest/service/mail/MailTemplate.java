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
package eu.europa.ec.digit.circabc.rest.service.mail;

import java.text.MessageFormat;
import java.util.function.Supplier;
import org.alfresco.service.cmr.repository.TemplateService;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Enumeration of all e-mail templates available in CIRCABC.
 *
 * <p>Each constant describes a single notification/message type and carries the metadata required
 * to render and send the corresponding mail:
 *
 * <ul>
 *   <li>the name of the template directory holding the FreeMarker template(s);
 *   <li>the default template file name to use within that directory (defaults to {@code
 *       default.ftl});
 *   <li>whether multiple recipients/instances are allowed for this template;
 *   <li>a {@link java.util.function.Supplier} that lazily resolves the localized default subject
 *       line. Resolution is deferred (via a supplier) so that the {@link
 *       org.springframework.extensions.surf.util.I18NUtil} lookup happens at send time with the
 *       correct locale rather than at enum initialization time.
 * </ul>
 *
 * <p>Subject lines may embed FreeMarker placeholders (e.g. {@code ${location.name}}) that are later
 * expanded against the mail model.
 *
 * @author yanick Pignot
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 I18NUtil was moved to Spring. This class seems to be
 * developed for CircaBC
 */
public enum MailTemplate {
  NOTIFY_DOC("notifyDoc", () ->
    translate("notification_message_subject", C.LOCATION_NAME, C.CIRCABC_NAME)
  ),
  NOTIFY_POST("notifyPost", () ->
    translate(
      "notification_message_post_subject",
      "${titleOrName(location.parent)}",
      C.CIRCABC_NAME
    )
  ),
  INVITE_USER("inviteUser", true, () ->
    translate(
      "invite_circabc_user_template_subject_new",
      C.IG_TITLE,
      C.CIRCABC_NAME
    )
  ),
  UPDATE_USER_PROFILE("updateUserProfile", true, () ->
    translate(
      "update_circabc_user_profile_template_subject_new",
      C.IG_TITLE,
      C.CIRCABC_NAME
    )
  ),
  APPLY_FOR_MEMBERSHIP("applyForMembership", () ->
    translate(
      "apply_application_mail_template_subject",
      C.IG_TITLE,
      C.CIRCABC_NAME
    )
  ),
  REFUSE_APPLICATION("refuseApplication", () ->
    translate(
      "reject_application_mail_template_subject",
      C.IG_TITLE,
      C.CIRCABC_NAME
    )
  ),
  SHARE_SPACE_NOTIFICATION("shareSpace", true, () ->
    translate("share_space_subject", "${circabcPath(location)}", C.CIRCABC_NAME)
  ),
  SHARE_SPACE_PERMISSION_UPDATE_NOTIFICATION(
    "shareSpacePermissionUpdate",
    true,
    () ->
      translate(
        "share_space_permission_update_subject",
        "${circabcPath(location)}",
        C.CIRCABC_NAME
      )
  ),
  EVENT_CREATE_NOTIFICATION("eventCreateNotification", () ->
    translate(
      "create_event_audience_notification_subject",
      C.APPOINTMENT_TITLE,
      C.CIRCABC_NAME
    )
  ),
  EVENT_UPDATE_NOTIFICATION("eventUpdateNotification", () ->
    translate(
      "update_event_audience_notification_subject",
      C.APPOINTMENT_TITLE,
      C.CIRCABC_NAME
    )
  ),
  EVENT_DELETE_NOTIFICATION("eventDeleteNotification", () ->
    translate(
      "delete_event_audience_notification_subject",
      C.APPOINTMENT_TITLE,
      C.CIRCABC_NAME
    )
  ),
  EVENT_REMINDER("eventReminder", () ->
    translate(
      "event_author_reminder_subject",
      C.APPOINTMENT_TITLE,
      C.CIRCABC_NAME
    )
  ),
  EVENT_DELETE_REMINDER("eventDeleteReminder", () ->
    translate(
      "event_author_reminder_subject",
      C.APPOINTMENT_TITLE,
      C.CIRCABC_NAME
    )
  ),
  MEETING_REMINDER("meetingReminder", () ->
    translate(
      "meeting_author_reminder_subject",
      C.APPOINTMENT_TITLE,
      C.CIRCABC_NAME
    )
  ),
  MEETING_DELETE_REMINDER("meetingDeleteReminder", () ->
    translate(
      "meeting_author_reminder_subject",
      C.APPOINTMENT_TITLE,
      C.CIRCABC_NAME
    )
  ),
  REJECT_POST("rejectPost", () ->
    translate("reject_post_mail_subject", C.CIRCABC_NAME)
  ),
  SIGNAL_ABUSE("signalAbuse", () ->
    translate("signal_abuse_mail_subject", C.CIRCABC_NAME)
  ),
  AUTO_UPLOAD_SUCCESS("autoUploadSuccess", "success.ftl", false, () ->
    translate("auto_upload_mail_template_success_title")
  ),
  AUTO_UPLOAD_ERROR("autoUploadError", "error.ftl", false, () ->
    translate("auto_upload_mail_template_error_title")
  ),
  AUTO_UPLOAD_FTP_PROBLEM(
    "autoUploadFtpProblem",
    "ftp-problem.ftl",
    false,
    () -> translate("auto_upload_mail_template_ftp_problem_title")
  ),
  ADD_MEMBERSHIP_NOTIFICATION("addMembershipNotification", () ->
    translate(
      "add_membership_notification_subject",
      C.PERSON_FULL_NAME,
      C.IG_TITLE
    )
  ),
  UPDATE_MEMBERSHIP_NOTIFICATION("updateMembershipNotification", () ->
    translate(
      "update_membership_notification_subject_new",
      C.PERSON_FULL_NAME,
      C.IG_TITLE
    )
  ),
  SUCCESSFUL_DOCUMENT_TRANSLATION("successfulDocumentTranslation", () ->
    translate("machine_translate_doc_success_message_subject")
  ),
  SUCCESSFUL_PROPERTY_TRANSLATION("successfulPropertyTranslation", () ->
    translate("machine_translate_prop_success_message_subject")
  ),
  UNSUCCESSFUL_DOCUMENT_TRANSLATION("unsuccessfulDocumentTranslation", () ->
    translate("machine_translate_doc_error_message_subject")
  ),
  UNSUCCESSFUL_PROPERTY_TRANSLATION("unsuccessfulPropertyTranslation", () ->
    translate("machine_translate_prop_error_message_subject")
  ),
  ADD_NEW_TRANSLATION_EDITION("addNewTranslationEdition", () ->
    translate("add_new_translation_edition")
  ),
  GROUP_REQUEST("requestInterestGroup", () ->
    translate("group_request_template_mail_subject")
  ),
  GROUP_DELETE_REQUEST("requestDeleteInterestGroup", () ->
    translate("group_request_delete_template_mail_subject")
  ),
  GROUP_DELETE_REQUEST_LEADERS("requestDeleteInterestGroupLeaders", () ->
    translate("group_request_delete_template_mail_subject")
  ),
  CATEGORY_ADMIN_CONTACT("categoryAdminContact", () ->
    translate("category_admin_contact_template_mail_subject")
  ),
  CATEGORY_ADMIN_CONTACT_CONFIRMATION("categoryAdminContactConfirmation", () ->
    translate("confirmation_message")
  ),
  NOTIFY_NEWS("notifyNews", () ->
    translate("notification_message_news_subject", C.CIRCABC_NAME)
  ),
  GROUP_MEMBERS_CONTACT("groupMembersContact", () ->
    translate("group_members_contact_template_mail_subject")
  ),
  GROUP_LEADERS_CONTACT("groupLeadersContact", () ->
    translate("group_Leaders_contact_template_mail_subject")
  ),
  HELPDESK_CONTACT("helpdeskContact", () ->
    translate("help_desk_contact_template_mail_subject")
  ),
  HELPDESK_CONTACT_CONFIRMATION("helpdeskConfirmation", () ->
    translate("help_desk_contact_template_mail_confirmation_subject")
  ),
  CATEGORY_GROUP_REQUEST_REFUSE(
    "categoryGroupRequest",
    "refusal.ftl",
    false,
    () -> translate("category_group_request_refusal_template_mail_subject")
  ),
  CATEGORY_GROUP_REQUEST_ACCEPT(
    "categoryGroupRequest",
    "acceptation.ftl",
    false,
    () -> translate("category_group_request_acceptation_template_mail_subject")
  ),
  NOTIFY_DOC_BULK("notifyDocumentsBulk", () ->
    translate("new_documents_bulk_template_mail_subject")
  ),
  NOTIFY_MOVE_BULK("notifyMoveDocumentsBulk", () ->
    translate("move_documents_bulk_template_mail_subject")
  ),
  NOTIFY_DELETE_BULK("notifyDeleteDocumentsBulk", () ->
    translate("delete_documents_bulk_template_mail_subject")
  ),
  NOTIFY_EDIT_BULK("notifyEditDocumentsBulk", () ->
    translate("edit_documents_bulk_template_mail_subject")
  ),
  NOTIFY_SYSTEM_MESSAGE("notifySystemMessage", () ->
    translate("notify_system_message_subject")
  ),
  CATEGORY_GROUP_DELETE_REQUEST_REFUSE(
    C.CAT_GROUP_DEL_REQ_DIR,
    "refusal.ftl",
    false,
    () -> translate(C.CAT_GROUP_DEL_REQ_SUBJECT)
  ),
  CATEGORY_GROUP_DELETE_REQUEST_REFUSE_LEADER(
    C.CAT_GROUP_DEL_REQ_DIR,
    "refusal_leaders.ftl",
    false,
    () -> translate(C.CAT_GROUP_DEL_REQ_SUBJECT)
  ),
  CATEGORY_GROUP_DELETE_REQUEST_ACCEPT(
    C.CAT_GROUP_DEL_REQ_DIR,
    "acceptation.ftl",
    false,
    () -> translate(C.CAT_GROUP_DEL_REQ_SUBJECT)
  );

  /** Nested class to hold constants that enum constructors can reference without forward-reference issues. */
  private static final class C {

    static final String LOCATION_NAME = "${location.name}";
    static final String CIRCABC_NAME = "CIRCABC";
    static final String IG_TITLE = "${titleOrName(interestGroup)}";
    static final String APPOINTMENT_TITLE = "${appointment.title}";
    static final String PERSON_FULL_NAME = "${fullName(person)}";
    static final String CAT_GROUP_DEL_REQ_SUBJECT =
      "category_group_delete_request_template_mail_subject";
    static final String CAT_GROUP_DEL_REQ_DIR = "categoryGroupDeleteRequest";
  }

  /** Template file name used when a constant does not specify a custom one. */
  private static final String DEFAULT_TEMPLATE_NAME = "default.ftl";

  public static final String KEY_ME = "me";
  public static final String KEY_PERSON = TemplateService.KEY_PERSON;
  public static final String KEY_DATE = TemplateService.KEY_DATE;
  public static final String KEY_IMAGE_RESOLVER =
    TemplateService.KEY_IMAGE_RESOLVER;
  public static final String KEY_COMPANY_HOME =
    TemplateService.KEY_COMPANY_HOME;
  public static final String KEY_USER_HOME = TemplateService.KEY_USER_HOME;
  public static final String KEY_LOCATION = "location";
  public static final String KEY_SPACE = "space";
  public static final String KEY_DOCUMENT = "document";
  public static final String KEY_CIRCABC = "circabc";
  public static final String KEY_INTEREST_GROUP = "interestGroup";
  public static final String KEY_CATEGORY = "category";
  public static final String KEY_APPOINTMENT = "appointment";
  public static final String KEY_EVENT_SERVICE = "eventService";
  public static final String KEY_APPOINTMENT_FIRST_OCCURENCE =
    "appointmentFirstOccurence";
  public static final String KEY_APPOINTMENT_ID = "appointmentId";
  public static final String KEY_PERMISSION = "permission";
  public static final String KEY_REJECT_DATE = "rejectDate";
  public static final String KEY_REJECT_REASON = "rejectReason";
  public static final String KEY_REJECTED_CONTENT = "rejectedContent";
  public static final String KEY_ABUSE_DATE = "abuseDate";
  public static final String KEY_ABUSE_REASON = "abuseReason";

  /** Name of the directory (under the mail templates root) that holds this template's files. */
  private final String templateDirectoryName;
  /** File name of the FreeMarker template to render within {@link #templateDirectoryName}. */
  private final String defaultTemplateName;
  /** Whether this template may be used to address multiple recipients/instances. */
  private final boolean multipleAllowed;
  /** Lazily resolves the localized default subject line for this template. */
  private final Supplier<String> defaultSubjectSupplier;

  /**
   * Full constructor allowing a custom template file name and multiple-recipient flag.
   *
   * @param templateDirectoryName name of the directory holding the template files
   * @param defaultTemplateName the FreeMarker template file name to render
   * @param multipleAllowed {@code true} if the template may target multiple recipients/instances
   * @param defaultSubjectSupplier supplier that resolves the localized default subject at send time
   */
  MailTemplate(
    String templateDirectoryName,
    String defaultTemplateName,
    boolean multipleAllowed,
    Supplier<String> defaultSubjectSupplier
  ) {
    this.templateDirectoryName = templateDirectoryName;
    this.defaultTemplateName = defaultTemplateName;
    this.multipleAllowed = multipleAllowed;
    this.defaultSubjectSupplier = defaultSubjectSupplier;
  }

  /**
   * Convenience constructor using {@link #DEFAULT_TEMPLATE_NAME} and {@code multipleAllowed=false}.
   *
   * @param templateDirectoryName name of the directory holding the template files
   * @param defaultSubjectSupplier supplier that resolves the localized default subject at send time
   */
  MailTemplate(
    String templateDirectoryName,
    Supplier<String> defaultSubjectSupplier
  ) {
    this(
      templateDirectoryName,
      DEFAULT_TEMPLATE_NAME,
      false,
      defaultSubjectSupplier
    );
  }

  /**
   * Convenience constructor using {@link #DEFAULT_TEMPLATE_NAME} with a custom multiple-recipient
   * flag.
   *
   * @param templateDirectoryName name of the directory holding the template files
   * @param multipleAllowed {@code true} if the template may target multiple recipients/instances
   * @param defaultSubjectSupplier supplier that resolves the localized default subject at send time
   */
  MailTemplate(
    String templateDirectoryName,
    boolean multipleAllowed,
    Supplier<String> defaultSubjectSupplier
  ) {
    this(
      templateDirectoryName,
      DEFAULT_TEMPLATE_NAME,
      multipleAllowed,
      defaultSubjectSupplier
    );
  }

  /**
   * Returns the name of the directory holding this template's files.
   *
   * @return the template directory name
   */
  public String getTemplateDirectoryName() {
    return templateDirectoryName;
  }

  /**
   * Returns the FreeMarker template file name to render for this template.
   *
   * @return the default template file name
   */
  public String getDefaultTemplateName() {
    return defaultTemplateName;
  }

  /**
   * Resolves the localized default subject line for this template.
   *
   * <p>The value is computed on each call via the underlying supplier, so the current locale is
   * honored at invocation time.
   *
   * @return the localized default subject (may still contain FreeMarker placeholders)
   */
  public String getDefaultSubject() {
    return defaultSubjectSupplier.get();
  }

  /**
   * Indicates whether this template may address multiple recipients/instances.
   *
   * @return {@code true} if multiple recipients/instances are allowed
   */
  public boolean multipleAllowed() {
    return multipleAllowed;
  }

  /**
   * Looks up the {@link MailTemplate} whose template directory matches the given folder name.
   *
   * @param folder the template directory name to match; may be {@code null}
   * @return the matching {@link MailTemplate}, or {@code null} if {@code folder} is {@code null} or
   *     no template matches
   */
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

  /**
   * Resolves and formats a localized message for the given resource key.
   *
   * <p>When {@code params} are supplied, the resolved message is treated as a {@link
   * java.text.MessageFormat} pattern and formatted with the arguments; otherwise the raw localized
   * message is returned.
   *
   * @param key the i18n resource key to resolve
   * @param params optional {@link MessageFormat} arguments; may be {@code null} or empty
   * @return the localized (and, if applicable, formatted) message
   */
  public static String translate(final String key, final Object... params) {
    if (params == null || params.length < 1) {
      return I18NUtil.getMessage(key);
    } else {
      return MessageFormat.format(I18NUtil.getMessage(key), params);
    }
  }
}
