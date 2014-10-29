package interactivespaces.service.comm.network.server.internal.netty;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Jdk14Logger;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.FixedReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.DatagramChannel;
import org.jboss.netty.channel.socket.DatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Keith M. Hughes
 */
public class NettyUdpBroadcastMasterNetworkCommunicationEndpoint {

  public static void main(String[] args) throws Exception {
    ExecutorService executorService = Executors.newScheduledThreadPool(1000);
    NettyUdpBroadcastMasterNetworkCommunicationEndpoint endpoint =
        new NettyUdpBroadcastMasterNetworkCommunicationEndpoint(23867, executorService, new Jdk14Logger("foo"));
    endpoint.startup();

    Thread.sleep(1000);
    for (int i = 0; i < 10; i++) {
      endpoint.writeBroadcastData("Hello".getBytes());
      Thread.sleep(1000);
    }

    endpoint.shutdown();
    executorService.shutdown();
  }

  /**
   * The port the client is listening to.
   */
  private final int port;

  /**
   * The bootstrap for the UDP client.
   */
  private ConnectionlessBootstrap bootstrap;

  /**
   * Executor service for this endpoint
   */
  private final ExecutorService executorService;

  /**
   * Logger for this endpoint.
   */
  private final Log log;

  /**
   * The datagram channel for this endpoint.
   */
  private DatagramChannel channel;

  /**
   * The broadcast address to send all items on.
   */
  private InetSocketAddress broadcastAddress;

  /**
   * Construct a netty UDP broadcast client.
   *
   * @param port
   *          the broadcast port
   * @param executorService
   *          the executor service to use
   * @param log
   *          the logger to use
   */
  public NettyUdpBroadcastMasterNetworkCommunicationEndpoint(int port, ExecutorService executorService, Log log) {
    this.port = port;
    this.executorService = executorService;
    this.log = log;
  }

  // @Override
  public void startup() {
    DatagramChannelFactory channelFactory = new NioDatagramChannelFactory(executorService);
    bootstrap = new ConnectionlessBootstrap(channelFactory);

    // Configure the pipeline factory.
    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() throws Exception {
        return Channels.pipeline(new NettyUdpBroadcastMasterHandler());
      }
    });

    // Enable broadcast
    bootstrap.setOption("broadcast", "true");

    // Allow packets as large as up to 1024 bytes (default is 768).
    // You could increase or decrease this value to avoid truncated packets
    // or to improve memory footprint respectively.
    //
    // Please also note that a large UDP packet might be truncated or
    // dropped by your router no matter how you configured this option.
    // In UDP, a packet is truncated or dropped if it is larger than a
    // certain size, depending on router configuration. IPv4 routers
    // truncate and IPv6 routers drop a large packet. That's why it is
    // safe to send small packets in UDP.
    bootstrap.setOption("receiveBufferSizePredictorFactory", new FixedReceiveBufferSizePredictorFactory(1024));

    channel = (DatagramChannel) bootstrap.bind(new InetSocketAddress(0));

    broadcastAddress = new InetSocketAddress("255.255.255.255", port);
  }

  // @Override
  public void shutdown() {
    if (bootstrap != null) {
      bootstrap.shutdown();
      bootstrap = null;
    }
  }

  /**
   * Write data on the broadcast channel.
   *
   * @param data
   *          the data to write
   */
  public void writeBroadcastData(byte[] data) {
    ChannelBuffer buffer = ChannelBuffers.copiedBuffer(data);
    channel.write(buffer, broadcastAddress);
  }

  /**
   * Join a multicast group.
   *
   * @param multicastAddress
   *          the address of the group to join
   */
  public void joinGroup(InetAddress multicastAddress) {
    channel.joinGroup(multicastAddress);
  }

  /**
   * Join a multicast group.
   *
   * @param multicastAddress
   *          the address of the group to join
   * @param networkInterface
   *          the interface to connect to the group on
   */
  public void joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface) {
    channel.joinGroup(multicastAddress, networkInterface);
  }

  /**
   * Leave a multicast group.
   *
   * @param multicastAddress
   *          the address of the group to leave
   */
  public void leaveGroup(InetAddress multicastAddress) {
    channel.leaveGroup(multicastAddress);
  }

  /**
   * Leave a multicast group.
   *
   * @param multicastAddress
   *          the address of the group to leave
   * @param networkInterface
   *          the interface the group was joined on
   */
  public void leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface) {
    channel.leaveGroup(multicastAddress, networkInterface);
  }

  /**
   * Netty handler for  UDP broadcast messages.
   *
   * @author Keith M. Hughes
   */
  public class NettyUdpBroadcastMasterHandler extends SimpleChannelHandler {

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
     log.info("Got something");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
      log.error("Error during netty UDP broadcast master handler processing", e.getCause());
    }
  }
}
