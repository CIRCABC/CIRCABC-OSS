/**
 * CIRCABC REST API
 * This is the first version of the CIRCABC REST API used by the new User Interface 
 *
 * The version of the OpenAPI document: 4.2.4.3
 * Contact: DIGIT-CIRCABC-SUPPORT@ec.europa.eu
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


/**
 * one log activity entry describing the action performed on the IG
 */
export interface IGTimelineElement { 
    monthActivity?: string;
    service?: string;
    activity?: string;
    actionId?: number;
    actionNumber?: number;
}

