/**
 * EU Captcha Rest API
 * API for use of EU Captcha
 *
 * The version of the OpenAPI document: 1.0
 * Contact: DIGIT-EU-CAPTCHA@ec.europa.eu
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


export interface CaptchaResultDto { 
    /**
     * Generated ID of the Captcha
     */
    captchaId: string;
    /**
     * The CaptchaImage
     */
    captchaImg: string;
    /**
     * Type of the Captcha
     */
    captchaType: CaptchaResultDto.CaptchaTypeEnum;
    audioCaptcha: string;
}
export namespace CaptchaResultDto {
    export type CaptchaTypeEnum = 'STANDARD' | 'WHATS_UP';
    export const CaptchaTypeEnum = {
        STANDARD: 'STANDARD' as CaptchaTypeEnum,
        WHATSUP: 'WHATS_UP' as CaptchaTypeEnum
    };
}


