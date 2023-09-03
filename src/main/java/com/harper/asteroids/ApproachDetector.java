package com.harper.asteroids;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harper.asteroids.model.NearEarthObject;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Receives a set of neo ids and rates them after earth proximity.
 * Retrieves the approach data for them and sorts to the n closest.
 * https://api.nasa.gov/neo/rest/v1/neo/
 * Alerts if someone is possibly hazardous.
 */
public class ApproachDetector {
    private static final String NEO_URL = "https://api.nasa.gov/neo/rest/v1/neo/";
    private List<String> nearEarthObjectIds;
    private Client client;
    private ObjectMapper mapper = new ObjectMapper();

    public ApproachDetector(List<String> ids) {
        this.nearEarthObjectIds = ids;
        this.client = ClientBuilder.newClient();
    }

    /**
     * Get the n closest approaches in a given time period
     *
     * @param limit     - n
     * @param startDate - starting date of the period
     * @param endDate - end date of the period
     * @return A list of NearEarthObjects
     */
    public List<NearEarthObject> getClosestApproaches(int limit, LocalDate startDate, LocalDate endDate) {
        List<NearEarthObject> neos = new ArrayList<>(limit);
        for(String id: nearEarthObjectIds) {
            try {
                System.out.println("Check passing of object " + id);
                Response response = client
                    .target(NEO_URL + id)
                    .queryParam("api_key", App.API_KEY)
                    .request(MediaType.APPLICATION_JSON)
                    .get();

                NearEarthObject neo = mapper.readValue(response.readEntity(String.class), NearEarthObject.class);
                neos.add(neo);
            } catch (IOException e) {
                System.err.println("Failed scanning for asteroids: " + e);
            }
        }
        System.out.println("Received " + neos.size() + " neos, now sorting");

        return getClosest(neos, limit, startDate, endDate);
    }

    /**
     * Get the n closest approaches in a given time period.
     *
     * @param neoList      the NearEarthObjects
     * @param limit        the n number of closest approaches
     * @param startDate    the start date of the time period
     * @param endDate      the end date of the time period
     * @return List of NearEarthObjects
     */
    public static List<NearEarthObject> getClosest(
            List<NearEarthObject> neoList,
            int limit,
            LocalDate startDate,
            LocalDate endDate) {
        long startEpochTime = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endEpochTime = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return neoList.stream()
                .filter(neo -> neo.getCloseApproachData() != null && ! neo.getCloseApproachData().isEmpty())
                .filter(neo -> isApproachingWithinTargetPeriod(neo, startEpochTime, endEpochTime))
                .sorted(new VicinityComparator(startDate, endDate))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private static boolean isApproachingWithinTargetPeriod(
            NearEarthObject neo,
            Long startEpochTime,
            Long endEpochTime) {
        return neo.getCloseApproachData().stream()
                .anyMatch(approachData -> {
                    long approachEpochTime = approachData.getCloseApproachEpochDate();
                    return approachEpochTime >= startEpochTime && approachEpochTime <= endEpochTime;
                });
    }

}
