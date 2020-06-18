package View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Concave extends BaseFrame {
	final int CON_WIDTH = 900, CON_HEIGHT = 900, SPACE = CON_WIDTH / 18;
	int x = 20, y = 30, radius = 40, back_count = 3;
	int time_count = time;
	TimeThread time_thread = new TimeThread();
	boolean time_chk = true;
	String color;
	ArrayList<Integer> clicked_pointx = new ArrayList<Integer>();
	ArrayList<Integer> clicked_pointy = new ArrayList<Integer>();
	ArrayList<Integer> ban_pointx = new ArrayList<Integer>();
	ArrayList<Integer> ban_pointy = new ArrayList<Integer>();
	JPopupMenu menu = new JPopupMenu();
	JPanel panel;
	UDP_Communication udp_Comunication;
	JTextArea display_msg = new JTextArea();
	JTextField msg = new JTextField();
	JButton send = new JButton("����"), surren = new JButton("�׺�");

	public Concave(UDP_Communication udp, String color) {
		super("����", 1600, 1000);
		this.color = color;
		setTitle("���� " + color);
		udp_Comunication = udp;
		display_msg.setFont(new Font("", Font.BOLD, 20));
		display_msg.setEditable(false);
		udp.start();
	}

	void goToPrev(ActionEvent e) {
		int last = clicked_pointx.size() - 1;
		if (last <= -1)
			return;
		clicked_pointx.remove(last);
		clicked_pointy.remove(last);
		udp_Comunication.send_msg("�ǵ�����");
		back_count--;
		JMenuItem back = (JMenuItem) e.getSource();
		back.setText("�ǵ����� " + back_count);
		if (back_count == 0)
			back.setEnabled(false);
		updateUI();
	}

	void action() {
		JMenuItem back = new JMenuItem("�ǵ����� " + back_count);
		menu.add(back);
		surren.addActionListener(it -> {
			int yes_no = JOptionPane.showConfirmDialog(null, "�׺��Ͻðڽ��ϱ�?", "�׺�", JOptionPane.YES_NO_OPTION);
			if (yes_no == JOptionPane.YES_OPTION) {
				udp_Comunication.send_msg("�׺�");
				restart();
			}
		});

		back.addActionListener(it -> goToPrev(it));

		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == 3) {
					menu.show(Concave.this, e.getX(), e.getY());
					return;
				}
				int size = clicked_pointx.size();
				if (size % 2 == 1 && color.equals("��"))
					return;
				if (size % 2 == 0 && color.equals("��"))
					return;

				int pointx = e.getX(), pointy = e.getY();

				pointx = fixPoint(pointx, x);
				pointy = fixPoint(pointy, y);

				for (int i = 0; i < clicked_pointx.size(); i++)
					if (pointx == clicked_pointx.get(i) && pointy == clicked_pointy.get(i))
						return;

//				System.out.println("���� ��ǥ " + (pointx == ban_pointx.get(0)) + ", " + (pointy == ban_pointy.get(0)));
				for (int i = 0; i < ban_pointx.size(); i++) {
					System.out.println("for���� - " + ban_pointx.get(i) + ", " + ban_pointy.get(i));
					if (ban_pointx.get(i) == pointx && pointy == ban_pointy.get(i)) {
						System.out.println("�����Ǵ� ��ǥ " + ban_pointx.get(0) + ", " + ban_pointy.get(0));
						return;
					}
				}

				clicked_pointx.add(pointx);
				clicked_pointy.add(pointy);
				System.out.println("\n\n\n");

				updateUI();
				udp_Comunication.sendPoint(pointx, pointy);
				checkWinner();
				if (color.equals("��")) {
					doubleThreeCheck();

				}
			}
		});

		msg.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					send_msg();
			};
		});

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				udp_Comunication.thread_flag = false;
				udp_Comunication.ds.close();
			};
		});
	}

	void doubleThreeCheck() {
		ban_pointx.clear();
		ban_pointy.clear();
		for (int i = x; i <= CON_WIDTH + x; i += 50) {
			for (int j = y; j <= CON_HEIGHT + y; j += 50) {
				int cnt = 0;
				ban_pointx.add(fixPoint(i, x));
				ban_pointy.add(fixPoint(j, y));
				if (getDoubleCheck(0, SPACE) == 3)
					cnt++;
				if (getDoubleCheck(SPACE, 0) == 3)
					cnt++;
				if (getDoubleCheck(SPACE, SPACE) == 3)
					cnt++;
				if (getDoubleCheck(SPACE, -SPACE) == 3)
					cnt++;
				if (cnt < 2) {
					ban_pointx.remove(ban_pointx.size() - 1);
					ban_pointy.remove(ban_pointy.size() - 1);
				} else {
					System.out.println("�����Ǵ� ��ǥ " + ban_pointx.get(0) + ", " + ban_pointy.get(0));
				}
			}
		}
		System.out.println(ban_pointx.size());
	}

	void send_msg() {
		udp_Comunication.send_msg("message: " + msg.getText());
		display_msg.append("��: " + msg.getText() + "\n");
		msg.setText("");
		display_msg.setCaretPosition(display_msg.getDocument().getLength());
	}

	void checkWinner() {
		int start = (clicked_pointx.size() + 1) % 2;
		String winner = start == 0 ? "��" : "��";
		if (getStoneCount(0, SPACE) == 5 || getStoneCount(SPACE, 0) == 5 || getStoneCount(SPACE, SPACE) == 5
				|| getStoneCount(SPACE, -SPACE) == 5) {
			msg(winner + "���� �¸��Ͽ����ϴ�.", winner + "�� �¸�");
			restart();
		}
	}

	void restart() {
		int yes_no = JOptionPane.showConfirmDialog(null, "�ٽ� �Ͻðڽ��ϱ�?", "�����", JOptionPane.YES_NO_OPTION);
		if (yes_no == JOptionPane.YES_OPTION) {
			time_count = time;
			clicked_pointx.clear();
			clicked_pointy.clear();
			updateUI();
		} else
			dispose();
	}

	void updateUI() {
		repaint();
		revalidate();
	}

	void init() {
		panel = new JPanel() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				String turn_color = clicked_pointx.size() % 2 == 0 ? "��" : "��";
				g.setColor(new Color(0x005096));
				g.drawString(turn_color + "���Դϴ�.", CON_WIDTH / 2, 20);
				drawLine(g);

				for (int i = 0; i < clicked_pointx.size(); i++) {
					g.setColor(i % 2 == 0 ? Color.black : Color.white);
					if (i == clicked_pointx.size() - 1)
						g.setColor(new Color(0x808080));
					g.fillOval(clicked_pointx.get(i), clicked_pointy.get(i), radius, radius);
				}
//				System.out.println("paint ����");
			}
		};

		panel.setBackground(new Color(0x964b00));

		setEast();
		add(panel);
	}

//	���� ����(ä��â �� �ð� ī����)
	void setEast() {

		JPanel east = new JPanel(new BorderLayout()); // ������ ��° �г�
		JPanel north = new JPanel() {
			@Override
			public void paint(Graphics g) {
				// TODO Auto-generated method stub
				super.paint(g);
				g.setColor(Color.red);
				g.drawString(time_count + "�� ���ҽ��ϴ�.", 10, 10);
			}
		};

		JPanel south = new JPanel(new BorderLayout());
		JPanel south_east = new JPanel(new GridLayout(0, 1));

		if (time != 0) {
			east.add(north, BorderLayout.NORTH);
			time_thread.start();

		}

		east.setPreferredSize(new Dimension(500, 300));
		east.add(new JScrollPane(display_msg));
		east.add(south, BorderLayout.SOUTH);

		south.setPreferredSize(new Dimension(200, 50));
		south.add(msg);
		south.add(south_east, BorderLayout.EAST);

		south_east.add(send);
		south_east.add(surren);

		add(east, BorderLayout.EAST);
	}

	int getStoneCount(int xPlus, int yPlus) {
//		+1�� �������� ������ ��, �� �ڽ��� ���� ��
		return getStone(xPlus, yPlus, 0) + getStone(-xPlus, -yPlus, 0) + 1;
	}

	int getDoubleCheck(int xPlus, int yPlus) {
//		+1�� ������ �� �� �ڽ��� �����Ͽ� 3������ �˻��ϱ� ���� ��, �� �ڽ��� ���� ��.
		return getStone(xPlus, yPlus, 500) + getStone(-xPlus, -yPlus, 500) + 1;
	}

	int getStone(int xPlus, int yPlus, int plus_option) {
//		33 üũ�� �ϱ� ���� �޼���
		int start = (clicked_pointx.size() + 1) % 2;
		int cnt = 0;
		ArrayList<Integer> pointx, pointy;
		if (plus_option == 0) {
			pointx = clicked_pointx;
			pointy = clicked_pointy;
		} else {
			pointx = ban_pointx;
			pointy = ban_pointy;
			start = 0;
		}
		int clicked_point_size = clicked_pointx.size(), lastIdx = pointx.size() - 1;
		if (lastIdx == -1)
			return 0;

		if (clicked_point_size == -1)
			return 0;
		int x = pointx.get(lastIdx);
		int y = pointy.get(lastIdx);

//		33���ؿ����� �ڵ� �ؼ� �� plus_option�� 0�� �ƴ� ��
//		ArrayList�� ban_point ���(?)�� �Ͽ� x, y�� ���� �̰� �߿���. x�� y�� ban���� ������ ��ǥ�� ��
//		(x+xPlus, y+yPlus)������ �������̶�� �����ϰ���
//		���� ������ ��ǥ x, y�� ���������� ���̳� �鵹�� ������ �ִ°� �˻� 
//		������ ��ǥ �����⿡ �鵹�� �������ִٸ� 33 �ƴ� cnt�� plus_option��ŭ ����
//				�����⿡ �浹�� �������ִٸ� 33 ���ɼ��� ���� cnt�� 1 ������Ű��, �� ���������� �浹�̳� �鵹�� �� �������ִ��� �ݺ��ؼ� �˻�
//		�̷��� �˻��ؼ� ��ȯ�� ������ ���� 3�̶�� ������ ���� 33�� ���ɼ��� ����
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < clicked_point_size; j++) {
				if (y + yPlus == clicked_pointy.get(j) && x + xPlus == clicked_pointx.get(j)) {
					if (j % 2 == start) {
						cnt++;
						x += xPlus;
						y += yPlus;
					} else
						cnt += plus_option;
				}
			}
		return cnt;
	}

	int fixPoint(int point, int minus) {
//		point�� Ŭ���� ��ǥ, minus�� point���� �� ��. �������� �������� (0, 0)�� �ƴϴ�. �������� (x, y). �� Ŭ���� ��ǥ���� minus�� ���־�� �������� ��Ȯ�� ���� ���� �� �ִ�.
//		SPACE�� ������ point�� ���ϰ� SPACE�� ������. �̷��� SPACE�� ������ point�� ���ϴ� ������ ������ ������ �� SPACE�� ���� ���� 1 �ö󰡴��� �ƴ����� Ŭ�� ��ǥ���� ���� ����� ��ǥ�� ã�´�.
//		SPACE�� ������ ���Ͽ��� ��ǥ�� ���� �� �¾ƶ������� �� �ڿ� �������� ���ݸ�ŭ�� ���� ���� �׸� �� ���߾ӿ� �׷����� �Ѵ�. �׸��� ó���� ���� minus���� �ٽ� �����ִ� �� �⺻. 
		return (point - minus + SPACE / 2) / SPACE * SPACE + minus - radius / 2;
	}

	void drawLine(Graphics g) {
		g.setColor(Color.black);
		for (int i = 0; i < 19; i++)
			g.drawLine(x, y + SPACE * i, CON_WIDTH + x, y + SPACE * i);
		for (int i = 0; i < 19; i++)
			g.drawLine(x + SPACE * i, y, x + SPACE * i, CON_HEIGHT + y);
	}

	public class TimeThread extends Thread {
		@Override
		public void run() {
			while (udp_Comunication.thread_flag) {
				if (clicked_pointx.size() % 2 == 0 && color.contentEquals("��")) {
					System.out.print("");
					continue;
				}
				if (clicked_pointx.size() % 2 == 1 && color.contentEquals("��")) {
					System.out.print("");
					continue;
				}
				try {
					Thread.sleep(1000);
					time_count--;
					if (time_count == 0 && time_chk) {
						String winner = color.contentEquals("��") ? "��" : "��";
						JOptionPane.showMessageDialog(null, winner + "�� �¸��Դϴ�.");
						udp_Comunication.send_msg("�׺�");
						restart();
					}
					updateUI();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
