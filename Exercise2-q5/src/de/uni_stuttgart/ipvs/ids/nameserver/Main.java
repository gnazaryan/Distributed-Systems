/* 
 * DO NOT MODIFY THIS FILE!
 * 
 * Main.java
 * - set up name server hierarchy
 * - run lookup scenario
 *
 * Distributed Systems Exercise
 * Assignment 2 Part II
 */

package de.uni_stuttgart.ipvs.ids.nameserver;

public class Main {

	public static void main(String[] args) {
		// instantiate name servers
		Nameserver root = new Nameserver("root", null, 4000, 4001);
		Nameserver de = new Nameserver("de", root, 4002, 4003);
		Nameserver com = new Nameserver("com", root, 4004, 4005);
		Nameserver stuttgart = new Nameserver("uni-stuttgart", de, 4006, 4007);
		Nameserver mannheim = new Nameserver("uni-mannheim", de, 4008, 4009);
		Nameserver informatik = new Nameserver("informatik", stuttgart, 4010, 4011);
		Nameserver mathematik = new Nameserver("mathematik", stuttgart, 4012, 4013);

		// set up hierarchy
		root.addChild("de", de);
		root.addChild("com", com);
		de.addChild("uni-stuttgart", stuttgart);
		de.addChild("uni-mannheim", mannheim);
		stuttgart.addChild("informatik", informatik);
		stuttgart.addChild("mathematik", mathematik);
		mannheim.addNameAddressPair("www", "192.169.10.11");
		mathematik.addNameAddressPair("foo", "192.168.1.1");
		informatik.addNameAddressPair("nayaknh", "192.168.0.1");
		informatik.addNameAddressPair("bhowmisa", "192.168.0.3");
		informatik.addNameAddressPair("bachts", "192.168.0.4");

		// create client and request
		Client client = new Client();
		String[] names = {"nayaknh.informatik.uni-stuttgart.de", 
							"foo.mathematik.uni-stuttgart.de", 
							"www.uni-mannheim.de",
							"bachts.informatik.uni-stuttgart.de",
							"unknown.informatik.uni-stuttgart.de"
							};		

		// address resolution for part a)
		System.out.println("========== a) Recursive Lookup ==========");
		for (String name : names)
		{
			String Address = client.recursiveNameResolution(name, "localhost", 4000);
			System.out.println("Address for \"" + name + "\" is: " + Address);
		}

		// address resolution for part b)
		System.out.println("\n========== b) Iterative Lookup ==========");
		for (String name : names)
		{
			String Address = client.iterativeNameResolution(name, "localhost", 4001);
			System.out.println("Address for \"" + name + "\" is: " + Address);
		}

		// terminate all name server threads
		System.out.println("\nBye-bye!");
		System.exit(0);
	}
}