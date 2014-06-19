package plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import enums.TurnType;
import agents.NomicAgent;
import actions.ProposeRuleChange;

/**
 * Defines a internal data structure to store simulation data about an Agent.
 * @author Hanguang Sun
 *
 */
public class AgentStatistics{
	String name;
	float finalpoints;
	float cumulativerep;
	Map<Integer,Float> pointslog;
	Map<Integer,Float> moralitylog;
	Map<Integer,Float> authoritylog;
	ArrayList<ProposeRuleChange> proposals;
	int numofexclusions;
	int numofsucproposals;
	int numofyesvotes;
	int numofnovotes;
	
	
	public AgentStatistics(String name) {
		super();
		this.name = name;
		finalpoints = 0;
		cumulativerep = 0;
		numofexclusions = 0;
		numofsucproposals = 0;
		numofyesvotes = 0;
		numofnovotes = 0;
		pointslog = new HashMap<Integer,Float>();
		moralitylog = new HashMap<Integer,Float>();
		authoritylog = new HashMap<Integer,Float>();
		proposals = new ArrayList<ProposeRuleChange>();
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public float getFinalpoints() {
		return finalpoints;
	}
	public void setFinalpoints(float finalpoints) {
		this.finalpoints = finalpoints;
	}
	public float getCumulativerep() {
		return cumulativerep;
	}
	public void addCumulativerep(float rep) {
		this.cumulativerep = this.cumulativerep + rep;
	}
	public Map<Integer, Float> getPointslog() {
		return pointslog;
	}
	public void putpoints(int turn, float pts) {
		this.pointslog.put(turn, pts);
	}
	public Map<Integer, Float> getMoralitylog() {
		return moralitylog;
	}
	public void putmorality(int turn, float pts) {
		this.moralitylog.put(turn, pts);
	}
	public Map<Integer, Float> getAuthoritylog() {
		return authoritylog;
	}
	public void putauthority(int turn, float pts) {
		this.authoritylog.put(turn, pts);
	}
	public ArrayList<ProposeRuleChange> getProposals(){
		return proposals;
	}
	public void putproposals(ProposeRuleChange p){
		this.proposals.add(p);
	}
	
	public ArrayList<Float> pointcurve(){
		ArrayList<Float> r = new ArrayList<Float>();
		for (int i=0; i<pointslog.size(); ++i){
			r.add(pointslog.get(i));
		}
		return r;
	}
	
	public float getaverageMorality(){
		float r = 0;
		for (int i=0; i<moralitylog.size(); ++i){
			r = r + moralitylog.get(i);
		}
		r = r / moralitylog.size();
		return r;
	}
	
	public float getaverageAuthority(){
		float r = 0;
		for (int i=0; i<authoritylog.size(); ++i){
			r = r + authoritylog.get(i);
		}
		r = r / authoritylog.size();
		return r;
	}
	
	public int getNumofexclusions() {
		return numofexclusions;
	}
	public void addexclusions() {
		this.numofexclusions ++ ;
	}
	public int getNumofsucproposals() {
		return numofsucproposals;
	}
	public void addsucproposals() {
		this.numofsucproposals ++;
	}
	public int getNumofyesvotes() {
		return numofyesvotes;
	}
	public void addNumofyesvotes() {
		this.numofyesvotes++;
	}
	public int getNumofnovotes() {
		return numofnovotes;
	}
	public void addNumofnovotes() {
		this.numofnovotes++;
	}

	
}