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
	JButton ok = new JButton("완료");

	public Select_Option() {
		super("옵션 선택", 500, 300);
		// UI 그리기
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
			err("포트");
			return;
		}
		if (color_str == "") {
			err("색깔");
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
			err_msg("올바른 IP를 적어주세요.", "올바르지 않은 IP");
			return;
		}
	}

	void err(String str) {
		err_msg(str + "을(를) 선택해주세요. " + str + "이(가) 선택되지 않았습니다.", str + " 선택");
	}

	void init() {
		JPanel center = new JPanel(new GridLayout(0, 1)), south = new JPanel();
		add(center);
		add(south, BorderLayout.SOUTH);

		String[] str_port = { "3000", "2500" }, str_color = { "흑", "백" },
				str_label = { "포트를 선택하세요.", "색깔을 선택하세요.", "시간을 선택하세요." }, str_time = { "없음", "30", "60" };
		JPanel row[] = { new JPanel(new FlowLayout(FlowLayout.LEFT)), new JPanel(new FlowLayout(FlowLayout.LEFT)),
				new JPanel(new FlowLayout(FlowLayout.LEFT)), new JPanel(new FlowLayout(FlowLayout.LEFT)) };
		ButtonGroup group[] = { new ButtonGroup(), new ButtonGroup() };

		try {
			center.add(new JLabel("  당신의 IP주소는 " + InetAddress.getLocalHost().getHostAddress() + "입니다."));
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

		row[0].add(new JLabel("상대방의 IP주소를 입력하세요."));
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
