import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BeerTest {
	final String DRIVER = "com.mysql.cj.jdbc.Driver";
	final String URL = "jdbc:mysql://remotemysql.com:3306/jl07j91KKz";
	final String USER = "jl07j91KKz";
	final String PASS = "uXZpEwWWma";
	
	final int R = 6371; // Radius of the earth
	Double LAT, LONG, REMAINING_KM = 2000.00;
	
	List<Beer> beers;
	List<Geocode> geocodes;
	List<Brewery> breweries;
	
	String query;
	
	/**
	 * Main route calculation method
	 */
	void travel() {
		Double currentLAT = LAT;
		Double currentLONG = LONG;
		
		List<Brewery> visitedBreweries = new ArrayList<Brewery>();
		List<Beer> beersCollected = new ArrayList<Beer>();
		
		Double totalDistanceTraveled = 0.00;
		
		System.out.println("Starting to travel...");
		
		Double minDistance = Double.MAX_VALUE;
		Brewery nextBrewery = null;
		// Travel while we have fuel in our HELICOPTER tank
		while(REMAINING_KM > 0) {
			for(Brewery b : breweries) {
				Double distance = calculateDistance(currentLAT, currentLONG, b.geocode.latitude, b.geocode.longitude);
				
				// Find closest not visited factory
				if((distance < minDistance) && (!visitedBreweries.contains(b))) {
					minDistance = distance;
					nextBrewery = b;
				}
			}
			
			// check if going there we would have enough fuel to come back home
			Double distanceToHome = calculateDistance(nextBrewery.geocode.latitude, nextBrewery.geocode.longitude, LAT, LONG);
			if((minDistance + distanceToHome) <= REMAINING_KM) {
				currentLAT = nextBrewery.geocode.latitude;
				currentLONG = nextBrewery.geocode.longitude;
				REMAINING_KM -= minDistance;
				totalDistanceTraveled += minDistance;
				visitedBreweries.add(nextBrewery);
				
				for(Beer b : nextBrewery.beers)
					beersCollected.add(b);
				
				System.out.format("              Visiting -> [%s] (%s types) %s | Distance: %.2f km | Fuel remaining: %.2f km\n", 
						nextBrewery.id,
						nextBrewery.beers.size(),
						nextBrewery.name,
						minDistance, 
						REMAINING_KM);
			} else {
				// if we dont have enough fuel, we go home
				System.out.println("Going home...");
				distanceToHome = calculateDistance(currentLAT, currentLONG, LAT, LONG);
				totalDistanceTraveled += distanceToHome;
				System.out.format("Home reached. Fuel remaining: %.2f km\n\n", REMAINING_KM - distanceToHome);
				break;
			}
			
			minDistance = Double.MAX_VALUE;
			nextBrewery = null;
		}
		
		System.out.format("Beer factories visited: %s\n", visitedBreweries.size());
		System.out.format("Total distance traveled: %.2f km\n\n", totalDistanceTraveled);
		
		System.out.format("%s different types of beer collected:\n", beersCollected.size());
		for(Beer b : beersCollected)
			System.out.println(b.toString());
	}
	
	/**
	 * Haversine Distance Calculation
	 * @param cLAT		- current latitude
	 * @param cLONG		- current longitude
	 * @param latitude	- new brewery latitude
	 * @param longitude	- new brewery longitude
	 * @return
	 */
	Double calculateDistance(Double cLAT, Double cLONG, Double latitude, Double longitude) {
		Double latDistance = toRad(latitude - cLAT);
		Double longDistance = toRad(longitude - cLONG);
		
		Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) 
					+ Math.cos(toRad(cLAT)) * Math.cos(toRad(latitude))
					* Math.sin(longDistance / 2) * Math.sin(longDistance / 2);
		
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		
		return R * c;
	}
	
	/**
	 * Value conversion to Radiant
	 * @param value
	 * @return
	 */
	Double toRad(Double value) {
		return value * Math.PI / 180;
	}
	
	/**
	 * Remove breweries with missing geocodes or no beers
	 */
	void removeEntriesWithMissingData() {
		List<Brewery> temp = new ArrayList<Brewery>();
		
		for(Brewery b : breweries) {
			if(b.geocode.latitude != null && b.geocode.longitude != null && b.beers.size() != 0) {
				temp.add(b);
			}
		}
		
		breweries = temp;
	}
	
	/**
	 * Method for reading data from database
	 */
	void readData() {
		try {
			// MySQL Connection
			Class.forName(DRIVER).newInstance();
			Connection conn = DriverManager.getConnection(URL, USER, PASS);
			//System.out.println("Connection successful.");
			
			Statement st;
			ResultSet rs;
			
			// READ BEERS DATA
			
			query = "SELECT * from beers";
			// create the java statement
		    st = conn.createStatement();
		    // execute the query, and get beer list
		    rs = st.executeQuery(query);
			
		    beers = new ArrayList<Beer>();
		    while(rs.next()) {
		    	int brewery_id = rs.getInt("brewery_id");
		    	String name = rs.getString("name");
		    	
		    	Beer beer = new Beer(brewery_id, name);
		    	beers.add(beer);
		    	//System.out.format("%s : %s", brewery_id, name).println();
		    }
		    
		    // -----------------
		    // READ GEOCODES DATA
			
		 	query = "SELECT * from geocodes";
		 	// create the java statement
		 	st = conn.createStatement();
		 	// execute the query, and get geocode list
		 	rs = st.executeQuery(query);
		 			
		 	geocodes = new ArrayList<Geocode>();
		 	while(rs.next()) {
		 		int brewery_id = rs.getInt("brewery_id");
		 		Double latitude = rs.getDouble("latitude");
		 		Double longitude = rs.getDouble("longitude");
		 		
		 		Geocode geocode = new Geocode(brewery_id, latitude, longitude);
		 		geocodes.add(geocode);
		 		//System.out.format("%s : %s", brewery_id, latitude, longitude).println();
		 	}
		 		    
		 	// -----------------
			// READ BREWERIES DATA
			
			query = "SELECT * from breweries";
			// create the java statement
		    st = conn.createStatement();
		    // execute the query, and get brewery list
		    rs = st.executeQuery(query);
		    
		    breweries = new ArrayList<Brewery>();
		    while(rs.next()) {
		    	int id = rs.getInt("id");
		    	String name = rs.getString("name");
		    	
		    	Geocode gc = geocodes.stream().filter((_gc) -> _gc.brewery_id == id).findFirst().orElse(new Geocode(id, null, null));
		    	
		    	List<Beer> bList = new ArrayList<Beer>();
		    	for(Beer b : beers){
		    		if(b.brewery_id == id) {
		    			bList.add(b);
		    		}
		    	}
		    	
		    	Brewery brewery = new Brewery(id, name, gc, bList);
		    	breweries.add(brewery);
		    	
//		    	System.out.format("%s : %s %.8f %.8f", 
//		    			brewery.id, 
//		    			brewery.name, 
//		    			brewery.geocode.latitude, 
//		    			brewery.geocode.longitude).println();
		    }
		    
		    removeEntriesWithMissingData();
		    
//		    for(Brewery b : breweries)
//		    	b.print();
		    
		    // ----------------
		} catch(ClassNotFoundException e) {
			System.out.println("Driver exception!");
			System.out.print(e.getMessage());
		} catch(SQLException e) {
			System.out.println("SQL exception!");
			System.out.print(e.getMessage());
		} catch(Exception e) {
			System.out.print(e.getMessage());
		}
		
	}
	
	void run(String LAT, String LONG) {
		this.LAT = Double.parseDouble(LAT);
		this.LONG = Double.parseDouble(LONG);
		
//		System.out.print(LAT + " ");
//		System.out.println(LONG);
		
		readData();
		
		travel();
	}
}
