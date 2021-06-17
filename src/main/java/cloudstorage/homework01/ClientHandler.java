package cloudstorage.homework01;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
	private final Socket socket;

	public ClientHandler(Socket socket) {
		this.socket = socket;
	}


	@Override
	public void run() {
		try (
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				DataInputStream in = new DataInputStream(socket.getInputStream())
		) {
			System.out.printf("Client %s connected\n", socket.getInetAddress());
			while (true) {
				String command = in.readUTF();
				String status = "";
				if ("upload".equals(command)) {
					try {
						File file = new File("server"  + File.separator + in.readUTF());
						if (!file.exists()) {
							file.createNewFile();
						}
						FileOutputStream fos = new FileOutputStream(file);

						long size = in.readLong();

						byte[] buffer = new byte[8 * 1024];

//						т.к. размер файла зачастую некратен нашему буферу байт,
//						то прибавляем размер нашего буффера минус 1 (-1 чтобы избежать лишней итерации цикла,
//						если размер файла будет кратен буферу) к размеру файла при подсчете количества
//						целых шагов цикла
						for (int i = 0; i < (size + (buffer.length - 1)) / (buffer.length); i++) {
							int read = in.read(buffer);
							fos.write(buffer, 0, read);
						}
						fos.close();
						status = "OK";
					} catch (Exception e) {
						status = "FATAL ERROR";
					}
				}

				if ("download".equals(command)) {
					try {
						File file = new File("server"  + File.separator + in.readUTF());
						if (!file.exists()) {
							throw  new FileNotFoundException();
						}

						long fileLength = file.length();

						FileInputStream fis = new FileInputStream(file);

						out.writeLong(fileLength);

						int read = 0;
						byte[] buffer = new byte[8 * 1024];
						while ((read = fis.read(buffer)) != -1) {
							out.write(buffer, 0, read);
						}
						out.flush();

						status = in.readUTF();

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if ("exit".equals(command)) {
					System.out.printf("Client %s disconnected correctly\n", socket.getInetAddress());
					break;
				}

				System.out.printf("%s command status: %s\n", command, status);
				out.writeUTF(status);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
