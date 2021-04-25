package constants;

import io.javalin.core.security.Role;

public enum MyRole implements Role {
    ANYONE, USER, ROLE_TWO, ADMIN;
}
