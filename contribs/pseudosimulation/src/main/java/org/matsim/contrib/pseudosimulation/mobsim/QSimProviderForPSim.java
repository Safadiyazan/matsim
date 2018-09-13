/*
 *  *********************************************************************** *
 *  * project: org.matsim.*
 *  * QSimProvider.java
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  * copyright       : (C) 2015 by the members listed in the COPYING, *
 *  *                   LICENSE and WARRANTY file.                            *
 *  * email           : info at matsim dot org                                *
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  *   This program is free software; you can redistribute it and/or modify  *
 *  *   it under the terms of the GNU General Public License as published by  *
 *  *   the Free Software Foundation; either version 2 of the License, or     *
 *  *   (at your option) any later version.                                   *
 *  *   See also COPYING, LICENSE and WARRANTY file                           *
 *  *                                                                         *
 *  * ***********************************************************************
 */

package org.matsim.contrib.pseudosimulation.mobsim;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.pseudosimulation.MobSimSwitcher;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.ControlerConfigGroup;
import org.matsim.core.controler.IterationCounter;
import org.matsim.core.mobsim.framework.AgentSource;
import org.matsim.core.mobsim.framework.Mobsim;
import org.matsim.core.mobsim.framework.listeners.MobsimListener;
import org.matsim.core.mobsim.jdeqsim.JDEQSimConfigGroup;
import org.matsim.core.mobsim.jdeqsim.JDEQSimulation;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.mobsim.qsim.ComponentRegistry;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.QSimBuilder;
import org.matsim.core.mobsim.qsim.components.NamedComponentUtils;
import org.matsim.core.mobsim.qsim.components.QSimComponents;
import org.matsim.core.mobsim.qsim.interfaces.ActivityHandler;
import org.matsim.core.mobsim.qsim.interfaces.DepartureHandler;
import org.matsim.core.mobsim.qsim.interfaces.MobsimEngine;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class QSimProviderForPSim implements Provider<Mobsim> {
	private static final Logger log = Logger.getLogger(QSimProviderForPSim.class);

	private Injector injector;
	private Config config;
	private Collection<AbstractQSimModule> modules;
	private List<AbstractQSimModule> overridingModules;
	private QSimComponents components;
	@Inject(optional = true)
	private IterationCounter iterationCounter;

	@Inject
	private Scenario scenario;
	@Inject
	private EventsManager eventsManager;
	@Inject
	private MobSimSwitcher mobSimSwitcher;
	@Inject
	private PSimProvider pSimProvider;

	@Inject
	QSimProviderForPSim(Injector injector, Config config, Collection<AbstractQSimModule> modules,
			QSimComponents components, @Named("overrides") List<AbstractQSimModule> overridingModules) {
		this.injector = injector;
		this.modules = modules;
		// (these are the implementations)
		this.config = config;
		this.components = components;
		this.overridingModules = overridingModules;
	}

	@Override
	public Mobsim get() {

		if (!this.config.controler().getMobsim().equals(ControlerConfigGroup.MobsimType.qsim.toString())) {
			throw new RuntimeException("Expected: " + ControlerConfigGroup.MobsimType.qsim.toString());
		}		
		if (!mobSimSwitcher.isQSimIteration()) {
			return pSimProvider.get();
		} // else ... the below

		modules.forEach(m -> m.setConfig(config));

		AbstractQSimModule qsimModule = AbstractQSimModule.overrideQSimModules(modules, overridingModules);

		AbstractModule module = new AbstractModule() {
			@Override
			protected void configure() {
				install(qsimModule);
				bind(QSim.class).asEagerSingleton();
				bind(Netsim.class).to(QSim.class);
			}
		};

		Injector qsimInjector = injector.createChildInjector(module);
		if (iterationCounter == null
				|| config.controler().getFirstIteration() == iterationCounter.getIterationNumber()) {
			// trying to somewhat reduce logfile verbosity. kai, aug'18
			org.matsim.core.controler.Injector.printInjector(qsimInjector, log);
		}
		QSim qSim = qsimInjector.getInstance(QSim.class);

		ComponentRegistry<MobsimEngine> mobsimEngineRegistry = new ComponentRegistry<>("MobsimEngine");
		ComponentRegistry<ActivityHandler> activityHandlerRegistry = new ComponentRegistry<>("ActivityHandler");
		ComponentRegistry<DepartureHandler> departureHandlerRegistry = new ComponentRegistry<>("DepartureHandler");
		ComponentRegistry<AgentSource> agentSourceRegistry = new ComponentRegistry<>("AgentSource");
		ComponentRegistry<MobsimListener> listenerRegistry = new ComponentRegistry<>("MobsimListener");

		NamedComponentUtils.find(qsimInjector, MobsimEngine.class).forEach(mobsimEngineRegistry::register);
		NamedComponentUtils.find(qsimInjector, ActivityHandler.class).forEach(activityHandlerRegistry::register);
		NamedComponentUtils.find(qsimInjector, DepartureHandler.class).forEach(departureHandlerRegistry::register);
		NamedComponentUtils.find(qsimInjector, AgentSource.class).forEach(agentSourceRegistry::register);
		NamedComponentUtils.find(qsimInjector, MobsimListener.class).forEach(listenerRegistry::register);

		mobsimEngineRegistry.getOrderedComponents(components.activeMobsimEngines).stream()
				.map(qsimInjector::getInstance).forEach(qSim::addMobsimEngine);
		activityHandlerRegistry.getOrderedComponents(components.activeActivityHandlers).stream()
				.map(qsimInjector::getInstance).forEach(qSim::addActivityHandler);
		departureHandlerRegistry.getOrderedComponents(components.activeDepartureHandlers).stream()
				.map(qsimInjector::getInstance).forEach(qSim::addDepartureHandler);
		agentSourceRegistry.getOrderedComponents(components.activeAgentSources).stream().map(qsimInjector::getInstance)
				.forEach(qSim::addAgentSource);
		listenerRegistry.getOrderedComponents(components.activeMobsimListeners).stream().map(qsimInjector::getInstance)
				.forEach(qSim::addQueueSimulationListeners);

		return qSim;
	}

}
