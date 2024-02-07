package chat4;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

// 서버가 보내는 Echo메세지를 읽어오는 쓰레드 클래스
public class Receiver extends Thread {
	// 멤버변수
	Socket socket;
	BufferedReader in = null;

	/*
	 * 클라이언트가 접속시 생성한 Socket인스턴스를 생성자에서 매개변수로 받은 후 멤버변수를 초기화한다.
	 */
	public Receiver(Socket socket) {
		this.socket = socket;
		// Socket 인스턴스를 기반으로 입력스트림을 생성한다.
		try {
			in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		} catch (Exception e) {
			System.out.println("예외1: " + e);
		}
	}

	/*
	 * 쓰레드의 main() 메서드 역할을 하는 메서드로 직접 호출하면 안 되고, 반드시 start()를 통해 간접호출해야 쓰레드가 생성된다.
	 */
	@Override
	public void run() {
		while (in != null) {
			try {
				// 입력 스트림을 통해 서버가 보낸 내용을 지속적으로 읽어오는 역할을 한다.
				System.out.println("Thread Receive: " + in.readLine());
			} catch (SocketException e) {
				/*
				 * 클라이언트가 q를 입력하여 접속을 종료하면 무한루프가 발생되므로 탈출할 수 있도록 catch 블럭을 추가하고 break를 추가한다.
				 */
				System.out.println("SocketException 발생됨. " + "루프탈출.");
				break;
			} catch (Exception e) {
				// 클라이언트가 접속을 종료하면 SocketException이 발생하면서 무한루프에 빠지게된다.
				System.out.println("예외2: " + e);
			}
		}

		try {
			in.close();
		} catch (Exception e) {
			System.out.println("예외3: " + e);
		}
	}
}
