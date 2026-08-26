package io.swagger.api;

import io.swagger.model.PingInfo;
import java.net.UnknownHostException;

/**
 * @author Alain Morlet
 */
public interface PingInfoApi {
  /**
   * Get the ping information of the server.
   *
   * @return PingInfo object containing server details.
   * @throws UnknownHostException if the local host name could not be resolved into an address.
   */
  PingInfo getPingInfo() throws UnknownHostException;
}
