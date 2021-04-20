import io.javalin.Javalin;
import io.javalin.http.ForbiddenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        Javalin app = Javalin.create().start(Integer.parseInt(System.getenv("PORT")));
        app.get("/", ctx -> ctx.result("Welcome to root"));

        app.get("/ping", ctx -> ctx.result("pong"));

        app.get("/user/:id", ctx -> ctx.result("Hola " + ctx.pathParam("id")));

        app.get("/json", context -> context.json(new HashMap<>()));

        app.get("/forbidden", context ->  { throw new ForbiddenResponse("Off limits!"); });

        app.get("/exception", ctx -> throwError());
        app.exception(Exception.class, (e, ctx) -> {
            log.error("Unhandled error reached controller", e);

            { throw new ForbiddenResponse("Off limits!"); }
        });
    }

    private static void throwError() {
        throw new RuntimeException("Custom error");
    }
}