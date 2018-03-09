package soapboxrace.world.mpfr.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ConcurrentUtils {
    public static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(50);
}
