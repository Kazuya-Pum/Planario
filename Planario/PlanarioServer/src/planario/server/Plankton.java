package planario.server;

public class Plankton {
	private static int id;
	private int localId;
	private int x;
	private int y;
	private boolean virus = false;

	public Plankton(int x, int y, boolean virus) {
		this.x = x;
		this.y = y;
		this.virus = virus;

		localId = ++id;
	}

	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Pop ");
		buf.append(this.localId);
		buf.append(" ");
		buf.append(this.x);
		buf.append(" ");
		buf.append(this.y);
		buf.append(" ");
		buf.append(this.virus ? 1 : 0);
		return buf.toString();
	}

	public int getId() {
		return this.localId;
	}
}
