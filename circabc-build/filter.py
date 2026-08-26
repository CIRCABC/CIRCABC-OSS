ALLOWED_BUILD_CONFIG_SUBFOLDERS = {
    b'buildconfig/config1/',
    b'buildconfig/tomcat-docker/',
    b'buildconfig/tomcat-solr-docker/',
}

ALLOWED_CIRCAB_BUILD_FILES = {
    b'circab-build/docker-tomcat-build.sh',
    b'circab-build/docker-tomcat-deploy.sh',
    b'circab-build/docker-tomcat-run.sh',
}

EXACT_FILE_EXCLUDES = {
    b'build.sh',
    b'build-parallel.sh',
    b'stop.sh',
    b'circabc-resources/alfresco.war',
}

FOLDER_EXCLUDES = [
    b'solr/',
    b'start/',
    b'circabc2.0-poc/',
]

def should_exclude(path):
    # Exclude exact top-level files
    if path in EXACT_FILE_EXCLUDES:
        return True

    # Exclude specific folders completely
    for folder in FOLDER_EXCLUDES:
        if path.startswith(folder):
            return True

    # Exclude all buildconfig/ subfolders except the allowed ones
    if path.startswith(b'buildconfig/'):
        if not any(path.startswith(allowed) for allowed in ALLOWED_BUILD_CONFIG_SUBFOLDERS):
            return True

    # Exclude all circab-build/ files except the allowed ones
    if path.startswith(b'circab-build/'):
        if path not in ALLOWED_CIRCAB_BUILD_FILES:
            return True

    return False

def filter_commit(commit):
    commit.file_changes = [
        fc for fc in commit.file_changes if not should_exclude(fc.filename)
    ]
