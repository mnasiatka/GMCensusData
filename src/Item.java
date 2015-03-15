public class Item {

	public String zip, state;
	public String[] headers;

	public Item(String zip, String state) {
		this.zip = zip;
		this.state = state;
	}

	public Item(String[] headers) {
		this.headers = headers;
	}

}
