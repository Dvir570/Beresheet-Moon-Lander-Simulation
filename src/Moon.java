
public class Moon {
	public static final double GRAVITY_ACC = 1.622;// m/s^2
	public static final double EQ_SPEED = 1700;// m/s
	
	/**
	 * Calculate the acceleration of the gravity on an object with horizontal velocity.
	 * Because the object has horizontal velocity, it has centrifugal force that balance the gravity force. 
	 * @param horizontal_velocity of the object.
	 * @return the acceleration of the gravity on this object. 
	 */
	public static double getGravityAcc(double horizontal_velocity) {
		double n = Math.abs(horizontal_velocity)/EQ_SPEED;
		double ans = (1-n)*GRAVITY_ACC;
		return ans;
	}
}
