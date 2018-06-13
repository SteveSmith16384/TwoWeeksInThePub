package twoweeks.server.maps;

import com.jme3.math.Vector3f;

public interface IMapCreator {
	
	Vector3f getStartPos();

	void createMap();
}
