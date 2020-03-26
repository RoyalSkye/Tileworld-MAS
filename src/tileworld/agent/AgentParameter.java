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
//    public static ArrayList<Color> color = new ArrayList<Color>();

    public static double lifetimeLearningRate = 0.15;  // 每次错误估计lifetime学习率
    public static double lifetimeFinalLearningRate = 0.05;  // 最后的学习率
    public static double lifetimeIncreaseLearningRatio = 0.0;  // 每次上升lifetime学习率

    public static final int pickGreedya = 7; // 搜索的时候顺便捡的贪心算法      // 4:283   7:289
    public static final double leastFuelAddParam = 1.15; // 去加油参数，一定要大于1
    public static final double aPlanSearchScore = 1.5; // 策略a,小于这个就搜索
    public static final double aPlanPickStop = 0.5; // 策略a,是否结束pick，转而询问scheme
    public static final double aPlanPickStart = 2.9; // 策略a,真正预计可以得到的分数,是否开始

    public static final int pickGreedyb = 2; // 搜索的时候顺便捡的贪心算法
    public static final double bPlanPickScore = 3.0; // 策略b，agent1 pick的score
    public static final int agentMaxDis = 1000; // agent2 Pick 的距离
    public static final int agent1pickDis = 14; // agent1 Pick 的距离

    public static final int pickGreedyc = 10; // 搜索的时候顺便捡的贪心算法


}
