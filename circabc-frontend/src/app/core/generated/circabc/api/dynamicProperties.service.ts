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
/* tslint:disable:no-unused-variable member-ordering */

import { Inject, Injectable, Optional }                      from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams,
         HttpResponse, HttpEvent, HttpParameterCodec
        }       from '@angular/common/http';
import { CustomHttpParameterCodec }                          from '../encoder';
import { Observable }                                        from 'rxjs';

// @ts-ignore
import { DynamicPropertyDefinition } from '../model/dynamicPropertyDefinition';

// @ts-ignore
import { BASE_PATH, COLLECTION_FORMATS }                     from '../variables';
import { Configuration }                                     from '../configuration';



@Injectable({
  providedIn: 'root'
})
export class DynamicPropertiesService {

    protected basePath = 'http://../service/circabc';
    public defaultHeaders = new HttpHeaders();
    public configuration = new Configuration();
    public encoder: HttpParameterCodec;

    constructor(protected httpClient: HttpClient, @Optional()@Inject(BASE_PATH) basePath: string|string[], @Optional() configuration: Configuration) {
        if (configuration) {
            this.configuration = configuration;
        }
        if (typeof this.configuration.basePath !== 'string') {
            if (Array.isArray(basePath) && basePath.length > 0) {
                basePath = basePath[0];
            }

            if (typeof basePath !== 'string') {
                basePath = this.basePath;
            }
            this.configuration.basePath = basePath;
        }
        this.encoder = this.configuration.encoder || new CustomHttpParameterCodec();
    }


    // @ts-ignore
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
                    httpParams = httpParams.append(key, (value as Date).toISOString().substring(0, 10));
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
     * remove one dynamic property from the Interest group
     * @param id 
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public deleteDynamicPropertyDefinition(id: string, observe?: 'body', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json',}): Observable<DynamicPropertyDefinition>;
    public deleteDynamicPropertyDefinition(id: string, observe?: 'response', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json',}): Observable<HttpResponse<DynamicPropertyDefinition>>;
    public deleteDynamicPropertyDefinition(id: string, observe?: 'events', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json',}): Observable<HttpEvent<DynamicPropertyDefinition>>;
    public deleteDynamicPropertyDefinition(id: string, observe: any = 'body', reportProgress: boolean = false, options?: {httpHeaderAccept?: 'application/json',}): Observable<any> {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling deleteDynamicPropertyDefinition.');
        }

        let localVarHeaders = this.defaultHeaders;

        let localVarCredential: string | undefined;
        // authentication (basicAuth) required
        localVarCredential = this.configuration.lookupCredential('basicAuth');
        if (localVarCredential) {
            localVarHeaders = localVarHeaders.set('Authorization', 'Basic ' + localVarCredential);
        }

        let localVarHttpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
        if (localVarHttpHeaderAcceptSelected === undefined) {
            // to determine the Accept header
            const httpHeaderAccepts: string[] = [
                'application/json'
            ];
            localVarHttpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }



        let responseType_: 'text' | 'json' | 'blob' = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            } else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            } else {
                responseType_ = 'blob';
            }
        }

        let localVarPath = `/dynprops/${this.configuration.encodeParam({name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: undefined})}`;
        return this.httpClient.request<DynamicPropertyDefinition>('delete', `${this.configuration.basePath}${localVarPath}`,
            {
                responseType: <any>responseType_,
                withCredentials: this.configuration.withCredentials,
                headers: localVarHeaders,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * get one dynamic property from the Interest group
     * @param id 
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public getDynamicPropertyDefinition(id: string, observe?: 'body', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json',}): Observable<DynamicPropertyDefinition>;
    public getDynamicPropertyDefinition(id: string, observe?: 'response', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json',}): Observable<HttpResponse<DynamicPropertyDefinition>>;
    public getDynamicPropertyDefinition(id: string, observe?: 'events', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json',}): Observable<HttpEvent<DynamicPropertyDefinition>>;
    public getDynamicPropertyDefinition(id: string, observe: any = 'body', reportProgress: boolean = false, options?: {httpHeaderAccept?: 'application/json',}): Observable<any> {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling getDynamicPropertyDefinition.');
        }

        let localVarHeaders = this.defaultHeaders;

        let localVarCredential: string | undefined;
        // authentication (basicAuth) required
        localVarCredential = this.configuration.lookupCredential('basicAuth');
        if (localVarCredential) {
            localVarHeaders = localVarHeaders.set('Authorization', 'Basic ' + localVarCredential);
        }

        let localVarHttpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
        if (localVarHttpHeaderAcceptSelected === undefined) {
            // to determine the Accept header
            const httpHeaderAccepts: string[] = [
                'application/json'
            ];
            localVarHttpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }



        let responseType_: 'text' | 'json' | 'blob' = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            } else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            } else {
                responseType_ = 'blob';
            }
        }

        let localVarPath = `/dynprops/${this.configuration.encodeParam({name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: undefined})}`;
        return this.httpClient.request<DynamicPropertyDefinition>('get', `${this.configuration.basePath}${localVarPath}`,
            {
                responseType: <any>responseType_,
                withCredentials: this.configuration.withCredentials,
                headers: localVarHeaders,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * get the list of dynamic properties definition in an Interest group
     * @param id 
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public getDynamicPropertyDefinitions(id: string, observe?: 'body', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json',}): Observable<Array<DynamicPropertyDefinition>>;
    public getDynamicPropertyDefinitions(id: string, observe?: 'response', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json',}): Observable<HttpResponse<Array<DynamicPropertyDefinition>>>;
    public getDynamicPropertyDefinitions(id: string, observe?: 'events', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json',}): Observable<HttpEvent<Array<DynamicPropertyDefinition>>>;
    public getDynamicPropertyDefinitions(id: string, observe: any = 'body', reportProgress: boolean = false, options?: {httpHeaderAccept?: 'application/json',}): Observable<any> {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling getDynamicPropertyDefinitions.');
        }

        let localVarHeaders = this.defaultHeaders;

        let localVarCredential: string | undefined;
        // authentication (basicAuth) required
        localVarCredential = this.configuration.lookupCredential('basicAuth');
        if (localVarCredential) {
            localVarHeaders = localVarHeaders.set('Authorization', 'Basic ' + localVarCredential);
        }

        let localVarHttpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
        if (localVarHttpHeaderAcceptSelected === undefined) {
            // to determine the Accept header
            const httpHeaderAccepts: string[] = [
                'application/json'
            ];
            localVarHttpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }



        let responseType_: 'text' | 'json' | 'blob' = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            } else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            } else {
                responseType_ = 'blob';
            }
        }

        let localVarPath = `/groups/${this.configuration.encodeParam({name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: undefined})}/dynprops`;
        return this.httpClient.request<Array<DynamicPropertyDefinition>>('get', `${this.configuration.basePath}${localVarPath}`,
            {
                responseType: <any>responseType_,
                withCredentials: this.configuration.withCredentials,
                headers: localVarHeaders,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * create a new dynamic property inside the Interest group 
     * @param id 
     * @param dynamicPropertyDefinition 
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public postPropertyDefinition(id: string, dynamicPropertyDefinition: DynamicPropertyDefinition, observe?: 'body', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json',}): Observable<DynamicPropertyDefinition>;
    public postPropertyDefinition(id: string, dynamicPropertyDefinition: DynamicPropertyDefinition, observe?: 'response', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json',}): Observable<HttpResponse<DynamicPropertyDefinition>>;
    public postPropertyDefinition(id: string, dynamicPropertyDefinition: DynamicPropertyDefinition, observe?: 'events', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json',}): Observable<HttpEvent<DynamicPropertyDefinition>>;
    public postPropertyDefinition(id: string, dynamicPropertyDefinition: DynamicPropertyDefinition, observe: any = 'body', reportProgress: boolean = false, options?: {httpHeaderAccept?: 'application/json',}): Observable<any> {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling postPropertyDefinition.');
        }
        if (dynamicPropertyDefinition === null || dynamicPropertyDefinition === undefined) {
            throw new Error('Required parameter dynamicPropertyDefinition was null or undefined when calling postPropertyDefinition.');
        }

        let localVarHeaders = this.defaultHeaders;

        let localVarCredential: string | undefined;
        // authentication (basicAuth) required
        localVarCredential = this.configuration.lookupCredential('basicAuth');
        if (localVarCredential) {
            localVarHeaders = localVarHeaders.set('Authorization', 'Basic ' + localVarCredential);
        }

        let localVarHttpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
        if (localVarHttpHeaderAcceptSelected === undefined) {
            // to determine the Accept header
            const httpHeaderAccepts: string[] = [
                'application/json'
            ];
            localVarHttpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }



        // to determine the Content-Type header
        const consumes: string[] = [
            'application/json'
        ];
        const httpContentTypeSelected: string | undefined = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Content-Type', httpContentTypeSelected);
        }

        let responseType_: 'text' | 'json' | 'blob' = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            } else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            } else {
                responseType_ = 'blob';
            }
        }

        let localVarPath = `/groups/${this.configuration.encodeParam({name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: undefined})}/dynprops`;
        return this.httpClient.request<DynamicPropertyDefinition>('post', `${this.configuration.basePath}${localVarPath}`,
            {
                body: dynamicPropertyDefinition,
                responseType: <any>responseType_,
                withCredentials: this.configuration.withCredentials,
                headers: localVarHeaders,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * update a dynamic property inside the Interest group 
     * @param id 
     * @param dynamicPropertyDefinition 
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public putDynamicPropertyDefinition(id: string, dynamicPropertyDefinition: DynamicPropertyDefinition, observe?: 'body', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json',}): Observable<DynamicPropertyDefinition>;
    public putDynamicPropertyDefinition(id: string, dynamicPropertyDefinition: DynamicPropertyDefinition, observe?: 'response', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json',}): Observable<HttpResponse<DynamicPropertyDefinition>>;
    public putDynamicPropertyDefinition(id: string, dynamicPropertyDefinition: DynamicPropertyDefinition, observe?: 'events', reportProgress?: boolean, options?: {httpHeaderAccept?: 'application/json',}): Observable<HttpEvent<DynamicPropertyDefinition>>;
    public putDynamicPropertyDefinition(id: string, dynamicPropertyDefinition: DynamicPropertyDefinition, observe: any = 'body', reportProgress: boolean = false, options?: {httpHeaderAccept?: 'application/json',}): Observable<any> {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling putDynamicPropertyDefinition.');
        }
        if (dynamicPropertyDefinition === null || dynamicPropertyDefinition === undefined) {
            throw new Error('Required parameter dynamicPropertyDefinition was null or undefined when calling putDynamicPropertyDefinition.');
        }

        let localVarHeaders = this.defaultHeaders;

        let localVarCredential: string | undefined;
        // authentication (basicAuth) required
        localVarCredential = this.configuration.lookupCredential('basicAuth');
        if (localVarCredential) {
            localVarHeaders = localVarHeaders.set('Authorization', 'Basic ' + localVarCredential);
        }

        let localVarHttpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
        if (localVarHttpHeaderAcceptSelected === undefined) {
            // to determine the Accept header
            const httpHeaderAccepts: string[] = [
                'application/json'
            ];
            localVarHttpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }



        // to determine the Content-Type header
        const consumes: string[] = [
            'application/json'
        ];
        const httpContentTypeSelected: string | undefined = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Content-Type', httpContentTypeSelected);
        }

        let responseType_: 'text' | 'json' | 'blob' = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            } else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            } else {
                responseType_ = 'blob';
            }
        }

        let localVarPath = `/dynprops/${this.configuration.encodeParam({name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: undefined})}`;
        return this.httpClient.request<DynamicPropertyDefinition>('put', `${this.configuration.basePath}${localVarPath}`,
            {
                body: dynamicPropertyDefinition,
                responseType: <any>responseType_,
                withCredentials: this.configuration.withCredentials,
                headers: localVarHeaders,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

}
