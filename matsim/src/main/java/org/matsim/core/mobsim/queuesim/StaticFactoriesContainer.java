package org.matsim.core.mobsim.queuesim;

import org.matsim.api.core.v01.population.Person;
import org.matsim.core.mobsim.framework.MobsimDriverAgent;
import org.matsim.ptproject.qsim.agents.DefaultAgentFactory;
import org.matsim.ptproject.qsim.helpers.MobsimTimer;
import org.matsim.ptproject.qsim.interfaces.MobsimTimerI;
import org.matsim.ptproject.qsim.qnetsimengine.QVehicle;
import org.matsim.ptproject.qsim.qnetsimengine.QVehicleUtils;
import org.matsim.vehicles.Vehicle;

public class StaticFactoriesContainer {

	static QVehicle createQueueVehicle(Vehicle vehicle) {
	//		return new QueueVehicle(basicVehicle);
			return (QVehicle) QVehicleUtils.createMobsimVehicle(vehicle);
		}

	static QVehicle createQueueVehicle(Vehicle vehicle, double sizeInEquivalents) {
	//		return new QueueVehicle(basicVehicle, sizeInEquivalents);
			return (QVehicle) QVehicleUtils.createMobsimVehicle(vehicle, sizeInEquivalents);
		}

	public static MobsimDriverAgent createQueuePersonAgent(Person p, QueueSimulation simulation) {
//		return new QueuePersonAgent(p, simulation);
//		return new DefaultPersonDriverAgent(p, simulation);
		return new DefaultAgentFactory( simulation ).createMobsimAgentFromPersonAndInsert( p ) ;
	}

	public static MobsimTimerI createSimulationTimer(double stepSize) {
//		return new SimulationTimer(stepSize);
		return new MobsimTimer(stepSize);
	}

}
