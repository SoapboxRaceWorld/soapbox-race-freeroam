package soapboxrace.world.mpfr.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import soapboxrace.world.mpfr.freeroam.FreeroamTalker;
import soapboxrace.world.mpfr.server.udp.FreeroamAllTalkers;

public class PlayerInfoHandler extends ChannelInboundHandlerAdapter
{
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        DatagramPacket datagramPacket = (DatagramPacket) msg;
        ByteBuf buf = datagramPacket.content();
        if (isPlayerInfoPacket(buf))
        {
//            System.out.println("INFO");
            FreeroamTalker freeroamTalker = FreeroamAllTalkers.get(datagramPacket);
            if (freeroamTalker != null)
            {
                freeroamTalker.handleSession(datagramPacket);
            }
        }

        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        cause.printStackTrace();
    }

    private boolean isPlayerInfoPacket(ByteBuf buf)
    {
        return (buf.getByte(2) == (byte) 0x07);
    }
}
