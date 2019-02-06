package planario;

import java.awt.Component;

public class FieldPane extends Resizable {
	private static final long serialVersionUID = 1L;

	public FieldPane(int size) {
		super("res/mizuumi.png");

		setBounds(0, 0, size, size);
		setOpaque(false);
		setLayout(null);
	}

	@Override
	public Component add(Component comp) {
		try {
			return super.add(comp);
		} catch (IllegalArgumentException e) {	// �H�ɃG���[���������邽�ߗ�O�����ǉ�
			System.err.println(e.getMessage());
			return add(comp);
		}
	}
}
