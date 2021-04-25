import io.javalin.Javalin;
import io.javalin.core.security.Role;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.UnauthorizedResponse;
import login.LoginUtils;
import constants.MyRole;
import login.SessionUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static constants.SessionAttributesKeys.ROLE;
import static constants.SessionAttributesKeys.USERNAME;
import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.core.security.SecurityUtil.roles;
import static constants.MyRole.*;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        Javalin app = Javalin
                .create(config -> {
                    config.sessionHandler(SessionUtils::fileSessionHandler);
                    config.accessManager((handler, ctx, roles) -> {
                        MyRole userRole = ctx.sessionAttribute(ROLE.toString()); // retrieve user stored during login
                        log.info("Role before handling request: {}", userRole);

                        if (roles.isEmpty()) {
                            log.info("No role is required, handling request");
                            handler.handle(ctx);
                        } else if (userRole == null) {
                            log.info("Role is required but session does not exist, logging in and then handling");
                            LoginUtils.login(ctx);
                            handler.handle(ctx);
                        } else if (userHasValidRole(ctx, roles)) {
                            log.info("User role is valid, handling request");
                            handler.handle(ctx);
                        } else {
                            throw new UnauthorizedResponse("User not authorized");
                        }
                    });

                }).start(Integer.parseInt(System.getenv("PORT")));


        // Routes
        app.post("/login", LoginUtils::login);

        app.post("/logout", ctx -> {
            log.info("Logging out user");
            LoginUtils.logout(ctx);
            ctx.status(HttpStatus.NO_CONTENT_204);
        });

        // Example of route group
        app.routes(() -> {
            path("example", () -> {
                get("/", context -> {
                    //code snippet
                });
            });
        });

        app.get("/", ctx -> ctx.redirect("/ping"));

        app.get("/ping", ctx -> ctx.result("pong"));

        // Example of role requirement
        app.get("/user/:id", ctx -> ctx.result("Hola " + ctx.pathParam("id")), roles(USER));

        // Example of JSON response
        app.get("/session", context -> {
            Map<String, Object> response = new HashMap<>();
            response.put("username", context.sessionAttribute(USERNAME.toString()));
            response.put("role", context.sessionAttribute(ROLE.toString()));
            context.json(response);
        }, roles(USER, ADMIN));

        // Example of error response
        app.get("/exception", ctx -> { throw new RuntimeException("Custom error");}); // 500


        // Error handlers

        // Last resource Handler
        app.exception(Exception.class, (e, ctx) -> {
            log.error("Unhandled error reached controller", e);
            { throw new InternalServerErrorResponse("An unknown error occurred"); }
        });
    }

    private static void throwError() {

    }

    private static boolean userHasValidRole(Context ctx, Set<Role> roles) {
        log.info("userHasValidRole");
        if (roles.isEmpty() || roles.contains(ANYONE)) {
            return true;
        } else return roles.contains(ctx.sessionAttribute(ROLE.toString()));// your code here
    }

    private static void redirectToLogin(Context ctx) {
        log.info("Redirecting to login");
        ctx.redirect("http://localhost:5000/login");
    }
}