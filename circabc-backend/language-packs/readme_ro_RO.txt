======================
Alfresco Language Pack
======================

For release: 2.1.0

For locale: ro-RO 


==============================
Contents of this Language Pack
==============================

action-config_ro_RO.properties 
action-service_ro_RO.properties
application-model_ro_RO.properties
avm-messages_ro_RO.properties 
bootstrap-spaces_ro_RO.properties
bootstrap-templates_ro_RO.properties
bootstrap-tutorial_ro_RO.properties
bpm-messages_ro_RO.properties 
coci-service_ro_RO.properties
content-model_ro_RO.properties 
content-service_ro_RO.properties
copy-service_ro_RO.properties
dictionary-messages_ro_RO.properties
dictionary-model_ro_RO.properties
forum-model_ro_RO.properties
lock-service_ro_RO.properties
patch-service_ro_RO.properties 
permissions-service_ro_RO.properties
rule-config_ro_RO.properties
system-messages_ro_RO.properties
system-model_ro_RO.properties 
template-service_ro_RO.properties
version-service_ro_RO.properties 
webclient_ro_RO.properties 
webdav-messages_ro_RO.properties

Note: These are the names of the default language pack.  All other packs should name the
files with the appropriate locale as part of the name following the pattern:
  default-name_LC_RC.properties
where LC is the standard 2 character language code and RC is the standard 2 character region
code.  For example, 'action-config_en_GB.properties'.

We have found the Open Source tool Attesoro (http://attesoro.org/) to be ideal for editing the
language pack files.


=============
Common Words
=============
space = zona
check in = actualizeaza si deblocheaza
check out = modifica si blocheaza
repository = depozit date
store = stoc
dashboard= tabla de lucru
deploy = a instala / instalare
contributor = autor
reviewer = referent / redactor

============
Installation
============

- Copy all files into <extension-config>/messages folder.

- Edit the 'web-client-config-custom.xml' file in the <extension-config> folder to set what languages
  you wish to be available:

  - Find the '<languages>' section
  - Add or remove languages of the form:

       '<language locale="ro_RO">Romana</language>'

- The order of the language entries determines the order they are presented on the login prompt.
- Save the file.

- Restart the Alfresco server.

=================
Known Bugs
=================
- In wcm in xforms editor, date fields don't appear because of wrong format, 
a workaround is to add in the beginning of file <alf_home>\tomcat\webapps\alfresco\scripts\ajax\xforms.js, 
in constants section, next lines

////////////////////////////////////////////////////////////////////////////////
//begin romanian locale workaround
if (alfresco.xforms.constants.DATE_FORMAT == 'jj.nn.aaaa') {
	alfresco.xforms.constants.DATE_FORMAT = 'M/d/yy'
}
if (alfresco.xforms.constants.TIME_FORMAT == 'HH:mm'){
	alfresco.xforms.constants.TIME_FORMAT = 'h:mm a'
}
if (alfresco.xforms.constants.DATE_TIME_FORMAT == 'jj.nn.aaaa HH:mm') {
	alfresco.xforms.constants.DATE_TIME_FORMAT = 'M/d/yy h:mm a'
}
//end romanian locale workaround


==================================
Contributors to this Language Pack
==================================

See the Alfresco Forum for status on Language Packs:
http://forums.alfresco.com/viewforum.php?f=16

See the Alfresco Forum for status on Romanian Language Pack:
http://forums.alfresco.com/viewtopic.php?t=7788

Original Author(s): http://www.gzk.ro, GZK Software, Bucuresti, Romania
Contributors: Monica Guzik