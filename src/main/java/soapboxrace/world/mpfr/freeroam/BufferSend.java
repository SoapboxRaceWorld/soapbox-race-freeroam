package soapboxrace.world.mpfr.freeroam;

import soapboxrace.world.mpfr.freeroam.packet.PacketBuffer;

import javax.annotation.Nonnull;

public class BufferSend implements Runnable
{
    private final PacketBuffer buffer;

    BufferSend(@Nonnull PacketBuffer buffer)
    {
        this.buffer = buffer;
    }

    @Override
    public void run()
    {
        if (this.buffer.isDirty())
        {
           byte[] serialized = this.buffer.serialize();

            this.buffer.getTalker().sendFullPacket(serialized);
//            System.out.println("D I R T Y  A F");
        }
    }
}
