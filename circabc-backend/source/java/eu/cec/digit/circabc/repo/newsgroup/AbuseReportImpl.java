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
package eu.cec.digit.circabc.repo.newsgroup;

import eu.cec.digit.circabc.service.newsgroup.AbuseReport;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.util.ParameterCheck;

import java.io.Serializable;
import java.util.Date;

/**
 * Concrete implementation of an abuse in a moderation process.
 *
 * @author Yanick Pignot
 */
public class AbuseReportImpl implements AbuseReport, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -3614767880321596490L;

    private final Date reportDate;
    private final String reporter;
    private final String message;

    /**
     * @param reportDate
     * @param reporter
     * @param message
     */
    public AbuseReportImpl(Date reportDate, String reporter, String message) {
        super();
        ParameterCheck.mandatory("The date", reportDate);
        ParameterCheck.mandatoryString("The reporter", reporter);

        this.reportDate = reportDate;
        this.reporter = reporter;
        this.message = message == null ? "" : message;
    }

    /**
     * @param message
     */
    public AbuseReportImpl(final String message) {
        this(new Date(), AuthenticationUtil.getFullyAuthenticatedUser(), message);
    }

    public final String getMessage() {
        return message;
    }

    public final Date getReportDate() {
        return reportDate;
    }

    public final String getReporter() {
        return reporter;
    }

    @Override
    public String toString() {
        return "Abuse Report: [Date:"
                + reportDate
                + ", Reporter:"
                + reporter
                + ", message:"
                + message
                + "]";
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((message == null) ? 0 : message.hashCode());
        result = PRIME * result + ((reportDate == null) ? 0 : reportDate.hashCode());
        result = PRIME * result + ((reporter == null) ? 0 : reporter.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbuseReportImpl other = (AbuseReportImpl) obj;
        if (message == null) {
            if (other.message != null) {
                return false;
            }
        } else if (!message.equals(other.message)) {
            return false;
        }
        if (reportDate == null) {
            if (other.reportDate != null) {
                return false;
            }
        } else if (!reportDate.equals(other.reportDate)) {
            return false;
        }
        if (reporter == null) {
            if (other.reporter != null) {
                return false;
            }
        } else if (!reporter.equals(other.reporter)) {
            return false;
        }
        return true;
    }
}
