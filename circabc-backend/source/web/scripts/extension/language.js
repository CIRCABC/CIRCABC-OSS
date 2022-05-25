/* --- Language selector --- */

arrowSwitch = true;			// Global variable to switch between opening and closing

function initLanguage(lang) {
  var p = document.getElementById('official');
  var s = document.getElementById('linkBoxLanguage');
  var t = document.getElementById('linkBoxArrow')
	if (p) {
	document.write('<style type="text/css" media="screen">#linkBoxTools {margin-right: 115px;}\n#linkBoxLanguage {display: block;}</style>');

	var a = p.getElementsByTagName ('a'); // all the <a>
  var SelectedLangTitle;

  var html = '<div id="langsContainer"><div id="langsContent">';

  if( a.length > 0) {
		html += '<div class="langs" onblur="ypSlideOutMenu.hideMenu(\'langs\');">';
    for (var i = 0; i < a.length; i++) {
    	var A_href     = a.item (i).getAttribute ('href');
    	var A_class    = a.item (i).className;
    	var A_lang     = a.item (i).getAttribute ('lang');
    	var A_hreflang = a.item (i).getAttribute ('hreflang');
    	var A_title    = a.item (i).getAttribute ('title');
    	var A_lng      = a.item (i).innerHTML;
			
			
			
    	if (A_class == 'lang') {
				html += '<a onfocus="ypSlideOutMenu.showMenu(\'langs\');"';
				if (i == 0) html += ' id="firstLang"';
				 html += ' href="'+A_href+'" title="'+A_title+'">'+A_title+ ' (' + A_lang + ')<br>';
	      		} else {
				SelectedLangTitle = A_title+ ' (' + A_lang + ')';
		      html += '<a focus="ypSlideOutMenu.showMenu(\'langs\');"'; 
			if (i == 0) html += ' id="firstLang"';
			html+= ' href="'+A_href+'" title="'+A_title+'" class="langSelected">'+A_title + ' (' + A_lang + ')<br>';
		    }
    	}
       html += '<\/div>';
    }
    p.innerHTML = '';
    }
    p = document.getElementById('unofficial');

    if (p) {
    a = p.getElementsByTagName ('a'); // all the <a>
    
    if( a.length > 0) {
			html+= '<div class="unofficialLangs" onblur="ypSlideOutMenu.hideMenu(\'langs\');">';

        for (var i = 0; i < a.length; i++) {
      		var A_href     = a.item (i).getAttribute ('href');
      		var A_class    = a.item (i).className;
      		var A_lang     = a.item (i).getAttribute ('lang');
      		var A_hreflang = a.item (i).getAttribute ('hreflang');
      		var A_title    = a.item (i).getAttribute ('title');
      		var A_lng      = a.item (i).innerHTML;

			
			
      		if (A_class == 'lang') {
        		html += '<a focus="ypSlideOutMenu.showMenu(\'langs\');"';
			if (i == 0) html += ' id="firstLang"';
			 html += ' href="'+A_href+'" title="'+A_title+'" class="lang_item">' + A_title + ' (' + A_lang + ')<br>';

      		} else {
			SelectedLangTitle = A_title + ' (' + A_lang + ')';
        		html += '<a focus="ypSlideOutMenu.showMenu(\'langs\');"';
			if (i == 0) html += ' id="firstLang"';
			html+= ' href="'+A_href+'" title="'+A_title+'" class="langSelected">' + A_title + ' (' + A_lang + ')<br>';
      		}
    	}
	html += '<\/div>';
	
	document.getElementById('firstTab').onfocus = function() { ypSlideOutMenu.hideMenu('langs'); };
	document.getElementById('legalNotice').onfocus = function() { ypSlideOutMenu.hideMenu('langs'); };
	}
	p.innerHTML = '';
	}

    html += '<\/div><\/div>';
    s.innerHTML = '<a style="float: right;" onclick="showMenu(\'arrow\');" onmouseout="ypSlideOutMenu.hideMenu(\'langs\');" href="#">'+SelectedLangTitle +'</a>';
		t.innerHTML = '<img src="/wel/template_2007/images/languagebox_cursor.jpg"  border="0" id="languageArrow" onclick="showMenu(\'arrow\');">';
    document.getElementById('langsFormContainer').innerHTML = html;
}

function ResetShortcuts () {
  
}

function showMenu()	{
		ypSlideOutMenu.showMenu('langs');
		document.getElementById('firstLang').focus();	
	}

// Show and hide menu by clicking the Arrow
function showMenu(arrow)	{
	if(arrow) {
		arrowSwitch = !arrowSwitch;
		if (arrowSwitch)
			{ ypSlideOutMenu.hideMenu('langs');	}
			else { 
				ypSlideOutMenu.showMenu('langs');
				document.getElementById('firstLang').focus();
				arrowSwitch = !arrowSwitch;
			}
	}	else {
			ypSlideOutMenu.showMenu('langs');
			document.getElementById('firstLang').focus();	
		}
}



function JumpMenu (item) {
  document.location = item;
}



/*****************************************************
* ypSlideOutMenu
* 3/04/2001
*
* a nice little script to create exclusive, slide-out
* menus for ns4, ns6, mozilla, opera, ie4, ie5 on 
* mac and win32. I've got no linux or unix to test on but 
* it should(?) work... 
*
* Revised:
* - 08/29/2002 : added .hideAll()
* - 04/15/2004 : added .writeCSS() to support more 
*                than 30 menus.
*
* --youngpup--
*****************************************************/

ypSlideOutMenu.Registry = []
ypSlideOutMenu.aniLen = 250
ypSlideOutMenu.hideDelay = 900
ypSlideOutMenu.minCPUResolution = 5


// constructor
function ypSlideOutMenu(id, dir, left, top, width, height) {
	
	this.ie = document.all ? 1 : 0
	this.ns4 = document.layers ? 1 : 0
	this.dom = document.getElementById ? 1 : 0
	if (this.ie || this.ns4 || this.dom) {
		this.id = id
		this.dir = dir
		this.orientation = dir == "left" || dir == "right" ? "h" : "v"
		this.dirType = dir == "right" || dir == "down" ? "-" : "+"
		this.dim = this.orientation == "h" ? width : height
		this.hideTimer = false
		this.aniTimer = false
		this.open = false
		this.over = false
		this.startTime = 0
		this.gRef = "ypSlideOutMenu_"+id
		eval(this.gRef+"=this")
		ypSlideOutMenu.Registry[id] = this
		var d = document
		var strCSS = "";
		strCSS += '#' + this.id + 'container { '
		strCSS += 'top:15px; '
		strCSS += 'z-index:10; }'
		strCSS += '#' + this.id + 'container, #' + this.id + 'Content {position:absolute; '
		strCSS += '}'

		this.css = strCSS;
		this.load()

		
	}
}

ypSlideOutMenu.writeCSS = function() {
	document.writeln('<style type="text/css">');
	for (var id in ypSlideOutMenu.Registry) {
		document.writeln(ypSlideOutMenu.Registry[id].css);
	}
	document.writeln('</style>');
}

ypSlideOutMenu.prototype.load = function() {
	var d = document
	var lyrId1 = this.id + "Container"
	var lyrId2 = this.id + "Content"
	var obj1 = this.dom ? d.getElementById(lyrId1) : this.ie ? d.all[lyrId1] : d.layers[lyrId1]
	if (obj1) var obj2 = this.ns4 ? obj1.layers[lyrId2] : this.ie ? d.all[lyrId2] : d.getElementById(lyrId2)
	var temp
	if (!obj1 || !obj2) 
		window.setTimeout(this.gRef + ".load()", 100)
	else {
		this.container = obj1
		this.menu2 = obj2
		this.style = this.ns4 ? this.menu2 : this.menu2.style
		this.homePos = eval("0" + this.dirType + this.dim)
		this.outPos = 0
		this.accelConst = (this.outPos - this.homePos) / ypSlideOutMenu.aniLen / ypSlideOutMenu.aniLen 
		// set event handlers.
		if (this.ns4) this.menu2.captureEvents(Event.MOUSEOVER | Event.MOUSEOUT);
		this.menu2.onmouseover = new Function("ypSlideOutMenu.showMenu('" + this.id + "')")
		this.menu2.onmouseout = new Function("ypSlideOutMenu.hideMenu('" + this.id + "')")
		//set initial state
		this.endSlide()
	}
}

ypSlideOutMenu.showMenu = function(id) {
	var reg = ypSlideOutMenu.Registry
	var obj = ypSlideOutMenu.Registry[id]

	if (obj.container) {
		obj.over = true
		for (menu2 in reg) if (id != menu2) ypSlideOutMenu.hide(menu2)
		if (obj.hideTimer) { reg[id].hideTimer = window.clearTimeout(reg[id].hideTimer) }
		if (!obj.open && !obj.aniTimer) reg[id].startSlide(true)
	}
}

ypSlideOutMenu.hideMenu = function(id) {
	var obj = ypSlideOutMenu.Registry[id]
	if (obj.container) {
		if (obj.hideTimer) window.clearTimeout(obj.hideTimer)
		obj.hideTimer = window.setTimeout("ypSlideOutMenu.hide('" + id + "')", ypSlideOutMenu.hideDelay);
	}
}

ypSlideOutMenu.hideAll = function() {
	var reg = ypSlideOutMenu.Registry
	for (menu2 in reg) {
		ypSlideOutMenu.hide(menu2);
		if (menu2.hideTimer) window.clearTimeout(menu2.hideTimer);
	}
}

ypSlideOutMenu.hide = function(id) {
	var obj = ypSlideOutMenu.Registry[id]
	obj.over = false
	if (obj.hideTimer) window.clearTimeout(obj.hideTimer)
	obj.hideTimer = 0
	if (obj.open && !obj.aniTimer) obj.startSlide(false)
}

ypSlideOutMenu.prototype.startSlide = function(open) {
	this[open ? "onactivate" : "ondeactivate"]()
	this.open = open
	if (open) this.setVisibility(true)
	this.startTime = (new Date()).getTime() 
	this.aniTimer = window.setInterval(this.gRef + ".slide()", ypSlideOutMenu.minCPUResolution)
}

ypSlideOutMenu.prototype.slide = function() {
	var elapsed = (new Date()).getTime() - this.startTime
	if (elapsed > ypSlideOutMenu.aniLen) 
		this.endSlide()
	else {
		var d = Math.round(Math.pow(ypSlideOutMenu.aniLen-elapsed, 2) * this.accelConst)
		if (this.open && this.dirType == "-") d = -d
		else if (this.open && this.dirType == "+") d = -d
		else if (!this.open && this.dirType == "-") d = -this.dim + d
		else d = this.dim + d
		this.moveTo(d)
	}
}

ypSlideOutMenu.prototype.endSlide = function() {
	this.aniTimer = window.clearTimeout(this.aniTimer)
	this.moveTo(this.open ? this.outPos : this.homePos)
	if (!this.open) this.setVisibility(false)
	if ((this.open && !this.over) || (!this.open && this.over)) {
		this.startSlide(this.over)
	}
}

ypSlideOutMenu.prototype.setVisibility = function(bShow) { 
	var s = this.ns4 ? this.container : this.container.style
//	s.visibility = bShow ? "visible" : "hidden"
	s.visibility = "visible";
}

ypSlideOutMenu.prototype.moveTo = function(p) { 
	this.style[this.orientation == "h" ? "left" : "top"] = this.ns4 ? p : p + "px"
}

ypSlideOutMenu.prototype.getPos = function(c) {
	return parseInt(this.style[c])
}

ypSlideOutMenu.prototype.onactivate = function() {}
ypSlideOutMenu.prototype.ondeactivate = function() { }


//function ypSlideOutMenu(id, dir, left, top, width, height) {

// set menu2 specs here
var menus = [
	new ypSlideOutMenu("langs", "down", 0, 2, 160, 800),
	
]


 ypSlideOutMenu.writeCSS();