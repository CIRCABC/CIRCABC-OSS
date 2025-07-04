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
import { GroupConfigurationNewsgroups } from './groupConfigurationNewsgroups';


/**
 * JSON object that contains all the configuration variables of an Interest Group 
 */
export interface GroupConfiguration { 
    information?: { [key: string]: string; };
    library?: { [key: string]: string; };
    newsgroups: GroupConfigurationNewsgroups;
    events?: { [key: string]: string; };
    dashboard?: { [key: string]: string; };
}

