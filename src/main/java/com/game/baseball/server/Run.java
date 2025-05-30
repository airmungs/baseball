package com.game.baseball.server;

import org.glassfish.tyrus.server.Server;

import java.util.Scanner;

public class Run {
    public static void main(String[] args) {
        Server server = new Server("localhost", 8025, "/", null, com.game.baseball.server.Server.class);
        try {
            server.start();
            System.out.println("서버 실행 중 (ws://localhost:8025/game)");
            new Scanner(System.in).nextLine(); // 엔터 치면 종료
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.stop();
        }
    }
}
