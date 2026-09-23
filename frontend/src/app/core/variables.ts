import { InjectionToken } from '@angular/core';

/**
 * Dependency-injection token that provides the base path (URL) of the
 * Alfresco backend REST API. Injected into generated API clients and services
 * so they know where to send Alfresco-targeted requests.
 */
export const ALF_BASE_PATH = new InjectionToken<string>('alfBasePath');

/**
 * Dependency-injection token that provides the base path (URL) of the CIRCABC
 * backend REST API. Injected into generated API clients and services so they
 * know where to send CIRCABC-targeted requests.
 */
export const CBC_BASE_PATH = new InjectionToken<string>('cbcBasePath');

/**
 * Dependency-injection token that provides the root server URL used by the
 * application (for example when building absolute links or non-API requests).
 */
export const SERVER_URL = new InjectionToken<string>('serverURL');

/**
 * Dependency-injection token that provides the current CIRCABC frontend
 * application version string, typically surfaced in the UI or diagnostics.
 */
export const APP_VERSION = new InjectionToken<string>('appVersion');

/**
 * Dependency-injection token that provides the version string of the Alfresco
 * backend the application is running against.
 */
export const APP_ALF_VERSION = new InjectionToken<string>('appAlfVersion');

/**
 * Dependency-injection token that provides the name of the server node
 * serving the application, useful for diagnostics in clustered deployments.
 */
export const NODE_NAME = new InjectionToken<string>('nodeName');

/**
 * Dependency-injection token that provides the build date of the current
 * application bundle, typically displayed for support and diagnostics.
 */
export const BUILD_DATE = new InjectionToken<string>('buildDate');
