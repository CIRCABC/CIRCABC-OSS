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
 * Representation of an Help Article in CIRCABC in a JSON object
 */
export interface HelpArticle { 
    id?: string;
    parentId?: string;
    /**
     * The object that is used to compile all the translations of a node into a JSON object. It is basically composed of a map with a key languaguage code and its value 
     */
    title?: { [key: string]: string; };
    /**
     * The object that is used to compile all the translations of a node into a JSON object. It is basically composed of a map with a key languaguage code and its value 
     */
    content?: { [key: string]: string; };
    lastUpdate?: string;
    author?: string;
    highlighted?: boolean;
    visitCounter?: number;
}

