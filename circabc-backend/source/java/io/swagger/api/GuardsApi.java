package io.swagger.api;

import io.swagger.exception.NonExistingNodeException;
import io.swagger.model.GuardAuthorization;

/**
 * @author beaurpi
 */
public interface GuardsApi {

    GuardAuthorization guardsGroupIdGet(String id);

    GuardAuthorization guardsAccessIdGet(String id) throws NonExistingNodeException;

    GuardAuthorization guardsEditionIdGet(String id) throws NonExistingNodeException;

    GuardAuthorization guardsGroupIdServiceNameGet(String groupIp, String serviceName);

    GuardAuthorization guardsGroupIdMembersAdminGet(String groupIp);

    GuardAuthorization guardsAdministrationIdGet(String nodeId);
}
