package soapboxrace.world.mpfr.freeroam.packet;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PacketGroup
{
    private static final byte[] NULL_PLAYER_BYTES = { (byte) 0xff, (byte) 0xff };
    
    private final List<byte[]> packets;

    public PacketGroup()
    {
        this.packets = new ArrayList<>(Collections.nCopies(14, NULL_PLAYER_BYTES));
    }

    public void set(int index, byte[] packet)
    {
        indexCheck(index);

        this.packets.set(index, packet);
    }

    public byte[] get(int index)
    {
        indexCheck(index);

        return this.packets.get(index);
    }

    public byte[] serializeToBytes()
    {
        if (this.packets.stream().mapToInt(b -> b.length).sum() > 2048)
        {
            throw new IllegalStateException("Packets sum up to a size too large to fit");
        }

        ByteBuffer buffer = ByteBuffer.allocate(2048);
        int bufferSize = 0;
        
        for (byte[] packet : this.packets)
        {
            bufferSize += packet.length;
            buffer.put(packet);
        }
        
        byte[] bytes = new byte[bufferSize];
        System.arraycopy(buffer.array(), 0, bytes, 0, bytes.length);
        
        buffer = null;
        
        return bytes;
    }

    private void indexCheck(int index)
    {
        if (index >= packets.size())
        {
            throw new IllegalArgumentException("Maximum index: 14");
        }
    }
    
    public static byte[] addGroupingBytes(byte[] data, boolean isNull)
    {
        ByteBuffer buffer = ByteBuffer.allocate(data.length + 2);
        buffer.put((byte) (isNull ? 0xff : 0x00));
        buffer.put(data);
        buffer.put((byte) 0xff);
        
        byte[] bytes = buffer.array();
        data = null;
        buffer = null;
        
        return bytes;
    }
}
