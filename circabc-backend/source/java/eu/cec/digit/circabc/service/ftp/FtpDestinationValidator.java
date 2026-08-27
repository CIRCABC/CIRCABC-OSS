package eu.cec.digit.circabc.service.ftp;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Validates outbound FTP destinations before they are persisted or connected.
 *
 * <p>The validator resolves the host name, rejects any destination that resolves to a loopback,
 * link-local, site-local, multicast or otherwise private address, and restricts the destination to
 * the standard FTP control port. It returns the resolved address so callers connect to the exact
 * address that was validated, defeating DNS-rebinding between validation and connect.
 */
public final class FtpDestinationValidator {

  private static final int FTP_CONTROL_PORT = 21;

  private FtpDestinationValidator() {
    throw new IllegalStateException("Utility class");
  }

  public static String validateAndResolveHost(String host, Integer port) {
    if (host == null || host.trim().isEmpty()) {
      throw new IllegalArgumentException("FTP host cannot be null or empty.");
    }

    if (port == null || port != FTP_CONTROL_PORT) {
      throw new IllegalArgumentException(
        "Invalid FTP port. Only the FTP control port 21 is allowed."
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

    // Connect to the validated address, not the original hostname, to avoid
    // a second DNS lookup selecting a different destination.
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
