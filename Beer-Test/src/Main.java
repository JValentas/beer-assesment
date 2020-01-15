/**
 * 
 * @author Valentas Jonauskas
 *
 */

public class Main {
	
	// TEST CASE 1: 51.742503 19.432956
	// TEST CASE 2: 51.355468 11.100790cls
	
	// TEST CASE 3: 34.052235 -118.243683
	
	public static void main(String[] args) {
		
		BeerTest bt = new BeerTest();
		//bt.run("51.742503", "19.432956");
		
		try {
			long start = System.nanoTime();
			
			bt.run(args[0], args[1]);
			
			long timeSpent = System.nanoTime() - start;
			System.out.format("\nProgram took %s ms\n", timeSpent / 1e+6);
			
		} catch (Exception e) {
			System.out.println("Invalid LAT and/or LONG parameter(-s)."); 
		}
		 
	}
}
