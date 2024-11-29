package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final PropertyLoader PROPERTIES = new PropertyLoader("application.properties");
    private static final int PORT = Integer.parseInt(PROPERTIES.getPort());
    private final List<ClientHandler> clients = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);

        logger.info("Сервер запущен на порту {}", PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(clientSocket);
            clients.add(handler);
            new Thread(handler).start(); // запускаем новый поток для обработки клиента
        }
    }

    private class ClientHandler implements Runnable {
        private String nickname;
        private final Socket socket;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Получение ника при первом соединении
                nickname = in.readLine();
                logger.info("{} присоединился к чату.", nickname);
                broadcastMessage(nickname + " Join to chat.");
                sendUserList();

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/list")) { // запрос списка пользователей
                        sendUserList();
                    } else if (message.startsWith("/private")) { // личное сообщение
                        handlePrivateMessage(message);
                    } else { // широковещательная рассылка
                        broadcastMessage(nickname + ": " + message);
                    }
                }
            } catch (IOException e) {
                logger.error("Ошибка при обработке клиента: {}", e.getMessage());
            } finally {
                closeConnection();
            }
        }

        private void handlePrivateMessage(String message) {
            String[] parts = message.split(" ", 3); // /private <адресат> <сообщение>
            if (parts.length == 3) {
                String recipientNickname = parts[1];
                String msgContent = parts[2];
                for (ClientHandler client : clients) {
                    if (client.nickname.equals(recipientNickname)) {
                        client.sendMessage(nickname + "(Lichnoe): " + msgContent);
                        logger.info("Отправлено личное сообщение от {} к {}: {}", nickname, recipientNickname, msgContent);
                        break;
                    }
                }
            }
        }

        private void sendUserList() {
            StringBuilder sb = new StringBuilder("Connected clients:\n");
            for (ClientHandler client : clients) {
                sb.append("- ").append(client.nickname).append("\n");
            }
            out.println(sb);
        }

        private void broadcastMessage(String message) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
            logger.info("Широковещательное сообщение от {}: {}", nickname, message);
        }

        private void sendMessage(String message) {
            out.println(message);
        }

        private void closeConnection() {
            clients.remove(this);
            try {
                socket.close();
            } catch (IOException e) {
                logger.warn("Ошибка закрытия соединения: {}", e.getMessage());
            }
            broadcastMessage(nickname + " leave chat.");
            logger.info("{} покинул чат.", nickname);
        }
    }

    public static void main(String[] args) throws IOException {
        new Main().start();
    }
}