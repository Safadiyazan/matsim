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

package org.matsim.core.replanning.strategies;

import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.config.groups.ControlerConfigGroup;
import org.matsim.core.config.groups.ControlerConfigGroup.PlanCheckerLevel;
import org.matsim.core.config.groups.GlobalConfigGroup;
import org.matsim.core.replanning.PlanStrategy;
import org.matsim.core.replanning.PlanStrategyImpl;
import org.matsim.core.replanning.PlanStrategyImpl.Builder;
import org.matsim.core.replanning.modules.PlanChecker;
import org.matsim.core.replanning.selectors.RandomPlanSelector;
import org.matsim.core.router.TripRouter;
import org.matsim.facilities.ActivityFacilities;

import javax.inject.Inject;
import javax.inject.Provider;

public class ReRoute implements Provider<PlanStrategy> {

	@Inject private ControlerConfigGroup controlerConfigGroup;
	@Inject private GlobalConfigGroup globalConfigGroup;
	@Inject private ActivityFacilities facilities;
	@Inject private Provider<TripRouter> tripRouterProvider;

	@Override
	public PlanStrategy get() {
		Builder builder = new PlanStrategyImpl.Builder(new RandomPlanSelector<Plan,Person>()) ;
		if (controlerConfigGroup.getPlanCheckerLevel().equals(PlanCheckerLevel.AFTER_EACH_MODIFICATION)) {
			builder.addStrategyModule(new PlanChecker(globalConfigGroup, "Strategy ReRoute after Step PlanSelector"));
		}
		builder.addStrategyModule(new org.matsim.core.replanning.modules.ReRoute(facilities, tripRouterProvider, globalConfigGroup));
		if (controlerConfigGroup.getPlanCheckerLevel().equals(PlanCheckerLevel.AFTER_EACH_MODIFICATION) ||
				controlerConfigGroup.getPlanCheckerLevel().equals(PlanCheckerLevel.MEDIUM)) {
			builder.addStrategyModule(new PlanChecker(globalConfigGroup, "Strategy ReRoute after Step ReRoute"));
		}
		return builder.build() ;
	}

}
