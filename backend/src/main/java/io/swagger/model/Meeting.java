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
package io.swagger.model;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Domain model for a meeting within CIRCABC.
 *
 * <p>A meeting is a specialized {@link Appointment} that, in addition to the common appointment
 * attributes (title, dates, location, participants, etc.), carries meeting-specific details such as
 * its availability status, organizing body, agenda, type and an optional link to a library section
 * where related documents are stored. Instances are typically backed by an Alfresco node and
 * exchanged between the REST layer and the underlying content repository.
 */
public interface Meeting extends Appointment {
  /**
   * Returns the availability status of the meeting.
   *
   * @return the current {@link MeetingAvailability}
   */
  MeetingAvailability getAvailability();

  /**
   * Sets the availability status of the meeting.
   *
   * @param value the {@link MeetingAvailability} to assign
   */
  void setAvailability(MeetingAvailability value);

  /**
   * Returns the organization hosting or responsible for the meeting.
   *
   * @return the organization name
   */
  String getOrganization();

  /**
   * Sets the organization hosting or responsible for the meeting.
   *
   * @param value the organization name to assign
   */
  void setOrganization(String value);

  /**
   * Returns the agenda of the meeting.
   *
   * @return the agenda text
   */
  String getAgenda();

  /**
   * Sets the agenda of the meeting.
   *
   * @param value the agenda text to assign
   */
  void setAgenda(String value);

  /**
   * Returns the meeting type as its string representation.
   *
   * @return the meeting type string
   */
  String getMeetingTypeString();

  /**
   * Sets the meeting type from its string representation.
   *
   * @param value the meeting type string to assign
   */
  void setMeetingTypeString(String value);

  /**
   * Returns a reference to the library section associated with the meeting, if any.
   *
   * @return the {@link NodeRef} of the linked library section, or {@code null} if none
   */
  NodeRef getLibrarySection();

  /**
   * Sets the reference to the library section associated with the meeting.
   *
   * @param value the {@link NodeRef} of the library section to link
   */
  void setLibrarySection(NodeRef value);

  /**
   * Returns the sequence number of the meeting, used for ordering.
   *
   * @return the sequence number
   */
  Integer getSequence();

  /**
   * Sets the sequence number of the meeting, used for ordering.
   *
   * @param value the sequence number to assign
   */
  void setSequence(Integer value);
}
