package soapboxrace.world.mpfr.server;

import io.netty.channel.ChannelFuture;
import org.slf4j.LoggerFactory;
import soapboxrace.world.mpfr.server.netty.NettyServer;

/**
 * The entry point class of the freeroam server.
 */
public class Main
{
    public static void main(String[] args)
    {
        NettyServer server;

        try
        {
            server = new NettyServer(9999);
            ChannelFuture future = server.start();

            LoggerFactory.getLogger("Freeroam").info("Started UDP server!");

            // Wait until the connection is closed.
            future.channel().closeFuture().sync();
        } catch (InterruptedException ex)
        {
            System.err.println(ex.getMessage());
        }
    }
}