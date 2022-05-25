package io.swagger.api;

import io.swagger.model.PermissionDefinition;

/**
 * @author beaurpi
 */
public interface PermissionsApi {

    PermissionDefinition getNodeIdPermissionsGet(String id);

    PermissionDefinition nodeIdPermissionsPut(String id, PermissionDefinition body);

    void nodeIdPermissionsDelete(String id, String authority, String permission);

    void nodeIdPermissionsClear(String id, String authority);
}
