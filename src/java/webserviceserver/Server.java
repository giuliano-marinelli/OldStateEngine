package webserviceserver;

import engine.State;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

//Service Endpoint Interface
@WebService
@SOAPBinding(style = Style.DOCUMENT)
public interface Server {

    //Este metodo devuelve un hash que va a representar a su session.
    @WebMethod
    void receiveAction(String action,String session);

    //Este ataque es para los personajes de rango melee.
    @WebMethod
    void playerEnter(String rol);

    //Este ataque es para los personajes de rango no melee.
    @WebMethod
    void playerExit(String juego);
    
    @WebMethod
    String getFullState();
    
    @WebMethod
    String getState();
    
      


}
