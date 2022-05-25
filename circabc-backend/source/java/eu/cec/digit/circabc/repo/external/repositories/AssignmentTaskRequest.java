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
package eu.cec.digit.circabc.repo.external.repositories;

import java.util.List;

/**
 * Encapsulates the properties to add a workflow task that will be used to classify the document.
 *
 * @author schwerr
 */
public class AssignmentTaskRequest {

    private String assignmentUserName = null;
    private String documentId = null;
    private List<String> assigneeIds = null;

    /**
     * Gets the value of the assignmentUserName
     *
     * @return the assignmentUserName
     */
    public String getAssignmentUserName() {
        return assignmentUserName;
    }

    /**
     * Sets the value of the assignmentUserName
     *
     * @param assignmentUserName the assignmentUserName to set.
     */
    public void setAssignmentUserName(String assignmentUserName) {
        this.assignmentUserName = assignmentUserName;
    }

    /**
     * Gets the value of the documentId
     *
     * @return the documentId
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     * Sets the value of the documentId
     *
     * @param documentId the documentId to set.
     */
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    /**
     * Gets the value of the assigneeIds
     *
     * @return the assigneeIds
     */
    public List<String> getAssigneeIds() {
        return assigneeIds;
    }

    /**
     * Sets the value of the assigneeIds
     *
     * @param assigneeIds the assigneeIds to set.
     */
    public void setAssigneeIds(List<String> assigneeIds) {
        this.assigneeIds = assigneeIds;
    }
}
