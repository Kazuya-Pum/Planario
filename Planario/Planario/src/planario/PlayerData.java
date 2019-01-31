package planario;

import java.util.concurrent.ConcurrentHashMap;

public class PlayerData {
	final int playerID;
	int skin = 0;

	public ConcurrentHashMap<Integer,EatableObj> planariaData = new ConcurrentHashMap<Integer,EatableObj>();

	public PlayerData(int ID) {
		playerID = ID;
	}

	public int getSize() {
		return planariaData.size();
	}
}
