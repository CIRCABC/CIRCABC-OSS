function addLoadEvent(func) {
    var oldonload = window.onload;
    if (typeof window.onload !== 'function') {
        window.onload = func;
    } else {
        window.onload = function () {
            if (oldonload) {
                oldonload();
            }
            func();
        };
    }
}
var noscript = addLoadEvent(noscript);
addLoadEvent(function () {
/* more code to run on page load */
 document.getElementById("script").style.visibility = "visible";
});
function noscript()
{
    if (document.removeChild)
    {
        var div = document.getElementById("noscript");
        div.parentNode.removeChild(div);
    }
    else if (document.getElementById)
    {
        document.getElementById("noscript").style.display = "none";
    }
}