package main;

import java.util.ArrayList;
import java.util.List;

public class Sequence {
	private List<Observation> observations;
	
	public Sequence() {
		this.observations = new ArrayList<>();
	}
	
	public Sequence(List<Observation> observations) {
		this.observations = observations;
	}
	
	public Observation get(int i) {
		return this.observations.get(i);
	}
	
	public void add(Observation observation) {
		this.observations.add(observation);
	}
	
	public void add(List<Observation> observations) {
		this.observations.addAll(observations);
	}
	
	public int size() {
		return this.observations.size();
	}
}
