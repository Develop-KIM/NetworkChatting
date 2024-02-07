package chat2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiServer {

	public static void main(String[] args) {
		// 소켓과 스트림 인스턴스 변수 선언
		ServerSocket serverSocket = null;
		Socket socket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		// 클라이언트의 메세지를 저장
		String s = "";
		// 클라이언트의 이름을 저장
		String name = "";

		try {
			// 포트를 기반으로 서버소켓을 생성한 후 클라이언트의 접속을 기다린다.
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");
			// 클라이언트의 접속을 허가한다.
			socket = serverSocket.accept();
			System.out.println(socket.getInetAddress() + "의" + socket.getPort() + "포트에 " + socket.getLocalAddress()
					+ "의 " + socket.getLocalPort() + "포트로 연결되었습니다.");
			// 메세지를 보낼준비(서버 -> 클라이언트)
			out = new PrintWriter(socket.getOutputStream(), true);
			// 메세지를 읽을(받을)준비(클라이언트 -> 서버)
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			/*
			 * 클라이언트가 서버로 전송하는 최초의 메세지는 "대화명"이므로 메세지를 읽은 후 변수에 저장하고 클라이언트 측으로 Echo 해준다.
			 */
			if (in != null) {
				// 클라이언트의 이름을 읽어 변수에 저장
				name = in.readLine();
				// 서버의 콘솔에 출력
				System.out.println(name + " 접속");
				// 클라이언트 측으로 메세지를 전송
				out.println("> " + name + "님이 접속했습니다.");
			}

			/*
			 * 클라이언트가 서버로 전송하는 두 번째 메세지부터는 실제 대화 내용이므로 읽어와서 콘솔에 출력하고 동시에 클라이언트 측으로 Echo 해준다.
			 */
			while (in != null) {
				// 메세지를 읽는다.
				s = in.readLine();
				if (s == null) {
					break;
				}
				// 서버의 콘솔에 출력
				System.out.println(name + " ==> " + s);
				// 클라이언트 측으로 Echo
				out.println(">  " + name + s);
			}

			System.out.println("Bye...!!!");
		} catch (Exception e) {
			System.out.println("예외1: " + e);
			// e.printStackTrace();
		} finally {
			try {
				in.close();
				out.close();
				socket.close();
				serverSocket.close();
			} catch (Exception e) {
				System.out.println("예외2: " + e);
				// e.printStackTrace();
			}
		}
	}

}
