package soapboxrace.world.mpfr.freeroam;

import soapboxrace.world.mpfr.server.udp.FreeroamAllTalkers;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FreeroamTalkerCleanup implements Runnable
{
    public FreeroamTalkerCleanup()
    {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(this, 3000, 3000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run()
    {
        for (FreeroamTalker talker : FreeroamAllTalkers.getFreeroamTalkers().values())
        {
            if (!talker.isAlive())
            {
                FreeroamAllTalkers.remove(talker.getPort());
            }
        }
    }
}
