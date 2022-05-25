======================
Alfresco Language Pack
======================

For release: 1.0

For locale: EN-US (default)


==============================
Contents of this Language Pack
==============================

action-config.properties
action-service.properties
application-model.properties
bootstrap-spaces.properties
bootstrap-templates.properties
bootstrap-tutorial.properties
content-model.properties
content-service.properties
dictionary-model.properties
permissions-service.properties
rule-config.properties
system-model.properties
version-service.properties
webclient.properties

Note: These are the names of the default language pack.  All other packs should name the
files with the appropriate locale as part of the name following the pattern:
  default-name_LC_RC.properties
where LC is the standard 2 character language code and RC is the standard 2 character region
code.  For example, 'action-config_en_GB.properties'.


============
Installation
============

- Copy all files into <config root>/messages folder.

- Edit the 'web-client-config.xml' file in the <config root> folder to set what languages
  you wish to be available:

  - Find the '<languages>' section
  - Add or remove languages of the form:

       '<language locale="XX_YY">LangName</language>'

- The order of the language entries determines the order they are presented on the login prompt.
- Save the file.

- Restart the Alfresco server.


====================
Note for translators
====================

If the message contains a variable e.g. {0} then any single quotes must be doubled up as '' to escape them correctly.
If the message does not contain a variable then a single quote character should be used as normal.



==================================
Contributors to this Language Pack
==================================

See the Alfresco Forum for status on Language Packs:
http://www.alfresco.org/forums/viewforum.php?f=16

Original Author(s): Alfresco Team
Contributors:
