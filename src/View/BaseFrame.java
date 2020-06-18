package View;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class BaseFrame extends JFrame {
	static Concave con;
	static int time = 0;

	public BaseFrame(String title, int w, int h) {
		setTitle(title);
		setSize(w, h);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(BaseFrame.DISPOSE_ON_CLOSE);
	}

	void msg(String msg, String title) {
		JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
	}

	void err_msg(String msg, String title) {
		JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
	}
}
