
public class MainEngine {
	private final int ENGINE_THRUST = 430; // N
	private final double FUEL_BURNING_RATE = 0.15; // kg fuel per second
	private boolean isOn = false;
	
	public boolean isOn() {
		return isOn;
	}

	public void setOn(boolean on) {
		this.isOn = on;
	}

	public int getEngineThrust() {
		return ENGINE_THRUST;
	}
	
	public double getFuelBurningRate() {
		return FUEL_BURNING_RATE;
	}
}
