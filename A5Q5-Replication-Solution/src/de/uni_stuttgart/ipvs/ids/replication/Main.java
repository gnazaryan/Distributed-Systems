package de.uni_stuttgart.ipvs.ids.replication;

import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

// !!! DO NOT EDIT !!!

public class Main {

	/**
	 * @param args
	 * @throws SocketException 
	 * @throws QuorumNotReachedException 
	 */
	public static void main(String[] args) throws SocketException, QuorumNotReachedException {

		int replicaPort = 4000;
		int noReplicas = 10;
		double prob = 0.9; // Funktionier-wahrscheinlichkeit
		double value = 2.0;

		List<Replica<Double>> replicas = new ArrayList<Replica<Double>>(noReplicas);
		List<SocketAddress> replicaAddrs = new ArrayList<SocketAddress>(replicas.size());
		
		for (int i = 0; i < noReplicas; i++) {
			Replica<Double> r = new Replica<Double>(i, replicaPort + i, prob, value, 1);
			r.start();
			replicas.add(r);
			replicaAddrs.add(r.getSocketAddress());
		}

		WeightedVoting<Double> wv = new WeightedVoting<Double>(replicaAddrs, noReplicas);
		
		try {
		double y = wv.get().getValue();
		System.out.println("Weighted Voting: y is " + y);

		wv.set(3.0);
		y = wv.get().getValue();
		
		System.out.println("Weighted Voting: y is " + y);
		} catch (Exception e) {
			System.err.println("Exception occured: " + e.getMessage());
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}
}
