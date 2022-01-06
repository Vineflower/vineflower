package pkg;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnsupportedAddressTypeException;

import static java.util.Objects.requireNonNull;

public class TestTryWithResourcesFake {
  // Adapted from java.nio.channels.SocketChannel
  public static SocketChannel open(SocketAddress remote)
    throws IOException
  {

    SocketChannel sc;
    requireNonNull(remote);
    if (remote instanceof InetSocketAddress)
      sc = SocketChannel.open();
    else if (remote instanceof UnixDomainSocketAddress)
      sc = SocketChannel.open(StandardProtocolFamily.UNIX);
    else
      throw new UnsupportedAddressTypeException();

    try {
      sc.connect(remote);
    } catch (Throwable x) {
      try {
        sc.close();
      } catch (Throwable suppressed) {
        x.addSuppressed(suppressed);
      }
      throw x;
    }
    assert sc.isConnected();
    return sc;
  }
}
