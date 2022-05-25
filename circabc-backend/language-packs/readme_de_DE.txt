readme.txt: 

====================== 
Alfresco Language Pack 
====================== 

For release: 2.1RC

For locale: de-DE (default) 


============================== 
Contents of this Language Pack 
============================== 

*_de_DE.properties 


============ 
Installation 
============ 

- Download the File ge_language_pack_21rc1 from SourceForge 

- Decompress it to a temporary folder 

- Copy all files into <config>/messages folder. From release 1.2, the config folder can be in a 
  different location to the web application root. <config> under tomcat is usually something like: 
  alfresco/tomcat/webapps/alfresco/WEB-INF/classes/alfresco 

- Edit the 'web-client-config.xml' file in the <config root> folder to set what languages you wish 
  to be available: 

- Find the '<languages>' section 
- Add or remove languages of the form: 

'<language locale="de_DE">German</language>' 

- The order of the language entries determines the order they are presented on the login prompt. 
- Save the file. 

- Restart the Alfresco server. 

================================== 
Contributors to this Language Pack 
================================== 

See the Alfresco Forum for status on Language Packs: 
http://www.alfresco.org/forums/viewforum.php?f=16 

Original Author(s): http://www.cinq.net, Frankfurt Germany 
