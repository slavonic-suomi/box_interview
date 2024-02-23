package pl.jbazil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DatabaseClient {

    private final Database database;
    public Response execute(String input) {
        Request request = parseRequest(input);
        return database.execute(request);
    }

    private Request parseRequest(String request) {
        //todo add validation
        String[] args = request.trim().split(" ");
        Request.Type type = Request.Type.valueOf(args[0]);
        String key = null;
        String value = null;
        if (args.length > 1) {
            key = args[1];
        }
        if (args.length > 2) {
            value = args[2];
        }
        return new Request(type, key, value);
    }
}
