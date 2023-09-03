package com.harper.asteroids;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harper.asteroids.model.NearEarthObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestApproachDetector {

    private ObjectMapper mapper = new ObjectMapper();
    private NearEarthObject neo1, neo2;

    @Before
    public void setUp() throws IOException {
        neo1 = mapper.readValue(getClass().getResource("/neo_example.json"), NearEarthObject.class);
        neo2 = mapper.readValue(getClass().getResource("/neo_example2.json"), NearEarthObject.class);

    }

    @Test
    public void testFiltering() {

        List<NearEarthObject> neoList = List.of(neo1, neo2);

        //Neo2 has the closest passing at 5261628 kms away.
        //Neo2's closest passing is in 2028.
        List<NearEarthObject> filteredList1 = ApproachDetector.getClosest(
                neoList, 2,
                LocalDate.of(2028, 1, 1),
                LocalDate.of(2028, 12, 31));
        assertEquals(1, filteredList1.size());
        assertEquals(neo2, filteredList1.get(0));

        // In Jan 2020, neo1 is closer (5390966 km, vs neo2's at 7644137 km)
        List<NearEarthObject> filteredList2 = ApproachDetector.getClosest(
                neoList, 2,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2020, 12, 31));
        assertEquals(2, filteredList2.size());
        assertEquals(neo1, filteredList2.get(0));
        assertEquals(neo2, filteredList2.get(1));
    }
}
