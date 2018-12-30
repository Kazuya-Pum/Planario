package planario;

import java.util.HashMap;
import java.util.Map;

public class PlayerData {
	final int playerID;
	int skin = 1;

	public Map<Integer,CanEatObj> planariaData = new HashMap<Integer,CanEatObj>();

	public PlayerData(int ID) {
		playerID = ID;
	}
}
