package tileworld.agent;

import java.util.ArrayList;
import java.awt.Color;
/**
 * Parameters for agent thinking
 *
 */
public class AgentParameter {
    public static String scheme="C";
    public static boolean isPickNeedOneStep=false;

    public static double lifetimeLearningRate = 0.15;
    public static double lifetimeFinalLearningRate = 0.05;
    public static double lifetimeIncreaseLearningRatio = 0.0;

    public static final int pickGreedya = 7;
    public static final double leastFuelAddParam = 1.15; // must greater than 1
    public static final double aPlanSearchScore = 1.5;
    public static final double aPlanPickStop = 0.5;
    public static final double aPlanPickStart = 2.9;

    public static final int pickGreedyb = 2; // pick greedily during searching
    public static final double bPlanPickScore = 3.0;
    public static final int agentMaxDis = 1000; // agent2 Pick distance
    public static final int agent1pickDis = 14; // agent1 Pick distance

    public static final int pickGreedyc = 10;

}
