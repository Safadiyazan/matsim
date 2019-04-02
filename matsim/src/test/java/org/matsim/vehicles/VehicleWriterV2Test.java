/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.vehicles;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.testcases.MatsimTestCase;
import org.matsim.vehicles.VehicleType.DoorOperationMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * @author dgrether
 */
public class VehicleWriterV2Test extends MatsimTestCase {
	private static final Logger log = Logger.getLogger( VehicleWriterV2Test.class ) ;

	private static final String TESTXML  = "testVehicles_v2.xml";

	private final Id<Vehicle> id23 = Id.create("23", Vehicle.class);
	private final Id<Vehicle> id42 = Id.create("42", Vehicle.class);
	private final Id<Vehicle> id42_23 = Id.create(" 42  23", Vehicle.class); //indeed this should be double blank in the middle but due to collapse this is only one blank

	public void testWriter() throws FileNotFoundException, IOException {

		String outfileName = this.getOutputDirectory() + "testOutputVehicles.xml";

		//read it
		Vehicles vehicles = VehicleUtils.createVehiclesContainer();
		MatsimVehicleReader reader = new MatsimVehicleReader(vehicles);
		reader.readFile(this.getPackageInputDirectory() + TESTXML);

		//write it
		VehicleWriterV2 writer = new VehicleWriterV2(vehicles);
		writer.writeFile(outfileName);
		assertTrue(new File(outfileName).exists()); 
		//read it again
		vehicles = VehicleUtils.createVehiclesContainer();
		reader = new MatsimVehicleReader(vehicles);
		reader.readFile(this.getOutputDirectory() + "testOutputVehicles.xml");

		//check it, check it, check it now!
		this.checkContent(vehicles);
	}

	private void checkContent(Vehicles vehdef) {
		Map<Id<VehicleType>, VehicleType> vehicleTypes = vehdef.getVehicleTypes();
		Map<Id<Vehicle>, Vehicle> vehicles = vehdef.getVehicles();

		assertNotNull(vehicleTypes);
		assertEquals(2, vehicleTypes.size());
		VehicleType vehType = vehicleTypes.get(Id.create("normal&Car", VehicleType.class));
		assertNotNull(vehType);
		assertEquals(9.5, vehType.getLength(), EPSILON);
		assertEquals(3.0, vehType.getWidth(), EPSILON);
		assertEquals(42.0, vehType.getMaximumVelocity(), EPSILON);
		assertNotNull(vehType.getCapacity());
		assertEquals(Integer.valueOf(5), vehType.getCapacity().getSeats());
		assertEquals(Integer.valueOf(20), vehType.getCapacity().getStandingRoom());
		assertNotNull(vehType.getCapacity().getFreightCapacity());
		assertEquals(23.23, vehType.getCapacity().getFreightCapacity().getVolume(), EPSILON);
		assertNotNull(vehType.getEngineInformation());
		assertEquals(EngineInformation.FuelType.diesel, vehType.getEngineInformation().getFuelType());
		assertEquals(0.23, VehicleUtils.getFuelConsumption(vehType), EPSILON);
		assertEquals(23.23, VehicleUtils.getAccessTime(vehType), EPSILON);
		assertEquals(42.42, VehicleUtils.getEgressTime(vehType), EPSILON);
		assertEquals(DoorOperationMode.parallel, VehicleUtils.getDoorOperationMode(vehType));
		assertEquals(2.0, vehType.getPcuEquivalents());
		assertEquals(1.5, vehType.getFlowEfficiencyFactor());
		assertEquals("abc", vehType.getAttributes().getAttribute("Attribute1"));
		assertEquals(1.3, (double) vehType.getAttributes().getAttribute("Attribute2"), EPSILON);


		vehType = vehicleTypes.get(Id.create("defaultValue>Car", VehicleType.class));
		assertNotNull(vehType);
		assertEquals(7.5, vehType.getLength(), EPSILON);
		assertEquals(1.0, vehType.getWidth(), EPSILON);
		assertTrue(Double.isInfinite(vehType.getMaximumVelocity()));
		assertNull(vehType.getCapacity());
		assertEquals(DoorOperationMode.serial, VehicleUtils.getDoorOperationMode(vehType));
		assertEquals(1.0, vehType.getPcuEquivalents());

		assertNotNull(vehicles);
		assertEquals(3, vehicles.size());

		assertNotNull(vehicles.get(id23));
		assertEquals(id23, vehicles.get(id23).getId());
		assertEquals(Id.create("normal&Car", VehicleType.class), vehicles.get(id23).getType().getId());

		assertNotNull(vehicles.get(id42));
		assertEquals(id42, vehicles.get(id42).getId());
		assertEquals(Id.create("defaultValue>Car", VehicleType.class), vehicles.get(id42).getType().getId());

		assertNotNull(vehicles.get(id42_23));
		assertEquals(id42_23, vehicles.get(id42_23).getId());


	}

}
