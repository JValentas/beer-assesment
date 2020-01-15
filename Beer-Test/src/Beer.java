
public class Beer {
	int brewery_id;
	String name;
	
	public Beer(int brewery_id, String name) {
		this.brewery_id = brewery_id;
		this.name = name;
	}
	
	@Override
	public String toString() {
		return String.format("              * %s", name);
	}
}
