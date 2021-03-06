package cloudstorage.homework01;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
	public Server() {
		ExecutorService service = Executors.newFixedThreadPool(4);
		try (ServerSocket server = new ServerSocket(5678)){
			System.out.println("Server started");
			while (true) {
				service.execute(new ClientHandler(server.accept()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Server();
	}
}
