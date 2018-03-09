package soapboxrace.world.mpfr.freeroam.packet;

import soapboxrace.world.mpfr.freeroam.FreeroamTalker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PacketBuffers
{
    private static final Map<FreeroamTalker, PacketBuffer> bufferMap;

    static
    {
        bufferMap = new ConcurrentHashMap<>();
    }
    
    public static PacketBuffer get(FreeroamTalker talker)
    {
        return bufferMap.computeIfAbsent(talker, PacketBuffer::new);
    }
    
    public static void remove(FreeroamTalker talker)
    {
        bufferMap.remove(talker);
    }
}
