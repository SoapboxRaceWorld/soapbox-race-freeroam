package soapboxrace.world.mpfr.freeroam;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soapboxrace.world.mpfr.freeroam.packet.PacketBuffers;
import soapboxrace.world.mpfr.freeroam.player.FreeroamRangeCalc;
import soapboxrace.world.mpfr.freeroam.player.FreeroamVisibleTalkers;
import soapboxrace.world.mpfr.server.udp.UdpSender;
import soapboxrace.world.mpfr.utils.ConcurrentUtils;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * A freeroam "talker" (client).
 */
public class FreeroamTalker
{
    private final byte[] cliTime;

    private final long startedTime = new Date().getTime();

    private UdpSender sender;

    private SbrwParser sbrwParser;

    private String sessionId;

    private long latestTime = new Date().getTime();

    private int countA = 0;

    private int countB = 1;

    private boolean ready = false;

    private Logger logger;

    private final FreeroamVisibleTalkers visibleTalkers;
    
    private final FreeroamPlayerUpdates playerUpdates;

    public FreeroamTalker(ChannelHandlerContext ctx, DatagramPacket packet)
    {
        ByteBuf content = packet.content();

        this.logger = LoggerFactory.getLogger("talker-" + packet.sender().getPort());
        this.sender = new UdpSender(packet.sender(), ctx);
        this.cliTime = ByteBufUtil.getBytes(content, 52, 2);
        this.visibleTalkers = new FreeroamVisibleTalkers(this);
        this.playerUpdates = new FreeroamPlayerUpdates(this);
    }

    /**
     * Sends data to the talker's UDP endpoint
     *
     * @param data The data to send
     */
    public void send(byte[] data)
    {
        this.sender.send(new DatagramPacket(
                Unpooled.copiedBuffer(data),
                this.sender.getAddress()
        ));
    }

    /**
     * handle a player info or state-pos packet
     *
     * @param packet the packet
     */
    public void handleSession(DatagramPacket packet)
    {
        if (sessionId == null || sessionId.isEmpty())
        {
            logger.debug("Talker has no session ID");
            this.handleProtocolSession(packet);
        } else
        {
            this.handle(packet);
        }

        this.latestTime = new Date().getTime();
    }

    /**
     * Sends a packet to the talker with header and crc
     *
     * @param packet The packet data
     */
    public void sendFullPacket(byte[] packet)
    {
        byte[] header = header();
        ByteBuffer allocate = ByteBuffer.allocate(header.length + packet.length + CRC_BYTES.length);
        allocate.put(header);
        allocate.put(packet);
        allocate.put(CRC_BYTES);
        byte[] byteArray = allocate.array();
        // System.out.println("size: [" + byteArray.length + "]");
        // System.out.println(UdpDebug.byteArrayToHexString(byteArray));
        allocate = null;
        header = null;
        packet = null;
        send(byteArray);
        byteArray = null;
    }

    /**
     * Get the talker's remote port.
     *
     * @return the talker remote port
     */
    public int getPort()
    {
        return this.sender.getPort();
    }

    /**
     * get the talker's time difference (now - started)
     *
     * @return the talker's time difference
     */
    public long getTimeDiff()
    {
        long now = new Date().getTime();
        return now - startedTime;
    }

    /**
     * get the talker's update time difference (now - last packet)
     *
     * @return the talker's update time difference
     */
    public long getUpdatedDiff()
    {
        return new Date().getTime() - latestTime;
    }

    /**
     * Get the client-hello-time bytes.
     *
     * @return The client-hello-time bytes.
     */
    public byte[] getCliTime()
    {
        return cliTime;
    }

    /**
     * Get the sequence bytes.
     *
     * @return The sequence bytes.
     */
    public byte[] getSequence()
    {
        return ByteBuffer.allocate(2)
                .putShort((short) countA++)
                .array();
    }

    public FreeroamPlayerUpdates getPlayerUpdates()
    {
        return playerUpdates;
    }

    /**
     * Get the client-time-diff bytes.
     *
     * @return The client-time-diff bytes.
     */
    public byte[] getTimeDiffBytes()
    {
        long timeDiff = getTimeDiff();
        return ByteBuffer.allocate(2).putShort((short) timeDiff).array();
    }

    /**
     * Get the talker's X-position in the game world
     *
     * @return the talker's X-position in the game world
     */
    public int getXPos()
    {
        return this.sbrwParser.getXPos();
    }

    /**
     * Get the talker's Y-position in the game world
     *
     * @return the talker's Y-position in the game world
     */
    public int getYPos()
    {
        return this.sbrwParser.getYPos();
    }

    /**
     * Get the talker's Z-position in the game world
     *
     * @return the talker's Z-position in the game world
     */
    public int getZPos()
    {
        return this.sbrwParser.getZPos();
    }

    public byte[] getPlayerInfo(long timeDiff)
    {
        return sbrwParser.getPlayerPacket(timeDiff);
    }

    public byte[] getPlayerXYZ(long timeDiff)
    {
        return sbrwParser.getStatePosPacket(timeDiff);
    }

    public boolean isAlive()
    {
        long now = new Date().getTime();
        long aliveTime = now - latestTime;
        return (aliveTime < 2000L);
    }

    /**
     * Gets the visible talker manager
     *
     * @return The visible talker manager
     */
    public FreeroamVisibleTalkers getVisibleTalkers()
    {
        return visibleTalkers;
    }

    /**
     * Get the talker's name
     *
     * @return The talker's name
     */
    public String getName()
    {
        return this.sbrwParser.getName();
    }

    /**
     * Get the talker's channel
     *
     * @return The talker's channel
     */
    public String getChannel()
    {
        return this.sbrwParser.getChannel();
    }

    /**
     * check if the talker is ready to receive packets
     *
     * @return true if the talker is ready to receive packets
     */
    public boolean isReady()
    {
        return ready && isAlive();
    }

    @Override
    public String toString()
    {
        return "FreeroamTalker{name=" + sbrwParser.getName() + "}";
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj != null
                && obj instanceof FreeroamTalker
                && ((FreeroamTalker) obj).getPort() == this.getPort();
    }

    /**
     * handle player-info packet
     *
     * @param packet player-info packet
     */
    private void handleProtocolSession(DatagramPacket packet)
    {
        ByteBuf buf = packet.content();
        byte[] data = ByteBufUtil.getBytes(buf);

        if (this.sbrwParser == null)
        {
            this.sbrwParser = new SbrwParser(data);
            this.ready = true;

            String channel = this.sbrwParser.getChannel(), name = this.sbrwParser.getName();

            if (channel != null)
            {
                this.sessionId = channel;

                if (name != null)
                {
                    this.logger = LoggerFactory.getLogger("talker-" + name);
                    logger.debug("{} joined {}", name, channel.replace('.', ' '));
                    
                    FreeroamRangeCalc.setVisibleTalkersForTalker(this);
                    logger.debug("Set visible talkers");

                    this.sendFirstPlayer();
                    logger.debug("sendFirstPlayer called");
                    
                    this.startBroadcaster();
                    logger.debug("Started buffer sender");
                    
//                    this.startUpdater();
//                    logger.debug("Started player updater");
                }
            }
        } else
        {
            this.parse(data);
        }
    }

    /**
     * handle state-pos packet
     *
     * @param packet state-pos packet
     */
    private void handle(DatagramPacket packet)
    {
        ByteBuf buf = packet.content();
        byte[] data = ByteBufUtil.getBytes(buf);

        this.parse(data);
    }

    private void parse(byte[] data)
    {
        this.sbrwParser.parseInputData(data);
        this.latestTime = new Date().getTime();
        this.ready = true;
    }

    private void startBroadcaster()
    {
        ConcurrentUtils.scheduler.scheduleAtFixedRate(
                new BufferSend(PacketBuffers.get(this)),
                75,
                85,
                TimeUnit.MILLISECONDS
        );
    }

    private void sendFirstPlayer()
    {
        this.getVisibleTalkers().sendFirstPlayer();
    }

    private void startUpdater()
    {
        ConcurrentUtils.scheduler.scheduleAtFixedRate(
                new FreeroamPlayerUpdates(this),
                105,
                125,
                TimeUnit.MILLISECONDS
        );
    }

    private byte[] header()
    {
        byte[] seqArray = getSequence();
        byte[] seqArray2 = ByteBuffer.allocate(2).putShort((short) countB++).array();
        byte[] timeDiffBytes = getTimeDiffBytes();
        byte[] header = new byte[]{ //
                seqArray[0], seqArray[1], // seq
                (byte) 0x02, // fixo
                timeDiffBytes[0], timeDiffBytes[1], // time
                cliTime[0], cliTime[1], //
                seqArray2[0], seqArray2[1], // counter?? (with counter, need to start at same time, cli like it and stop sending id packets)
                // (byte) 0xff, (byte) 0xff, // counter?? (without counter, can start any time, cli dont like it and keep sending id packets, need sync time
                // bytes
                // on 12:1a packets)
                (byte) 0xff, //
                (byte) 0xff, //
                (byte) 0x00//
        };
        seqArray = null;
        seqArray2 = null;
        timeDiffBytes = null;
        return header;
    }

    private static final byte[] CRC_BYTES = {0x01, 0x02, 0x03, 0x04};
}
