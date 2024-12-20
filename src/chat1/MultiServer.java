package chat1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiServer {

	public static void main(String[] args) {
		// 멤버변수 : 소켓 관련, IO스트림 관련, String 인스턴스
		ServerSocket serverSocket = null;
		Socket socket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		String s = "";

		try {
			/*
			 * 9999번으로 포트번호를 설정한 후 서버 인스턴스를 생성하고 클라이언트의 접속을 기다린다. (Scanner 인스턴스와 같이 대기 상태가
			 * 된다.)
			 */
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");
			// ... 접속 대기중 ...

			/*
			 * 클라이언트가 접속 요청을 하면 accept()메서드를 통해 접속을 허가한다.
			 */
			socket = serverSocket.accept();
			/*
			 * getInetAddress() / getPort() : 원격지 즉 클라이언트의 IP주소와 Port번호를 얻어온다.
			 * getLocalAddress() / getLocalPort() : 연결이 시작된 네트워크 즉 서버측의 정보를 얻어온다.
			 */
			System.out.println(socket.getInetAddress() + "(클라이언트)의" + socket.getPort() + "포트를 통해 "
					+ socket.getLocalAddress() + "(서버)의 " + socket.getLocalPort() + "포트로 연결되었습니다.");
			// 서버 -> 클라이언트 측으로 메세지를 전송하기 위한 출력스트림 생성
			out = new PrintWriter(socket.getOutputStream(), true);
			// 클라이언트로부터 메세지를 받기위한 입력스트림 생성
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// 클라이언트가 보낸 메세지를 라인단위로 읽어온다.
			s = in.readLine();
			// 읽어온 내용을 콘솔에 출력한다.
			System.out.println("Clinent에서 읽어옴: " + s);
			// 클라이언트 측으로 응답메세지(Echo)를 보내준다.
			out.println("Server에서 응답: " + s);
			// 콘솔에 종료메세지를 출력한다.
			System.out.println("Bye...!!!");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 앞에서 생성했던 모든 자원을 해제한다.
			try {
				// 입출력 스트림 종료
				in.close();
				out.close();
				// 소켓 종료
				socket.close();
				serverSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
