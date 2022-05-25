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
package eu.cec.digit.circabc;

import eu.cec.digit.circabc.config.CircabcConfiguration;

@SuppressWarnings("checkstyle:LineLength")
public final class CircabcConfig {

    private static final String BUILD_RELEASE = CircabcConfiguration.getProperty(CircabcConfiguration.BUILD_RELEASE);
    public static final boolean OSS = BUILD_RELEASE.equalsIgnoreCase("oss");
    public static final boolean ENT = !OSS;
    public static final boolean ECHA = BUILD_RELEASE.equalsIgnoreCase("echa");
}
