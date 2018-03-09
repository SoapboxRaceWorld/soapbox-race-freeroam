package soapboxrace.world.mpfr.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import soapboxrace.world.mpfr.freeroam.FreeroamTalker;
import soapboxrace.world.mpfr.server.udp.FreeroamAllTalkers;
import soapboxrace.world.mpfr.utils.MainLogger;

import java.net.InetSocketAddress;

public class HelloHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        DatagramPacket datagramPacket = (DatagramPacket) msg;
        InetSocketAddress sender = datagramPacket.sender();
        ByteBuf buf = datagramPacket.content();
        if (isHelloPacket(buf)) {
            MainLogger.logger.info("Received HELLO from {}:{}", sender.getHostString(), sender.getPort());
//            System.out.println("HELLO");
            FreeroamTalker freeroamTalker = new FreeroamTalker(ctx, datagramPacket);
            FreeroamAllTalkers.put(freeroamTalker);
            freeroamTalker.send(welcomePacket(freeroamTalker));
        }

        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    private byte[] welcomePacket(FreeroamTalker freeroamTalker) {
        byte[] cliTime = freeroamTalker.getCliTime();
        byte[] sequence = freeroamTalker.getSequence();
        byte[] timeArray = freeroamTalker.getTimeDiffBytes();
        byte[] welcomePacket = { //
                sequence[0], sequence[1], // seq
                (byte) 0x01, // hello header
                timeArray[0], timeArray[1], // time
                cliTime[0], cliTime[1], // cli time?
                (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01 // crc
        };
        timeArray = null;
        cliTime = null;
        sequence = null;
        return welcomePacket;
    }


    private boolean isHelloPacket(ByteBuf buf) {
        return (buf.getByte(2) == (byte) 0x06);
    }
}
