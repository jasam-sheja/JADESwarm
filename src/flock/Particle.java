package flock;

public class Particle implements IParticle {

	private static final long serialVersionUID = -9134098385256410306L;

	private float x, y;
	private float vx, vy;

	Particle(float x, float y, float vx, float vy) {
		this.x = x;
		this.y = y;
		this.vx = vx;
		this.vy = vy;
	}

	@Override
	public float getPosX() {
		// TODO Auto-generated method stub
		return x;
	}

	@Override
	public float getPosY() {
		// TODO Auto-generated method stub
		return y;
	}

	@Override
	public void setPosX(float x) {
		this.x = x;

	}

	@Override
	public void setPosY(float y) {
		this.y = y;

	}

	@Override
	public float getVelX() {
		return vx;
	}

	@Override
	public float getVelY() {
		// TODO Auto-generated method stub
		return vy;
	}

	@Override
	public void setVelX(float vx) {
		this.vx = vx;
	}

	@Override
	public void setVelY(float vy) {
		this.vy = vy;
	}

	@Override
	public String toString() {
		return String.format("%s (pos=(%f,%f), velocity=(%f,%f)", getClass().getName(), x, y, vx, vy);
	}

}
