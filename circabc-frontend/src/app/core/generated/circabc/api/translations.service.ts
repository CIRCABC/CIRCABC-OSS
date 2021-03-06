/**
 * CIRCABC REST API
 * This is the first version of the CIRCABC REST API used by the new User Interface 
 *
 * The version of the OpenAPI document: 4.2.0
 * Contact: DIGIT-CIRCABC-SUPPORT@ec.europa.eu
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
/* tslint:disable:no-unused-variable member-ordering */

import { Inject, Injectable, Optional }                      from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams,
         HttpResponse, HttpEvent, HttpParameterCodec }       from '@angular/common/http';
import { CustomHttpParameterCodec }                          from '../encoder';
import { Observable }                                        from 'rxjs';

import { ContentIdTranslationsEnhancedDynamicProperties } from '../model/models';
import { ContentIdTranslationsEnhancedTitle } from '../model/models';
import { Translations } from '../model/models';

import { BASE_PATH, COLLECTION_FORMATS }                     from '../variables';
import { Configuration }                                     from '../configuration';



@Injectable({
  providedIn: 'root'
})
export class TranslationsService {

    protected basePath = 'http://localhost/service/circabc';
    public defaultHeaders = new HttpHeaders();
    public configuration = new Configuration();
    public encoder: HttpParameterCodec;

    constructor(protected httpClient: HttpClient, @Optional()@Inject(BASE_PATH) basePath: string, @Optional() configuration: Configuration) {
        if (configuration) {
            this.configuration = configuration;
        }
        if (typeof this.configuration.basePath !== 'string') {
            if (typeof basePath !== 'string') {
                basePath = this.basePath;
            }
            this.configuration.basePath = basePath;
        }
        this.encoder = this.configuration.encoder || new CustomHttpParameterCodec();
    }

    /**
     * @param consumes string[] mime-types
     * @return true: consumes contains 'multipart/form-data', false: otherwise
     */
    private canConsumeForm(consumes: string[]): boolean {
        const form = 'multipart/form-data';
        for (const consume of consumes) {
            if (form === consume) {
                return true;
            }
        }
        return false;
    }


    private addToHttpParams(httpParams: HttpParams, value: any, key?: string): HttpParams {
        if (typeof value === "object" && value instanceof Date === false) {
            httpParams = this.addToHttpParamsRecursive(httpParams, value);
        } else {
            httpParams = this.addToHttpParamsRecursive(httpParams, value, key);
        }
        return httpParams;
    }

    private addToHttpParamsRecursive(httpParams: HttpParams, value?: any, key?: string): HttpParams {
        if (value == null) {
            return httpParams;
        }

        if (typeof value === "object") {
            if (Array.isArray(value)) {
                (value as any[]).forEach( elem => httpParams = this.addToHttpParamsRecursive(httpParams, elem, key));
            } else if (value instanceof Date) {
                if (key != null) {
                    httpParams = httpParams.append(key,
                        (value as Date).toISOString().substr(0, 10));
                } else {
                   throw Error("key may not be null if value is Date");
                }
            } else {
                Object.keys(value).forEach( k => httpParams = this.addToHttpParamsRecursive(
                    httpParams, value[k], key != null ? `${key}.${k}` : k));
            }
        } else if (key != null) {
            httpParams = httpParams.append(key, value);
        } else {
            throw Error("key may not be null if value is not object or array");
        }
        return httpParams;
    }

    /**
     * create request for machine translation 
     * @param id 
     * @param language 
     * @param notify 
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public postMachineTranslation(id: string, language: string, notify?: boolean, observe?: 'body', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json'}): Observable<Translations>;
    public postMachineTranslation(id: string, language: string, notify?: boolean, observe?: 'response', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json'}): Observable<HttpResponse<Translations>>;
    public postMachineTranslation(id: string, language: string, notify?: boolean, observe?: 'events', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json'}): Observable<HttpEvent<Translations>>;
    public postMachineTranslation(id: string, language: string, notify?: boolean, observe: any = 'body', reportProgress: boolean = false, options?: {httpHeaderAccept?: 'application/json'}): Observable<any> {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling postMachineTranslation.');
        }
        if (language === null || language === undefined) {
            throw new Error('Required parameter language was null or undefined when calling postMachineTranslation.');
        }

        let queryParameters = new HttpParams({encoder: this.encoder});
        if (notify !== undefined && notify !== null) {
          queryParameters = this.addToHttpParams(queryParameters,
            <any>notify, 'notify');
        }

        let headers = this.defaultHeaders;

        // authentication (basicAuth) required
        if (this.configuration.username || this.configuration.password) {
            headers = headers.set('Authorization', 'Basic ' + btoa(this.configuration.username + ':' + this.configuration.password));
        }
        let httpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
        if (httpHeaderAcceptSelected === undefined) {
            // to determine the Accept header
            const httpHeaderAccepts: string[] = [
                'application/json'
            ];
            httpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (httpHeaderAcceptSelected !== undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }


        let responseType: 'text' | 'json' = 'json';
        if(httpHeaderAcceptSelected && httpHeaderAcceptSelected.startsWith('text')) {
            responseType = 'text';
        }

        return this.httpClient.post<Translations>(`${this.configuration.basePath}/content/${encodeURIComponent(String(id))}/machinetranslation/${encodeURIComponent(String(language))}`,
            null,
            {
                params: queryParameters,
                responseType: <any>responseType,
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * create a new translation of a  node (content) 
     * @param id 
     * @param lang 
     * @param fileData 
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public postTranslation(id: string, lang: string, fileData: Blob, observe?: 'body', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json'}): Observable<Translations>;
    public postTranslation(id: string, lang: string, fileData: Blob, observe?: 'response', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json'}): Observable<HttpResponse<Translations>>;
    public postTranslation(id: string, lang: string, fileData: Blob, observe?: 'events', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json'}): Observable<HttpEvent<Translations>>;
    public postTranslation(id: string, lang: string, fileData: Blob, observe: any = 'body', reportProgress: boolean = false, options?: {httpHeaderAccept?: 'application/json'}): Observable<any> {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling postTranslation.');
        }
        if (lang === null || lang === undefined) {
            throw new Error('Required parameter lang was null or undefined when calling postTranslation.');
        }
        if (fileData === null || fileData === undefined) {
            throw new Error('Required parameter fileData was null or undefined when calling postTranslation.');
        }

        let headers = this.defaultHeaders;

        // authentication (basicAuth) required
        if (this.configuration.username || this.configuration.password) {
            headers = headers.set('Authorization', 'Basic ' + btoa(this.configuration.username + ':' + this.configuration.password));
        }
        let httpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
        if (httpHeaderAcceptSelected === undefined) {
            // to determine the Accept header
            const httpHeaderAccepts: string[] = [
                'application/json'
            ];
            httpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (httpHeaderAcceptSelected !== undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
            'multipart/form-data'
        ];

        const canConsumeForm = this.canConsumeForm(consumes);

        let formParams: { append(param: string, value: any): any; };
        let useForm = false;
        let convertFormParamsToString = false;
        // use FormData to transmit files using content-type "multipart/form-data"
        // see https://stackoverflow.com/questions/4007969/application-x-www-form-urlencoded-or-multipart-form-data
        useForm = canConsumeForm;
        if (useForm) {
            formParams = new FormData();
        } else {
            formParams = new HttpParams({encoder: this.encoder});
        }

        if (lang !== undefined) {
            formParams = formParams.append('lang', <any>lang) as any || formParams;
        }
        if (fileData !== undefined) {
            formParams = formParams.append('fileData', <any>fileData) as any || formParams;
        }

        let responseType: 'text' | 'json' = 'json';
        if(httpHeaderAcceptSelected && httpHeaderAcceptSelected.startsWith('text')) {
            responseType = 'text';
        }

        return this.httpClient.post<Translations>(`${this.configuration.basePath}/content/${encodeURIComponent(String(id))}/translations`,
            convertFormParamsToString ? formParams.toString() : formParams,
            {
                responseType: <any>responseType,
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * create a new translation of a  node (content) this does not fire any upload notification you must call \&quot;fireNewContentNotification\&quot; 
     * @param id 
     * @param notify 
     * @param name 
     * @param title 
     * @param description 
     * @param keywords 
     * @param author 
     * @param reference 
     * @param expirationDate 
     * @param securityRanking 
     * @param status 
     * @param lang 
     * @param dynamicProperties 
     * @param file 
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public postTranslationEnhanced(id: string, notify?: boolean, name?: string, title?: ContentIdTranslationsEnhancedTitle, description?: ContentIdTranslationsEnhancedTitle, keywords?: Array<string>, author?: string, reference?: string, expirationDate?: string, securityRanking?: string, status?: string, lang?: string, dynamicProperties?: ContentIdTranslationsEnhancedDynamicProperties, file?: Blob, observe?: 'body', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json'}): Observable<Translations>;
    public postTranslationEnhanced(id: string, notify?: boolean, name?: string, title?: ContentIdTranslationsEnhancedTitle, description?: ContentIdTranslationsEnhancedTitle, keywords?: Array<string>, author?: string, reference?: string, expirationDate?: string, securityRanking?: string, status?: string, lang?: string, dynamicProperties?: ContentIdTranslationsEnhancedDynamicProperties, file?: Blob, observe?: 'response', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json'}): Observable<HttpResponse<Translations>>;
    public postTranslationEnhanced(id: string, notify?: boolean, name?: string, title?: ContentIdTranslationsEnhancedTitle, description?: ContentIdTranslationsEnhancedTitle, keywords?: Array<string>, author?: string, reference?: string, expirationDate?: string, securityRanking?: string, status?: string, lang?: string, dynamicProperties?: ContentIdTranslationsEnhancedDynamicProperties, file?: Blob, observe?: 'events', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json'}): Observable<HttpEvent<Translations>>;
    public postTranslationEnhanced(id: string, notify?: boolean, name?: string, title?: ContentIdTranslationsEnhancedTitle, description?: ContentIdTranslationsEnhancedTitle, keywords?: Array<string>, author?: string, reference?: string, expirationDate?: string, securityRanking?: string, status?: string, lang?: string, dynamicProperties?: ContentIdTranslationsEnhancedDynamicProperties, file?: Blob, observe: any = 'body', reportProgress: boolean = false, options?: {httpHeaderAccept?: 'application/json'}): Observable<any> {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling postTranslationEnhanced.');
        }

        let queryParameters = new HttpParams({encoder: this.encoder});
        if (notify !== undefined && notify !== null) {
          queryParameters = this.addToHttpParams(queryParameters,
            <any>notify, 'notify');
        }

        let headers = this.defaultHeaders;

        // authentication (basicAuth) required
        if (this.configuration.username || this.configuration.password) {
            headers = headers.set('Authorization', 'Basic ' + btoa(this.configuration.username + ':' + this.configuration.password));
        }
        let httpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
        if (httpHeaderAcceptSelected === undefined) {
            // to determine the Accept header
            const httpHeaderAccepts: string[] = [
                'application/json'
            ];
            httpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (httpHeaderAcceptSelected !== undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
            'multipart/form-data'
        ];

        const canConsumeForm = this.canConsumeForm(consumes);

        let formParams: { append(param: string, value: any): any; };
        let useForm = false;
        let convertFormParamsToString = false;
        // use FormData to transmit files using content-type "multipart/form-data"
        // see https://stackoverflow.com/questions/4007969/application-x-www-form-urlencoded-or-multipart-form-data
        useForm = canConsumeForm;
        if (useForm) {
            formParams = new FormData();
        } else {
            formParams = new HttpParams({encoder: this.encoder});
        }

        if (name !== undefined) {
            formParams = formParams.append('name', <any>name) as any || formParams;
        }
        if (title !== undefined) {
            formParams = formParams.append('title', useForm ? new Blob([JSON.stringify(title)], {type: 'application/json'}) : <any>title) as any || formParams;
        }
        if (description !== undefined) {
            formParams = formParams.append('description', useForm ? new Blob([JSON.stringify(description)], {type: 'application/json'}) : <any>description) as any || formParams;
        }
        if (keywords) {
            if (useForm) {
                keywords.forEach((element) => {
                    formParams = formParams.append('keywords', <any>element) as any || formParams;
            })
            } else {
                formParams = formParams.append('keywords', keywords.join(COLLECTION_FORMATS['csv'])) as any || formParams;
            }
        }
        if (author !== undefined) {
            formParams = formParams.append('author', <any>author) as any || formParams;
        }
        if (reference !== undefined) {
            formParams = formParams.append('reference', <any>reference) as any || formParams;
        }
        if (expirationDate !== undefined) {
            formParams = formParams.append('expirationDate', <any>expirationDate) as any || formParams;
        }
        if (securityRanking !== undefined) {
            formParams = formParams.append('securityRanking', <any>securityRanking) as any || formParams;
        }
        if (status !== undefined) {
            formParams = formParams.append('status', <any>status) as any || formParams;
        }
        if (lang !== undefined) {
            formParams = formParams.append('lang', <any>lang) as any || formParams;
        }
        if (dynamicProperties !== undefined) {
            formParams = formParams.append('dynamicProperties', useForm ? new Blob([JSON.stringify(dynamicProperties)], {type: 'application/json'}) : <any>dynamicProperties) as any || formParams;
        }
        if (file !== undefined) {
            formParams = formParams.append('file', <any>file) as any || formParams;
        }

        let responseType: 'text' | 'json' = 'json';
        if(httpHeaderAcceptSelected && httpHeaderAcceptSelected.startsWith('text')) {
            responseType = 'text';
        }

        return this.httpClient.post<Translations>(`${this.configuration.basePath}/content/${encodeURIComponent(String(id))}/translations/enhanced`,
            convertFormParamsToString ? formParams.toString() : formParams,
            {
                params: queryParameters,
                responseType: <any>responseType,
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

}
