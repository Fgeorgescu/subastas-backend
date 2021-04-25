package login;

import constants.MyRole;
import constants.SessionAttributesKeys;
import io.javalin.core.security.BasicAuthCredentials;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static constants.MyRole.*;
import static constants.SessionAttributesKeys.ROLE;
import static constants.SessionAttributesKeys.USERNAME;

public class LoginUtils {
    private static final Logger log = LoggerFactory.getLogger(LoginUtils.class);


    private static final Map<String, MyRole> testUsers;
    private static final String MASTER_PASSWORD = "test";
    private static final String ROLE = SessionAttributesKeys.ROLE.toString();

    static {
        testUsers = Map.of("ADMIN", ADMIN, "USER", USER, "FGEORGESCU", ADMIN);
    }



    public static void login(Context context) {
        if (context.sessionAttribute(USERNAME.toString()) != null) {
            // Desde la app no deberíamos hacer el login si ya existe la sesión
            logout(context);
        }

        try {
            final String username = context.basicAuthCredentials().getUsername();
            final String password = context.basicAuthCredentials().getPassword();

            if (testUsers.containsKey(username.toUpperCase()) && MASTER_PASSWORD.equalsIgnoreCase(password)) {
                context.sessionAttribute(ROLE, testUsers.getOrDefault(username.toUpperCase(), ANYONE));
                context.sessionAttribute(USERNAME.toString(), username);

            } else if (credentialsAreCorrect(context.basicAuthCredentials())) {
                context.sessionAttribute(ROLE, getUserRole(username));
                context.sessionAttribute(USERNAME.toString(), username);

            } else {
                throw new UnauthorizedResponse("Invalid username and password combination");

            }
        } catch (IllegalArgumentException e) {
            log.error("No auth headers", e);
            throw new UnauthorizedResponse("There was a problem logging in. Please check Authorization headers");
        }
    }

    public static void logout(Context context) {
        context.req.getSession().invalidate();
    }

    private static boolean credentialsAreCorrect(BasicAuthCredentials creds) {
        //Acá validaríamos contra la DB de usuarios
        return true;
    }

    private static MyRole getUserRole(String username) {
        log.info("Setting role {} for user {}", ANYONE, username);
        return ANYONE;
    }
}
