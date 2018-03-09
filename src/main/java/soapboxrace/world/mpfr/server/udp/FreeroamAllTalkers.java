package soapboxrace.world.mpfr.server.udp;

import java.util.HashMap;

import io.netty.channel.socket.DatagramPacket;
import soapboxrace.world.mpfr.freeroam.FreeroamTalker;
import soapboxrace.world.mpfr.freeroam.FreeroamTalkerCleanup;

public class FreeroamAllTalkers
{

    private static HashMap<Integer, FreeroamTalker> freeroamTalkers = new HashMap<>();

    public static void put(FreeroamTalker freeroamTalker)
    {
        freeroamTalkers.put(freeroamTalker.getPort(), freeroamTalker);
        
        if (freeroamTalkers.size() == 1)
        {
            new FreeroamTalkerCleanup();
        }
    }

    public static FreeroamTalker get(DatagramPacket datagramPacket)
    {
        return freeroamTalkers.get(datagramPacket.sender().getPort());
    }

    public static HashMap<Integer, FreeroamTalker> getFreeroamTalkers()
    {
        return freeroamTalkers;
    }

    public static void remove(int port)
    {
        freeroamTalkers.remove(port);
    }
}
