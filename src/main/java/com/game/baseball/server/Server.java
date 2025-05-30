package com.game.baseball.server;

import javax.websocket.server.ServerEndpoint;
import javax.websocket.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;


@ServerEndpoint("/game")
public class Server {
    private static Set<Session> sessions = ConcurrentHashMap.newKeySet();
    private static Map<Session, String> userNumbers = new ConcurrentHashMap<>();
    private static Map<Session, Session> opponents = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) throws IOException {
        sessions.add(session);
        System.out.println("접속됨: " + session.getId());

        if (sessions.size() == 2) {
            Iterator<Session> it = sessions.iterator();
            Session player1 = it.next();
            Session player2 = it.next();

            opponents.put(player1, player2);
            opponents.put(player2, player1);

            player1.getBasicRemote().sendText("게임 시작! 숫자 3자리 입력하세요:");
            player2.getBasicRemote().sendText("게임 시작! 숫자 3자리 입력하세요:");
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        if (!userNumbers.containsKey(session)) {
            if (!isValidNumber(message)) {
                session.getBasicRemote().sendText("유효하지 않은 숫자입니다. 서로 다른 3자리 숫자를 입력하세요:");
                return;
            }
            userNumbers.put(session, message);
            session.getBasicRemote().sendText("숫자 저장 완료! 이제 상대방의 숫자를 추측해보세요:");
            return;
        }

        Session opponent = opponents.get(session);
        if (opponent == null || !userNumbers.containsKey(opponent)) {
            session.getBasicRemote().sendText("상대방의 준비가 완료되지 않았습니다. 잠시 기다려주세요.");
            return;
        }

        String target = userNumbers.get(opponent);
        String result = judge(target, message);

        session.getBasicRemote().sendText("결과: " + result);

        if (result.equals("3스트라이크")) {
            session.getBasicRemote().sendText("🎉 승리!");
            opponent.getBasicRemote().sendText("😢 패배! 상대가 정답을 맞췄습니다.");
            session.close();
            opponent.close();
        }
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        userNumbers.remove(session);
        Session opp = opponents.remove(session);
        if (opp != null) opponents.remove(opp);
        System.out.println("연결 종료: " + session.getId());
    }

    private boolean isValidNumber(String num) {
        return num.matches("\\d{3}") && num.chars().distinct().count() == 3;
    }

    private String judge(String answer, String guess) {
        int strike = 0, ball = 0;
        for (int i = 0; i < 3; i++) {
            if (guess.charAt(i) == answer.charAt(i)) strike++;
            else if (answer.contains(String.valueOf(guess.charAt(i)))) ball++;
        }
        return strike + "스트라이크 " + ball + "볼";
    }
}
