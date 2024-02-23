package pl.jbazil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Database {

    private static class Transaction {
        private final Map<String, String> values = new HashMap<>();
        private final Set<String> removals = new HashSet<>();
    }

    private final ConcurrentMap<String, String> values = new ConcurrentHashMap<>();

    private final List<Transaction> activeTransactions = new LinkedList<>();

    public Response execute(Request request) {
        Response res;
        switch (request.getType()) {
            case SET      -> res = setValue(request.getKey(), request.getValue());
            case GET      -> res = getValue(request.getKey());
            case DELETE   -> res = deleteValue(request.getKey());
            case COUNT    -> res = count(request.getKey());
            case BEGIN    -> res = beginTrx();
            case ROLLBACK -> res = rollbackTrx();
            case COMMIT   -> res = commitTrx();
            default       -> throw new IllegalStateException();
        }
        return res;
    }



    private Response commitTrx() {
        if (activeTransactions.isEmpty()) {
            return Response.error("NO TRANSACTION");
        }
        for (Transaction trx : activeTransactions) {
            for (String removal : trx.removals) {
                values.remove(removal);
            }
            values.putAll(trx.values);
        }

        activeTransactions.clear();

        return Response.none();
    }

    private Response rollbackTrx() {
        if (activeTransactions.isEmpty()) {
            return Response.error("NO TRANSACTION");
        }
        activeTransactions.removeLast();
        return Response.none();
    }

    private Response beginTrx() {
        activeTransactions.add(new Transaction());
        return Response.none();
    }

    private Response setValue(String key, String value) {
        Map<String, String> dataSource;
        if (activeTransactions.isEmpty()) {
            dataSource = values;
        } else {
            dataSource = activeTransactions.getLast().values;
        }
        dataSource.put(key, value);
        return Response.none();
    }

    private Response getValue(String key) {
        if (!activeTransactions.isEmpty()) {
            ListIterator<Transaction> iter = activeTransactions.listIterator(activeTransactions.size());
            while (iter.hasPrevious()) {
                Transaction trx = iter.previous();
                if (trx.removals.contains(key)) {
                    return Response.payload(null);
                }
                if (trx.values.containsKey(key)) {
                    return Response.payload(trx.values.get(key));
                }
            }
        }

        return Response.payload(values.get(key));
    }

    private Response deleteValue(String key) {
        if (!activeTransactions.isEmpty()) {
            Transaction trx = activeTransactions.getLast();
            trx.values.remove(key);
            trx.removals.add(key);
        } else {
            values.remove(key);
        }
        return Response.none();
    }

    // O (n*t)

    // O (log(n))
    private Response count(String value) {
        Set<String> matches = new HashSet<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (entry.getValue().equals(value)) {
                matches.add(entry.getKey());
            }
        }
        for (Transaction trx : activeTransactions) {
            matches.removeIf(trx.removals::contains);

            for (Map.Entry<String, String> entry : trx.values.entrySet()) {
                if (entry.getValue().equals(value)) {
                    matches.add(entry.getKey());
                }
            }
        }

        return Response.payload(String.valueOf(matches.size()));

    }

}
