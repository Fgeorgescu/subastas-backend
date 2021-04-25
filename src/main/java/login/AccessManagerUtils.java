package login;

import io.javalin.core.JavalinConfig;
import io.javalin.core.security.AccessManager;
import io.javalin.core.security.Role;
import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.util.Set;
import java.util.function.Consumer;

public class AccessManagerUtils {

    public static AccessManager accessManager(Handler handler, Context context, Set<Role> permittedRoles) {
        return null;
    }
}
