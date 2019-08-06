package flock;

import java.io.Serializable;

public interface IParticle extends Serializable {

	float getPosX();

	float getPosY();

	void setPosX(float x);

	void setPosY(float y);

	float getVelX();

	float getVelY();

	void setVelX(float vx);

	void setVelY(float vy);
}
