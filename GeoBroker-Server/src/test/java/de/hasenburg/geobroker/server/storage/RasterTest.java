package de.hasenburg.geobroker.server.storage;

import de.hasenburg.geobroker.commons.model.spatial.Geofence;
import de.hasenburg.geobroker.commons.model.spatial.Location;
import de.hasenburg.geobroker.server.storage.Raster;
import de.hasenburg.geobroker.server.storage.RasterEntry;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.spatial4j.exception.InvalidShapeException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RasterTest {

	private static final Logger logger = LogManager.getLogger();

	Method privateMethod_calculateIndexLocation;
	Method privateMethod_calculateIndexLocations;
	Raster raster;

	@Before
	public void setUpTest() throws NoSuchMethodException {
		privateMethod_calculateIndexLocation = Raster.class.getDeclaredMethod("calculateIndexLocation", Location.class);
		privateMethod_calculateIndexLocations = Raster.class.getDeclaredMethod("calculateIndexLocations", Geofence.class);

		privateMethod_calculateIndexLocation.setAccessible(true);
		privateMethod_calculateIndexLocations.setAccessible(true);
	}

	@After
	public void tearDownTest() {
		privateMethod_calculateIndexLocation = null;
		privateMethod_calculateIndexLocations = null;
		raster = null;
	}

	public Location invokeCalculateIndexLocation(Location location) {
		try {
			return (Location) privateMethod_calculateIndexLocation.invoke(raster, location);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
			fail("Could not invoke private method");
		}
		// Stupid, I never get here, why do I need to return something?
		return null;
	}

	public List<RasterEntry> invokeCalculateIndexLocations(Geofence geofence) {
		try {
			return (List<RasterEntry>) privateMethod_calculateIndexLocations.invoke(raster, geofence);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
			fail("Could not invoke private method");
		}
		// Stupid, I never get here, why do I need to return something?
		return null;
	}

	@Test(expected = InvalidShapeException.class)
	public void testCalculateIndexGranularity1() {
		raster = new Raster(1);
		Location calculatedIndex;

		// even
		calculatedIndex = invokeCalculateIndexLocation(new Location(10, -10));
		assertEquals(new Location(10, -10), calculatedIndex);

		// many fractions
		calculatedIndex = invokeCalculateIndexLocation(new Location(10.198, -11.198));
		assertEquals(new Location(10, -12), calculatedIndex);

		// exact boundary
		calculatedIndex = invokeCalculateIndexLocation(new Location(90, -180));
		assertEquals(new Location(90, -180), calculatedIndex);

		// out of bounds, expect throw
		invokeCalculateIndexLocation(new Location(91, -181));
	}

	@Test(expected = InvalidShapeException.class)
	public void testCalculateIndexGranularity10() {

		raster = new Raster(10);
		Location calculatedIndex;

		// even
		calculatedIndex = invokeCalculateIndexLocation(new Location(10, -10));
		assertEquals(new Location(10, -10), calculatedIndex);

		// many fractions
		calculatedIndex = invokeCalculateIndexLocation(new Location(10.198, -11.198));
		assertEquals(new Location(10.1, -11.2), calculatedIndex);

		// exact boundary
		calculatedIndex = invokeCalculateIndexLocation(new Location(90, -180));
		assertEquals(new Location(90, -180), calculatedIndex);

		// out of bounds, expect throw
		invokeCalculateIndexLocation(new Location(91, -181));
	}

	@Test(expected = InvalidShapeException.class)
	public void testCalculateIndexGranularity100() {

		raster = new Raster(100);
		Location calculatedIndex;

		// even
		calculatedIndex = invokeCalculateIndexLocation(new Location(10, -10));
		assertEquals(new Location(10, -10), calculatedIndex);

		// many fractions
		calculatedIndex = invokeCalculateIndexLocation(new Location(10.198, -11.198));
		assertEquals(new Location(10.19, -11.2), calculatedIndex);

		// exact boundary
		calculatedIndex = invokeCalculateIndexLocation(new Location(90, -180));
		assertEquals(new Location(90, -180), calculatedIndex);

		// out of bounds, expect throw
		invokeCalculateIndexLocation(new Location(91, -181));
	}

	@Test
	public void testCalculateIndexLocationsForGeofenceRectangle() {
		raster = new Raster(1);
		Geofence fence = Geofence.polygon(Arrays.asList(
				new Location(-0.5, -0.5),
				new Location(-0.5, 1.5),
				new Location(1.5, 1.5),
				new Location(1.5, -0.5)
		));

		List<RasterEntry> result = invokeCalculateIndexLocations(fence);
		assertEquals(9, result.size());
		assertTrue(containsLocation(result, new Location(-1, -1)));
		assertTrue(containsLocation(result, new Location(-1, 0)));
		assertTrue(containsLocation(result, new Location(-1, 1)));
		assertTrue(containsLocation(result, new Location(0, -1)));
		assertTrue(containsLocation(result, new Location(0, 0)));
		assertTrue(containsLocation(result, new Location(0, 1)));
		assertTrue(containsLocation(result, new Location(1, -1)));
		assertTrue(containsLocation(result, new Location(1, 0)));
		assertTrue(containsLocation(result, new Location(1, 1)));
	}

	@Test
	public void testCalculateIndexLocationsForGeofenceTriangle() {
		raster = new Raster(1);
		Geofence fence = Geofence.polygon(Arrays.asList(
				new Location(-0.5, -1.5),
				new Location(-0.5, 0.7),
				new Location(1.7, -1.5)
		));

		List<RasterEntry> result = invokeCalculateIndexLocations(fence);
		assertEquals(8, result.size());
		assertTrue(containsLocation(result, new Location(-1, -2)));
		assertTrue(containsLocation(result, new Location(-1, -1)));
		assertTrue(containsLocation(result, new Location(-1, 0)));
		assertTrue(containsLocation(result, new Location(0, -2)));
		assertTrue(containsLocation(result, new Location(0, -1)));
		assertTrue(containsLocation(result, new Location(0, 0)));
		assertTrue(containsLocation(result, new Location(1, -2)));
		assertTrue(containsLocation(result, new Location(1, -1)));
	}

	@Test
	public void testCalculateIndexLocationsForGeofenceCircle() {
		raster = new Raster(1);
		Geofence fence = Geofence.circle(new Location(0.5, 0), 1.1);

		List<RasterEntry> result = invokeCalculateIndexLocations(fence);
		assertEquals(8, result.size());
		assertTrue(containsLocation(result, new Location(-1, -1)));
		assertTrue(containsLocation(result, new Location(-1, 0)));
		assertTrue(containsLocation(result, new Location(0, -2)));
		assertTrue(containsLocation(result, new Location(0, -1)));
		assertTrue(containsLocation(result, new Location(0, 0)));
		assertTrue(containsLocation(result, new Location(0, 1)));
		assertTrue(containsLocation(result, new Location(1, -1)));
		assertTrue(containsLocation(result, new Location(1, 0)));
	}

	@Test
	public void testCalculateIndexLocationsForGeofenceCircle2() {
		raster = new Raster(5);
		Location l = new Location(39.984702, 116.318417);
		Geofence fence = Geofence.circle(l, 0.1);

		List<RasterEntry> result = invokeCalculateIndexLocations(fence);
		assertTrue(containsLocation(result, new Location(39.8, 116.2)));
		assertTrue(containsLocation(result, new Location(39.8, 116.4)));
		assertTrue(containsLocation(result, new Location(40, 116.2)));
		assertTrue(containsLocation(result, new Location(40, 116.4)));
		assertEquals(4, result.size());
	}

	@Test
	public void testCalculateIndexLocationsForGeofenceCircle3() {
		raster = new Raster(10);
		Location l = new Location(39.984702, 116.318417);
		Geofence fence = Geofence.circle(l, 0.1);

		List<RasterEntry> result = invokeCalculateIndexLocations(fence);
		assertTrue(containsLocation(result, new Location(39.8, 116.2)));
		assertTrue(containsLocation(result, new Location(39.8, 116.3)));
		assertTrue(containsLocation(result, new Location(39.9, 116.2)));
		assertTrue(containsLocation(result, new Location(39.9, 116.3)));
		assertTrue(containsLocation(result, new Location(39.9, 116.4)));
		assertTrue(containsLocation(result, new Location(40.0, 116.2)));
		assertTrue(containsLocation(result, new Location(40.0, 116.3)));
		assertTrue(containsLocation(result, new Location(40.0, 116.4)));
		assertEquals(8, result.size());
	}

	@Test
	public void testPutAndThenGet() {
		raster = new Raster(25);
		Location l = new Location(40.007499, 116.320013);
		Geofence fence = Geofence.circle(l, 0.01);
		ImmutablePair<String, Integer> sid = new ImmutablePair<>("test", 1);

		List<RasterEntry> result = invokeCalculateIndexLocations(fence);
		Location index = invokeCalculateIndexLocation(l);
		assertTrue(containsLocation(result, index));

		raster.putSubscriptionIdIntoRasterEntries(fence, sid);
		Map<String, Set<ImmutablePair<String, Integer>>> ids =
				raster.getSubscriptionIdsForPublisherLocation(l);
		logger.info(ids);
	}

	private boolean containsLocation(List<RasterEntry> result, Location l) {
		for (RasterEntry rasterEntry : result) {
			if (rasterEntry.getIndex().equals(l)) {
				return true;
			}
		}

		return  false;
	}

}