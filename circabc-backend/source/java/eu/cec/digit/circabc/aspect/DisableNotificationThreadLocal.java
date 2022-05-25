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
package eu.cec.digit.circabc.aspect;

public class DisableNotificationThreadLocal {

    private static ThreadLocalBoolean flag = new ThreadLocalBoolean();

    public void remove() {
        flag.remove();
    }

    public void set(Boolean value) {
        flag.set(value);
    }

    public Boolean get() {
        return flag.get();
    }

    private static class ThreadLocalBoolean extends ThreadLocal<Boolean> {

        public Boolean initialValue() {
            return Boolean.FALSE;
        }
    }
}
