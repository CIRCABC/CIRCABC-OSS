/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European
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
package eu.cec.digit.circabc.repo.security.authentication;

import org.alfresco.repo.security.authentication.MD4PasswordEncoderImpl;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CircabcPasswordEncoderImpl extends MD4PasswordEncoderImpl {

    public CircabcPasswordEncoderImpl() {
        super();
    }

    @Override
    public boolean isPasswordValid(String encPass, String rawPass, Object salt) {

        if (isAlgorithhmPresent(encPass)) {
            String algorithm = getAlgorithm(encPass);
            if (algorithm.equals("SHA") || algorithm.equals("MD5")) {
                String pass1 = encPass;
                String pass2 = this.encodePassword(algorithm, rawPass, salt);
                return pass1.equals(pass2);
            } else {
                throw new IllegalArgumentException("Not supported algorithm : " + algorithm);
            }
        } else {
            return super.isPasswordValid(encPass, rawPass, salt);
        }
    }

    private String getAlgorithm(String encPass) {
        return encPass.substring(encPass.indexOf('{') + 1, encPass.indexOf('}'));
    }

    private boolean isAlgorithhmPresent(String encPass) {
        return encPass.indexOf('{') == 0 && encPass.indexOf('}') > 0;
    }

    public String encodePassword(String rawPass, Object salt) {
        return super.encodePassword(rawPass, salt);
    }

    private String encodePassword(String algorithm, String rawPass, Object salt) {
        String result = null;
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No such algorithm : " + algorithm, e);
        }
        md.update(rawPass.getBytes());
        byte[] bytes = md.digest();

        // Print out value in Base64 encoding
        String hash = DatatypeConverter.printBase64Binary(bytes);
        result = "{" + algorithm + "}" + hash;
        return result;
    }
}
