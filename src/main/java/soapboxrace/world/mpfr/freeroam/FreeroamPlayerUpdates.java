package soapboxrace.world.mpfr.freeroam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soapboxrace.world.mpfr.freeroam.packet.Packet;
import soapboxrace.world.mpfr.freeroam.packet.PacketBuffers;
import soapboxrace.world.mpfr.freeroam.packet.PacketGroup;
import soapboxrace.world.mpfr.freeroam.player.FreeroamRangeCalc;
import soapboxrace.world.mpfr.freeroam.player.FreeroamVisibleTalkers;
import soapboxrace.world.mpfr.freeroam.player.FreeroamVisibleTalkers.PlayerListDifferences;

import java.util.Map;

public class FreeroamPlayerUpdates implements Runnable
{
    private final FreeroamTalker talker;
    private final Logger logger;

    FreeroamPlayerUpdates(FreeroamTalker talker)
    {
        this.talker = talker;
        this.logger = LoggerFactory.getLogger(String.format("updates-%s", talker.getName()));
    }

    @Override
    public void run()
    {
        logger.debug("{} at X {} / Y {} / Z {}", talker.getName(), talker.getXPos(), talker.getYPos(), talker.getZPos());
        PlayerListDifferences differences = FreeroamRangeCalc.setVisibleTalkersForTalker(this.talker);

        PacketBuffers.get(this.talker).addPacket(this.generatePacket(differences));
    }

    private Packet generatePacket(PlayerListDifferences differences)
    {
        // Declare variables (maps, etc)
        PacketGroup packetGroup = new PacketGroup();

        // Map structure:
        // key = index in talker list
        // value = FreeroamTalker instance
        final Map<Integer, FreeroamTalker> addedTalkers = differences.getAdded(),
                keptTalkers = differences.getKept(),
                removedTalkers = differences.getRemoved(),
                replacedTalkers = differences.getReplaced();

        for (int i = 0; i < FreeroamVisibleTalkers.VISIBLE_PLAYERS_LIMIT; i++)
        {
            SlotType slotType = this.getSlotType(i, differences);

            if (slotType == SlotType.ReplacedPlayer)
            {
                FreeroamTalker replacedTalker = replacedTalkers.get(i);
                byte[] playerPacket = replacedTalker.getPlayerInfo(this.talker.getTimeDiff() - 15);
                packetGroup.set(i, PacketGroup.addGroupingBytes(playerPacket, false));
            } else if (slotType == SlotType.AddedPlayer)
            {
                FreeroamTalker addedTalker = addedTalkers.get(i);
                byte[] playerPacket = addedTalker.getPlayerInfo(this.talker.getTimeDiff() - 15);
                packetGroup.set(i, PacketGroup.addGroupingBytes(playerPacket, false));
            } else if (slotType == SlotType.KeptPlayer)
            {
                FreeroamTalker keptTalker = keptTalkers.get(i);
                byte[] playerPacket = keptTalker.getPlayerXYZ(this.talker.getTimeDiff());
                packetGroup.set(i, PacketGroup.addGroupingBytes(playerPacket, false));
            } else if (slotType == SlotType.RemovedPlayer)
            {
                packetGroup.set(i, new byte[] { (byte) 0xff, (byte) 0xff });
            }
        }

        return new Packet(packetGroup.serializeToBytes());
    }

    private SlotType getSlotType(int slot, PlayerListDifferences differences)
    {
        final Map<Integer, FreeroamTalker> addedTalkers = differences.getAdded(),
                keptTalkers = differences.getKept(),
                removedTalkers = differences.getRemoved(),
                replacedTalkers = differences.getReplaced();
        if (replacedTalkers.containsKey(slot))
            return SlotType.ReplacedPlayer;
        else if (addedTalkers.containsKey(slot))
            return SlotType.AddedPlayer;
        else if (keptTalkers.containsKey(slot))
            return SlotType.KeptPlayer;
        else if (removedTalkers.containsKey(slot))
            return SlotType.RemovedPlayer;
        else
            return SlotType.NobodyHere;
    }

    private enum SlotType
    {
        AddedPlayer,
        KeptPlayer,
        RemovedPlayer,
        ReplacedPlayer,
        NobodyHere
    }
}
