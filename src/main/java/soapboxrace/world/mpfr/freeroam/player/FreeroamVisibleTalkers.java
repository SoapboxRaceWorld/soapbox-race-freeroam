package soapboxrace.world.mpfr.freeroam.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soapboxrace.world.mpfr.freeroam.FreeroamTalker;
import soapboxrace.world.mpfr.freeroam.packet.Packet;
import soapboxrace.world.mpfr.freeroam.packet.PacketBuffers;
import soapboxrace.world.mpfr.utils.ListDiffer;
import soapboxrace.world.mpfr.utils.ListDiffer.ListDifferences;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the list of visible talkers for a single talker.
 */
public class FreeroamVisibleTalkers
{
    public static final Integer VISIBLE_PLAYERS_LIMIT = 14;
    private static final Integer PLAYER_UPDATE_TICKS;
    
    static {
        if (System.getProperty("PLAYER_TICKS") != null)
            PLAYER_UPDATE_TICKS = Integer.parseInt(System.getProperty("PLAYER_TICKS"));
        else
            PLAYER_UPDATE_TICKS = 30;
    }

    private final ArrayList<FreeroamTalker> visibleTalkers;

    private final FreeroamTalker talker;

    private final Logger logger;

    private boolean removing = false;
    
    private int switchingCount = 0;

    public FreeroamVisibleTalkers(FreeroamTalker talker)
    {
        this.talker = talker;
        this.visibleTalkers = new ArrayList<>(Collections.nCopies(VISIBLE_PLAYERS_LIMIT, null));
        this.logger = LoggerFactory.getLogger(String.format("VisibleTalkers-%d", talker.getPort()));
    }

    /**
     * Adds a talker to the list of visible talkers
     *
     * @param talker The talker to add
     */
    public void addVisibleTalker(FreeroamTalker talker)
    {
        for (int i = 0; i < VISIBLE_PLAYERS_LIMIT; i++)
        {
            if (visibleTalkers.get(i) == null)
            {
//                this.logger.debug("Added talker {} at index {}", talker.getName(), i);
                visibleTalkers.set(i, talker);
                return;
            }
        }
    }

    /**
     * Removes a talker from the list of visible talkers
     *
     * @param talker The talker to remove
     */
    public void removeVisibleTalker(FreeroamTalker talker)
    {
        int index = visibleTalkers.indexOf(talker);

        if (index != -1)
        {
            visibleTalkers.set(index, null);
        }
    }

    public void sendFirstPlayer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        int size = 2;
        FreeroamTalker firstTalker;

        buffer.put((byte) 0x00);

        if ((firstTalker = this.visibleTalkers.get(0)) != null)
        {
            logger.debug("First player: {}", firstTalker.getName());

            byte[] playerPacket = firstTalker.getPlayerInfo(this.talker.getTimeDiff() - 20);

            if (playerPacket != null)
            {
                size += playerPacket.length;
                buffer.put(playerPacket);
                playerPacket = null;
            }
        }

        buffer.put((byte) 0xff);

        for (int i = 0; i < 14; i++)
        {
            buffer.put((byte) 0xff);
            buffer.put((byte) 0xff);
            size += 2;
        }

        byte[] bytes = new byte[size];
        System.arraycopy(buffer.array(), 0, bytes, 0, bytes.length);

        PacketBuffers.get(this.talker).addPacket(new Packet(bytes));
        bytes = null;
        buffer = null;
    }

    /**
     * Replaces the list of visible talkers with a new list.
     *
     * @param talkers The new list
     * @return The differences between the new talker list and the old one
     */
    public PlayerListDifferences setVisibleTalkers(List<FreeroamTalker> talkers)
    {
        // Compute the differences between the current talker list and the new list
        ListDifferences<FreeroamTalker> differences = ListDiffer.getDifferences(this.visibleTalkers, talkers);

        // Get the lists of talkers who were either:
        // - removed
        // - added
        // - kept
        List<FreeroamTalker> added = new ArrayList<>(differences.getAdded()),
                kept = new ArrayList<>(differences.getKept()),
                removed = new ArrayList<>(differences.getRemoved());

        // Map setup
        Map<Integer, FreeroamTalker> addedTalkers = new ConcurrentHashMap<>(),
                removedTalkers = new ConcurrentHashMap<>(),
                keptTalkers = new ConcurrentHashMap<>(),
                replacedTalkers = new ConcurrentHashMap<>();

        if (++switchingCount == PLAYER_UPDATE_TICKS) 
        {
            if (this.removing)
            {
                // For each removed talker
                for (FreeroamTalker anRemoved : removed)
                {
                    if (anRemoved != null)
                    {
                        // Insert the removed talker into the "removed talkers" map
                        removedTalkers.put(this.visibleTalkers.indexOf(anRemoved), anRemoved);
                        this.removeVisibleTalker(anRemoved);
                    }
                }
            } else
            {
                // For each added talker
                for (FreeroamTalker anAdded : added)
                {
                    if (anAdded != null)
                    {
                        this.addVisibleTalker(anAdded);
                        addedTalkers.put(this.visibleTalkers.indexOf(anAdded), anAdded);
                    }
                }
            }

            this.removing = !this.removing;
            this.switchingCount = 0;
        }

        // For every kept talker
        for (FreeroamTalker anKept : kept)
        {
            if (anKept != null)
            {
                // Insert the removed talker into the "removed talkers" map
                keptTalkers.put(this.visibleTalkers.indexOf(anKept), anKept);
            }
        }

        return new PlayerListDifferences(addedTalkers, keptTalkers, removedTalkers, replacedTalkers);
    }

    /**
     * A wrapper around results returned by the ListDiffer helper.
     * Contains four Map instances (added, kept, removed, replaced) where the keys = item indexes & values = items themselves
     * <p>
     * Example: the {@code added} map can contain talkers that were added. The key = the index of the new talker in the
     * list; the value = the talker instance
     */
    public static class PlayerListDifferences
    {
        private final Map<Integer, FreeroamTalker> added;

        private final Map<Integer, FreeroamTalker> kept;

        private final Map<Integer, FreeroamTalker> removed;

        private final Map<Integer, FreeroamTalker> replaced;

        /**
         * Initializes the differences wrapper
         *
         * @param added    added
         * @param kept     kept
         * @param removed  removed
         * @param replaced replaced
         */
        public PlayerListDifferences(Map<Integer, FreeroamTalker> added, Map<Integer, FreeroamTalker> kept, Map<Integer, FreeroamTalker> removed, Map<Integer, FreeroamTalker> replaced)
        {
            this.added = added;
            this.kept = kept;
            this.removed = removed;
            this.replaced = replaced;
        }

        public Map<Integer, FreeroamTalker> getAdded()
        {
            return added;
        }

        public Map<Integer, FreeroamTalker> getKept()
        {
            return kept;
        }

        public Map<Integer, FreeroamTalker> getRemoved()
        {
            return removed;
        }

        public Map<Integer, FreeroamTalker> getReplaced()
        {
            return replaced;
        }
    }
}