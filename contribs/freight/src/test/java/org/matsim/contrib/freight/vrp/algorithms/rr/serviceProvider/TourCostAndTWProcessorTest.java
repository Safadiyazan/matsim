/*******************************************************************************
 * Copyright (C) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.matsim.contrib.freight.vrp.algorithms.rr.serviceProvider;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matsim.contrib.freight.vrp.algorithms.rr.TDCosts;
import org.matsim.contrib.freight.vrp.algorithms.rr.VRPTestCase;
import org.matsim.contrib.freight.vrp.basics.Coordinate;
import org.matsim.contrib.freight.vrp.basics.Driver;
import org.matsim.contrib.freight.vrp.basics.Locations;
import org.matsim.contrib.freight.vrp.basics.Shipment;
import org.matsim.contrib.freight.vrp.basics.TourImpl;
import org.matsim.contrib.freight.vrp.basics.Vehicle;
import org.matsim.contrib.freight.vrp.basics.VehicleImpl;
import org.matsim.contrib.freight.vrp.utils.VrpTourBuilder;
import org.matsim.contrib.freight.vrp.utils.VrpUtils;

public class TourCostAndTWProcessorTest extends VRPTestCase{
	
	static class MyLocations implements Locations{

		private Map<String,Coordinate> locations = new HashMap<String, Coordinate>();

		public void addLocation(String id, Coordinate coord){
			locations.put(id, coord);
		}

		@Override
		public Coordinate getCoord(String id) {
			return locations.get(id);
		}
	}
	
	TourImpl tour;
	
	Driver driver;
	
	Vehicle vehicle;
	
	TourImpl anotherTour;
	
	TourStatusProcessor statusUpdater;
	
	TourStatusProcessor tdTourStatusProcessor;
	
	@Override
	public void setUp(){
		
		driver = new Driver(){};
		
		vehicle = new VehicleImpl("dummy", "dummy", null);
		
		initJobsInPlainCoordinateSystem();
		
		VrpTourBuilder tourBuilder = new VrpTourBuilder();
		Shipment s1 = createShipment("1", makeId(0,10), makeId(0,0));
		s1.setPickupTW(VrpUtils.createTimeWindow(8,12));
		
		Shipment s2 = createShipment("2", makeId(10,0), makeId(0,0));
		s2.setPickupTW(VrpUtils.createTimeWindow(30,30));
		
		tourBuilder.scheduleStart(makeId(0,0), 0.0, 0.0);
		tourBuilder.schedulePickup(s1);
		tourBuilder.schedulePickup(s2);
		tourBuilder.scheduleDelivery(s1);
		tourBuilder.scheduleDelivery(s2);
		tourBuilder.scheduleEnd(makeId(0,0), 0.0, Double.MAX_VALUE);
		tour = tourBuilder.build();
		
		statusUpdater = new TourCostAndTWProcessor(costs);
		
		MyLocations loc = new MyLocations();
		loc.addLocation(makeId(0,10),new Coordinate(0,10));
		loc.addLocation(makeId(10,0),new Coordinate(10,0));
		loc.addLocation(makeId(0,0),new Coordinate(0,0));
		
		double depotClosingTime = 100.0;
		List<Double> timeBins = new ArrayList<Double>();
		timeBins.add(0.2*depotClosingTime);
		timeBins.add(0.4*depotClosingTime);
		timeBins.add(0.6*depotClosingTime);
		timeBins.add(0.8*depotClosingTime);
		timeBins.add(1.0*depotClosingTime);
		
		List<Double> speedValues = new ArrayList<Double>();
		speedValues.add(1.0);
		speedValues.add(2.0);
		speedValues.add(1.0);
		speedValues.add(2.0);
		speedValues.add(1.0);
		
		TDCosts tdCosts = new TDCosts(loc, timeBins, speedValues);
		tdTourStatusProcessor = new TourCostAndTWProcessor(tdCosts);
	}

	
	public void testCalculatedTimeWithTDCost(){
		tdTourStatusProcessor.process(tour, vehicle, driver);
		assertEquals((10.0+10.0+5.0+5.0), tour.tourData.transportTime);
	}

	public void testEarliestArrStart(){
		statusUpdater.process(tour, vehicle, driver);
		assertEquals(0.0,tour.getActivities().get(0).getEarliestOperationStartTime());
	}
	
	public void testLatestArrStart(){
		statusUpdater.process(tour, vehicle, driver);
		assertEquals(0.0,tour.getActivities().get(0).getLatestOperationStartTime());
	}
	
	public void testEarliestArrAtFirstPickup(){
		statusUpdater.process(tour, vehicle, driver);
		assertEquals(10.0,tour.getActivities().get(1).getEarliestOperationStartTime());
	}
	
	public void testEarliestArrAtFirstPickupWithTDCost(){
		tdTourStatusProcessor.process(tour, vehicle, driver);
		assertEquals(10.0,tour.getActivities().get(1).getEarliestOperationStartTime());
	}
	
	public void testLatestArrAtFirstPickup(){
		statusUpdater.process(tour, vehicle, driver);
		assertEquals(10.0,tour.getActivities().get(1).getLatestOperationStartTime());
	}
	
	public void testLatestArrAtFirstPickupWithTDCost(){
		tdTourStatusProcessor.process(tour, vehicle, driver);
		assertEquals(12.0,tour.getActivities().get(1).getLatestOperationStartTime());
	}
	
	public void testEarliestArrAtSecondPickup(){
		statusUpdater.process(tour, vehicle, driver);
		assertEquals(30.0,tour.getActivities().get(2).getEarliestOperationStartTime());
	}
	
	public void testEarliestArrAtSecondPickupWithTDCosts(){
		tdTourStatusProcessor.process(tour, vehicle, driver);
		assertEquals(30.0,tour.getActivities().get(2).getEarliestOperationStartTime());
	}
	
	public void testLatestArrAtSecondPickup(){
		statusUpdater.process(tour, vehicle, driver);
		assertEquals(30.0,tour.getActivities().get(2).getLatestOperationStartTime());
	}
	
	public void testLatestArrAtSecondPickupWithTDCosts(){
		tdTourStatusProcessor.process(tour, vehicle, driver);
		assertEquals(30.0,tour.getActivities().get(2).getLatestOperationStartTime());
	}
	
	public void testEarliestArrAtEnd(){
		statusUpdater.process(tour, vehicle, driver);
		assertEquals(40.0,tour.getActivities().get(5).getEarliestOperationStartTime());
	}
	
	public void testEarliestArrAtEndWithTDCosts(){
		tdTourStatusProcessor.process(tour, vehicle, driver);
		assertEquals(35.0,tour.getActivities().get(5).getEarliestOperationStartTime());
	}
	
	public void testLatestArrAtEnd(){
		statusUpdater.process(tour, vehicle, driver);
		assertEquals(Double.MAX_VALUE,tour.getActivities().get(5).getLatestOperationStartTime());
	}
	
	public void testLatestArrAtEndWithTDCosts(){
		tdTourStatusProcessor.process(tour, vehicle, driver);
		assertEquals(Double.MAX_VALUE,tour.getActivities().get(5).getLatestOperationStartTime());
	}
	

}
