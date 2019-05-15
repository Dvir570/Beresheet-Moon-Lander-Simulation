
public class SubEngine {
	private final int ENGINE_THRUST = 25; // N
	private double tiltAngle; //In the surface of the center of gravity of the spacecraft
	private double angularAcceleration; // angle per second^2
	private double angularVelocity = 0; // angle per second
	private boolean on = false;

	public SubEngine(double tiltAngle, double angularAcceleration) {
		this.tiltAngle = tiltAngle;
		this.angularAcceleration = angularAcceleration;
	}
	
	public double getTiltAngle() {
		return this.tiltAngle;
	}
	
	public double getAngularAcceleration() {
		return this.angularAcceleration;
	}
	
	public double getAngularVelocity() {
		return angularVelocity;
	}

	/**
	 * Update the angular velocity of *this specific sub-engine only*, dependent on the contrary sub-engine.
	 * @param t delta time, since the last update.
	 * @param contraryEngine the sub-engine that might exert an opposing force.
	 */
	public void setAngularVelocity(int t, SubEngine contraryEngine) {
		if(this.isOn() && !contraryEngine.isOn())
			this.angularVelocity += this.angularAcceleration*t;			
		else if(!this.isOn() && contraryEngine.isOn())
			this.angularVelocity -= contraryEngine.angularAcceleration*t;
	}
	
	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	}

	public int getEngineThrust() {
		return ENGINE_THRUST;
	}
	
}
