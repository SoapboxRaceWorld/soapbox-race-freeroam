package soapboxrace.world.mpfr.freeroam.packet;

import soapboxrace.world.mpfr.freeroam.FreeroamTalker;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A specialized packet buffer.
 * Supports 2 types of packets: player info series and car state series
 * (Both can be combined)
 * <p>
 * Allows the server to effectively and reliably control the data being sent.
 */
public class PacketBuffer
{
    private final FreeroamTalker talker;

    private List<Packet> packets;

    /**
     * Primes the buffer with a talker instance
     * and sets up the packet lists
     *
     * @param talker The talker instance
     */
    PacketBuffer(FreeroamTalker talker)
    {
        this.talker = talker;
        this.packets = new ArrayList<>();
    }

    /**
     * Serializes the buffer to a byte array.
     *
     * @return The array of bytes representing this buffer.
     */
    public byte[] serialize()
    {
        if (this.packets.stream().mapToInt(b -> b.getData().length).sum() > 2048)
        {
            throw new IllegalStateException("Too much data; buffer would certainly overflow");
        }

        ByteBuffer buffer = ByteBuffer.allocate(2048);
        int size = 0;
        boolean countBInc = false;

        for (Packet packet : this.packets)
        {
            size += packet.getData().length;

            buffer.put(packet.getData());
        }

        byte[] bytes = new byte[size];
        System.arraycopy(buffer.array(), 0, bytes, 0, size);

        this.flush();
        
        return bytes;
    }

    public boolean isDirty()
    {
        return !this.packets.isEmpty();
    }

    public FreeroamTalker getTalker()
    {
        return talker;
    }

    public void addPacket(Packet packet)
    {
        if (this.packets.stream().mapToInt(b -> b.getData().length).sum() + packet.getData().length > 2048)
        {
            throw new IllegalStateException("Adding this packet would cause buffer to overflow");
        }

        this.packets.add(packet);
    }

    /**
     * Flushes the buffer of all packets.
     */
    public void flush()
    {
        this.packets.clear();
    }
}
