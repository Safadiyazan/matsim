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


import org.matsim.contrib.freight.vrp.algorithms.rr.VRPTestCase;
import org.matsim.contrib.freight.vrp.basics.Driver;
import org.matsim.contrib.freight.vrp.basics.Shipment;
import org.matsim.contrib.freight.vrp.basics.TourImpl;
import org.matsim.contrib.freight.vrp.basics.Vehicle;
import org.matsim.contrib.freight.vrp.basics.VehicleImpl;
import org.matsim.contrib.freight.vrp.utils.VrpTourBuilder;

public class TourCostProcessorTest extends VRPTestCase{
	
	TourImpl tour;
	
	Driver driver;
	
	Vehicle vehicle;
	
	TourImpl anotherTour;
	
	TourCostProcessor statusUpdater;
	
	@Override
	public void setUp(){
		initJobsInPlainCoordinateSystem();
		VrpTourBuilder tourBuilder = new VrpTourBuilder();
		Shipment s1 = createShipment("1", makeId(0,0), makeId(0,10));
		Shipment s2 = createShipment("2", makeId(0,0), makeId(10,0));
		Shipment s3 = createShipment("3", makeId(0,9), makeId(10,0));
		
		tourBuilder.scheduleStart(makeId(0,0), 0.0, Double.MAX_VALUE);
		tourBuilder.schedulePickup(s1);
		tourBuilder.schedulePickup(s2);
		tourBuilder.scheduleDelivery(s1);
		tourBuilder.scheduleDelivery(s2);
		tourBuilder.scheduleEnd(makeId(0,0), 0.0, Double.MAX_VALUE);
		tour = tourBuilder.build();
		
		VrpTourBuilder anotherTourBuilder = new VrpTourBuilder();
		anotherTourBuilder.scheduleStart(makeId(0,0), 0.0, Double.MAX_VALUE);
		anotherTourBuilder.schedulePickup(s3);
		anotherTourBuilder.scheduleDelivery(s3);
		anotherTourBuilder.scheduleEnd(makeId(0,0), 0.0, Double.MAX_VALUE);
		anotherTour = anotherTourBuilder.build();
		
		statusUpdater = new TourCostProcessor(costs);
		
		driver = new Driver(){};
		
		vehicle = new VehicleImpl("dummy", "dummy", null);
	}
	
	@Override
	public void tearDown(){
		
	}
	
	public void testCalculatedCosts(){
		statusUpdater.process(tour, vehicle, driver);
		assertEquals(40.0, tour.tourData.transportCosts);
	}
	
	public void testCalculatedTime(){
		statusUpdater.process(tour, vehicle, driver);
		assertEquals(40.0, tour.tourData.transportTime);
	}
	
	public void testCalculatedCostsForAnotherTour(){
		statusUpdater.process(anotherTour, vehicle, driver);
		assertEquals(38.0, anotherTour.tourData.transportCosts);
	}
	
	public void testCalculatedTimeForAnotherTour(){
		statusUpdater.process(anotherTour, vehicle, driver);
		assertEquals(38.0, anotherTour.tourData.transportTime);
	}

}
