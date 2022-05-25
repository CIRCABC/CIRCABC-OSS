===========================
Alfresco Language Pack
===========================

Release : 1.4.0

Locale : ca_ES


===========================
Contingut del Language Pack
===========================

*_ca_ES.properties


===========================
Instalacio
===========================

- Copia tots els arxius a la carpeta <extension-config>/messages

- Edita l'arxiu 'web-client-config-custom.xml' a la carpeta <extension-config> per indicar els idiomes disponibles:

  - Cerca la seccio '<languages>'
  - Afegeix o esborra languages:

       '<language locale="XX_YY">LangName</language>'

	Exemple de resultat:

   <config evaluator="string-compare" condition="Languages">
     <languages>
     	<language locale="ca_ES">Catal&#224;</language>
        <language locale="fr_FR">French</language>
        <language locale="de_DE">German</language>
        <language locale="ja_JP">Japanese</language>
     </languages>
   </config>
	   
- L'ordre determinara com apareixen a la pantalla de login.
- Guarda l'arxiu.
- Reinicia el servidor d'Alfresco

======================================
Contribuents del Catalan Language Pack
======================================
Autors Originals: Alfresco Team
Traductors: Jose Blanco & Jose Carrasco @ www.in2.es {Alfresco Gold Partner}
Forge: http://forge.alfresco.com/projects/alf-catalan/