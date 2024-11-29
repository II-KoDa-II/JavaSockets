package org.example;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    private static final PropertyLoader PROPERTIES = new PropertyLoader("application.properties");
    private static final String SERVER_HOST = PROPERTIES.getHost();
    private static final int SERVER_PORT = Integer.parseInt(PROPERTIES.getPort());
    private final Scanner scanner = new Scanner(System.in);
    private BufferedReader in;
    private PrintWriter out;

    public void connect() throws IOException {
        Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        System.out.print("Your nickname: ");
        String nickname = scanner.nextLine();
        out.println(nickname); // отправляем никнейм на сервер

        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                System.err.println("Server data error: " + e.getMessage());
            }
        }).start();

        interactWithUser();
    }

    private void interactWithUser() {
        String input;
        do {
            System.out.println("Select message type:");
            System.out.println("(1) to all");
            System.out.println("(2) to someone");
            System.out.println("(3) gel clients list");
            System.out.print(": ");

            input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    sendBroadcastMessage();
                    break;
                case "2":
                    selectRecipientAndSendMessage();
                    break;
                case "3":
                    requestUserList();
                    break;
                default:
                    System.err.println("error choise.");
            }
        } while (!input.equalsIgnoreCase("exit"));
    }

    private void sendBroadcastMessage() {
        System.out.print("enter your message: ");
        String message = scanner.nextLine();
        if (!message.isEmpty()) {
            out.println(message); // широковещательное сообщение
        }
    }

    private void selectRecipientAndSendMessage() {
        requestUserList(); // сначала получаем список пользователей
        System.out.print("nickname your recipient: ");
        String recipient = scanner.nextLine();

        System.out.print("your message: ");
        String message = scanner.nextLine();

        if (recipient != null && !recipient.isEmpty() && message != null && !message.isEmpty()) {
            // формат личного сообщения: /private <ник_получателя> <текст_сообщения>
            out.println("/private " + recipient + " " + message);
        }
    }

    private void requestUserList() {
        out.println("/list"); // запрос списка пользователей
    }

    public static void main(String[] args) throws IOException {
        Main client = new Main();
        client.connect();
    }
}