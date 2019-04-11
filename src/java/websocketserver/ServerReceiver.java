package websocketserver;

import engine.Lobby;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ApplicationScoped
@ServerEndpoint("/GameWebSocket")
public class ServerReceiver {

    //private Set<Session> sessions = new HashSet<>();
    private Lobby lobby;

    @OnOpen
    public void playerEnter(Session session) throws InterruptedException {
        //para cada jugador que ingresa al juego crea un hilo que 
        //le enviara los nuevos estados del juego
        System.out.println("Player " + session.getId() + " entered the game.");
        //sessions.add(session);
        lobby = Lobby.startGame();
        lobby.addPlayer(session.getId());
        lobby.addAction(session.getId(), "enter");
        ServerSender serverSender = new ServerSender(session, lobby);
        Thread threadServerSender = new Thread(serverSender);
        threadServerSender.start();

    }

    @OnMessage
    public void recieveAction(String action, Session session) {
        //cada vez recive una accion de un jugador la agrega 
        //al buffer de acciones del juego
        //System.out.println("Player " + session.getId() + " send action: " + action);
        lobby.addAction(session.getId(), action);
    }

    @OnClose
    public void playerExit(Session session) {
        System.out.println("Player " + session.getId() + " leave the game.");
        lobby.removePlayer(session.getId());
        lobby.addAction(session.getId(), "leave");
        //sessions.remove(session);
    }

    @OnError
    public void onError(Throwable error) {
        System.out.println(error.getMessage());
        error.printStackTrace();
    }
}
