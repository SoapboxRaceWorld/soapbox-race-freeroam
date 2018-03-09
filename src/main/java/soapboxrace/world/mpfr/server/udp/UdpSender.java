package soapboxrace.world.mpfr.server.udp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

public class UdpSender {
    private final InetSocketAddress address;
    private final ChannelHandlerContext context;

    public UdpSender(InetSocketAddress address, ChannelHandlerContext context) {
        this.address = address;
        this.context = context;
    }
    
    public void send(DatagramPacket packet) {
        this.context.writeAndFlush(packet);
    }
    
    public int getPort() {
        return this.address.getPort();
    }

    public InetSocketAddress getAddress() {
        return address;
    }
}
