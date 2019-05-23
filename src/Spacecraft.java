import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Spacecraft {	
	private final int NET_WEIGHT = 165; // kg
	//Relative to the surface of the moon,
	//always at least 90 degrees. 
	private final double INITIAL_AXIS_Z_ANGLE = 58.3 + 90; // 90 is vertical (as in landing)
	private final double INITIAL_ALTITUDE = 30_000; // meters
	
	private double fuelWeight = 216.06; // kg
	private double horizontalVelocity = 1_700; // m/s
	private double verticalVelocity = -43; // m/s
	private double driveAngle = 270;
	private double tiltAngle = 0;
	//Relative to the surface of the moon,
	//always at least 90 degrees. 
	private double axisZAngle = INITIAL_AXIS_Z_ANGLE;
	private boolean availableOrientedDirection = true;
	private double altitude = INITIAL_ALTITUDE;
	private double accX = 0;
	private double accY = 0;
	private double accZ = 0;
	private SubEngine[] subEngines;
	private MainEngine mainEngine;
	
	public Spacecraft() {
		this.mainEngine = new MainEngine();
		this.subEngines = new SubEngine[8];
		for(int i = 0; i < 8; i++) {
			this.subEngines[i] = new SubEngine(45*i);
		}
	}
	
	/**
	 * The land process of the spacecraft on the surface of the Moon.
	 * @return true if the spacecraft landed successfully, and false otherwise.
	 */
	public boolean land() {
		double time = 0;
		double dt = 1; //second
		
		try {
			BufferedWriter writer = writer = new BufferedWriter(new FileWriter("log.csv"));
		    writer.write("time, altitude, axis Z angle, vertical acc, vertical vel, horizontal acc, horizontal vel\n");

			while(this.altitude > 990) {
				updateAccZ();
				time += dt;
				updateVerticalVelocity(dt);
				updateHorizontalVelocity(dt);
				updateAltitude(dt);
				updateZOrientation(dt);
				updateFuelWeight(dt);
				writer.write(time + ", " + this.altitude + ", " + this.axisZAngle
						+ ", " + (this.accZ*Math.cos(this.axisZAngle-90)-Moon.getGravityAcc(horizontalVelocity))
						+ ", " + this.verticalVelocity + ", " + (-accZ*Math.sin(this.axisZAngle-90)*dt)
						+ ", " + this.horizontalVelocity + "\n");
				System.out.println("Time: " + time
						+ "\tAlt: " + this.altitude
						+ "\tZangle: " + this.axisZAngle
						+ "\tVerAcc: " + (this.accZ*Math.cos(this.axisZAngle-90)-Moon.getGravityAcc(horizontalVelocity))
						+ "\tVerVel: " + this.verticalVelocity
						+ "\tHorAcc: " + (-accZ*Math.sin(this.axisZAngle-90)*dt)
						+ "\tHorVel: " + this.horizontalVelocity);
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}
	
	/**
	 * Update the new horizontal velocity of  the spacecraft after delta time.
	 * @param dt delta time in seconds from the last horizontal velocity update
	 */
	private void updateHorizontalVelocity(double dt) {
		this.horizontalVelocity -= accZ*Math.sin(this.axisZAngle-90)*dt; 
	}
	
	/**
	 * Update the new vertical velocity of  the spacecraft after delta time.
	 * The updated vertical velocity depends on the Moon gravity force. 
	 * @param dt delta time in seconds from the last vertical velocity update
	 */
	private void updateVerticalVelocity(double dt) {
		this.verticalVelocity += (accZ*Math.cos(this.axisZAngle-90) - Moon.getGravityAcc(horizontalVelocity))*dt; 
	}
	
	/**
	 * Update the new altitude of  the spacecraft after delta time.
	 * @param dt delta time in seconds from the last altitude update
	 */
	private void updateAltitude(double dt) {
		double verticalAcc = accZ*Math.cos(this.axisZAngle-90)-Moon.getGravityAcc(horizontalVelocity);
		this.altitude += verticalVelocity*dt + 0.5*verticalAcc*dt*dt;
	}
	
	/**
	 * Update the up to date acceleration on the Z axis,
	 * depends on the total thrust engines and the total weight.
	 */
	private void updateAccZ() {
		int totalThrust = mainEngine.getEngineThrust();
		for(int i = 0; i < 8; i++)
			if(subEngines[i].isOn())
				totalThrust += subEngines[i].getEngineThrust();
		this.accZ = totalThrust/(NET_WEIGHT+fuelWeight);
	}
	
	/**
	 * Update the angle of the spacecraft relative to the surface of the moon.
	 * The angle of Z axis determine the orientation of the spacecraft.
	 * @param dt time passed from the last Z orientation update in seconds
	 */
	private void updateZOrientation(double dt) {
		for(int i = 0; i < 8; i++)
			subEngines[i].setAngularVelocity(dt, subEngines[(i+4)%8]);
		//== this.axisZAngle += subEngines[90/45].getAngularVelocity()*t;
		this.axisZAngle -= subEngines[270/45].getAngularVelocity()*dt;
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
	 * @param dt time passed from the last fuel update in seconds
	 */
	private void updateFuelWeight(double dt) {
		fuelWeight -= mainEngine.getFuelBurningRate()*dt;
		for(int i = 0; i < 8; i++)
			if(subEngines[i].isOn())
				fuelWeight -= subEngines[i].getFuelBurningRate()*dt;
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
