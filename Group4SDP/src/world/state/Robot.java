package world.state;

public class Robot extends Entity {
	
	@Deprecated public double x;
	@Deprecated public double y;
	public double bearing;
	public RobotType type;
	
	public Robot(RobotType type) {
		this.type = type;
	}
	
	public String name() {
		return "Robot (" + type.toString() + ")";
	}
	
	public String position() {
		return String.format("%s: %s bearing %f",
				this.type, this.getPosition(), this.bearing);
	}
	
}