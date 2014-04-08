/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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

package playground.anhorni.rc;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.controler.Controler;
import org.matsim.core.network.NetworkChangeEvent;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.network.NetworkChangeEvent.ChangeType;
import org.matsim.core.network.NetworkChangeEvent.ChangeValue;

import playground.christoph.burgdorf.BurgdorfRoutes;


public class RCControler extends Controler {
					
	public RCControler(final String[] args) {
		super(args);	
	}

	public static void main (final String[] args) { 
		RCControler controler = new RCControler(args);
		controler.setOverwriteFiles(true);
		controler.setScoringFunctionFactory(new RCScoringFunctionFactory(
				controler.getConfig().planCalcScore(), controler.getScenario()));
		
		if (Boolean.parseBoolean(controler.getConfig().findParam("rc", "withinday"))) {
			Set<Id> links = controler.createTunnelLinks();
			controler.addControlerListener(new WithindayListener(controler, links));
			controler.addNetworkChange(controler, links);
		}		
    	controler.run();
    }
	
	public void addNetworkChange(Controler controler, Set<Id> links) {
		NetworkImpl network = (NetworkImpl) controler.getNetwork();
		ChangeValue changeValue;
		NetworkChangeEvent networkChangeEvent;
		
		// reduce capacity
		networkChangeEvent = network.getFactory().createNetworkChangeEvent(15.5 * 3600.0);
		
		for (Id id : links) {
			Link link = network.getLinks().get(id);
			networkChangeEvent.addLink(link);
		}		
		changeValue = new ChangeValue(ChangeType.FACTOR, 0.0);
		networkChangeEvent.setFlowCapacityChange(changeValue);
		network.addNetworkChangeEvent(networkChangeEvent);
	}
	
	
	public Set<Id> createTunnelLinks() {
		Set<Id> links = new HashSet<Id>();
		String tunnellinksfile = this.config.findParam("rc", "tunnellinksfile");
		
		 try {
	          final BufferedReader in = new BufferedReader(new FileReader(tunnellinksfile));
	          
	          int cnt = 0;
	          String curr_line = in.readLine(); // Skip header
	          while ((curr_line = in.readLine()) != null) {
	        	  links.add(new IdImpl(curr_line));
		          cnt++;
	          }
	          in.close();
	          log.info("added " + cnt + " links");
	          
	        } // end try
	        catch (IOException e) {
	        	e.printStackTrace();
	        }			
		return links;
	}
	
}
