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
package eu.cec.digit.circabc.repo.version;

import org.alfresco.repo.version.VersionModel;
import org.alfresco.repo.version.VersionServicePolicies.CalculateVersionLabelPolicy;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.VersionNumber;

import java.io.Serializable;
import java.util.Map;

/**
 * The serial version label policy. Implementation to start with major version number 1.0
 *
 * @author Roy Wetherall
 */
public class SerialVersionLabelPolicy implements CalculateVersionLabelPolicy {
    // TODO need to add support for branches into this labeling policy

    /**
     * Get the version label value base on the data provided.
     *
     * @param preceedingVersion the preceeding version, null if none
     * @param versionNumber     the new version number
     * @param versionProperties the version property values
     * @return the version label
     */
    public String calculateVersionLabel(
            QName classRef, Version preceedingVersion, Map<String, Serializable> versionProperties) {
        return calculateVersionLabel(classRef, preceedingVersion, 0, versionProperties);
    }

    /**
     * Get the version label value base on the data provided.
     *
     * @param preceedingVersion the preceeding version, null if none
     * @param versionNumber     the new version number
     * @param versionProperties the version property values
     * @return the version label
     */
    public String calculateVersionLabel(
            QName classRef,
            Version preceedingVersion,
            int versionNumber,
            Map<String, Serializable> versionProperties) {
        SerialVersionLabel serialVersionNumber = null;

        VersionType versionType = null;
        if (versionProperties != null) {
            versionType = (VersionType) versionProperties.get(VersionModel.PROP_VERSION_TYPE);
        }

        if (preceedingVersion != null) {
            // There is a preceeding version
            serialVersionNumber = new SerialVersionLabel(preceedingVersion.getVersionLabel());
        } else {
            // This is the first version
            serialVersionNumber = new SerialVersionLabel(null);

            return serialVersionNumber.toString();
        }

        if (VersionType.MAJOR.equals(versionType) == true) {
            serialVersionNumber.majorIncrement();
        } else {
            serialVersionNumber.minorIncrement();
        }

        return serialVersionNumber.toString();
    }

    /**
     * Inner class encapsulating the notion of the serial version number.
     *
     * @author Roy Wetherall
     */
    private static class SerialVersionLabel {

        /**
         * The version number delimiter
         */
        private static final String DELIMITER = ".";

        /**
         * The major revision number (default 1)
         */
        private int majorRevisionNumber;

        /**
         * The minor revision number (default 0)
         */
        private int minorRevisionNumber;

        /**
         * Constructor
         *
         * @param version the vesion to take the version from
         */
        public SerialVersionLabel(String versionLabel) {
            if (versionLabel != null && versionLabel.length() != 0) {
                VersionNumber versionNumber = new VersionNumber(versionLabel);
                majorRevisionNumber = versionNumber.getPart(0);
                minorRevisionNumber = versionNumber.getPart(1);
            } else {
                majorRevisionNumber = 1;
                minorRevisionNumber = 0;
            }
        }

        /**
         * Increments the major revision numebr and sets the minor to zero.
         */
        public void majorIncrement() {
            this.majorRevisionNumber += 1;
            this.minorRevisionNumber = 0;
        }

        /**
         * Increments only the minor revision number
         */
        public void minorIncrement() {
            this.minorRevisionNumber += 1;
        }

        /**
         * Converts the serial version number into a string
         */
        public String toString() {
            return this.majorRevisionNumber + DELIMITER + this.minorRevisionNumber;
        }
    }
}
