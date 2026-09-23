/*
 * Copyright 2006 European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package eu.europa.ec.digit.circabc.rest.service.ftp;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Validates outbound FTP destinations before they are persisted or connected to.
 *
 * <p>Auto-upload configurations let an interest-group administrator choose the remote FTP host the
 * server connects to. Without validation this can be abused for Server-Side Request Forgery (SSRF):
 * pointing the host at {@code localhost}, link-local (e.g. the cloud metadata endpoint
 * {@code 169.254.169.254}) or private/internal addresses to probe or reach services behind the
 * server's trust boundary.
 *
 * <p>This validator resolves the supplied host and rejects it when any resolved address is a
 * loopback, any-local, link-local, site-local/private or multicast address. It returns the resolved
 * public IP address so callers can connect to the validated address directly, avoiding a second DNS
 * lookup that could resolve to a different (internal) address (DNS-rebinding).
 */
public final class FtpDestinationValidator {

  private FtpDestinationValidator() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Validates the given FTP host/port and resolves the host to a public IP address.
   *
   * @param host the FTP host name or IP address
   * @param port the FTP port
   * @return the resolved public IP address to connect to
   * @throws IllegalArgumentException if the host is blank, the port is out of range, the host cannot
   *     be resolved, or it resolves to a non-public (loopback/private/link-local/...) address
   */
  public static String validateAndResolveHost(String host, Integer port) {
    if (host == null || host.trim().isEmpty()) {
      throw new IllegalArgumentException("FTP host cannot be null or empty.");
    }

    if (port == null || port < 1 || port > 65535) {
      throw new IllegalArgumentException(
        "Invalid FTP port. Must be a number in the range [1..65535]."
      );
    }

    InetAddress[] addresses;
    try {
      addresses = InetAddress.getAllByName(host.trim());
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException(
        "FTP host could not be resolved: " + host,
        e
      );
    }

    if (addresses.length == 0) {
      throw new IllegalArgumentException(
        "FTP host could not be resolved: " + host
      );
    }

    for (InetAddress address : addresses) {
      if (isBlockedAddress(address)) {
        throw new IllegalArgumentException(
          "FTP host must resolve only to public addresses."
        );
      }
    }

    // Connect to the validated address, not the original hostname, to avoid a
    // second DNS lookup selecting a different (internal) destination.
    return addresses[0].getHostAddress();
  }

  private static boolean isBlockedAddress(InetAddress address) {
    return (
      address.isAnyLocalAddress() ||
      address.isLoopbackAddress() ||
      address.isLinkLocalAddress() ||
      address.isSiteLocalAddress() ||
      address.isMulticastAddress() ||
      isPrivateAddress(address)
    );
  }

  private static boolean isPrivateAddress(InetAddress address) {
    byte[] bytes = address.getAddress();

    if (bytes.length == 4) {
      return isPrivateIpv4(bytes, 0);
    }

    if (bytes.length == 16) {
      if ((bytes[0] & 0xfe) == 0xfc) {
        // fc00::/7 unique local addresses
        return true;
      }

      if (isIpv4MappedAddress(bytes)) {
        return isPrivateIpv4(bytes, 12);
      }
    }

    return false;
  }

  private static boolean isPrivateIpv4(byte[] bytes, int offset) {
    int first = bytes[offset] & 0xff;
    int second = bytes[offset + 1] & 0xff;

    return (
      first == 10 ||
      (first == 172 && second >= 16 && second <= 31) ||
      (first == 192 && second == 168)
    );
  }

  private static boolean isIpv4MappedAddress(byte[] bytes) {
    for (int index = 0; index < 10; index++) {
      if (bytes[index] != 0) {
        return false;
      }
    }

    return bytes[10] == (byte) 0xff && bytes[11] == (byte) 0xff;
  }
}
