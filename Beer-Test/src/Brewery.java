import java.util.List;

public class Brewery {
	int id;
	String name;
	Geocode geocode;
	List<Beer> beers;
	
	public Brewery(int id, String name, Geocode geocode, List<Beer> beers) {
		this.id = id;
		this.name = name;
		this.geocode = geocode;
		this.beers = beers;
	}
	
	public void print() {
		System.out.format("[%s] %s %.8f %.8f \n %16s \n", id, name, geocode.latitude, geocode.longitude, "Beers:");
		
		//toString();
		
		for(Beer b : beers) {
			System.out.println(b.toString());
		}
			
	}
	
	@Override
	public String toString() {
		
		return String.format("[%s] %s %.8f %.8f \n %16s", id, name, geocode.latitude, geocode.longitude, "Beers:");
	}
}
