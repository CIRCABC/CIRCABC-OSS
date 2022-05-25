/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.springframework.jndi;

public class JndiObjectFactoryBean extends org.springframework.jndi.JndiObjectFactoryBean {

    private String defaultAutoCommit;

    public final String getDefaultAutoCommit() {
        return defaultAutoCommit;
    }

    public final void setDefaultAutoCommit(final String defaultAutoCommit) {
        this.defaultAutoCommit = defaultAutoCommit;
    }
}
