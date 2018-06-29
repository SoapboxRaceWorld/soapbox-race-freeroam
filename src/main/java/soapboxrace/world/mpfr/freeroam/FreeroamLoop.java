package soapboxrace.world.mpfr.freeroam;

import soapboxrace.world.mpfr.server.udp.FreeroamAllTalkers;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FreeroamLoop implements Runnable
{
    public FreeroamLoop()
    {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(this, 0, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run()
    {
        for (FreeroamTalker talker : FreeroamAllTalkers.getFreeroamTalkers().values())
        {
            if (talker.isReady())
            {
                talker.getPlayerUpdates().run();
            }
        }
    }
}
