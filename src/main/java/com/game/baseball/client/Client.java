package com.game.baseball.client;

import javax.websocket.*;
import java.net.URI;
import java.util.Scanner;

@ClientEndpoint
public class Client {
    private static Session session;

    @OnOpen
    public void onOpen(Session session) {
        Client.session = session;
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("[서버 응답] " + message);
    }

    public static void main(String[] args) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(Client.class, URI.create("ws://localhost:8025/game"));

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("입력: ");
            String input = scanner.nextLine();
            session.getBasicRemote().sendText(input);
        }
    }

}
