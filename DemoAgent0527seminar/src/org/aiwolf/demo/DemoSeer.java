package org.aiwolf.demo;
 
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
 
import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.sample.lib.AbstractSeer;
 
public class DemoSeer extends AbstractSeer {
    //defining field
    /** me */
    Agent me;
    /** newest GameInfo */
    GameInfo currentGameInfo;
    /** list of divining results which have never reported */
    Deque<Judge> myDivinationQueue = new LinkedList<>();
    /** list of human */
    List<Agent> whiteList = new ArrayList<>();
    /** list of werewolf */
    List<Agent> blackList = new ArrayList<>();
    /** list of gray */
    List<Agent> grayList;
    /** whether you have done coming-out or not */
    boolean isCO = false;
    /** GameInfo.talkList */
    int talkListHead; // initialize at dayStart()
    /** Coming-out information */
    Map<Agent, Role> comingoutMap = new HashMap<>(); // initialize at initialize
 
 
    //defining utility method
    /** whether agent is alive or not */
    boolean isAlive(Agent agent) {
        return currentGameInfo.getAliveAgentList().contains(agent);
    }
 
    /** return form list at random */
    <T> T randomSelect(List<T> list) {
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get((int) (Math.random() * list.size()));
        }
    }
 
 
 
    @Override
    public void dayStart() {
        // TODO Auto-generated method stub
        // getting result of divining 
        Judge divination = currentGameInfo.getDivineResult();
        if (divination != null) {
            myDivinationQueue.offer(divination);
            Agent target = divination.getTarget();
            Species result = divination.getResult();
            // updating gray・white・black list
            grayList.remove(target);
            if (result == Species.HUMAN) {
                whiteList.add(target);
            } else {
                blackList.add(target);
            }
        }
        //initializing
        talkListHead=0;
    }
 
    @Override
    public Agent divine() {
        // TODO Auto-generated method stub
        // list of candidates
        List<Agent> candidates = new ArrayList<>();
 
        // adding alive gray player to candidates
        for (Agent agent : grayList) {
            if (isAlive(agent)) {
                candidates.add(agent);
            }
        }
        // don't divine if there is no agent in candidates
        if (candidates.isEmpty()) {
            return null;
        }
        // divining at random from candidates
        return randomSelect(candidates);
    }
 
    @Override
    public void finish() {
        // TODO Auto-generated method stub
 
    }
 
    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "DemoSeer";
    }
 
    @Override
    public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
        // TODO Auto-generated method stub
        // initializing field
        me = gameInfo.getAgent();
        grayList = new ArrayList<>(gameInfo.getAgentList());
        grayList.remove(me);
        whiteList.clear();
        blackList.clear();
        myDivinationQueue.clear();
        comingoutMap = new HashMap<>();
    }
 
    @Override
    public String talk() {
        // coming out if you find werewolf
        if (!isCO) {
            if (!myDivinationQueue.isEmpty() && 
            		myDivinationQueue.peekLast().getResult() == Species.WEREWOLF) {
                isCO = true;
                ContentBuilder builder = new ComingoutContentBuilder(me, Role.SEER);
                return new Content(builder).getText();
            }
        }
        // After coming out, reporting divine result
        else {
            if (!myDivinationQueue.isEmpty()) {
                Judge divination = myDivinationQueue.poll();
              //making content
                ContentBuilder builder = new DivinedResultContentBuilder(divination.getTarget(),
                                                                         divination.getResult());
                return new Content(builder).getText();
            }
        }
 
        return Content.OVER.getText();
    }
 
    @Override
    public void update(GameInfo gameInfo) {
        // updating currentGameInfo
        currentGameInfo = gameInfo;
        // From GameInfo.talkList, getting CO・divine report・identifying report
        for (int i = talkListHead; i < currentGameInfo.getTalkList().size(); i++) {
            Talk talk = currentGameInfo.getTalkList().get(i);
            Agent talker = talk.getAgent();
            if (talker == me) {
                continue;
            }
            Content content = new Content(talk.getText()); // parse content
            switch (content.getTopic()) {
            case COMINGOUT: // processing Coming-out
                // reading Coming-out
                comingoutMap.put(talker, content.getRole());
                break;
            case DIVINED: // processing divine result
               break;
            case IDENTIFIED: // processing identifying result
                break;
            default:
                break;
            }
        }
        talkListHead = currentGameInfo.getTalkList().size();
    }
 
    @Override
    public Agent vote() {
        // TODO Auto-generated method stub
         // list of candidates
        List<Agent> candidates = new ArrayList<>();
 
        // adding alive werewolf to candidates
        for (Agent agent : blackList) {
            if (isAlive(agent)) {
                candidates.add(agent);
            }
        }
        // If there is no agent in candidates, adding alive gray player to candidates
        if (candidates.isEmpty()) {
            for (Agent agent : grayList) {
                if (isAlive(agent)) {
                    candidates.add(agent);
                }
            }
        }
        // If there is no agent in candidates, return null
        //（voting at random from alive agent except for me）
        if (candidates.isEmpty()) {
            return null;
        }
        // voting at random from candidates
        return randomSelect(candidates);
    }
 
}