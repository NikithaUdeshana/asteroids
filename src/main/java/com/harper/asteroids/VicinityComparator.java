package com.harper.asteroids;

import com.harper.asteroids.model.CloseApproachData;
import com.harper.asteroids.model.Distances;
import com.harper.asteroids.model.NearEarthObject;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Optional;

public class VicinityComparator implements Comparator<NearEarthObject> {
    private LocalDate startDate;
    private LocalDate endDate;

    public VicinityComparator(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int compare(NearEarthObject neo1, NearEarthObject neo2) {
        long startEpochTime = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endEpochTime = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        Optional<Distances> neo1ClosestPass = neo1.getCloseApproachData().stream()
                .filter(neo -> isApproachingWithinTargetPeriod(neo, startEpochTime, endEpochTime))
                .min(Comparator.comparing(CloseApproachData::getMissDistance))
                .map(min -> min.getMissDistance());
        Optional<Distances> neo2ClosestPass = neo2.getCloseApproachData().stream()
                .filter(neo -> isApproachingWithinTargetPeriod(neo, startEpochTime, endEpochTime))
                .min(Comparator.comparing(CloseApproachData::getMissDistance))
                .map(min -> min.getMissDistance());

        if(neo1ClosestPass.isPresent()) {
            if(neo2ClosestPass.isPresent()) {
                return neo1ClosestPass.get().compareTo(neo2ClosestPass.get());
            }
            else return 1;
        }
        else return -1;
    }

    private static boolean isApproachingWithinTargetPeriod(
            CloseApproachData approachData,
            Long startEpochTime,
            Long endEpochTime) {
                    long approachEpochTime = approachData.getCloseApproachEpochDate();
                    return approachEpochTime >= startEpochTime && approachEpochTime <= endEpochTime;
                }
}
