/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webserviceserver;

import gamelogic.Lobby;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 *
 * @author joan
 */
@Stateless
@Path("/server")
public class ServerImp {

    private Lobby lobby;
    int session;

    @GET
    @Path("/enter")
    public String playerEnter(@QueryParam("rol") String rol) {
        lobby = Lobby.startGame();
        SecureRandom random = new SecureRandom();
        rol = rol + random.nextInt(1000000000);
        //    String session = bytes.toString();
        lobby.addAction(rol, "enter");
        return rol;
    }

    @GET
    @Path("/test")
    public String test() {
        return "prueba";
    }

    @GET
    @Path("/exit")
    public void playerExit(@QueryParam("session") String session) {
        lobby = Lobby.startGame();
        lobby.addAction(session, "leave");
    }

    @GET
    @Path("/action")
    public String receiveAction(@QueryParam("action") String action, @QueryParam("session") String session) {
        System.out.println(action + " del jugador" + session);

        lobby = Lobby.startGame();
        if (action.equalsIgnoreCase("fire")) {
            String fire = "{\"name\": \"fire\", \"priority\": \"1\",\"parameters\": [{\"name\": \"x\", \"value\": \"" + 1 + "\"},{\"name\": \"y\", \"value\": \"" + 1 + "\"}]}";
            System.out.println(fire);
            lobby.addAction(session, fire);

        } else {
            lobby.addAction(session, action);

        }
        return "okey";
    }

    @GET
    @Path("/actionFire")
    public String rangeAtack(@QueryParam("x") String x, @QueryParam("y") String y, @QueryParam("session") String session) {

        lobby = Lobby.startGame();
        String fire = "{\"name\": \"fire\", \"priority\": \"1\",\"parameters\": [{\"name\": \"x\", \"value\": \"" + x + "\"},{\"name\": \"y\", \"value\": \"" + y + "\"}]}";
        System.out.println(fire);
        lobby.addAction(session, fire);
        return "okey";

    }

    @GET
    @Path("/ready")
    public String ready(@QueryParam("session") String session){
        
        lobby = Lobby.startGame();
        lobby.addAction(session, "ready");
        return "okey";
        
    }

    @GET
        @Path("/getFullState")
        public String getFullState() throws InterruptedException {
        lobby = Lobby.startGame();
        String state = "error";
        try {
            state = lobby.getFullState();
        

} catch (InterruptedException ex) {
            Logger.getLogger(ServerImp.class
.getName()).log(Level.SEVERE, null, ex);
        }
        return state;
    }
    
    @GET
        @Path("/getFullStaticState")
        public String getFullStaticState() {
        lobby = Lobby.startGame();
        String state = "error";
        try {
            state = lobby.getStaticState();
        

} catch (InterruptedException ex) {
            Logger.getLogger(ServerImp.class
.getName()).log(Level.SEVERE, null, ex);
        }
        return state;
    }

    @GET
        @Path("/getState")
        public String getState() {
        lobby = Lobby.startGame();
        String state = "error";
        try {
            state = lobby.getState();
        

} catch (InterruptedException ex) {
            Logger.getLogger(ServerImp.class
.getName()).log(Level.SEVERE, null, ex);
        }
        return state;
    }

}
