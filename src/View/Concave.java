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
	JButton send = new JButton("전송"), surren = new JButton("항복");

	public Concave(UDP_Communication udp, String color) {
		super("오목", 1600, 1000);
		this.color = color;
		setTitle("오목 " + color);
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
		udp_Comunication.send_msg("되돌리기");
		back_count--;
		JMenuItem back = (JMenuItem) e.getSource();
		back.setText("되돌리기 " + back_count);
		if (back_count == 0)
			back.setEnabled(false);
		updateUI();
	}

	void action() {
		JMenuItem back = new JMenuItem("되돌리기 " + back_count);
		menu.add(back);
		surren.addActionListener(it -> {
			int yes_no = JOptionPane.showConfirmDialog(null, "항복하시겠습니까?", "항복", JOptionPane.YES_NO_OPTION);
			if (yes_no == JOptionPane.YES_OPTION) {
				udp_Comunication.send_msg("항복");
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
				if (size % 2 == 1 && color.equals("흑"))
					return;
				if (size % 2 == 0 && color.equals("백"))
					return;

				int pointx = e.getX(), pointy = e.getY();

				pointx = fixPoint(pointx, x);
				pointy = fixPoint(pointy, y);

				for (int i = 0; i < clicked_pointx.size(); i++)
					if (pointx == clicked_pointx.get(i) && pointy == clicked_pointy.get(i))
						return;

//				System.out.println("현재 좌표 " + (pointx == ban_pointx.get(0)) + ", " + (pointy == ban_pointy.get(0)));
				for (int i = 0; i < ban_pointx.size(); i++) {
					System.out.println("for문임 - " + ban_pointx.get(i) + ", " + ban_pointy.get(i));
					if (ban_pointx.get(i) == pointx && pointy == ban_pointy.get(i)) {
						System.out.println("금지되는 좌표 " + ban_pointx.get(0) + ", " + ban_pointy.get(0));
						return;
					}
				}

				clicked_pointx.add(pointx);
				clicked_pointy.add(pointy);
				System.out.println("\n\n\n");

				updateUI();
				udp_Comunication.sendPoint(pointx, pointy);
				checkWinner();
				if (color.equals("흑")) {
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
					System.out.println("금지되는 좌표 " + ban_pointx.get(0) + ", " + ban_pointy.get(0));
				}
			}
		}
		System.out.println(ban_pointx.size());
	}

	void send_msg() {
		udp_Comunication.send_msg("message: " + msg.getText());
		display_msg.append("나: " + msg.getText() + "\n");
		msg.setText("");
		display_msg.setCaretPosition(display_msg.getDocument().getLength());
	}

	void checkWinner() {
		int start = (clicked_pointx.size() + 1) % 2;
		String winner = start == 0 ? "흑" : "백";
		if (getStoneCount(0, SPACE) == 5 || getStoneCount(SPACE, 0) == 5 || getStoneCount(SPACE, SPACE) == 5
				|| getStoneCount(SPACE, -SPACE) == 5) {
			msg(winner + "돌이 승리하였습니다.", winner + "돌 승리");
			restart();
		}
	}

	void restart() {
		int yes_no = JOptionPane.showConfirmDialog(null, "다시 하시겠습니까?", "재시작", JOptionPane.YES_NO_OPTION);
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
				String turn_color = clicked_pointx.size() % 2 == 0 ? "흑" : "백";
				g.setColor(new Color(0x005096));
				g.drawString(turn_color + "턴입니다.", CON_WIDTH / 2, 20);
				drawLine(g);

				for (int i = 0; i < clicked_pointx.size(); i++) {
					g.setColor(i % 2 == 0 ? Color.black : Color.white);
					if (i == clicked_pointx.size() - 1)
						g.setColor(new Color(0x808080));
					g.fillOval(clicked_pointx.get(i), clicked_pointy.get(i), radius, radius);
				}
//				System.out.println("paint 진입");
			}
		};

		panel.setBackground(new Color(0x964b00));

		setEast();
		add(panel);
	}

//	우측 세팅(채팅창 및 시간 카운팅)
	void setEast() {

		JPanel east = new JPanel(new BorderLayout()); // 오른쪽 통째 패널
		JPanel north = new JPanel() {
			@Override
			public void paint(Graphics g) {
				// TODO Auto-generated method stub
				super.paint(g);
				g.setColor(Color.red);
				g.drawString(time_count + "초 남았습니다.", 10, 10);
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
//		+1은 마지막에 놓여진 돌, 나 자신을 세는 것
		return getStone(xPlus, yPlus, 0) + getStone(-xPlus, -yPlus, 0) + 1;
	}

	int getDoubleCheck(int xPlus, int yPlus) {
//		+1은 임의의 점 나 자신을 포함하여 3개인지 검사하기 위한 것, 나 자신을 세는 것.
		return getStone(xPlus, yPlus, 500) + getStone(-xPlus, -yPlus, 500) + 1;
	}

	int getStone(int xPlus, int yPlus, int plus_option) {
//		33 체크를 하기 위한 메서드
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

//		33기준에서의 코드 해석 즉 plus_option이 0이 아닐 때
//		ArrayList를 ban_point 모드(?)로 하여 x, y를 정함 이게 중요함. x와 y가 ban당할 임의의 좌표가 됨
//		(x+xPlus, y+yPlus)방향을 순방향이라고 정의하겠음
//		현재 임의의 좌표 x, y의 순방향으로 흰돌이나 백돌이 놓여져 있는가 검사 
//		임의의 좌표 순방향에 백돌이 놓여져있다면 33 아님 cnt를 plus_option만큼 증가
//				순방향에 흑돌이 놓여져있다면 33 가능성이 있음 cnt를 1 증가시키고, 그 순방향으로 흑돌이나 백돌이 더 놓여져있는지 반복해서 검사
//		이렇게 검사해서 반환된 값들의 합이 3이라면 임의의 점은 33의 가능성이 있음
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
//		point는 클릭된 좌표, minus는 point에서 뺄 값. 오목판의 시작점이 (0, 0)이 아니다. 시작점은 (x, y). 즉 클릭된 좌표에서 minus를 뺴주어야 공식으로 정확한 값을 얻을 수 있다.
//		SPACE의 절반을 point에 더하고 SPACE로 나눈다. 이렇게 SPACE의 절반을 point에 더하는 이유는 절반을 더했을 때 SPACE로 나눈 값이 1 올라가는지 아닌지로 클릭 좌표에서 가장 가까운 좌표를 찾는다.
//		SPACE로 나누고 곱하여서 좌표를 점에 딱 맞아떨어지게 한 뒤에 반지름의 절반만큼을 빼서 돌을 그릴 때 정중앙에 그려지게 한다. 그리고 처음에 뺏던 minus값을 다시 더해주는 건 기본. 
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
				if (clicked_pointx.size() % 2 == 0 && color.contentEquals("백")) {
					System.out.print("");
					continue;
				}
				if (clicked_pointx.size() % 2 == 1 && color.contentEquals("흑")) {
					System.out.print("");
					continue;
				}
				try {
					Thread.sleep(1000);
					time_count--;
					if (time_count == 0 && time_chk) {
						String winner = color.contentEquals("흑") ? "백" : "흑";
						JOptionPane.showMessageDialog(null, winner + "의 승리입니다.");
						udp_Comunication.send_msg("항복");
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
