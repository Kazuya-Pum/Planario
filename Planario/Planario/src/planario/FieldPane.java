package planario;

public class FieldPane extends Resizable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public FieldPane(int size) {
		super("res/mizuumi.png");

		setBounds(0, 0, size, size);
		setOpaque(false);
		setLayout(null);
	}
}
