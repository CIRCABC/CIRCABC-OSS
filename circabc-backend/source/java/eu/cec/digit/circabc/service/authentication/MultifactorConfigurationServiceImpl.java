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
package eu.cec.digit.circabc.service.authentication;

/**
 * Stores the configuration properties of the multifactor system.
 *
 * @author schwerr
 */
public class MultifactorConfigurationServiceImpl implements MultifactorConfigurationService {

    private boolean multifactorEnabled = false;

    /**
     * @see eu.cec.digit.circabc.service.authentication.MultifactorConfigurationService#isMultifactorEnabled()
     */
    @Override
    public boolean isMultifactorEnabled() {
        return multifactorEnabled;
    }

    /**
     * @see eu.cec.digit.circabc.service.authentication.MultifactorConfigurationService#setMultifactorEnabled(boolean)
     */
    @Override
    public void setMultifactorEnabled(boolean enabled) {
        this.multifactorEnabled = enabled;
    }
}
