
public class SubEngine {
	private double tiltAngle; //In the surface of the center of gravity of the spacecraft
	private double angularVelocity; //Angle per second
	
	public SubEngine(double tiltAngle, double angularVelocity) {
		this.tiltAngle = tiltAngle;
		this.angularVelocity = angularVelocity;
	}
	
	public double getTiltAngle() {
		return this.tiltAngle;
	}
	
	public double getAngularVelocity() {
		return this.angularVelocity;
	}
}
