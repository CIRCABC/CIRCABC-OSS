/*
 * Copyright European Community 2006 - Licensed under the EUPL V.1.0
 *
 *  		   http://ec.europa.eu/idabc/en/document/6523
 */

var _alfContextPath = null;

function showWaitProgress()
{
	showWaitProgress(2000)
}

function showWaitProgress(time)
{
	setTimeout("showProgress()", time);
}

function showProgress()
{
	new Element('iframe').setProperty('id', 'TB_HideSelect').injectInside(document.body);
    $('TB_HideSelect').setOpacity(0);

    new Element('div').setProperty('id', 'TB_overlay').injectInside(document.body);
    $('TB_overlay').setOpacity(0);
    TB_overlaySize();

    new Element('div').setProperty('id', 'TB_load').injectInside(document.body);
    new Element('img')
      .setProperty(
        'src',
        getContextPath() + "/images/icons/process_animation.gif"
      )
      .setProperty('width', '174')
      .setProperty('height', '14')
      .injectInside($('TB_load'));
	TB_load_position();

    $('TB_overlay').set('tween', {
        duration: 1200
    });
    $('TB_overlay').tween('opacity', 0, 0.6);
}


/**
 * Calculates and returns the context path for the current page
 */
function getContextPath()
{
	if (_alfContextPath == null)
   {
      var path = window.location.pathname;
      var idx = path.indexOf("/", 1);
      if (idx != -1)
      {
         _alfContextPath = path.substring(0, idx);
      }
      else
      {
         _alfContextPath = "";
      }
   }

   return _alfContextPath;
}
