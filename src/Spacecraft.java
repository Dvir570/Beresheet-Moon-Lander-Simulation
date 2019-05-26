import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Spacecraft {	
	private final int NET_WEIGHT = 165; // kg
	//Relative to the surface of the moon, always at least 90 degrees. 
	private final double INITIAL_AXIS_Z_ANGLE = 90 + 90; // 90 is vertical (as in landing)
	private final double INITIAL_ALTITUDE = 30_000; // meters
	private final double INITIAL_HORIZONTAL_VELOCITY = 1_700; // m/s
	private final double INITIAL_VERTICAL_VELOCITY = -43; // m/s
	private final double EPS = 5;
	
	private double fuelWeight = 216.06; // kg
	private double horizontalVelocity = INITIAL_HORIZONTAL_VELOCITY; // m/s
	private double verticalVelocity = INITIAL_VERTICAL_VELOCITY; // m/s
	//Relative to the surface of the moon, always at least 90 degrees. 
	private double axisZAngle = INITIAL_AXIS_Z_ANGLE;
	private double altitude = INITIAL_ALTITUDE;
	private double accZ = 0; // Z axis of the spacecraft
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
		double dt = 1; //seconds
		
		try {
			BufferedWriter writer = writer = new BufferedWriter(new FileWriter("log.csv"));
		    writer.write("time, altitude, axis Z angle, vertical acc, vertical vel, horizontal acc, horizontal vel, fuel weight\n");

			while(this.altitude > 0) {
				time += dt;
				updateFuelWeight(dt);
				engineControl(dt);
				this.accZ = accZ();
				updateHorizontalVelocity(dt);
				updateVerticalVelocity(dt);
				updateAltitude(dt);
				
				writer.write(time + ", " + this.altitude + ", " + this.axisZAngle
						+ ", " + (this.accZ*Math.cos(Math.toRadians(this.axisZAngle-90))-Moon.getGravityAcc(horizontalVelocity))
						+ ", " + this.verticalVelocity + ", " + (-accZ*Math.sin(Math.toRadians(this.axisZAngle-90))*dt)
						+ ", " + this.horizontalVelocity
						+ ", " + this.fuelWeight + "\n");
				System.out.println("Time: " + time
						+ "\tAlt: " + this.altitude
						+ "\tZangle: " + this.axisZAngle
						+ "\tAccZ: " + this.accZ
						+ "\tUpAcc: " + this.accZ*Math.cos(Math.toRadians(this.axisZAngle-90))
						+ "\tGravityAcc: " + Moon.getGravityAcc(horizontalVelocity)
						+ "\tVerAcc: " + (this.accZ*Math.cos(Math.toRadians(this.axisZAngle-90))-Moon.getGravityAcc(horizontalVelocity))
						+ "\tVerVel: " + this.verticalVelocity
						+ "\tHorAcc: " + (-accZ*Math.sin(Math.toRadians(this.axisZAngle-90))*dt)
						+ "\tHorVel: " + this.horizontalVelocity);
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Fuel weight: " + fuelWeight);
		return altitude == 0 && verticalVelocity > -20 && horizontalVelocity < 20;
	}
	
	/**
	 * Update the new horizontal velocity of  the spacecraft after delta time.
	 * @param dt delta time in seconds from the last horizontal velocity update
	 */
	private void updateHorizontalVelocity(double dt) {
		this.horizontalVelocity -= accZ*Math.sin(Math.toRadians(this.axisZAngle-90))*dt; 
	}
	
	/**
	 * Update the new vertical velocity of  the spacecraft after delta time.
	 * The updated vertical velocity depends on the Moon gravity force. 
	 * @param dt delta time in seconds from the last vertical velocity update
	 */
	private void updateVerticalVelocity(double dt) {
		this.verticalVelocity += (accZ*Math.cos(Math.toRadians(this.axisZAngle-90)) - Moon.getGravityAcc(horizontalVelocity))*dt; 
	}
	
	/**
	 * Update the new altitude of  the spacecraft after delta time.
	 * @param dt delta time in seconds from the last altitude update
	 */
	private void updateAltitude(double dt) {
		double verticalAcc = accZ*Math.cos(Math.toRadians(this.axisZAngle-90))-Moon.getGravityAcc(horizontalVelocity);
		this.altitude += verticalVelocity*dt + 0.5*verticalAcc*dt*dt;
		if(this.altitude < 0) this.altitude = 0;
	}
	
	/**
	 * Update the up to date acceleration on the Z axis,
	 * depends on the total thrust engines and the total weight.
	 * @return acceleration in the Z axis of the spacecraft
	 */
	private double accZ() {
		int totalThrust = 0;
		if(mainEngine.isOn())
			totalThrust += mainEngine.getEngineThrust();
		for(int i = 0; i < 8; i++)
			if(subEngines[i].isOn())
				totalThrust += subEngines[i].getEngineThrust();
		return (double)totalThrust/(NET_WEIGHT+fuelWeight);
	}
	
	/**
	 * Monitoring all the landing process and control the engines to achieve nice landing.
	 * @param dt delta time since last engines control and monitoring, in seconds
	 */
	private void engineControl(double dt) {
		for(int i = 0; i < 8; i++)
			subEngines[i].setAngularVelocity(dt, subEngines[(i+4)%8]);
		
		//== this.axisZAngle += subEngines[90/45].getAngularVelocity()*t;
		this.axisZAngle -= subEngines[270/45].getAngularVelocity()*dt;
		
		if(this.altitude < 1_000 && this.axisZAngle <= 100 && this.axisZAngle >= 90 && subEngines[270/45].getAngularVelocity() == 0) {
			double minEnginesAcc =  (630/(NET_WEIGHT+this.fuelWeight))*Math.cos(Math.toRadians(this.axisZAngle-90)) - Moon.getGravityAcc(this.horizontalVelocity); // Lower bound of the engines acceleration ability
			double timeToStop = (-this.verticalVelocity)/minEnginesAcc; // The time it takes for vertical velocity to zero, given actual vertical velocity
			double thresholdAltitude = 5 - this.verticalVelocity*timeToStop - 0.5*minEnginesAcc*timeToStop*timeToStop; // Threshold height for full stop in the vertical axis (vertical velocity = 0) at 5m height, given actual vertical velocity & The time it takes for speed to zero
			// DEBUG: System.out.println("******** PART B: thresholdAltitude: " + thresholdAltitude);
			if(this.altitude + this.verticalVelocity*dt + 0.5*Moon.getGravityAcc(this.horizontalVelocity)*dt*dt > thresholdAltitude || this.altitude <= 5 || this.verticalVelocity > 0) {
				System.out.println("Up Threshold Altitude");
				mainEngine.setOn(false);
				setFullGas(false);
			}else if(this.altitude > 5 && this.altitude < thresholdAltitude) {
				System.out.println("Down Threshold Altitude");
				mainEngine.setOn(true);
				setFullGas(true);
			}
		} else {
			double goalVerticalVelocity = (INITIAL_VERTICAL_VELOCITY/(INITIAL_ALTITUDE-5))*(this.altitude-5);
			// DEBUG: System.out.println(goalVerticalVelocity);
			double goalHorizontalVelocity = (INITIAL_HORIZONTAL_VELOCITY/(INITIAL_ALTITUDE-1_000))*(this.altitude-1_000);
			if(this.altitude > 1_000 && goalHorizontalVelocity+EPS < this.horizontalVelocity) { // increase angle or altitude
				if((int)this.axisZAngle+1 < 180) {
					// DEBUG: System.out.println("Turn on SE2 - Angle increase (+)");
					setFullGas(false);
					subEngines[90/45].setOn(true);
				} else setFullGas(true);
			}
			if(goalVerticalVelocity-EPS > this.verticalVelocity) { //decrease angle or increase altitude  
				if(Math.round(this.axisZAngle) > 90) {
					// DEBUG: System.out.println("Turn on SE6 - Angle decrease (-)");
					setFullGas(false);
					subEngines[270/45].setOn(true);
				} else setFullGas(true);
			}
			if(goalVerticalVelocity+EPS < this.verticalVelocity && this.verticalVelocity < 0) { //increase angle or decrease altitude
				if((int)this.axisZAngle+1 < 180) {
					// DEBUG: System.out.println("Turn on SE2 - Angle increase (+)");
					setFullGas(false);
					subEngines[90/45].setOn(true);
				} else setFullGas(false);
			}
			if(goalVerticalVelocity+EPS < this.verticalVelocity && this.verticalVelocity > 0 && this.altitude < 1_000) {
				// DEBUG: System.out.println("Turn off all engines");
				setFullGas(false);
				mainEngine.setOn(false);
			}
			if(subEngines[90/45].getAngularVelocity() > 0) {
				// DEBUG: System.out.println("Turn on SE6 - Angle decrease (-)");
				setFullGas(false);
				subEngines[270/45].setOn(true);
			} else if(subEngines[270/45].getAngularVelocity() > 0 && (this.altitude > 1_000 || subEngines[270/45].getAngularVelocity() > 3 || this.axisZAngle < 90+2*EPS)) {
				// DEBUG: System.out.println("Turn on SE2 - Angle increase (+)");
				setFullGas(false);
				subEngines[90/45].setOn(true);
			}
		}
	}
	
	/**
	 * Turn on/off all sub-engines. To gain full gas, the main engine turn on together with the sub-engines.
	 * If adjusted direction needed, all sub-engines turned off without the main engine.
	 * @param fullGas true if full gas needed - all sub-engines and main engine will turn on, and false will turn off all sub-engines, without the main engine
	 */
	private void setFullGas(boolean fullGas) {
		if(fullGas) {
			mainEngine.setOn(true);
			for(int i = 0; i < 8; i++)
				subEngines[i].setOn(true);
		}else for(int i = 0; i < 8; i++)
				subEngines[i].setOn(false);
	}
	
	/**
	 * Update the weight of the fuel given the time passed
	 * from the last fuel weight update in seconds.
	 * @param dt time passed from the last fuel update in seconds
	 */
	private void updateFuelWeight(double dt) {
		if(mainEngine.isOn())
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

	public double getAltitude() {
		return altitude;
	}

	public double getAccZ() {
		return accZ;
	}
}
