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
import { Node } from './node';


export interface News { 
    id?: string;
    /**
     * The object that is used to compile all the translations of a node into a JSON object. It is basically composed of a map with a key languaguage code and its value 
     */
    title?: { [key: string]: string; };
    content?: string;
    date?: string;
    pattern?: News.PatternEnum;
    layout?: News.LayoutEnum;
    /**
     * valid values are  1 / 2 / 3 
     */
    size?: number;
    files?: Array<Node>;
    url?: string;
    modified?: string;
    modifier?: string;
    created?: string;
    creator?: string;
    /**
     * Representation of the permissions given to the user making the call. To know what are the permissions for each services inside one group. 
     */
    permissions?: { [key: string]: string; };
    properties?: { [key: string]: string; };
}
export namespace News {
    export type PatternEnum = 'text' | 'document' | 'image' | 'date' | 'iframe';
    export const PatternEnum = {
        Text: 'text' as PatternEnum,
        Document: 'document' as PatternEnum,
        Image: 'image' as PatternEnum,
        Date: 'date' as PatternEnum,
        Iframe: 'iframe' as PatternEnum
    };
    export type LayoutEnum = 'normal' | 'important' | 'reminder';
    export const LayoutEnum = {
        Normal: 'normal' as LayoutEnum,
        Important: 'important' as LayoutEnum,
        Reminder: 'reminder' as LayoutEnum
    };
}


