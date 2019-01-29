package planario;

import java.util.concurrent.ConcurrentHashMap;

public class PlayerData {
	final int playerID;
	int skin = 1;

	public ConcurrentHashMap<Integer,CanEatObj> planariaData = new ConcurrentHashMap<Integer,CanEatObj>();

	public PlayerData(int ID) {
		playerID = ID;
	}

	public int getSize() {
		return planariaData.size();
	}
}
