package com.sii.eucaptcha.controller;

import com.google.gson.JsonObject;
import com.sii.eucaptcha.controller.constants.CaptchaConstants;
import com.sii.eucaptcha.controller.dto.captchaquery.CaptchaQueryDto;
import com.sii.eucaptcha.controller.dto.captcharesult.CaptchaResultDto;
import com.sii.eucaptcha.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author mousab.aidoud
 * Captcha Rest Controller class with those methodes : getCaptchaImage , reloadCaptchaImage , validateCaptcha.
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class CaptchaController {

  private static final String LOCALE_MISSING_OR_INVALID =
    "Locale is missing or invalid!";
  private static final String CAPTCHA_ID_MISSING = "CaptchaId is missing!";

  private final CaptchaService captchaService;

  public CaptchaController(CaptchaService captchaService) {
    this.captchaService = captchaService;
  }

  /**
   * Get the Captcha (Id + Captcha Image + Captcha Audio)
   *
   * @param locale the chosen locale
   * @return response as String contains CaptchaID and Captcha Image
   */
  @CrossOrigin
  @Operation(
    summary = "Get a Captcha image",
    description = "Returns a captcha image as per locale, captchaLength, type and capitalization or not"
  )
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved captcha image"
      ),
      @ApiResponse(
        responseCode = "406",
        description = "Not Acceptable - Locale is missing or invalid"
      ),
    }
  )
  @GetMapping(value = "/captchaImg")
  public CaptchaResultDto getCaptchaImage(
    @RequestParam(defaultValue = "en-GB", required = false) String locale,
    @RequestParam(defaultValue = "8", required = false) Integer captchaLength,
    @RequestParam(
      defaultValue = CaptchaConstants.STANDARD,
      required = false
    ) String captchaType,
    @RequestParam(defaultValue = "true", required = false) boolean capitalized,
    @RequestParam(required = false) Integer degree
  ) {
    log.debug(
      "Request with language: {}, length: {}, type: {}, capitalized: {} and degrees: {}",
      locale,
      captchaLength,
      captchaType,
      capitalized,
      degree
    );

    if (
      StringUtils.isBlank(locale) ||
      "Undefined".equalsIgnoreCase(locale)
    ) {
      log.debug(LOCALE_MISSING_OR_INVALID);
      throw new ResponseStatusException(
        HttpStatus.NOT_ACCEPTABLE,
        LOCALE_MISSING_OR_INVALID
      );
    }

    CaptchaQueryDto captchaQueryDto =
      new CaptchaQueryDto.CaptchaQueryDtoBuilder(captchaType)
        .captchaLength(captchaLength)
        .locale(locale)
        .degree(degree)
        .capitalized(capitalized)
        .build();

    return captchaService.generateCaptchaWrapper(captchaQueryDto);
  }

  /**
   * Reloading the captcha Image
   *
   * @param previousCaptchaId the ID of the previous Captcha
   * @param locale            the chosen Locale
   * @return response as String contains CaptchaID and Captcha Image
   */
  @CrossOrigin
  @Operation(
    summary = "Refresh a previous Captcha image",
    description = "Returns a new captcha image as per locale, captchaLength, type and capitalization or not"
  )
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved captcha image"
      ),
      @ApiResponse(responseCode = "400", description = "CaptchaId is missing"),
      @ApiResponse(
        responseCode = "406",
        description = "Not Acceptable - Locale is missing or invalid"
      ),
    }
  )
  @GetMapping(value = "/reloadCaptchaImg/{previousCaptchaId}")
  public CaptchaResultDto reloadCaptchaImage(
    @PathVariable("previousCaptchaId") String previousCaptchaId,
    @RequestParam(required = false) String locale,
    @RequestParam(required = false) Integer captchaLength,
    @RequestParam(
      defaultValue = CaptchaConstants.STANDARD,
      required = false
    ) String captchaType,
    @RequestParam(required = false) Boolean capitalized,
    @RequestParam(required = false) Integer degree
  ) {
    log.debug(
      "Reload requested with previousCaptchaId: {}, language: {}, length: {}, type: {}, capitalized: {} and degrees: {}",
      previousCaptchaId,
      locale,
      captchaLength,
      captchaType,
      capitalized,
      degree
    );

    if (
      StringUtils.isBlank(locale) ||
      "Undefined".equalsIgnoreCase(locale)
    ) {
      log.debug(LOCALE_MISSING_OR_INVALID);
      throw new ResponseStatusException(
        HttpStatus.NOT_ACCEPTABLE,
        LOCALE_MISSING_OR_INVALID
      );
    }

    if (StringUtils.isBlank(previousCaptchaId)) {
      log.debug(CAPTCHA_ID_MISSING);
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        CAPTCHA_ID_MISSING
      );
    }

    CaptchaQueryDto captchaQueryDto =
      new CaptchaQueryDto.CaptchaQueryDtoBuilder(captchaType)
        .captchaLength(captchaLength)
        .previousCaptchaId(previousCaptchaId)
        .locale(locale)
        .degree(degree)
        .capitalized(Boolean.TRUE.equals(capitalized))
        .build();
    return captchaService.generateCaptchaWrapper(captchaQueryDto);
  }

  /**
   * Validating the captcha answer :
   *
   * @param captchaId     the ID of the Captcha
   * @param captchaAnswer the answer of the Captcha -> success or fail
   * @return fail or success as String response
   */
  @CrossOrigin
  @Operation(
    summary = "Validate a Captcha image",
    description = "Returns success or failed as an answer"
  )
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        description = "Successfull response can be success or fail"
      ),
      @ApiResponse(responseCode = "400", description = "CaptchaId is missing"),
    }
  )
  @PostMapping(value = "/validateCaptcha/{captchaId}")
  public ResponseEntity<String> validateCaptcha(
    @PathVariable(value = "captchaId", required = false) String captchaId,
    @RequestParam(
      value = "captchaAnswer",
      required = false
    ) String captchaAnswer,
    @RequestParam(value = "useAudio", required = false) Boolean useAudio,
    @RequestParam(
      value = "captchaType",
      defaultValue = CaptchaConstants.STANDARD,
      required = false
    ) String captchaType
  ) {
    log.debug(
      "Validation requested with captchaId: {}, captchaAnswer: {}, useAudio: {}, type: {}",
      captchaId,
      captchaAnswer,
      useAudio,
      captchaType
    );

    //check if captchaId is present
    if (StringUtils.isBlank(captchaId)) {
      log.error(CAPTCHA_ID_MISSING);
      return new ResponseEntity<>(CAPTCHA_ID_MISSING, HttpStatus.BAD_REQUEST);
    } else {
      //Verify the validity of the captcha answer.
      try {
        boolean responseCaptcha;
        responseCaptcha = captchaService.validateCaptcha(
          captchaId,
          captchaAnswer,
          captchaType,
          Boolean.TRUE.equals(useAudio)
        );
        JsonObject response = new JsonObject();
        //response captcha ( valid -> success || invalid -> fail  )
        response.addProperty(
          "responseCaptcha",
          responseCaptcha ? "success" : "fail"
        );
        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
      } catch (Exception e) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }
    }
  }
}
