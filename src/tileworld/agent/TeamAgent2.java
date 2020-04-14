/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tileworld.agent;

import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.Portrayal;
import tileworld.environment.TWDirection;
import tileworld.environment.TWEntity;
import tileworld.environment.TWEnvironment;
import tileworld.environment.TWHole;
import tileworld.environment.TWTile;
import tileworld.environment.TWFuelStation;
import tileworld.exceptions.CellBlockedException;
import tileworld.planners.AstarPathGenerator;
import tileworld.planners.TWPath;
import tileworld.environment.TWObstacle;
import tileworld.planners.TWPathStep;

import java.awt.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * TWContextBuilder
 *
 * @author michaellees
 * Created: Feb 6, 2011
 *
 * Copyright michaellees Expression year is undefined on line 16, column 24 in Templates/Classes/Class.java.
 *
 *
 * Description:
 *
 */
public class TeamAgent2 extends TWAgent{
    private String name="agent2";
    private String tempMessage = "";
    private String tempAllMessage = "";
    private int fuelX = -1;
    private int fuelY = -1;
    private final int mapsizeX = this.getEnvironment().getxDimension();
    private final int mapsizeY = this.getEnvironment().getyDimension();
    private int[][] seenMap = new int[mapsizeX][mapsizeY];
    private ArrayList<int[]> mapChain = new ArrayList<int[]>(); //  [[x1,y1], [x2,y2], [x3,y3],...]
    private final int mapChainLength;
    private String agentState1="initial";
    private int agentState2=0;
    private int agentState3=0;
    private String otherAgentState1="";
    private int otherAgentState2=0;
    private int otherAgentState3=0;
    private int otherCarriedTiles=0;
    private int[] otherASF=new int[] {-5,-5,-5,-5};
    private TWPath curPath=null;
    private int curPathStep=0;
    private AstarPathGenerator pathGenerator = new AstarPathGenerator(this.getEnvironment(), this, mapsizeX+mapsizeY);
    private ArrayList<int[]> search_tile_chain = new ArrayList<int[]>();
    private int rethinking=0;
    private int pickVanished = 0;
    private int[] otherAgentPosition = {-1,-1};
    public ArrayList<int[]> pick_tile_chain = new ArrayList<int[]>();
    private ArrayList<int[]> bPlanPickArea = new ArrayList<int[]>();
    private int[] bPlanPickTarget=new int[]{-1,-1};
    private ArrayList<int[]> cPlanSearchPoint= new ArrayList<int[]>();
    private int[] cPlanFuelPoint=new int[]{-1,-1};


    public TeamAgent2(String name, int xpos, int ypos, TWEnvironment env, double fuelLevel) {
        super(xpos,ypos,env,fuelLevel);
        this.name = name;
        mapChainLength = this.initalMapChain();
        for(int i=0; i<seenMap.length;i++) Arrays.fill(seenMap[i], -1);
    }

    private void cPlanSearchPointInitial(){
        if (cPlanSearchPoint.size() > 0) return;
        if (Math.abs(mapsizeX/2-fuelX) < Math.abs(mapsizeY/2-fuelY)){
            cPlanSearchPoint.clear();
            cPlanSearchPoint.add(new int[] {3,3});
            cPlanSearchPoint.add(new int[] {mapsizeX/2-4, 3});
            cPlanSearchPoint.add(new int[] {mapsizeX/2-4, mapsizeY-4});
            cPlanSearchPoint.add(new int[] {3, mapsizeY-4});
            addTempMessage("cPlanSearchPointInitial byX");
        } else {
            cPlanSearchPoint.clear();
            cPlanSearchPoint.add(new int[] {3,3});
            cPlanSearchPoint.add(new int[] {mapsizeX-4, 3});
            cPlanSearchPoint.add(new int[] {mapsizeX-4, mapsizeY/2-4});
            cPlanSearchPoint.add(new int[] {3, mapsizeY/2-4});
            addTempMessage("cPlanSearchPointInitial byY");
        }
        cPlanInitalFuelCheckPoint();
    }

    private void cPlanInitalFuelCheckPoint(){
        int curX=-1;
        int curY=-1;
        int curDis = 99999;
        for(int k=0; k<=3; k++){
            int start=cPlanSearchPoint.get(k)[k%2];
            int to=cPlanSearchPoint.get((k+1)%4)[k%2];
            for (int i=Math.min(start,to); i<Math.max(start,to); i++){
                int tempX=(k%2==0)? i : cPlanSearchPoint.get(k)[0];
                int tempY=(k%2==1)? i : cPlanSearchPoint.get(k)[1];
                if (Math.abs(fuelX-tempX)+Math.abs(fuelY-tempY) < curDis){
                    curX=tempX; curY=tempY;
                    curDis=Math.abs(fuelX-tempX)+Math.abs(fuelY-tempY);
                }
            }
        }
        cPlanFuelPoint = new int[]{curX, curY};
        System.out.println("cPlanFuelPoint x,y:"+cPlanFuelPoint[0]+" "+cPlanFuelPoint[1]);
    }

    public int nearSearchChainPoint(){
        int all_len = search_tile_chain.size();
        int nearIndex=0;
        int curDis=9999;
        for (int index=0; index<all_len; index++){
            if ( Math.abs(this.getX() - search_tile_chain.get(index)[0])+Math.abs(this.getY() - search_tile_chain.get(index)[1])< curDis){
                nearIndex = index;
                curDis = Math.abs(this.getX() - search_tile_chain.get(index)[0])+Math.abs(this.getY() - search_tile_chain.get(index)[1]);
            }
        }
        if (curDis==0) return (nearIndex+1)%all_len ;
        return nearIndex;
    }

    public void initalSearchTileChain(int x1, int y1, int x2, int y2, int ax, int ay){
        this.search_tile_chain.clear();
        int curX = x1;
        int curY = y1;
        int index = -1;
        int minDis=9999;
        int nearIndex = -1;
        int direction = 1;
        System.out.println(this.name);
        ArrayList<int[]> tempSearchChain = new ArrayList<int[]>();
        do {
            tempSearchChain.add(new int[]{Math.min(curX, x2),Math.min(curY, y2)});
            index++;
            if (Math.abs(curX-ax)+Math.abs(curY-ay) < minDis) {
                minDis = Math.abs(curX-ax)+Math.abs(curY-ay);
                nearIndex=index;
            }
            if (direction == 1 && curX < x2){
                curX += 7;
            } else if (direction == 1 && curY < y2){
                curY += 7;
                direction = -1;
            } else if (direction == 1){
                direction = -1;
                curX -= 7;
            } else if (direction == -1 && curX > x1+7){
                curX -= 7;
            } else if (direction == -1 && curY  < y2){
                direction = 1;
                curY += 7;
            } else if (direction == -1){
                curX -= 7;
                direction = 0;
            } else if (direction == 0){
                curY -= 7;
            }
        } while (curX != x1 || curY != y1);
        for (int i=0; i<=index; i++){
            System.out.println("x, y: " + (tempSearchChain.get((nearIndex+i)%(index+1))[0]) + " " + (tempSearchChain.get((nearIndex+i)%(index+1))[1]));
            search_tile_chain.add(new int[]{(tempSearchChain.get((nearIndex+i)%(index+1))[0]), (tempSearchChain.get((nearIndex+i)%(index+1))[1])});
        }
    }

    public void initalSearchTileChainb(int ax, int ay){
        search_tile_chain.clear();
        boolean sizeMoreThan60=false;
        int curX = mapsizeX/2 - 17;
        int curY = mapsizeY/2 - 17;
        int[] allPoint = new int[] {0,0, 1,0, 1,1, 2,1, 2,0, 3,0, 3,1, 4,1, 4,0, 5,0, 5,1, 5,2, 5,3, 4,3, 4,2, 3,2, 3,3, 2,3, 2,2, 1,2, 1,3, 1,4,
                2,4, 3,4, 4,4, 5,4, 5,5, 4,5, 3,5, 2,5, 1,5, 0,5, 0,4, 0,3, 0,2, 0,1};
        int len=6*6;
        if (mapsizeX >= 60 && mapsizeY >= 60) {
            sizeMoreThan60=true;
            curX = mapsizeX/2 - 24;
            curY = mapsizeY/2 - 21;
            allPoint = new int[] {0,0, 1,0, 1,1, 2,1, 2,0, 3,0, 3,1, 4,1, 4,0, 5,0, 5,1, 6,1, 6,0, 7,0, 7,1, 7,2, 7,3, 6,3, 6,2, 5,2, 5,3,
                    4,3, 4,2, 3,2, 3,3, 2,3, 2,2, 1,2, 1,3, 1,4, 1,5, 2,5, 2,4, 3,4, 3,5, 4,5, 4,4, 5,4, 5,5, 6,5, 6,4, 7,4, 7,5, 7,6, 6,6, 5,6, 4,6,
                    3,6, 2,6, 1,6, 0,6, 0,5, 0,4, 0,3, 0,2, 0,1};
            len=8*7;
        }
        int x1 = curX; int y1 = curY;
        int cI = 0; int cDis = 999;
        for (int i=0; i<len; i++){
            int px=allPoint[2 * i]*7+curX;
            int py=allPoint[2*i+1]*7+curY;
            if (Math.abs(px-ax)+Math.abs(py-ay) < cDis){
                cDis=Math.abs(px-ax)+Math.abs(py-ay);
                cI = i;
            }

        }
        for (int i=0; i<len; i++){
            int realI = (i+cI) % (len);
            search_tile_chain.add(new int[] {allPoint[2*realI]*7+curX, allPoint[2*realI+1]*7+curY});
        }

        for (int i=0; i < search_tile_chain.size(); i++){
            System.out.println("Bplan SearchChain x,y" + search_tile_chain.get(i)[0]+" " +search_tile_chain.get(i)[1]);
        }
    }

    public int initalMapChain(){  // generate mapchain as scheme2 shown: index = 64 if 50*50
        int curX = 3;
        int curY = 3;
        int index = 0;
        int direction = 1;
        do {
            mapChain.add(new int[]{Math.min(curX, mapsizeX-3),Math.min(curY, mapsizeY-3)});
            index++;
            if (direction == 1 && curX + 3 < mapsizeX-1){
                curX += 7;
            } else if (direction == 1 && curY + 3 < mapsizeY-1){
                curY += 7;
                direction = -1;
            } else if (direction == 1){
                direction = -1;
                curX -= 7;
            } else if (direction == -1 && curX > 11){
                curX -= 7;
            } else if (direction == -1 && curY + 3 < mapsizeY-1){
                direction = 1;
                curY += 7;
            } else if (direction == -1){
                curX -= 7;
                direction = 0;
            } else if (direction == 0){
                curY -= 7;
            }

        } while (curX != 3 || curY != 3);

        return index;
    }

    private void updateSeenMap(int x, int y){
        for (int i=x-3;i<x+4;i++){
            for (int j=y-3;j<y+4;j++){
                if (0 <= i & i <= mapsizeX-1 & 0 <= j & j<= mapsizeY-1){
                    seenMap[i][j]=0;
                }
            }
        }
    }

    private void unseenMapOneStep(){
        for (int i=0; i < mapsizeX; i++){
            for (int j=0; j<mapsizeY; j++){
                if (seenMap[i][j] != -1 && seenMap[i][j] <= getELT()+5000) seenMap[i][j] += 1;
            }
        }
    }

    public void clearTempMessage(){
        this.tempMessage = "";
        this.tempAllMessage = "";
    }

    @Override
    public void addTempMessage(String mes){
        this.tempMessage = this.tempMessage + ";" + mes;
    }

    @Override
    public void addTempAllMessage(String mes){
        this.tempAllMessage = this.tempAllMessage + ";" + mes;
    }

    @Override
    public void communicate() {
        // System.out.println(this.getName() + " communicate");
        Message messAge = new Message(this.name, "private", tempMessage);
        this.getEnvironment().receiveMessage(messAge); // this will send the message to the broadcast channel of the environment
        Message messAge2 = new Message(this.name, "all", tempAllMessage);
        this.getEnvironment().receiveMessage(messAge2);
        this.clearTempMessage();
    }

    private double objectLifeRemainEstimate(TWAgentPercept twp) {
        return this.getMemory().objectLifeRemainEstimate(twp);
    }

    private double objectLifetimeEstimate(TWAgentPercept twp){
        return this.getMemory().objectLifetimeEstimate(twp);
    }

    private double distance_score(double dis, TWAgentPercept currentMemory){
        if (objectLifeRemainEstimate(currentMemory) <= dis ){
            return 0.0;
        } else if (dis <= 2){
            return 1.0;
        }
        return 1.0 * Math.min((objectLifeRemainEstimate(currentMemory) - dis) / dis, 1);
    }

    private double memory_tile_score(int ax, int ay){
        return memory_tile_score(ax, ay, 2);
    }

    private double memory_tile_score(int ax, int ay, int cTiles){
        double total_score_tile=0;
        double total_score_hole=0;

        for (int x = 0; x < this.getMemory().getObjects().length; x++) {
            for (int y = 0; y < this.getMemory().getObjects()[x].length; y++) {
                TWAgentPercept currentMemory =  this.getMemory().getObjects()[x][y];
                if (currentMemory != null && currentMemory.getO() instanceof TWTile){
                    total_score_tile += distance_score(Math.abs(ax - x) + Math.abs(ay - y), currentMemory);
                }
                if (currentMemory != null && currentMemory.getO() instanceof TWHole){
                    total_score_hole += distance_score(Math.abs(ax - x) + Math.abs(ay - y), currentMemory);
                }
            }
        }
        return total_score_tile*(4-cTiles)/4+total_score_hole*cTiles/4;
    }

    private int[] getSearchField(){
        return getSearchField(-5,-5,-5,-5);
    }

    private int[] getSearchField(int nx1, int ny1, int nx2, int ny2){
        int xlen=14;
        int ylen=14;
        if (getELT() <= 50){

        } else if (getELT() <= 75){
            xlen=21;
        } else if (getELT() <= 100){
            xlen=28;
        } else {
            xlen=21;
            ylen=28;
        }
        double curMax = 0.0;
        int curX = 0;
        int curY = 0;
        for (int i=0; i <= mapsizeX-xlen; i+=7){
            for (int j=0; j <= mapsizeY-ylen; j+=7){
                double curScore = 0.0;
                for (int ii=0; ii<xlen; ii++){
                    for (int jj=0; jj<ylen; jj++){
                        if ((nx1-3-ii-i)*(ii+i-nx2-4) < 0 && (ny1-3-jj-j)*(jj+j-ny2-4) < 0) curScore += seenMap[i+ii][j+jj];
                    }
                }
                curScore /= (1+ (Math.abs(this.getX()-i-xlen/2)+Math.abs(this.getX()-j-ylen/2))/80);
                if (curScore >= curMax){
                    curMax = curScore;
                    curX = i;
                    curY = j;
                }
            }
        }
        return new int[] {curX+3, curY+3, curX+xlen-4, curY+ylen-4};
    }

    private double getELT() {return this.getMemory().estimateLifeTime;}

    public boolean has(int[] intset, int tar, int start){
        for (int i=start; i<intset.length; i++){
            if (tar==intset[i]) return true;
        }
        return false;
    }

    private int getPickRoute(String type, int ax, int ay, int cTiles){
        ArrayList<TWAgentPercept> possibleTH = new ArrayList<TWAgentPercept>();
        TWAgent tempAgent=this;
        int curCarried = cTiles;
        for (int x = 0; x < this.getMemory().getObjects().length; x++) {
            for (int y = 0; y < this.getMemory().getObjects()[x].length; y++) {
                TWAgentPercept currentMemory =  this.getMemory().getObjects()[x][y];
                if (!(currentMemory == null)  && (currentMemory.getO() instanceof TWTile || currentMemory.getO() instanceof TWHole )) {
                    if ( (Math.abs(ax-currentMemory.getO().getX())+Math.abs(ay-currentMemory.getO().getY()) ) <
                            objectLifeRemainEstimate(currentMemory)) possibleTH.add(currentMemory);
                }
            }
        }
        Collections.sort(possibleTH, new Comparator<TWAgentPercept>() {
            @Override
            public int compare(TWAgentPercept twa1, TWAgentPercept twa2){
                if (tempAgent.getDistanceTo(twa1.getO()) < tempAgent.getDistanceTo(twa2.getO())) return -1;
                else if (tempAgent.getDistanceTo(twa1.getO()) == tempAgent.getDistanceTo(twa2.getO())) return 0;
                return 1;
            }
        });
        if (possibleTH.size()>=8){
            for (int i=possibleTH.size()-1; i >= 8; i--){
                possibleTH.remove(possibleTH.get(i));
            }
        }
        System.out.println("pickup Lenth " + possibleTH.size());

        ArrayList<int[]> curPossibleRoute = new ArrayList<int[]>();
        int curHighScore=0;
        int curPredictStep=0;
        ArrayList<int[]> finalRoute = new ArrayList<int[]>();
        curPossibleRoute.add(new int[]{0, curCarried});

        while (curPossibleRoute.size()>0){
            int[] tempR = curPossibleRoute.get(0);
            for (int i=0; i<possibleTH.size(); i++){
                if (has(tempR, i, 2)) continue;
                TWAgentPercept curObj = possibleTH.get(i);
                int cDis;
                if (tempR[0]==0) cDis = (Math.abs(ax-curObj.getO().getX())+Math.abs(ay-curObj.getO().getY()) );
                else cDis = (int) curObj.getO().getDistanceTo(possibleTH.get(tempR[tempR.length-1]).getO());
                if (cDis + tempR[0] < objectLifeRemainEstimate(curObj)){
                    if (curObj.getO() instanceof TWTile && tempR[1] < 3) {
                        int[] tempR0=new int[tempR.length+1];
                        System.arraycopy(tempR, 0, tempR0, 0, tempR.length);
                        tempR0[0] = cDis + tempR[0];
                        tempR0[1] += 1;
                        tempR0[tempR0.length-1] = i;
                        curPossibleRoute.add(tempR0);
                    } else if (curObj.getO() instanceof TWHole && tempR[1] > 0){
                        int[] tempR0=new int[tempR.length+1];
                        System.arraycopy(tempR, 0, tempR0, 0, tempR.length);
                        tempR0[0] = cDis + tempR[0];
                        tempR0[1] -= 1;
                        tempR0[tempR0.length-1] = i;
                        curPossibleRoute.add(tempR0);
                    }
                }
            }
            if (tempR.length-2 > curHighScore || (tempR.length-2==curHighScore && tempR[0] < curPredictStep)){
                curHighScore = tempR.length-2;
                curPredictStep = tempR[0];
                finalRoute.clear();
                for (int j=2; j<tempR.length; j++) finalRoute.add(new int[]{possibleTH.get(tempR[j]).getO().getX(), possibleTH.get(tempR[j]).getO().getY()});
            }
            curPossibleRoute.remove(tempR);
        }
        if ("score".equals(type)) return curHighScore;
        else {return 0;}
    }

    private ArrayList<int[]> getPickRoute(int ax, int ay, int cTiles){
        ArrayList<TWAgentPercept> possibleTH = new ArrayList<TWAgentPercept>();
        TWAgent tempAgent=this;
        int curCarried = cTiles;
        for (int x = 0; x < this.getMemory().getObjects().length; x++) {
            for (int y = 0; y < this.getMemory().getObjects()[x].length; y++) {
                TWAgentPercept currentMemory =  this.getMemory().getObjects()[x][y];
                if (!(currentMemory == null)  && (currentMemory.getO() instanceof TWTile || currentMemory.getO() instanceof TWHole )) {
                    if ( (Math.abs(ax-currentMemory.getO().getX())+Math.abs(ay-currentMemory.getO().getY()) ) <
                            objectLifeRemainEstimate(currentMemory)) possibleTH.add(currentMemory);
                }
            }
        }
        Collections.sort(possibleTH, new Comparator<TWAgentPercept>() {
            @Override
            public int compare(TWAgentPercept twa1, TWAgentPercept twa2){
                if (tempAgent.getDistanceTo(twa1.getO()) < tempAgent.getDistanceTo(twa2.getO())) return -1;
                else if (tempAgent.getDistanceTo(twa1.getO()) == tempAgent.getDistanceTo(twa2.getO())) return 0;
                return 1;
            }
        });
        if (possibleTH.size()>=8){
            for (int i=possibleTH.size()-1; i >= 8; i--){
                possibleTH.remove(possibleTH.get(i));
            }
        }
        System.out.println("pickup Lenth " + possibleTH.size());

        ArrayList<int[]> curPossibleRoute = new ArrayList<int[]>();
        int curHighScore=0;
        int curPredictStep=0;
        ArrayList<int[]> finalRoute = new ArrayList<int[]>();
        curPossibleRoute.add(new int[]{0, curCarried});

        while (curPossibleRoute.size()>0){
            int[] tempR = curPossibleRoute.get(0);
            for (int i=0; i<possibleTH.size(); i++){
                if (has(tempR, i, 2)) continue;
                TWAgentPercept curObj = possibleTH.get(i);
                int cDis;
                if (tempR[0]==0) cDis = (Math.abs(ax-curObj.getO().getX())+Math.abs(ay-curObj.getO().getY()) );
                else cDis =  (int)curObj.getO().getDistanceTo(possibleTH.get(tempR[tempR.length-1]).getO());
                if (cDis + tempR[0] < objectLifeRemainEstimate(curObj)){
                    if (curObj.getO() instanceof TWTile && tempR[1] < 3) {
                        int[] tempR0=new int[tempR.length+1];
                        System.arraycopy(tempR, 0, tempR0, 0, tempR.length);
                        tempR0[0] = cDis + tempR[0];
                        tempR0[1] += 1;
                        tempR0[tempR0.length-1] = i;
                        curPossibleRoute.add(tempR0);
                    } else if (curObj.getO() instanceof TWHole && tempR[1] > 0){
                        int[] tempR0=new int[tempR.length+1];
                        System.arraycopy(tempR, 0, tempR0, 0, tempR.length);
                        tempR0[0] = cDis + tempR[0];
                        tempR0[1] -= 1;
                        tempR0[tempR0.length-1] = i;
                        curPossibleRoute.add(tempR0);
                    }
                }
            }
            if (tempR.length-2 > curHighScore || (tempR.length-2==curHighScore && tempR[0] < curPredictStep)){
                curHighScore = tempR.length-2;
                curPredictStep = tempR[0];
                finalRoute.clear();
                for (int j=2; j<tempR.length; j++) finalRoute.add(new int[]{possibleTH.get(tempR[j]).getO().getX(), possibleTH.get(tempR[j]).getO().getY()});
            }
            curPossibleRoute.remove(tempR);
        }
        System.out.println(this.name+"   picking route");
        System.out.println("pick score: "+curHighScore + "   PredictStep: "+curPredictStep);
        for (int[] route : finalRoute){
            System.out.println("X, Y: "+route[0]+" "+route[1]);
        }
        return finalRoute;
    }

    private boolean change_state(String st1, int st2, int st3){
        if ("AddingFuel".equals(agentState1)){return false;} else
        if ("SearchingTile".equals(agentState1)){
            if (st1.equals(agentState1) && (st2==agentState2 || st2 == -1) && agentState3 < search_tile_chain.size()) return false;
            return true;
        } else
        if ("PickingTile".equals(agentState1) && st2==2){
            if ("SearchingTile".equals(st1)){
                if (AgentParameter.aPlanPickStop > memory_tile_score(this.getX(), this.getY(), this.carriedTiles.size()) || agentState3>=pick_tile_chain.size()){
                    return true;
                } else {return false;}
            }
            return true;
        } else if ("PickingTile".equals(agentState1) && st2==1){
            if ("SearchingTile".equals(st1)){
                return true;
            }
        }
        return true;
    }

    private void state_changer(String st1, int st2, int st3){
        if (!change_state(st1, st2, st3))return;
        agentState1 = st1;
        agentState2 = st2;
        agentState3 = st3;
        if ("SearchingTile".equals(st1) && st2==2){
            int[] searchField = getSearchField(otherASF[0], otherASF[1], otherASF[2], otherASF[3]);
            addTempMessage("MyLastSearchField "+searchField[0]+" "+searchField[1]+" "+searchField[2]+" "+searchField[3]);
            initalSearchTileChain(searchField[0], searchField[1], searchField[2], searchField[3], this.getX(), this.getY());
        }else
        if ("SearchingTile".equals(st1) && st2==1){
            if (search_tile_chain.size()==0) initalSearchTileChainb(this.fuelX, this.fuelY);
        }else
        if ("PickingTile".equals(st1) && st2==2){
            pick_tile_chain = getPickRoute(this.getX(), this.getY(), this.carriedTiles.size());
            agentState1 = "PickingTile";
            agentState2 = 2;
            agentState3 = 0;
        }
    }

    private double bPlanAgent1PickScore(){
        double preScore=0.0;
        if (bPlanPickArea.size()==0) return 0.0;
        int[] lastPoint=bPlanPickArea.get(bPlanPickArea.size()-1);
        for (int[] posA : bPlanPickArea){
            for (int i=-3; i <=3; i++){
                for (int j=-3; j<=3; j++){
                    TWAgentPercept currentMemory =  this.getMemory().getObjects()[posA[0]+i][posA[1]+j];
                    if (!(currentMemory == null)){
                        if (currentMemory.getO() instanceof TWTile || currentMemory.getO() instanceof TWHole ){
                            int curDis = (int)this.getDistanceTo(currentMemory.getO());
                            curDis += Math.abs(lastPoint[0]-currentMemory.getO().getX())+Math.abs(currentMemory.getO().getY()-lastPoint[1]);
                            curDis/=2;
                            if (curDis <= AgentParameter.agent1pickDis && getELT() > objectLifetimeEstimate(currentMemory)+this.getDistanceTo(currentMemory.getO())){
                                preScore++;
                            }
                        }
                    }
                }
            }
        }
        return preScore;
    }

    private Object getClosestObjectInMemory(Class<?> type){
        int indx=-9999;
        int indy=-9999;
        for (int x = 0; x < this.getMemory().getObjects().length; x++) {
            for (int y = 0; y < this.getMemory().getObjects()[x].length; y++) {
                TWAgentPercept currentMemory =  this.getMemory().getObjects()[x][y];
                if (!(currentMemory == null)  && currentMemory.getO().getClass().equals(type)){
                    if (Math.abs(x-this.getX()) + Math.abs(y-this.getY())  < Math.abs(indx-this.getX())+Math.abs(indy-this.getY())){
                        indx = x;
                        indy = y;
                    }
                }
            }
        }
        if (indx==-9999) return null;
        return this.getMemory().getObjects()[indx][indy].getO();
    }

    private int[] getPossiblePick(){
        int indx=-1;
        int indy=-1;
        int allowDis = AgentParameter.agentMaxDis;
        double curChoiceScore=99999.0;
        if (bPlanPickArea.size()==0) return new int[]{-1, -1};
        int[] lastPoint=bPlanPickArea.get(bPlanPickArea.size()-1);
        for (int[] posA : bPlanPickArea){
            for (int i=-3; i <=3; i++){
                for (int j=-3; j<=3; j++){
                    TWAgentPercept currentMemory =  this.getMemory().getObjects()[posA[0]+i][posA[1]+j];
                    if (!(currentMemory == null)){
                        if ((currentMemory.getO() instanceof TWTile && this.carriedTiles.size() < 3) ||
                                (currentMemory.getO() instanceof TWHole && this.carriedTiles.size() > 0)){
                            int curDis = (int)this.getDistanceTo(currentMemory.getO());
                            if ("agent2".equals(this.name)){
                                curDis += (int)currentMemory.getO().getDistanceTo(otherAgentPosition[0], otherAgentPosition[1]);
                            }
                            System.out.println("tile x,y:"+(posA[0]+i)+" "+(posA[1]+j)+"  distance:"+curDis+"  reached ELT"+(objectLifetimeEstimate(currentMemory)+this.getDistanceTo(currentMemory.getO())));
                            if (curDis <= allowDis && getELT() > objectLifetimeEstimate(currentMemory)+this.getDistanceTo(currentMemory.getO())){
                                if ( this.getDistanceTo(currentMemory.getO()) * 2.5 + objectLifeRemainEstimate(currentMemory) < curChoiceScore ){
                                    curChoiceScore = this.getDistanceTo(currentMemory.getO()) * 2 + objectLifeRemainEstimate(currentMemory);
                                    indx=posA[0]+i; indy=posA[1]+j;
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("possible pick x,y: "+indx+" "+indy);
        return new int[] {indx, indy};
    }

    public void printAgentState(){
        String reth = " ";
        if (rethinking==1) reth="rethinking";
        System.out.println("------------------curPathStep="+curPathStep+"----------------------"+reth);
        System.out.println("              "+this.name+"               curScore:"+this.score+"       EsLiTi"+getELT());
        System.out.println("Position x,y= "+ this.getX()+" "+this.getY()+"    fuellevel:"+this.getFuelLevel() + "   fuelpointxy:"+fuelX+" "+fuelY);
        System.out.println("State = "+ agentState1+" "+agentState2+" "+agentState3+"    carriedTiles:"+this.carriedTiles.size()+"  curPathStep:"+curPathStep);
        System.out.println("otherState = "+ otherAgentState1+" "+otherAgentState2+" "+otherAgentState3);
        if (curPath == null){
            System.out.println("currentPathLenth = 0  (null)");
        }else{
            System.out.println("PathLen = "+ curPath.getpath().size()+
                    "  next step--->("+curPath.getStep(Math.min(curPathStep,curPath.getpath().size()-1)).getX()+
                    ","+curPath.getStep(Math.min(curPathStep,curPath.getpath().size()-1)).getY()+")     " +
                    "target pos--->(" +curPath.getStep(curPath.getpath().size()-1).getX()+
                    ","+curPath.getStep(curPath.getpath().size()-1).getY()+")");
        }
        String bPPA = "bPlanPickArea: ";
        for (int[] p : bPlanPickArea){
            bPPA += "("+p[0]+","+p[1]+") ";
        }
        System.out.println(bPPA);
        System.out.println("------------------------------------");
    }

    protected TWThought think() {
        // System.out.println(this.getName() + " think");
        if (this.carriedTiles.size() < 3 && this.getMemory().getMemoryGrid().get(this.getX(), this.getY()) instanceof TWTile){
            return new TWThought(TWAction.PICKUP);
        }

        if (this.carriedTiles.size() > 0 && this.getMemory().getMemoryGrid().get(this.getX(), this.getY()) instanceof TWHole){
            return new TWThought(TWAction.PUTDOWN);
        }

        if (this.getX()==fuelX && this.getY()==fuelY && this.getFuelLevel() <= 490){
            return new TWThought(TWAction.REFUEL);
        }

        // if (this.rethinking==0) this.addTempMessage("MyPosition " + this.getX()+" "+ this.getY());
        if (this.rethinking==0 && fuelX != -1) this.unseenMapOneStep();
        this.addTempMessage("MyState " + this.agentState1 + " " + this.agentState2 + " " + this.agentState3);

        //receive message, and update memory
        if (rethinking == 0){
            for (Message message : this.getEnvironment().getMessages()){
                System.out.println(this.getName() + " receive message from " + message.getFrom() + " to " + message.getTo() + " :" + message.getMessage());
                if (!this.name.equals(message.getFrom())){
                    String[] meSplit = message.getMessage().split(";");
                    String[] meS;
                    String messageType;
                    for (String mes : meSplit){
                        meS = mes.split(" ");
                        messageType = meS[0];
                        switch(messageType){
                            case "Require":
                                if ("AddingFuel".equals(agentState1)) break;
                                String subrequire = meS[1];
                                if ("SearchingTile".equals(subrequire)){
                                    switch(meS[2]){
                                        case "AplanSearch":
                                            if(change_state(meS[1], 2, 0)){
                                                state_changer("SearchingTile", 2, 0);
                                            }
                                            break;
                                        case "BplanSearch":

                                            break;
                                    }
                                } else if ("PickingTile".equals(subrequire)){
                                    switch(meS[2]){
                                        case "AplanPick":
                                            if(change_state("PickingTile", 2, 0)){
                                                state_changer("PickingTile", 2, 0);
                                            }
                                            break;
                                        case "BplanPick":
                                            if(change_state("PickingTile", 1, 0)){
                                                state_changer("PickingTile", 1, 0);
                                            }
                                            break;
                                    }
                                } else if ("planC".equals(subrequire)){
                                    agentState1="planC";
                                    agentState2=3;
                                    agentState3=Math.max(0, agentState3);
                                }
                                break;

                            case "MyLastSearchField":
                                otherASF = new int[] {(int) Float.parseFloat(meS[1]), (int) Float.parseFloat(meS[2]), (int) Float.parseFloat(meS[3]), (int) Float.parseFloat(meS[4])};
                                break;

                            case "UpdateMemoryMap":
                                if (this.rethinking==0) this.getMemory().updateMemory(mes, otherAgentPosition[0], otherAgentPosition[1]);
                                break;

                            case "MyEstimateLifeTime":
                                this.getMemory().estimateLifeTime = Double.parseDouble(meS[1]);
                                break;

                            case "cPlanSearchPointInitial":
                                if (cPlanSearchPoint.size()==0){
                                    if ("byX".equals(meS[1])){
                                        cPlanSearchPoint.clear();
                                        cPlanSearchPoint.add(new int[] {mapsizeX/2+4, 3});
                                        cPlanSearchPoint.add(new int[] {mapsizeX-4, 3});
                                        cPlanSearchPoint.add(new int[] {mapsizeX-4, mapsizeY-4});
                                        cPlanSearchPoint.add(new int[] {mapsizeX/2+4, mapsizeY-4});
                                    } else if ("byY".equals(meS[1])){
                                        cPlanSearchPoint.clear();
                                        cPlanSearchPoint.add(new int[] {3,mapsizeY/2+4});
                                        cPlanSearchPoint.add(new int[] {mapsizeX-4, mapsizeY/2+4});
                                        cPlanSearchPoint.add(new int[] {mapsizeX-4, mapsizeY-4});
                                        cPlanSearchPoint.add(new int[] {3, mapsizeY-4});
                                    }
                                    cPlanInitalFuelCheckPoint();
                                }
                                break;

                            case "MyPosition":
                                otherAgentPosition[0] = Integer.parseInt(meS[1]);
                                otherAgentPosition[1] = Integer.parseInt(meS[2]);
                                break;

                            case "MyCarriedTiles":
                                otherCarriedTiles = Integer.parseInt(meS[1]);
                                break;

                            case "MyState":
                                otherAgentState1 = meS[1];
                                otherAgentState2 = Integer.parseInt(meS[2]);
                                otherAgentState3 = Integer.parseInt(meS[3]);
                                break;

                            case "GoToFindFuelStation":
                                this.agentState1 = "FuelStationFinding";
                                this.agentState2 = Integer.parseInt(meS[1]);
                                if (this.agentState2 == 9){
                                    this.agentState3 = Integer.parseInt(meS[2]);
                                } else {
                                    this.agentState3 = 1;
                                }
                                break;

                        }
                    }
                }

                if ("all".equals(message.getTo())){
                    String[] meSplit = message.getMessage().split(";");
                    String[] meS;
                    String messageType;
                    for (String mes : meSplit){
                        meS = mes.split(" ");
                        messageType = meS[0];
                        switch(messageType){

                            case "FindFuelStation":
                                if (fuelX == -1){
                                    this.fuelX = Integer.parseInt(meS[1]);
                                    this.fuelY = Integer.parseInt(meS[2]);
                                    System.out.println(this.name + " received FindFuelStation!!");
                                    this.agentState1="idle";
                                    this.agentState3=0;
                                    curPath = null;
                                    curPathStep=0;
                                }
                                break;
                            case "bPlanPickAreaUpdate":
                                if (bPlanPickArea.size()>0 && (bPlanPickArea.get(bPlanPickArea.size()-1)[0]==Integer.parseInt(meS[1])&&
                                        bPlanPickArea.get(bPlanPickArea.size()-1)[1]==Integer.parseInt(meS[2]))) {

                                }
                                else if (getELT() / 7 + 1> bPlanPickArea.size()){
                                    bPlanPickArea.add(new int[] {Integer.parseInt(meS[1]), Integer.parseInt(meS[2])});
                                } else {
                                    bPlanPickArea.remove(bPlanPickArea.get(0));
                                    bPlanPickArea.add(new int[] {Integer.parseInt(meS[1]), Integer.parseInt(meS[2])});
                                }
                                break;
                        }
                    }
                }
            }
        }

        if (otherAgentPosition[0] == -1) return new TWThought(TWAction.MOVE,TWDirection.Z);

        if (fuelX != -1 && Math.abs(this.getX()-fuelX)+Math.abs(this.getY()-fuelY) != 0 && this.agentState1!="AddingFuel"){
            int toFuelStepEst = Math.abs(this.getX()-fuelX)+Math.abs(this.getY() - fuelY);
            boolean addFuel=false;
            if (this.getFuelLevel() < toFuelStepEst * AgentParameter.leastFuelAddParam) {
                addFuel=true;
            }
            if ("planC".equals(this.agentState1)){
                if (this.getDistanceTo(cPlanFuelPoint[0], cPlanFuelPoint[1]) <= 10){
                    if (this.fuelLevel <= (mapsizeX+mapsizeY)/2*3-28){
                        addFuel=true;
                    }
                }
            } else
            if (this.getFuelLevel() < 250 && toFuelStepEst <= 16){
                addFuel=true;
            }
            if (addFuel){
                TWPath toFuelPath=pathGenerator.findPath(this.getX(), this.getY(), fuelX, fuelY);
                if (toFuelPath != null){
                    this.agentState1="AddingFuel";
                    this.curPath = toFuelPath;
                    this.curPathStep=0;
                    rethinking = 0;
                }
            }
        }

        if (this.rethinking==0){
            updateSeenMap(this.getX(), this.getY());
            updateSeenMap(otherAgentPosition[0], otherAgentPosition[1]);
        }

        printAgentState();
        if (!(curPath == null)  &&  this.rethinking==0 && ! "FuelStationFinding".equals(agentState1)){
            if ("PickingTile".equals(agentState1)){
                if (agentState2==1){
                    if ((this.getX()!=bPlanPickTarget[0] || this.getY()!=bPlanPickTarget[1]) && bPlanPickTarget[0] != -1){
                        Object curObj = this.getMemory().getMemoryGrid().get(bPlanPickTarget[0], bPlanPickTarget[1]);
                        if (curObj instanceof TWTile ||curObj instanceof TWHole){
                            System.out.println(this.name+" pos x,y"+this.getX() +" "+this.getY()+" targetx,y: "+bPlanPickTarget[0] +" "+bPlanPickTarget[1]);
                            return new TWThought(TWAction.MOVE, curPath.getStep(curPathStep).getDirection());
                        }
                    }
                    curPath=null;
                    curPathStep=0;
                    rethinking=1;
                    return think();
                } else if (agentState2==2){
                    if (pick_tile_chain.size()==0 && "agent2".equals(this.name)){
                        addTempMessage("Request");
                        agentState3 = 0;
                        return new TWThought(TWAction.MOVE,getRandomDirection());
                    }
                    Object curObj = this.getMemory().getMemoryGrid().get(pick_tile_chain.get(agentState3)[0], pick_tile_chain.get(agentState3)[1]);
                    if (!(curObj instanceof TWTile) && !(curObj instanceof TWHole)){
                        rethinking = 1;
                        pickVanished = 1;
                        System.out.println("VANISHED!!! x,y="+pick_tile_chain.get(agentState3)[0]+" "+pick_tile_chain.get(agentState3)[1]);
                        return think();
                    }
                }
            } else
            if ("SearchingTile".equals(agentState1) || "planC".equals(agentState1)){
                int greedy=0;
                if (agentState2==1) greedy=AgentParameter.pickGreedyb;
                if (agentState2==3) greedy=AgentParameter.pickGreedyc;
                if (agentState2==2) greedy=AgentParameter.pickGreedya;
                double tempDis = 999.0;
                TWTile closeTile = (TWTile) this.getMemory().getClosestObjectInSensorRange(TWTile.class);
                TWHole closeHole = (TWHole) this.getMemory().getClosestObjectInSensorRange(TWHole.class);
                if (closeTile != null && this.carriedTiles.size() < 3 &&
                        this.getDistanceTo(closeTile) <= greedy){
                    tempDis = this.getDistanceTo(closeTile);
                    curPath = pathGenerator.findPath(this.getX(), this.getY(), closeTile.getX(), closeTile.getY());
                    curPathStep=0;
                }
                if (closeHole != null && this.carriedTiles.size() > 0 &&
                        this.getDistanceTo(closeHole) <= Math.min(greedy, tempDis)){
                    curPath = pathGenerator.findPath(this.getX(), this.getY(), closeHole.getX(), closeHole.getY());
                    curPathStep=0;
                }
            }

            if (curPathStep >= curPath.getpath().size()){
                this.rethinking = 1;
                return think();
            }
            return new TWThought(TWAction.MOVE, curPath.getStep(curPathStep).getDirection());
        } else
        if ("AddingFuel".equals(this.agentState1)){
            if (rethinking==1){
                curPath = pathGenerator.findPath(this.getX(), this.getY(), fuelX, fuelY);
                curPathStep = 0;
            }
            if (curPath == null){
                System.out.println("WWWWWWWWWWWWWWWHHHHHHHHHHHHHHHHYYYYYYYYYYYYYYYYYYYYY1");
            } else {
                return new TWThought(TWAction.MOVE, curPath.getStep(curPathStep).getDirection());
            }
        } else
        if ("idle".equals(this.agentState1)){
            if ("agent2".equals(this.name)){
                addTempMessage("Request");
                return new TWThought(TWAction.MOVE,getRandomDirection());
            }
        } else
        if ("PickingTile".equals(this.agentState1)){
            if (this.agentState2 == 2){
                do{
                    // System.out.println("DODODODODDODODO");
                    curPath = null;
                    curPathStep = 0;
                    if (pick_tile_chain.size()==0 && "agent2".equals(this.name)){
                        addTempMessage("Request");
                        agentState3 = 0;
                        return new TWThought(TWAction.MOVE,getRandomDirection());
                    }
                    if (pickVanished == 1 || (this.getX()==pick_tile_chain.get(agentState3)[0] && this.getY()==pick_tile_chain.get(agentState3)[1])){
                        // System.out.println("VAlished cat");
                        agentState3 += 1;
                        pickVanished = 0;
                        if ( getPickRoute("score", this.getX(), this.getY(), this.carriedTiles.size())<AgentParameter.aPlanPickStop ||
                                agentState3 >= pick_tile_chain.size()){
                            // pick_tile_chain = getPickRoute(this.getX(), this.getY());
                            if ("agent2".equals(this.name)){
                                addTempMessage("Request");
                                if (agentState3 == pick_tile_chain.size()) agentState3-=1;
                                return new TWThought(TWAction.MOVE,getRandomDirection());
                            }
                        }
                    }
                    curPath = pathGenerator.findPath(this.getX(), this.getY(), pick_tile_chain.get(agentState3)[0], pick_tile_chain.get(agentState3)[1]);
                } while (curPath == null);
                return new TWThought(TWAction.MOVE, curPath.getStep(curPathStep).getDirection());
            } else

            if (this.agentState2 == 1){
                int[] bPlanPickTarget = getPossiblePick();
                if (bPlanPickTarget[0] != -1){
                    curPath = pathGenerator.findPath(this.getX(), this.getY(), bPlanPickTarget[0], bPlanPickTarget[1]);
                    curPathStep=0;
                    return new TWThought(TWAction.MOVE, curPath.getStep(curPathStep).getDirection());
                } else if (this.getDistanceTo(otherAgentPosition[0], otherAgentPosition[1])<=2){
                    curPath=null;
                    curPathStep=0;
                    return new TWThought(TWAction.MOVE,getRandomDirection());
                } else {
                    curPath = null;
                    curPathStep = 0;
                    TWAgentPercept currentObj = this.getMemory().getObjects()[otherAgentPosition[0]][otherAgentPosition[1]];
                    if (currentObj != null && currentObj.getO() instanceof TWObstacle){
                        return new TWThought(TWAction.MOVE,getRandomDirection());
                    }
                    return new TWThought(TWAction.MOVE, pathGenerator.findPath(this.getX(), this.getY(),
                            otherAgentPosition[0], otherAgentPosition[1]).getStep(0).getDirection());
                }
            }
        } else
        if ("SearchingTile".equals(this.agentState1)){
            if (this.agentState2 == 2){ // Plan a
                curPathStep = 0;
                curPath = pathGenerator.findPath(this.getX(), this.getY(),
                        search_tile_chain.get(agentState3)[0], search_tile_chain.get(agentState3)[1]);
                if (curPath == null){
                    this.agentState3 += 1;
                    // System.out.println("SearchingTile--------------------------------curPATH=NONE");
                    // printAgentState();
                    addTempMessage("Request");
                    if (agentState3 == search_tile_chain.size()) {
                        agentState1="idle";
                        return new TWThought(TWAction.MOVE,getRandomDirection());
                    }
                    this.rethinking = 1;
                    return think();
                } else {
                    return new TWThought(TWAction.MOVE, curPath.getStep(curPathStep).getDirection());
                }
            } else

            if (this.agentState2 == 1){ // plan b
                curPathStep=0;
                if (agentState3 >= search_tile_chain.size()) agentState3 = 0;
                addTempAllMessage("bPlanPickAreaUpdate "+search_tile_chain.get(agentState3)[0]+" "+search_tile_chain.get(agentState3)[1]);
                curPath = pathGenerator.findPath(this.getX(), this.getY(),
                        search_tile_chain.get(agentState3)[0], search_tile_chain.get(agentState3)[1]);
                if (curPath==null){
                    this.agentState3 += 1;
                    this.rethinking=1;
                    return think();
                } else{
                    return new TWThought(TWAction.MOVE, curPath.getStep(curPathStep).getDirection());
                }
            }
        } else
        if ("planC".equals(this.agentState1)){
            if ( (mapsizeY+mapsizeX)/2*3-28 < getELT() ){
                System.out.println((mapsizeY+mapsizeX)/2*3-28 +"  ?????");
            }
            int[] targetXY = cPlanSearchPoint.get(this.agentState3);
            curPath=pathGenerator.findPath(this.getX(), this.getY(),
                    cPlanSearchPoint.get(agentState3)[0], cPlanSearchPoint.get(agentState3)[1]);
            curPathStep=0;
            if (curPath==null || this.getDistanceTo(cPlanSearchPoint.get(agentState3)[0], cPlanSearchPoint.get(agentState3)[1]) <= 4){
                curPath=null;
                this.agentState3+=1;
                this.agentState3 = this.agentState3 % 4;
                this.rethinking=1;
                return think();
            } else {
                return new TWThought(TWAction.MOVE, curPath.getStep(curPathStep).getDirection());
            }

        } else if ("FuelStationFinding".equals(this.agentState1)){
            int lenX = (int) Math.ceil((double)mapsizeX/7);
            int lenY = (int) Math.ceil((double)mapsizeY/7);
            double tempX = Math.min(lenX*7 + 0.5 - 7*Math.abs((this.agentState3-1) % (2*lenX) + 0.5 - lenX)-1, mapsizeX-3);
            double tempY = Math.min(Math.ceil((double)this.agentState3/lenX)*7 - 3-1, mapsizeX-3);
            int targetX=0;
            int targetY=0;
            switch(this.agentState2){

                case 0:
                case 9:
                    targetX = mapChain.get(agentState3)[0];
                    targetY = mapChain.get(agentState3)[1];
                    break;

                case 1: // UR down
                    targetX = (int) (mapsizeX-1 - tempY);
                    targetY = (int) tempX;
                    break;
                case 2: // UR left
                    targetX = (int) (mapsizeX-1 - tempX);
                    targetY = (int) tempY;
                    break;
                case 3: // UL down
                    targetX = (int) tempY;
                    targetY = (int) tempX;
                    break;
                case 4: // UL right
                    targetX = (int) tempX;
                    targetY = (int) tempY;
                    break;
                case 5: // DR up
                    targetX = (int) (mapsizeX-1 - tempY);
                    targetY = (int) (mapsizeX-1 - tempX);
                    break;
                case 6: // DR left
                    targetX = (int) (mapsizeX-1 - tempX);
                    targetY = (int) (mapsizeX-1 - tempY);
                    break;
                case 7: // DL up
                    targetX = (int) tempY;
                    targetY = (int) (mapsizeX-1 - tempX);
                    break;
                case 8: // DL right
                    targetX = (int) tempX;
                    targetY = (int) (mapsizeX-1 - tempY);
                    break;
            }

            if ((this.getX() == targetX && this.getY() == targetY) || this.getMemory().isCellBlocked(targetX, targetY)){
                if (agentState2==0){
                    this.agentState3--;
                } else{
                    this.agentState3++;
                }

                if (this.agentState3 == mapChainLength){
                    this.agentState3 = 0;
                } else if (this.agentState3 == -1){
                    this.agentState3 = mapChainLength-1;
                }
                this.rethinking=1;
                return think();
            }

            if (this.getFuelLevel() > 250 && this.mapsizeX*this.mapsizeY <= 70*70){
                TWTile closeTile = (TWTile) this.getMemory().getClosestObjectInSensorRange(TWTile.class);
                TWHole closeHole = (TWHole) this.getMemory().getClosestObjectInSensorRange(TWHole.class);
                if (closeTile != null && this.carriedTiles.size() < 3 &&
                        this.getDistanceTo(closeTile) <= 3 && closeTile.getDistanceTo(targetX, targetY) <= 6){
                    curPath = pathGenerator.findPath(this.getX(), this.getY(), closeTile.getX(), closeTile.getY());
                    curPathStep=0;
                } else if (closeHole != null && this.carriedTiles.size() > 0 &&
                        this.getDistanceTo(closeHole) <= 3 && closeHole.getDistanceTo(targetX, targetY) <= 6){
                    curPath = pathGenerator.findPath(this.getX(), this.getY(), closeHole.getX(), closeHole.getY());
                    curPathStep=0;
                }
            }


            if (curPath == null || this.rethinking==1 || curPath.getpath().size() <= curPathStep){
                TWPath fuelPath = pathGenerator.findPath(this.getX(), this.getY(), targetX, targetY);

                if (fuelPath == null){
                    System.out.println("test1");
                } else {
                    curPath = fuelPath;
                    curPathStep = 0;
                }
            }
            this.rethinking = 0;
            return new TWThought(TWAction.MOVE, curPath.getStep(curPathStep).getDirection());

        }

        return new TWThought(TWAction.MOVE,getRandomDirection());
    }

    @Override
    protected void act(TWThought thought) {
        // System.out.println(this.getName() + " act");
        Object tempObject = this.getMemory().getMemoryGrid().get(this.getX(), this.getY());
        switch(thought.getAction()){

            case PICKUP:
                pickUpTile((TWTile) tempObject);
                this.getMemory().removeObject(this.getX(), this.getY()); // remove memory
                this.addTempMessage("UpdateMemoryMap " + this.getX()+" "+ this.getY()+" " + "null");

                // this.rethinking=1;
                if (!AgentParameter.isPickNeedOneStep){
                    act(this.think());
                }
                return;

            case PUTDOWN:
                putTileInHole((TWHole) tempObject);
                this.getMemory().removeObject(this.getX(), this.getY());
                this.addTempMessage("UpdateMemoryMap " + this.getX()+" "+ this.getY()+" " + "null");

                // this.rethinking=1;
                if (!AgentParameter.isPickNeedOneStep){
                    act(this.think());
                }
                return;

            case REFUEL:
                this.refuel();
                this.agentState1 = "idle";
                this.curPath = null;
                this.curPathStep = 0;
                if ("agent2".equals(this.name)){
                    addTempMessage("Request");
                    return;
                }

                // this.rethinking=1;
                if (!AgentParameter.isPickNeedOneStep){
                    act(this.think());
                }
                return;

        }

        try {
            this.move(thought.getDirection());
            rethinking=0;
            curPathStep++;
            this.getMemory().estimateLifeTime += AgentParameter.lifetimeLearningRate * AgentParameter.lifetimeIncreaseLearningRatio;
            if(AgentParameter.lifetimeLearningRate > AgentParameter.lifetimeFinalLearningRate)AgentParameter.lifetimeLearningRate *=0.999;
            this.addTempMessage("MyPosition " + this.getX()+" "+ this.getY());
            this.addTempMessage("MyCarriedTiles " + this.carriedTiles.size());
        } catch (CellBlockedException ex) {
            this.rethinking = 1;
            act(this.think());
        }
    }

    private TWDirection getRandomDirection(){

        TWDirection randomDir = TWDirection.values()[this.getEnvironment().random.nextInt(4)];

        if(this.getX()>=this.getEnvironment().getxDimension() ){
            randomDir = TWDirection.W;
        }else if(this.getX()<=1 ){
            randomDir = TWDirection.E;
        }else if(this.getY()<=1 ){
            randomDir = TWDirection.S;
        }else if(this.getY()>=this.getEnvironment().getxDimension() ){
            randomDir = TWDirection.N;
        }

        return randomDir;

    }

    @Override
    public String getName() {
        return name;
    }
}
