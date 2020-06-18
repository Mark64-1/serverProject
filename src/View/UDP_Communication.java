package View;

import static View.BaseFrame.con;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

public class UDP_Communication extends Thread {
	DatagramSocket ds;
	InetAddress inet;
	String ip;
	int port;
	boolean thread_flag = true;

	public UDP_Communication(String ip, int port, int port_your) throws UnknownHostException {
		this.ip = ip;
		this.port = port;

		try {
			inet = InetAddress.getByName(ip);
			ds = new DatagramSocket(port_your);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	

	void sendPoint(int x, int y) {
		String point = "point: " + x + "," + y;
		byte buffer[] = point.getBytes();

		try {
			con.time_chk = false;
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, inet, port);
			ds.send(packet);
//			System.out.println(con.color + "보낸 좌표: " + point + port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void send_msg(String msg) {
		String point = msg;
		byte buffer[] = point.getBytes();

		try {
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, inet, port);
			ds.send(packet);
//			System.out.println(con.color + "보낸 메시지: " + point + port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 나 : 192.168.0.26, 3000
	// 192.168.0.31, 2500

	void receive() {
		byte buffer[] = new byte[512];
		try {
			DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
			ds.receive(dp);

			String data = new String(dp.getData());

			if (data.startsWith("point")) {
				con.time_chk = true;
				data = data.replace("point: ", "").trim();
				String point[] = data.split(",");
//				System.out.println(con.color + "수신된 데이터 : " + point[0] + "," + point[1]);
				con.clicked_pointx.add(Integer.parseInt(point[0]));
				con.clicked_pointy.add(Integer.parseInt(point[1]));
				con.time_count = BaseFrame.time;
//				con.time_thread;
				con.updateUI();
				con.checkWinner();
				if (con.color.contentEquals("흑")) {
					System.out.println("흑 데이터를 받았습니다. 33체크를 시작합니다.");
					con.doubleThreeCheck();
				}
			} else if (data.trim().contentEquals("항복")) {
				JOptionPane.showMessageDialog(null, con.color + "의 승리입니다.");
				con.restart();
			} else if (data.trim().contentEquals("되돌리기")) {
				int last = con.clicked_pointx.size() - 1;
				con.clicked_pointx.remove(last);
				con.clicked_pointy.remove(last);
				if (con.color.contentEquals("흑")) {
					System.out.println("흑 데이터를 받았습니다. 33체크를 시작합니다.");
					con.doubleThreeCheck();
				}
				con.updateUI();
			} else {
				con.display_msg.append("상대방: " + data.replace("message: ", "") + "\n");
			}
		} catch (IOException e) {
//			e.printStackTrace();
//			System.out.println(con.color + " 패배 프로그램을 종료합니다.");
			System.out.println(con.color + "색깔의 프로그램을 종료합니당~~~");
			thread_flag = false;
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (thread_flag) {
			receive();
		}
	}
}
