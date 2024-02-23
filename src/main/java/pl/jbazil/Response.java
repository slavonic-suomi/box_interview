package pl.jbazil;

import lombok.Data;

@Data
public class Response {

    public static Response none() {
        return new Response(Type.NONE, null);
    }

    public static Response error(String message) {
        return new Response(Type.ERROR, message);
    }

    public static Response payload(String payload) {
        if (payload == null) payload = "NULL";
        return new Response(Type.VALUE, payload);
    }

    private final Type type;
    private final String payload;

    public enum Type {
        VALUE,
        ERROR,
        NONE
    }
}
