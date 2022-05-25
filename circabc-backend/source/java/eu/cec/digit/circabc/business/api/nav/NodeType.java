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
package eu.cec.digit.circabc.business.api.nav;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration of each specifc kind of node managed by Circabc.
 *
 * @author yanick pignot
 */

/**
 * @author Yanick Pignot
 */
public enum NodeType {
    /**
     * The circabc root node
     */
    CIRCABC_ROOT {
        public List<NodeType> getAvailableChilds() {
            return Collections.singletonList(HEADER);
        }
    },
    /**
     * A category heade
     */
    HEADER {
        public List<NodeType> getAvailableChilds() {
            return Collections.singletonList(CATEGORY);
        }
    },
    /**
     * A Category
     */
    CATEGORY {
        public List<NodeType> getAvailableChilds() {
            return Collections.singletonList(INTEREST_GROUP);
        }
    },
    /**
     * An Interest group
     */
    INTEREST_GROUP {
        public List<NodeType> getAvailableChilds() {
            return Arrays.asList(LIBRARY_ROOT,
                    NEWSGROUP_ROOT,
                    DIRECTORY_ROOT,
                    SURVEYS_ROOT,
                    CALENDAR_ROOT,
                    INFORMATION_ROOT);
        }
    },

    /**
     * An Interest group Library Service
     */
    LIBRARY_ROOT {
        public List<NodeType> getAvailableChilds() {
            return Arrays.asList(LIBRARY_SPACE,
                    LIBRARY_DOSSIER,
                    LIBRARY_CONTENT,
                    LIBRARY_URL,
                    LIBRARY_TRANSLATION,
                    LIBRARY_EMPTY_TRANSLATION,
                    LIBRARY_FILE_LINK,
                    LIBRARY_SPACE_LINK,
                    LIBRARY_SHARED_SPACE_LINK,
                    LIBRARY_ML_CONTENT);
        }
    },
    /**
     * An Interest group Newsgroup Service
     */
    NEWSGROUP_ROOT {
        public List<NodeType> getAvailableChilds() {
            return Collections.singletonList(NEWSGROUP_FORUM);
        }
    },
    /**
     * An Interest group Directory Service
     */
    DIRECTORY_ROOT {
        public List<NodeType> getAvailableChilds() {
            return Collections.singletonList(DIRECTORY_PROFILE);
        }
    },
    /**
     * An Interest group Survey Service
     */
    SURVEYS_ROOT {
        public List<NodeType> getAvailableChilds() {
            return Collections.singletonList(SURVEYS_SURVEY);
        }
    },
    /**
     * An Interest group Calendar (Events) Service
     */
    CALENDAR_ROOT {
        public List<NodeType> getAvailableChilds() {
            return Arrays.asList(CALENDAR_EVENT,
                    CALENDAR_MEETING);
        }
    },
    /**
     * An Interest group Information Service
     */
    INFORMATION_ROOT {
        public List<NodeType> getAvailableChilds() {
            return Arrays.asList(INFORMATION_SPACE,
                    INFORMATION_CONTENT,
                    INFORMATION_FILE_LINK,
                    INFORMATION_FOLDER_LINK);
        }
    },

    /**
     * A Library Service space
     */
    LIBRARY_SPACE {
        public List<NodeType> getAvailableChilds() {
            return Arrays.asList(LIBRARY_SPACE,
                    LIBRARY_DOSSIER,
                    LIBRARY_CONTENT,
                    LIBRARY_URL,
                    LIBRARY_TRANSLATION,
                    LIBRARY_EMPTY_TRANSLATION,
                    LIBRARY_FILE_LINK,
                    LIBRARY_SPACE_LINK,
                    LIBRARY_SHARED_SPACE_LINK,
                    LIBRARY_ML_CONTENT,
                    LIBRARY_DISCUSSION);
        }
    },
    /**
     * A Library Service dossier
     */
    LIBRARY_DOSSIER {
        public List<NodeType> getAvailableChilds() {
            return Arrays.asList(LIBRARY_URL,
                    LIBRARY_FILE_LINK,
                    LIBRARY_SPACE_LINK,
                    LIBRARY_DISCUSSION);
        }
    },
    /**
     * A Library Service content
     */
    LIBRARY_CONTENT {
        public List<NodeType> getAvailableChilds() {
            return Collections.singletonList(LIBRARY_DISCUSSION);
        }
    },
    /**
     * A Library Service url
     */
    LIBRARY_URL {
        public List<NodeType> getAvailableChilds() {
            return Collections.singletonList(LIBRARY_DISCUSSION);
        }
    },
    /**
     * A Library Service translation
     */
    LIBRARY_TRANSLATION {
        public List<NodeType> getAvailableChilds() {
            return Collections.singletonList(LIBRARY_DISCUSSION);
        }
    },
    /**
     * A Library Service empty translation
     */
    LIBRARY_EMPTY_TRANSLATION {
        public List<NodeType> getAvailableChilds() {
            return Collections.singletonList(LIBRARY_DISCUSSION);
        }
    },
    /**
     * A Library Service file link
     */
    LIBRARY_FILE_LINK {
        public List<NodeType> getAvailableChilds() {
            return Collections.singletonList(LIBRARY_DISCUSSION);
        }
    },
    /**
     * A Library Service space link
     */
    LIBRARY_SPACE_LINK {
        public List<NodeType> getAvailableChilds() {
            return Collections.singletonList(LIBRARY_DISCUSSION);
        }
    },
    /**
     * A Library Service shared space link
     */
    LIBRARY_SHARED_SPACE_LINK {
        public List<NodeType> getAvailableChilds() {
            return Collections.singletonList(LIBRARY_DISCUSSION);
        }
    },
    /**
     * A Library Service discussion
     */
    LIBRARY_DISCUSSION {
        public List<NodeType> getAvailableChilds() {
            return Collections.singletonList(LIBRARY_TOPIC);
        }
    },
    /**
     * A Library Service discussion topic
     */
    LIBRARY_TOPIC {
        public List<NodeType> getAvailableChilds() {
            return Collections.singletonList(LIBRARY_POST);
        }
    },
    /**
     * A Library Service discussion post
     */
    LIBRARY_POST {
        public List<NodeType> getAvailableChilds() {
            return Collections.emptyList();
        }
    },
    /**
     * A Library Service multilingual content
     */
    LIBRARY_ML_CONTENT {
        public List<NodeType> getAvailableChilds() {
            return Arrays.asList(LIBRARY_TRANSLATION,
                    LIBRARY_EMPTY_TRANSLATION,
                    LIBRARY_DISCUSSION);
        }
    },

    /**
     * A Surveys Service survey
     */
    SURVEYS_SURVEY {
        public List<NodeType> getAvailableChilds() {
            return Collections.emptyList();
        }
    },

    /**
     * A Newsgroup Service forum or subforum
     */
    NEWSGROUP_FORUM {
        public List<NodeType> getAvailableChilds() {
            return Collections.singletonList(NEWSGROUP_TOPIC);
        }
    },
    /**
     * A Newsgroup Service topic
     */
    NEWSGROUP_TOPIC {
        public List<NodeType> getAvailableChilds() {
            return Collections.singletonList(NEWSGROUP_POST);
        }
    },
    /**
     * A Newsgroup Service post
     */
    NEWSGROUP_POST {
        public List<NodeType> getAvailableChilds() {
            return Collections.emptyList();
        }
    },

    /**
     * An Information Service space
     */
    INFORMATION_SPACE {
        public List<NodeType> getAvailableChilds() {
            return Arrays.asList(INFORMATION_SPACE,
                    INFORMATION_CONTENT,
                    INFORMATION_FILE_LINK,
                    INFORMATION_FOLDER_LINK);
        }
    },

    /**
     * An Information Service content
     */
    INFORMATION_CONTENT {
        public List<NodeType> getAvailableChilds() {
            return Collections.emptyList();
        }
    },
    /**
     * An Information Service file link
     */
    INFORMATION_FILE_LINK {
        public List<NodeType> getAvailableChilds() {
            return Collections.emptyList();
        }
    },
    /**
     * An Information Service folder link
     */
    INFORMATION_FOLDER_LINK {
        public List<NodeType> getAvailableChilds() {
            return Collections.emptyList();
        }
    },

    /**
     * An Calendar  meeting
     */
    CALENDAR_MEETING {
        public List<NodeType> getAvailableChilds() {
            return Collections.emptyList();
        }
    },
    /**
     * An Calendar  meeting
     */
    CALENDAR_EVENT {
        public List<NodeType> getAvailableChilds() {
            return Collections.emptyList();
        }
    },

    /**
     * A directory Service profile
     */
    DIRECTORY_PROFILE {
        public List<NodeType> getAvailableChilds() {
            return Collections.singletonList(DIRECTORY_INVITED_USER);
        }
    },
    /**
     * A directory Service user invited in a profile
     */
    DIRECTORY_INVITED_USER {
        public List<NodeType> getAvailableChilds() {
            return Collections.emptyList();
        }
    };

    /**
     * @return All possible childs for a node type
     */
    public abstract List<NodeType> getAvailableChilds();

}
