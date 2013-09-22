package com.jug6ernaut.network.authenticator.server;

import com.jug6ernaut.network.authenticator.server.auth.SecurityFilter;
import com.jug6ernaut.network.authenticator.server.auth.UserContext;
import com.jug6ernaut.network.authenticator.server.dao.CredentialBodyHandler;
import com.jug6ernaut.network.authenticator.server.dao.DAO;
import com.jug6ernaut.network.authenticator.server.dao.GsonMessageBodyHandler;
import com.jug6ernaut.network.authenticator.server.dao.Session;
import com.jug6ernaut.network.authenticator.server.endpoints.AuthenticationEndpoint;
import com.jug6ernaut.network.authenticator.server.endpoints.DataEndpoint;
import com.jug6ernaut.network.authenticator.server.endpoints.PushEndpoint;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: williamwebb
 * Date: 8/19/13
 * Time: 5:39 PM
 */
public abstract class AuthApplication<T extends DAO> extends ResourceConfig {

    private static final Logger logger = Logger.getLogger(AuthApplication.class.getSimpleName());

//    private static final ListenerManager EVENT_NOTIFIER = new ListenerManager();

    @Inject
    public AuthApplication(ServiceLocator serviceLocator){

        reg(AuthenticationEndpoint.class);
        reg(DataEndpoint.class);
        reg(PushEndpoint.class);
        reg(CredentialBodyHandler.class);  // insures passwords are not sent back
        reg(GsonMessageBodyHandler.class); // serialize all objects with GSON
//        reg(UserContext.class);
        reg(SecurityFilter.class);
        reg(Session.class);
//        reg(ListenerManager.class);

        DynamicConfiguration dc = Injections.getConfiguration(serviceLocator);
        bind(dc,getDAO());

        property("jersey.config.workers.legacyOrdering",true);
    }

    public abstract Class<T> getDAO();

//    public void addEventNotifier(ListenerManager.Listener<?> listener){
//        EVENT_NOTIFIER.addListener(listener);
//    }

    private void reg(Class<?> clazz){
        //logger.info("Registering: " + clazz.getSimpleName());
        this.register(clazz);
    }

    public void bind(DynamicConfiguration dc, Class<T> daoClass){

        try {
            T t = (T) Class.forName(daoClass.getName()).newInstance();
            Injections.addBinding(
                    Injections.newBinder(t).to(DAO.class),
                    dc);
        } catch (Exception e) {
            logger.severe("Failed to register DAO");
        }
        try {
            Injections.addBinding(
                    Injections.newBinder(UserContext.class).to(SecurityContext.class).in(RequestScoped.class),
                    dc);
        } catch (Exception e) {
            logger.severe("Failed to register UserContext");
        }
        try {
            Injections.addBinding(
                    Injections.newBinder(Session.class).to(Session.class),
                    dc);
        } catch (Exception e) {
            logger.severe("Failed to register UserContext");
        }
//        try {
//            Injections.addBinding(
//                    Injections.newBinder(EVENT_NOTIFIER).to(ListenerManager.class),
//                    dc);
//        } catch (Exception e) {
//            logger.severe("Failed to register ListenerManager");
//        }

        // commits changes
        dc.commit();
    }


    private void isReg(Object o){
        logger.info("isRegistered("+o.getClass().getSimpleName()+"): " + isRegistered(o));
    }

    private void isReg(Class<?> clazz){
        logger.info("isRegistered("+clazz.getSimpleName()+"): " + isRegistered(clazz));
    }

}
