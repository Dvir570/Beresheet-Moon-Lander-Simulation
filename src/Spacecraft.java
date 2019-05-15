
public class Spacecraft {	
	private final int NET_WEIGHT = 165;
	private final int ANGULAR_ACCELERATION = 1; // angle per second^2
	private final int MAIN_ENGINE_THRUST = 430; // N
	private final double FUEL_BURNING_RATE = 0.2; // kg fuel per second
	//Relative to the surface of the moon,
	//always at least 90 degrees. 
	private final double INITIAL_AXIS_Z_ANGLE = 180;
	private final double INITIAL_ALTITUDE = 30_000;
	
	private double fuelWeight = 216.06;
	private double horizontalVelocity = 1_700;
	private double verticalVelocity = 43;
	private double driveAngle = 270;
	private double tiltAngle = 0;
	//Relative to the surface of the moon,
	//always at least 90 degrees. 
	private double axisZAngle = INITIAL_AXIS_Z_ANGLE;
	private boolean availableOrientedDirection = true;
	private double altitude = INITIAL_ALTITUDE;
	private double accX = 0;
	private double accY = 0;
	private double accZ = 1.6;
	private SubEngine[] subEngines;
	
	public Spacecraft() {
		this.subEngines = new SubEngine[8];
		for(int i = 0; i < 8; i++) {
			this.subEngines[i] = new SubEngine(45*i, ANGULAR_ACCELERATION);
		}
	}
	
	public boolean land() {
		return false;
	}
	
	/**
	 * Update the up to date acceleration on the Z axis,
	 * depends on the total thrust engines.
	 */
	private void updateAccZ() {
		int totalThrust = MAIN_ENGINE_THRUST;
		for(int i = 0; i < 8; i++)
			if(subEngines[i].isOn())
				totalThrust += subEngines[i].getEngineThrust();
		this.accZ = totalThrust/(NET_WEIGHT+fuelWeight);
	}
	
	/**
	 * Update the angle of the spacecraft relative to the surface of the moon.
	 * The angle of Z axis determine the orientation of the spacecraft.
	 * @param t time passed from the last Z orientation update in seconds
	 */
	private void updateZOrientation(int t) {
		for(int i = 0; i < 8; i++)
			subEngines[i].setAngularVelocity(t, subEngines[(i+4)%8]);
		//== this.axisZAngle += subEngines[90/45].getAngularVelocity()*t;
		this.axisZAngle -= subEngines[270/45].getAngularVelocity()*t;
		if(availableOrientedDirection)
			setAvailableOrientedDirection(false);
		double ratio = 1;
		if(this.altitude > 1_000)
			ratio = (INITIAL_ALTITUDE-this.altitude)/(INITIAL_ALTITUDE-1_000);
		double goalAxisZAngle = INITIAL_AXIS_Z_ANGLE - (INITIAL_AXIS_Z_ANGLE-90)*ratio*ratio;
		if(goalAxisZAngle < this.axisZAngle) {
				setAvailableOrientedDirection(true);
				subEngines[270/45].setOn(true);
		} else if(goalAxisZAngle > this.axisZAngle) {
				setAvailableOrientedDirection(true);
				subEngines[90/45].setOn(true);
		}
	}
	
	/**
	 * Allows you to change the availability of changing the orientation of the spacecraft.
	 * @param available true if you want to enable the availability to change orientation, and false otherwise.
	 */
	private void setAvailableOrientedDirection(boolean available) {
		if(available)
			for(int i = 0; i < 8; i++)
				subEngines[i].setOn(false);
		else
			for(int i = 0; i < 8; i++)
				subEngines[i].setOn(true);
		availableOrientedDirection = available;
	}
	
	/**
	 * Update the weight of the fuel given the time passed
	 * from the last fuel weight update in seconds.
	 * @param t time passed from the last fuel update in seconds
	 */
	private void updateFuelWeight(int t) {
		this.fuelWeight = this.fuelWeight - FUEL_BURNING_RATE*t;
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
