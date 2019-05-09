
public class Spacecraft {	
	private final int NET_WEIGHT = 164;
	private final int ANGULAR_VELOCITY = 1;
	private final double FUEL_BURNING_RATE = 0.2; // kg fuel per second
	private final double INITIAL_AXIS_Z_ANGLE = 180; //Relative to the surface of the moon
	private final double INITIAL_ALTITUDE = 30_000;
	
	private double fuelWeight = 216.06;
	private double horizontalVelocity = 1_700;
	private double verticalVelocity = 43;
	private double driveAngle = 270;
	private double tiltAngle = 0;
	private double axisZAngle = INITIAL_AXIS_Z_ANGLE; //Relative to the surface of the moon
	private double altitude = INITIAL_ALTITUDE;
	private double accX = 0;
	private double accY = 0;
	private double accZ = 1.6;
	private SubEngine[] subEngines;
	
	public Spacecraft() {
		this.subEngines = new SubEngine[8];
		for(int i = 0; i < 8; i++) {
			this.subEngines[i] = new SubEngine(45*i, ANGULAR_VELOCITY);
		}
	}
	
	public boolean land() {
		return false;
	}
	
	private double updatedZOrientation() {
		if(this.altitude > 1_000)
			return ((INITIAL_AXIS_Z_ANGLE-90)/(INITIAL_ALTITUDE-1_000))*(this.altitude-1_000) +90;
		else return 90;
	}

	public double getFuelWeight() {
		return fuelWeight;
	}

	public double getHorizontalVelocity() {
		return horizontalVelocity;
	}

	public double getVerticalVelocity() {
		return verticalVelocity;
	}

	public double getDriveAngle() {
		return driveAngle;
	}

	public double getTiltAngle() {
		return tiltAngle;
	}

	public double getAltitude() {
		return altitude;
	}

	public double getAccX() {
		return accX;
	}

	public double getAccY() {
		return accY;
	}

	public double getAccZ() {
		return accZ;
	}
}
