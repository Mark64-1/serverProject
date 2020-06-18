package View;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class Select_Option extends BaseFrame {
	JRadioButton port[] = new JRadioButton[2];
	JRadioButton color[] = new JRadioButton[2];
	JRadioButton time[] = new JRadioButton[3];
	JTextField ip = new JTextField(25);
	JButton ok = new JButton("�Ϸ�");

	public Select_Option() {
		super("�ɼ� ����", 500, 300);
		// UI �׸���
		init();
		setVisible(true);
	}

	void okAction() {
		int port_num = 0;
		String color_str = "";
		for (int i = 0; i < 2; i++) {
			if (port[i].isSelected())
				port_num = Integer.parseInt(port[i].getText());
			if (color[i].isSelected())
				color_str = color[i].getText();
		}
		if (port_num == 0) {
			err("��Ʈ");
			return;
		}
		if (color_str == "") {
			err("����");
			return;
		}
		if (ip.getText().contentEquals("")) {
			err("ip");
			return;
		}

		if (time[1].isSelected()) {
			BaseFrame.time = 30;
		}
		if (time[2].isSelected()) {
			BaseFrame.time = 60;
		}

		try {
			UDP_Communication udp = new UDP_Communication(ip.getText(), port_num == 3000 ? 2500 : 3000, port_num);
			con = new Concave(udp, color_str);
			con.init();
			con.action();
			con.setVisible(true);
			dispose();
		} catch (UnknownHostException e) {
//			e.printStackTrace();
			err_msg("�ùٸ� IP�� �����ּ���.", "�ùٸ��� ���� IP");
			return;
		}
	}

	void err(String str) {
		err_msg(str + "��(��) �������ּ���. " + str + "��(��) ���õ��� �ʾҽ��ϴ�.", str + " ����");
	}

	void init() {
		JPanel center = new JPanel(new GridLayout(0, 1)), south = new JPanel();
		add(center);
		add(south, BorderLayout.SOUTH);

		String[] str_port = { "3000", "2500" }, str_color = { "��", "��" },
				str_label = { "��Ʈ�� �����ϼ���.", "������ �����ϼ���.", "�ð��� �����ϼ���." }, str_time = { "����", "30", "60" };
		JPanel row[] = { new JPanel(new FlowLayout(FlowLayout.LEFT)), new JPanel(new FlowLayout(FlowLayout.LEFT)),
				new JPanel(new FlowLayout(FlowLayout.LEFT)), new JPanel(new FlowLayout(FlowLayout.LEFT)) };
		ButtonGroup group[] = { new ButtonGroup(), new ButtonGroup() };

		try {
			center.add(new JLabel("  ����� IP�ּҴ� " + InetAddress.getLocalHost().getHostAddress() + "�Դϴ�."));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < 4; i++)
			center.add(row[i]);
		for (int i = 0; i < 3; i++)
			row[i + 1].add(new JLabel(str_label[i]));
		addRadio(row[1], port, str_port);
		addRadio(row[2], color, str_color);
		addRadio(row[3], time, str_time);
		time[0].setSelected(true);

		row[0].add(new JLabel("������ IP�ּҸ� �Է��ϼ���."));
		row[0].add(ip);

		ok.addActionListener(it -> okAction());
		south.add(ok);
	}

	void addRadio(JPanel jP, JRadioButton radio[], String[] label) {
		ButtonGroup group = new ButtonGroup();
		for (int i = 0; i < label.length; i++) {
			jP.add(radio[i] = new JRadioButton(label[i]));
			group.add(radio[i]);
		}
	}

	public static void main(String[] args) {
		try {
			System.setProperty("file.encoding", "UTF-8");
			Field charset = Charset.class.getDeclaredField("defaultCharset");
			charset.setAccessible(true);
			charset.set(null, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Select_Option select = new Select_Option();
	}
}
