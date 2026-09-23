let useAudio = false;
let EuCaptchaToken;

function onPlayAudio() {
  useAudio = true;
}
function capitalized() {
  const checkBox = document.getElementById('capitalized');
  if (checkBox.checked === true) {
    sessionStorage.setItem('capitalized', 'true');
  } else {
    sessionStorage.setItem('capitalized', 'false');
  }
  getcaptcha();
}
function getLastSelectedValue() {
  const language = sessionStorage.getItem('language');
  if (language) {
    document.getElementById('dropdown-language').value = language;
  } else {
    sessionStorage.setItem('language', 'en-GB');
    document.getElementById('dropdown-language').value = 'en-GB';
  }
  // Initialize capitalized setting if not set
  const capitalized = sessionStorage.getItem('capitalized');
  if (!capitalized) {
    sessionStorage.setItem('capitalized', 'true');
  }
  // Load the captcha after initializing settings
  getcaptcha();
}
function getLanguage() {
  let language = sessionStorage.getItem('language');
  if (language) {
    return language;
  } else {
    return 'en-GB';
  }
}
function getCaptchaLength(queryString) {
  const urlParams = new URLSearchParams(queryString);
  const captchaLength = urlParams.get('captchaLength');
  if (captchaLength) {
    return captchaLength;
  } else {
    return 10;
  }
}
function getcaptcha() {
  const getCaptchaUrl = $.ajax({
    type: 'GET',
    url:
      './api/captchaImg?locale=' +
      getLanguage() +
      '&captchaLength=' +
      getCaptchaLength(window.location.search) +
      '&capitalized=' +
      sessionStorage.getItem('capitalized'),
    success: function (jsonData) {
      EuCaptchaToken = getCaptchaUrl.getResponseHeader('x-jwtString');
      $('#captchaImg').attr(
        'src',
        'data:image/png;base64,' + jsonData.captchaImg
      );
      $('#captchaImg').attr('captchaId', jsonData.captchaId);
      $('#audioCaptcha').attr(
        'src',
        'data:audio/wav;base64,' + jsonData.audioCaptcha
      );
    },
  });
}
$(function () {
  function reloadCaptcha() {
    const reloadCaptchaUrl = $.ajax({
      type: 'GET',
      url:
        './api/reloadCaptchaImg/' +
        $('#captchaImg').attr('captchaId') +
        '?locale=' +
        sessionStorage.getItem('language') +
        '&captchaLength=' +
        getCaptchaLength(window.location.search) +
        '&capitalized=' +
        sessionStorage.getItem('capitalized'),
      beforeSend: function (xhr) {
        xhr.setRequestHeader('Accept', 'application/json');
        xhr.setRequestHeader('Content-Type', 'application/json');
        xhr.setRequestHeader('x-jwtString', EuCaptchaToken);
      },
      success: function (jsonData) {
        EuCaptchaToken = reloadCaptchaUrl.getResponseHeader('x-jwtString');
        $('#captchaImg').attr(
          'src',
          'data:image/png;base64,' + jsonData.captchaImg
        );
        $('#captchaImg').attr('captchaId', jsonData.captchaId);
        $('#audioCaptcha').attr(
          'src',
          'data:audio/wav;base64,' + jsonData.audioCaptcha
        );
        $('#captchaAnswer').val('');
        useAudio = false;
      },
    });
  }
  function validateCaptcha() {
    const validateCaptcha = $.ajax({
      type: 'POST',
      url: './api/validateCaptcha/' + $('#captchaImg').attr('captchaId'),
      beforeSend: function (xhr) {
        xhr.setRequestHeader('Accept', 'application/json');
        xhr.setRequestHeader(
          'Content-Type',
          'application/x-www-form-urlencoded'
        );
        xhr.setRequestHeader('x-jwtString', EuCaptchaToken);
      },
      data: {
        captchaAnswer: $('#captchaAnswer').val(),
        useAudio: useAudio,
      },
      dataType: 'json',
      cache: false,
      timeout: 600000,
      success: function (obj) {
        $('input').css({ border: '' });
        if ('success' === obj.responseCaptcha) {
          $('#success').css('visibility', 'visible');
          $('#fail').css('visibility', 'hidden');
        } else {
          $('#fail').css('visibility', 'visible');
          $('#success').css('visibility', 'hidden');
          reloadCaptcha();
        }
      },
      error: function (e) {
        $('input').css({ border: '2px solid red' });
        $('#error').css('visibility', 'visible');
        $('#fail').css('visibility', 'hidden');
        $('#success').css('visibility', 'hidden');
        reloadCaptcha();
      },
    });
  }

  $('#captchaReload').click(function () {
    $('#fail').css('visibility', 'hidden');
    $('#success').css('visibility', 'hidden');
    reloadCaptcha();
  });

  $('#captchaSubmit').click(function () {
    validateCaptcha();
  });

  $('#captchaAnswer').keypress(function (e) {
    if (e.keyCode === 13) {
      validateCaptcha();
      return false; // prevent the button click from happening
    }
  });

  $(document).ready(function () {
    $('#dropdown-language').change(function () {
      const selectedOption = $('#dropdown-language').val();
      $('#dropdown-language').val(selectedOption);
      if (selectedOption !== '') {
        // Validate language parameter against allowed values to prevent open redirect
        const allowedLanguages = ['en-GB', 'fr-FR', 'de-DE', 'bg-BG', 'hr-HR', 'da-DK', 'es-ES', 'et-EE', 'fi-FI', 'el-GR', 'hu-HU', 'it-IT', 'lv-LV', 'lt-LT', 'mt-MT', 'nl-NL', 'pl-PL', 'pt-PT', 'ro-RO', 'sk-SK', 'sl-SI', 'sv-SE', 'cs-CZ'];
        if (allowedLanguages.includes(selectedOption)) {
          sessionStorage.setItem('language', selectedOption);
          window.location.replace('?lang=' + encodeURIComponent(selectedOption));
        }
      }
    });
    var checkBox = document.getElementById('capitalized');
    if (checkBox.checked === true) {
      sessionStorage.setItem('capitalized', 'true');
    } else {
      sessionStorage.setItem('capitalized', 'false');
    }
    // Remove duplicate getcaptcha() call - it's now called in getLastSelectedValue()
  });
});
