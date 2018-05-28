package soapboxrace.world.mpfr.freeroam.player;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;
import soapboxrace.world.mpfr.freeroam.FreeroamTalker;
import soapboxrace.world.mpfr.server.udp.FreeroamAllTalkers;
import soapboxrace.world.mpfr.utils.MainLogger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FreeroamRangeCalc
{
    public static FreeroamVisibleTalkers.PlayerListDifferences setVisibleTalkersForTalker(FreeroamTalker freeroamTalker)
    {
        return FreeroamRangeCalc.setVisibleClosestPlayers(freeroamTalker);
    }

    private static FreeroamVisibleTalkers.PlayerListDifferences setVisibleClosestPlayers(FreeroamTalker freeroamTalker)
    {
        List<FreeroamTalker> talkers = FreeroamAllTalkers.getFreeroamTalkers()
                .entrySet()
                .stream()
                .filter(t -> !t.getValue().equals(freeroamTalker))
                .filter(t -> t.getValue().isReady())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        List<Pair<FreeroamTalker, Double>> distancePairs = talkers.stream()
                .map(talker -> {
                    int[] self = {
                            freeroamTalker.getXPos(),
                            freeroamTalker.getYPos(),
                    };
                    int[] them = {
                            talker.getXPos(),
                            talker.getYPos()
                    };

                    return ImmutablePair.of(talker, Math.sqrt(Math.pow(them[0] - self[0], 2) + Math.pow(them[1] - self[1], 2)));
                })
                .filter(pair -> pair.getRight() <= 2000) // change?
                .sorted(Comparator.comparingDouble(Pair::getValue))
                .limit(FreeroamVisibleTalkers.VISIBLE_PLAYERS_LIMIT)
                .collect(Collectors.toList());
        final List<FreeroamTalker> closestList = distancePairs.stream()
                .map(Pair::getKey)
                .collect(Collectors.toList());
        MainLogger.logger.debug("closest for [{}] = {}", freeroamTalker, closestList);
        return freeroamTalker.getVisibleTalkers().setVisibleTalkers(
                closestList
        );

//        List<FreeroamTalker> closestList = FreeroamAllTalkers.getFreeroamTalkers()
//                .entrySet()
//                .stream()
//                .filter(t -> !t.getValue().equals(freeroamTalker))
//                .filter(t -> t.getValue().isReady())
//                .sorted(Comparator.comparingDouble(t -> {
//                    int[] self = {
//                            freeroamTalker.getXPos(),
//                            freeroamTalker.getYPos(),
//                    };
//                    int[] them = {
//                            t.getValue().getXPos(),
//                            t.getValue().getYPos()
//                    };
//                    return Math.sqrt(Math.pow(them[0] - self[0], 2) + Math.pow(them[1] - self[1], 2));//
//                }))
//                .limit(FreeroamVisibleTalkers.VISIBLE_PLAYERS_LIMIT)
//                .map(Map.Entry::getValue)
//                .collect(Collectors.toList());
////        MainLogger.logger.debug("closest for [{}] = {}", freeroamTalker, closestList);
//        
//        return freeroamTalker.getVisibleTalkers().setVisibleTalkers(closestList);
    }
}
