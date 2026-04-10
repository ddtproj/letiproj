package utils;


import engine.BPSimulator;
import engine.exception.BPSimulatorException;
import model.Activity;
import model.ProcessActivity;
import model.TokenFlow;


/**
 * Helper class for split-gateway path selections.
 *
 */
 public class GatewayPathSelector {
     private BPSimulator simulator;

     public GatewayPathSelector(BPSimulator simulator) {
         this.simulator = simulator;
     }


     /**
      * Given preceeding element and list of all possible flows selects
      * ones to activate. For different gateways selects only:
      * 1 - for XOR gateways;
      * all - for AND gateways;
      * at least 1 for OR gateways.
      * For OR gateways evaluates probabilities until at least one of the flows gets enabled.
      *
      * @param fromActivity- gateway element
      * @param flowIndexes - outgoing flow indexes
      * @return  array of flows to enable
      * @throws BPSimulatorException
      */
     public int[] getEnabledFlows(ProcessActivity fromActivity, int[] flowIndexes) throws BPSimulatorException {
         int enabledPaths[], n;
         Activity a = fromActivity.getActivity();

         //method is applicable only for split gateways
         if (a.getType() != Activity.ActivityType.GATEWAY || !a.isSplit()) {
             throw new RuntimeException("Activity " + a.toString() + " is not split gateway");
         }

         switch (a.getGatewayType()) {
             case AND:
                 return flowIndexes; // activate all flows
             case XOR:
                 if (flowIndexes.length > 0) {
                     TokenFlow tf = this.simulator.getTokenFlow(flowIndexes[0]);
                     double rand = this.simulator.getRandomGenerator().random(); // random between 0 and 1
                     double lastChecked = 0.0D;
                     int select;
                     for (select = 0; select < flowIndexes.length; select++) {
                         tf = this.simulator.getTokenFlow(flowIndexes[select]);
                         if (rand < tf.getProbability() + lastChecked) {
                             break;
                         }
                         lastChecked += tf.getProbability();
                     }
                     if (select == flowIndexes.length) {
                         select = (int) this.simulator.getRandomGenerator().uniform(0.0D, flowIndexes.length);
                     }
                     return new int[]{flowIndexes[select]}; //activate one selected flow
                 }
                 return flowIndexes;

             case OR:
                 enabledPaths = new int[flowIndexes.length];
                 n = 0;

                 while (true) {
                     for (int i = 0; i < flowIndexes.length; i++) {
                         double rand = this.simulator.getRandomGenerator().random();
                         double prob = this.simulator.getTokenFlow(flowIndexes[i]).getProbability();
                         if (rand <= prob || prob == 0.0D) //activate flow
                             enabledPaths[n++] = flowIndexes[i];
                     }
                     if (n != 0) // some flows should be activated
                         return enabledPaths;
                 }


             case EVENT:
                 return new int[0];
         }
         throw new RuntimeException("Invalid gateway type for: " + a.toString());
     }


 }