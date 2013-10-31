/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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
package playground.wrashid.parkingSearch.ppSim.jdepSim.searchStrategies.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.parking.lib.GeneralLib;
import org.matsim.contrib.parking.lib.obj.DoubleValueHashMap;

import playground.wrashid.lib.obj.IntegerValueHashMap;
import playground.wrashid.parkingSearch.planLevel.init.ParkingRoot;
import playground.wrashid.parkingSearch.ppSim.jdepSim.searchStrategies.ParkingSearchStrategy;
import playground.wrashid.parkingSearch.ppSim.jdepSim.searchStrategies.manager.EvaluationContainer;
import playground.wrashid.parkingSearch.ppSim.jdepSim.searchStrategies.manager.ParkingStrategyManager;
import playground.wrashid.parkingSearch.ppSim.jdepSim.zurich.ZHScenarioGlobal;

public class StrategyStats {

	private ArrayList<Double> averageBestList;
	private ArrayList<Double> averageExecutedList;
	private ArrayList<Double> averageWorstList;
	private ArrayList<Double> averageAverageList;
	
	private HashMap<ParkingSearchStrategy, ArrayList<Double>> strategySharesAll;
	private HashMap<ParkingSearchStrategy, ArrayList<Double>> strategySharesWithoutPrivateParking;
	
	public StrategyStats(){
		averageBestList = new ArrayList();
		averageExecutedList = new ArrayList();
		averageWorstList = new ArrayList();
		averageAverageList = new ArrayList();
		strategySharesAll=new HashMap<ParkingSearchStrategy, ArrayList<Double>>();
		strategySharesWithoutPrivateParking=new HashMap<ParkingSearchStrategy, ArrayList<Double>>();
	}
	
	
	public void addIterationData(HashMap<Id, HashMap<Integer, EvaluationContainer>> strategyEvaluations) {
		updateScores(strategyEvaluations);
		updateStrategySharesAll(strategyEvaluations);
	}


	private void updateStrategySharesAll(HashMap<Id, HashMap<Integer, EvaluationContainer>> strategyEvaluations) {
		IntegerValueHashMap<ParkingSearchStrategy> strategyCountAll=new IntegerValueHashMap<ParkingSearchStrategy>();
		
		int sampleSizeAll=0;
		for (Id personId : strategyEvaluations.keySet()) {
			for (Integer legIndex : strategyEvaluations.get(personId).keySet()) {
				EvaluationContainer evaluationContainer = strategyEvaluations.get(personId).get(legIndex);
				strategyCountAll.increment(evaluationContainer.getCurrentSelectedStrategy().strategy);
				sampleSizeAll++;
			}
		}
		
		for (ParkingSearchStrategy pss: ParkingStrategyManager.allStrategies){
			if (!strategySharesAll.containsKey(pss)){
				strategySharesAll.put(pss, new ArrayList<Double>());
			}
			
			if (!strategyCountAll.containsKey(pss)){
				strategyCountAll.set(pss, 0);
			}
			
			strategySharesAll.get(pss).add(100.0*strategyCountAll.get(pss)/sampleSizeAll);
		}
	}
	
	public void updateStrategySharesWithoutPP() {
		IntegerValueHashMap<ParkingSearchStrategy> strategyCount=new IntegerValueHashMap<ParkingSearchStrategy>();
		
		int sampleSize=0;
		
		for (ParkingEventDetails ped:ZHScenarioGlobal.parkingEventDetails){
			if (!ped.parkingActivityAttributes.getFacilityId().toString().contains("private")){
				strategyCount.increment(ped.parkingStrategy);
				sampleSize++;
			}
		}
		
		for (ParkingSearchStrategy pss: ParkingStrategyManager.allStrategies){
			if (!strategySharesWithoutPrivateParking.containsKey(pss)){
				strategySharesWithoutPrivateParking.put(pss, new ArrayList<Double>());
			}
			
			if (!strategyCount.containsKey(pss)){
				strategyCount.set(pss, 0);
			}
			
			strategySharesWithoutPrivateParking.get(pss).add(100.0*strategyCount.get(pss)/sampleSize);
		}
	}
	


	private void updateScores(HashMap<Id, HashMap<Integer, EvaluationContainer>> strategyEvaluations) {
		double averageBest=0;
		double averageExecuted=0;
		double averageWorst=0;
		double averageAverage=0;
		
		int sampleSize=0;
		for (Id personId : strategyEvaluations.keySet()) {
			for (Integer legIndex : strategyEvaluations.get(personId).keySet()) {
				EvaluationContainer evaluationContainer = strategyEvaluations.get(personId).get(legIndex);
				averageBest+=evaluationContainer.getBestStrategyScore();
				averageExecuted+=evaluationContainer.getSelectedStrategyScore();
				averageWorst+=evaluationContainer.getWorstStrategyScore();
				averageAverage+=evaluationContainer.getAverageStrategyScore();
				sampleSize++;
			}
		}
		
		int numberOfPeople = sampleSize;
		averageBestList.add(averageBest/numberOfPeople);
		averageExecutedList.add(averageExecuted/numberOfPeople);
		averageWorstList.add(averageWorst/numberOfPeople);
		averageAverageList.add(averageAverage/numberOfPeople);
	}


	public void writeStrategyScoresToFile() {
		String xLabel = "Iteration";
		String yLabel = "score";
		String title="Parking Strategy Score";
		int numberOfXValues = averageBestList.size();
		int numberOfFunctions = 4;
		double[] xValues=new double[numberOfXValues];
		String[] seriesLabels=new String[numberOfFunctions];
		
		seriesLabels[0]="average best";
		seriesLabels[1]="average executed";
		seriesLabels[2]="average worst";
		seriesLabels[3]="average average";
		
		double[][] matrix=new double[numberOfXValues][numberOfFunctions];
		
		for (int i=0;i<numberOfXValues;i++){
			matrix[i][0]=averageBestList.get(i);
			matrix[i][1]=averageExecutedList.get(i);
			matrix[i][2]=averageWorstList.get(i);
			matrix[i][3]=averageAverageList.get(i);
			xValues[i]=i;
		}

		GeneralLib.writeGraphic(ZHScenarioGlobal.outputFolder + "parkingStrategyScores.png", matrix, title, xLabel, yLabel, seriesLabels, xValues);
	}
	
	public void writeAllStrategySharesToFile() {
		writeStrategySharesToFile(strategySharesAll,"parkingStrategyShares.png","Parking Strategy Shares");
	}
	
	public void writeNonPPStrategySharesToFile() {
		writeStrategySharesToFile(strategySharesWithoutPrivateParking,"parkingStrategyShares_WithoutPrivateParking.png","Parking Strategy Shares (without PrivateParking)");
	}
	
	public void writeStrategySharesToFile(HashMap<ParkingSearchStrategy, ArrayList<Double>> shares,String outputFileName, String title) {
		String xLabel = "Iteration";
		String yLabel = "share [%]";
		int numberOfXValues = averageBestList.size();
		int numberOfFunctions = shares.size();
		double[] xValues=new double[numberOfXValues];
		String[] seriesLabels=new String[numberOfFunctions];
		
		int j=0;
		for (ParkingSearchStrategy pss: ParkingStrategyManager.allStrategies){
			seriesLabels[j]=pss.getName();
			j++;
		}
		
		double[][] matrix=new double[numberOfXValues][numberOfFunctions];
		
		for (int i=0;i<numberOfXValues;i++){
			j=0;
			for (ParkingSearchStrategy pss: ParkingStrategyManager.allStrategies){
				matrix[i][j]=shares.get(pss).get(i);
				j++;
			}
			xValues[i]=i;
		}

		GeneralLib.writeGraphic(ZHScenarioGlobal.outputFolder + outputFileName, matrix, title, xLabel, yLabel, seriesLabels, xValues);
	}
}

