<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8">

    <link rel="shortcut icon" href="#" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/font-awesome-4.7.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="css/eu-captcha-style.css">

    <script src="https://code.jquery.com/jquery-3.6.1.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js"></script>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.5.0/font/bootstrap-icons.css">
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/js/bootstrap.min.js"></script>
    <script>var ctx = "${pageContext.request.contextPath}"</script>
    <script src="${pageContext.request.contextPath}/js/restWhatsUpCaptcha.js" Prefer></script>

    <title>EU Captcha Rotate</title>
</head>
<body onload="getLastSelectedValue()">
    <div>
        <ul id="navigation" >
            <button class="btn btn-lg btn-primary" id="textual" onclick="location.href ='./'">Textual Captcha</button>
            <button class="btn btn-lg btn-primary" id="rotate" onclick="location.href ='./rotate'">Rotational Captcha</button>
        </ul>
    </div>
    <div class="container">
         <div class="row text-center">
             <div class="col-md-12">
                 <div class="row text-center">
                    <div class=" col-md-12 alert alert-success" id="success" role="alert" style="visibility : hidden">
                     <i class="fa fa-check-circle-o fa-3x" aria-hidden="true"></i> <fmt:message key="euCaptcha.valid" />
                    </div>
                    <div class=" col-md-12 alert alert-danger" id="fail" role="alert" style="visibility : hidden">
                     <i class="fa fa-exclamation-triangle fa-3x" aria-hidden="true"></i> <fmt:message key="euCaptcha.invalid.whatsup" />
                    </div>
                    <div class=" col-md-12 alert alert-danger" id="error" role="alert" style="visibility : hidden">
                     <i class="fa fa-exclamation-triangle fa-3x" aria-hidden="true"></i> <fmt:message key="euCaptcha.error" />
                    </div>
                </div>
                 <div class="panel panel-default bg border">
                     <div class="panel-body">
                         <div class="row ">
                             <div class="col-md-12">
                                 <div class="form-group">
                                    <label title="Change the language" for="dropdown-language"><fmt:message key="language.change" /> :  </label>
                                    <select id="dropdown-language" class="custom-select  form-control">
                                        <option value=" "> ... </option>
                                        <option value="en-GB">English</option>
                                        <option value="fr-FR">Français</option>
                                        <option value="de-DE">Deutsch</option>
                                        <option value="bg-BG">български</option>
                                        <option value="hr-HR">Hrvatski</option>
                                        <option value="da-DK">Dansk</option>
                                        <option value="es-ES">Español</option>
                                        <option value="et-EE">Eesti keel</option>
                                        <option value="fi-FI">Suomi</option>
                                        <option value="el-GR">ελληνικά</option>
                                        <option value="hu-HU" >Magyar</option>
                                        <option value="it-IT">Italiano</option>
                                        <option value="lv-LV">Latviešu valoda</option>
                                        <option value="lt-LT">Lietuvių kalba</option>
                                        <option value="mt-MT">Malti</option>
                                        <option value="nl-NL">Nederlands</option>
                                        <option value="pl-PL">Polski</option>
                                        <option value="pt-PT">Português</option>
                                        <option value="ro-RO">Română</option>
                                        <option value="sk-SK">Slovenčina</option>
                                        <option value="sl-SI">Slovenščina</option>
                                        <option value="sv-SE">Svenska</option>
                                        <option value="cs-CZ">čeština</option>
                                    </select>
                                 </div>
                             </div>
                             <div class="col-md-4"></div>
                         </div>
                         <div class="row">
                             <div class="col-md-1"></div>
                                <button title="Use a slider" id="btnslider" class="btn btn-warning"><fmt:message key="euCaptcha.button.slider"/></button>
                                <button title="Use arrows" id="btnarrows" class="btn btn-warning"><fmt:message key="euCaptcha.button.arrows"/></button>
                         </div>
                         <hr>
                         <div class="row">
                             <div class="col-md-1"></div>
                             <div class="col-md-8">
                                    <button id="btnToLeft" class="btn btn-primary btn-lg"><fmt:message key="euCaptcha.turn"/> <i class="bi-arrow-counterclockwise" style="font-size:30px"></i></button>
                                    <div class="captchaContainer">
                                        <img id="captchaImage" src="" alt="duck" width="200" height="200" captchaId="">
                                    </div>
                                    <button id="btnToRight" class="btn btn-primary btn-lg"><fmt:message key="euCaptcha.turn"/> <i class="bi bi-arrow-clockwise" style="font-size:30px"></i></button>
                                </div>
                             <div class="col-md-2">
                                 <label title="Reload the Captcha" for="captchaReload"><fmt:message key="euCaptcha.button.reload"/></label>
                                 <button title="Reload the Captcha" class="btn btn-primary btn-lg " id="captchaReload"> <em class="fa fa-refresh"></em> </button>
                             </div>
                         </div>
                         <hr>
                         <div class="row">
                             <div class="col-md-1"></div>
                             <div class="col-md-8">
                                 <div id="slidecontainer">
                                     <label title="Rotate slider" for="captcha-range"><fmt:message key="euCaptcha.button.slider"/></label>
                                    <input type="range" min="-360" max="360" value="0" class="slider" step="15" id="captcha-range">
                                    <p><fmt:message key="euCaptcha.rotation.angle" /> <span id="captcha-range-value"></span></p>
                                 </div>
                             </div>
                             <div class="col-md-2">
                                 <button title="Validate the Captcha" id="validateWhatsUpCaptcha" class="btn btn-success" ><fmt:message key="euCaptcha.validate" /></button>
                             </div>
                         </div>
                         <br>
                    </div>
                 </div>
                 <div class="col-md-3">
                    <form>
                        <input id="captchaAnswer" type="hidden" value="0">
                    </form>
                 </div>
             </div>
         </div>
    </div>
</body>
</html>