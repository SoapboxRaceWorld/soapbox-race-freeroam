package soapboxrace.world.mpfr.server.udp;

import java.util.HashMap;

import io.netty.channel.socket.DatagramPacket;
import soapboxrace.world.mpfr.freeroam.FreeroamLoop;
import soapboxrace.world.mpfr.freeroam.FreeroamTalker;
import soapboxrace.world.mpfr.freeroam.FreeroamTalkerCleanup;

public class FreeroamAllTalkers
{
    private static HashMap<Integer, FreeroamTalker> freeroamTalkers = new HashMap<>();
    private static boolean startedTasks = false;

    public static void put(FreeroamTalker freeroamTalker)
    {
        freeroamTalkers.put(freeroamTalker.getPort(), freeroamTalker);
        
        if (freeroamTalkers.size() == 1 && !startedTasks)
        {
            new FreeroamTalkerCleanup();
            new FreeroamLoop();
            
            startedTasks = true;
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
