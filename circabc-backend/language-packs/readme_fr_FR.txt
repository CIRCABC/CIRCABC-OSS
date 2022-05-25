======================
Alfresco Language Pack
======================

Pour la version : 3.0 - Interface SHARE
Pour la langue  : fr_FR


===========================
Contenu de ce Language Pack
===========================

La traduction de ces fichiers est réalisée par l'outil open-source OmegaT (http://www.omegat.org/).


============
Installation
============

Le pack doit être déployé dans :
	tomcat\shared\classes\alfresco\web-extension\site-webscripts

Le fichier "slingshot.properties" doit être placé dans :
	tomcat\shared\classes\alfresco\messages


IMPORTANT : 
- La structure doit être préservée (site-webscripts/org/alfresco/...)


==============
Fonctionnement
==============
	
L'ensemble sera chargé par Share automatiquement, et l'affichage fonction de la variable LOCALE envoyée par le navigateur. 

Exemple dans Firefox 3 : Options / Contenu / panneau "Langues" / bouton "Choisir..." /et placer "Français/France (fr-FR)" en tête de liste.

Fonctionnel uniquement à partir de :
- Alfresco Labs 3.0c et plus
- Alfresco Enterprise 3.0 et plus
	

=========================
Note pour les traducteurs
=========================

Si le message contient une variable, c'est à dire {0}, alors les apostrophes (') doivent être doublées ('') pour être affichées correctement. Si le message ne contient pas de variable, alors une simple apostrophe peut être utilisée.


================================
Groupes de discussion et projets
================================

Voir le groupe de discussion Alfresco Forum pour le statut des Language Packs :
http://www.alfresco.org/forums/viewforum.php?f=16

Pour le language pack Français :
http://forums.alfresco.com/viewtopic.php?t=150

Sur le forum francophone :
http://forums.alfresco.com/fr/viewtopic.php?f=9&t=2217

Pour télécharger le pack :
http://forge.alfresco.com/projects/languagefr/


=============
Contributeurs
=============

Auteur(s) Originaux                     : L'équipe Alfresco
Traduction originale Anglais - Français : Michael Harlaut
Contributeurs à cette version           : Nicolas Moreau 
                                          Laurent Meunier 
					  Thomas Broyer
