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
package eu.cec.digit.circabc.service.customisation.nav;

/**
 * @author Yanick Pignot
 */
public enum PropertyColumns {
    NAME {
        private static final String PROP_NAME = "cm:name";

        public Type getType() {
            return Type.FREE_TEXT;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    },
    TITLE {
        private static final String PROP_NAME = "cm:title";

        public Type getType() {
            return Type.FREE_TEXT;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    },
    SIZE {
        private static final String PROP_NAME = "size";

        public Type getType() {
            return Type.SIZE;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    },
    DESCRIPTION {
        private static final String PROP_NAME = "cm:description";

        public Type getType() {
            return Type.LONG_TEXT;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    },
    AUTHOR {
        private static final String PROP_NAME = "cm:author";

        public Type getType() {
            return Type.FREE_TEXT;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    },
    VERSION_LABEL {
        private static final String PROP_NAME = "ver:versionlabel";

        public Type getType() {
            return Type.FREE_TEXT;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    },
    AUTO_VERSION {
        private static final String PROP_NAME = "ver:autoversion";

        public Type getType() {
            return Type.BOOLEAN;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    },
    CREATOR {
        private static final String PROP_NAME = "cm:creator";

        public Type getType() {
            return Type.FREE_TEXT;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    },
    CREATED {
        private static final String PROP_NAME = "cm:created";

        public Type getType() {
            return Type.DATE_TIME;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    },
    MODIFIER {
        private static final String PROP_NAME = "cm:modifier";

        public Type getType() {
            return Type.FREE_TEXT;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    },
    MODIFIED {
        private static final String PROP_NAME = "cm:modified";

        public Type getType() {
            return Type.DATE_TIME;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    },
    LANGUAGE {
        private static final String PROP_NAME = "sys:locale";

        public Type getType() {
            return Type.LANGUAGE;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    },
    STATUS {
        private static final String PROP_NAME = "cd:status";

        public Type getType() {
            return Type.MULTI_SELECT;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    },
    ISSUE_DATE {
        private static final String PROP_NAME = "cd:issue_date";

        public Type getType() {
            return Type.DATE_TIME;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    },
    URL {
        private static final String PROP_NAME = "cm:url";

        public Type getType() {
            return Type.FREE_TEXT;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    },
    SECURITY_RANKING {
        private static final String PROP_NAME = "cm:security_ranking";

        public Type getType() {
            return Type.MULTI_SELECT;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    },
    EXPIRATION_DATE {
        private static final String PROP_NAME = "cm:expiration_date";

        public Type getType() {
            return Type.DATE_TIME;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    },
    EDIT_INLINE {
        private static final String PROP_NAME = "app:editinline";

        public Type getType() {
            return Type.BOOLEAN;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    },
    REFERENCE {
        private static final String PROP_NAME = "cm:reference";

        public Type getType() {
            return Type.FREE_TEXT;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    },
    PATH {
        private static final String PROP_NAME = "servicePath";

        public Type getType() {
            return Type.PATH;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    },
    KEYWORD {
        private static final String PROP_NAME = "cm:keyword";

        public Type getType() {
            return Type.NODE;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    },
    REPLIES_NUMBER {
        private static final String PROP_NAME = "replies_number";

        public Type getType() {
            return Type.INTEGER;
        }

        public String getShortQname() {
            return PROP_NAME;
        }
    };

    /**
     * @return The property type
     */
    public abstract Type getType();

    /**
     * @return The property name
     */
    public abstract String getShortQname();

    public enum Type {
        FREE_TEXT,
        LONG_TEXT,
        DATE,
        TIME,
        DATE_TIME,
        SIZE,
        BOOLEAN,
        PATH,
        NODE,
        INTEGER,
        LANGUAGE,
        MULTI_SELECT
    }
}
