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
package eu.cec.digit.circabc.business.impl;

import eu.cec.digit.circabc.business.api.BusinessRuntimeExpection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Util methods that ecapsulate the boring work for object assertion.
 *
 * <p>
 * Assertion failure throw an BusinessRuntimeExcpetion with a I18N user friendly with a logging
 * purposes relevant cause.
 * </p>
 *
 * @author Yanick Pignot
 */
public abstract class AssertUtils {

    private static final String MSG_UPLOADED_FILE = "business_error_upload_file";
    private static final String MSG_GENERIC = "business_error_generic";
    private static final String MSG_EMPTY_COLLECTION = "business_error_empty_collection";
    private static final String MSG_EMPTY_STRING = "business_error_empty_string";

    private AssertUtils() {
    }

    public static void notNull(final Object object) throws BusinessRuntimeExpection {
        if (object == null) {
            throw new BusinessRuntimeExpection(MSG_GENERIC, new NullPointerException());
        }
    }

    public static void notEmpty(final Collection collection) throws BusinessRuntimeExpection {
        notNull(collection);
        if (collection.size() < 1) {
            throw new BusinessRuntimeExpection(MSG_EMPTY_COLLECTION, new IllegalArgumentException(
                    "The collection is empty and MUST have at least one element."));
        }
    }

    public static void notEmpty(final Map map) throws BusinessRuntimeExpection {
        notNull(map);
        if (map.size() < 1) {
            throw new BusinessRuntimeExpection(MSG_EMPTY_COLLECTION,
                    new IllegalArgumentException("The map is empty and MUST have at least one element."));
        }
    }

    public static void notEmpty(final String string) throws BusinessRuntimeExpection {
        notNull(string);
        if (string.trim().length() < 1) {
            throw new BusinessRuntimeExpection(MSG_EMPTY_STRING,
                    new IllegalArgumentException("The map is empty and MUST have at least one element."));
        }
    }

    public static void canAccess(final File file) throws BusinessRuntimeExpection {
        final Throwable cause;

        if (file == null) {
            cause = new NullPointerException("The uploaded file is null");
        } else if (!file.exists()) {
            cause = new FileNotFoundException(
                    "The uploaded file doens't exist: " + file.getAbsolutePath());
        } else if (!file.canRead()) {
            cause = new IOException(
                    "Cannot obtain read permission on uploaded file : " + file.getAbsolutePath());
        } else {
            cause = null;
        }

        if (cause != null) {
            throw new BusinessRuntimeExpection(MSG_UPLOADED_FILE, cause);
        }
    }
}
