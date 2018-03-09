package soapboxrace.world.mpfr.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainLogger
{
    public static Logger logger;
    
    static {
        logger = LoggerFactory.getLogger("MPFR-SRV");
    }
}
