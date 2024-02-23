package pl.jbazil;

import lombok.Data;

@Data
public class Request {

    private final Type type;
    private final String key;
    private final String value;

    public enum Type {
        SET,
        GET,
        DELETE,
        COUNT,
        BEGIN,
        ROLLBACK,
        COMMIT
    }
}
