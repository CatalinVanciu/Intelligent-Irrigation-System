package Server;

public class Sensor {
	private String type;
	private double data;
	
	public Sensor() {
		
	}
	
	public Sensor(String type, double data) {
		this.type = type;
		this.data = data;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double getData() {
		return data;
	}

	public void setData(double data) {
		this.data = data;
	}
	
	
}
