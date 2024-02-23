package pl.jbazil;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseClientTest {

    @RequiredArgsConstructor
    static class DBTester {
        private final DatabaseClient client;

        void expectNone(String command) {
            Response response = client.execute(command);
            assertEquals(Response.none(), response);
        }

        void expectResponse(String command, String value) {
            Response response = client.execute(command);
            assertEquals(value, response.getPayload());
        }

        void expectError(String command, String message) {
            Response response = client.execute(command);
            assertEquals(message, response.getPayload());
        }

    }


    @Test
    void basicTest() {
        Database database = new Database();
        DatabaseClient client = new DatabaseClient(database);
        DBTester tester = new DBTester(client);


        tester.expectResponse("GET a", "NULL");
        tester.expectNone("SET a 10");
        tester.expectResponse("GET a", "10");
        tester.expectNone("DELETE a");
        tester.expectResponse("GET a", "NULL");

    }

    @Test
    void countTest() {
        Database database = new Database();
        DatabaseClient client = new DatabaseClient(database);
        DBTester tester = new DBTester(client);

        tester.expectNone("SET a 10");
        tester.expectNone("SET b 10");
        tester.expectResponse("COUNT 10", "2");
        tester.expectResponse("COUNT 20", "0");

        tester.expectNone("DELETE a");
        tester.expectResponse("COUNT 10", "1");

        tester.expectNone("SET b 30");
        tester.expectResponse("COUNT 10", "0");
    }

    @Test
    void rollbackTrxTest() {
        Database database = new Database();
        DatabaseClient client = new DatabaseClient(database);
        DBTester tester = new DBTester(client);

        tester.expectNone("BEGIN");
        tester.expectNone("SET a 10");
        tester.expectResponse("GET a", "10");
        tester.expectNone("BEGIN");
        tester.expectNone("SET a 20");
        tester.expectResponse("GET a", "20");
        tester.expectNone("ROLLBACK");
        tester.expectResponse("GET a", "10");
        tester.expectNone("ROLLBACK");
        tester.expectResponse("GET a", "NULL");
    }

    @Test
    void commitTrxTest() {
        Database database = new Database();
        DatabaseClient client = new DatabaseClient(database);
        DBTester tester = new DBTester(client);

        tester.expectNone("BEGIN");
        tester.expectNone("SET a 30");
        tester.expectResponse("GET a", "30");
        tester.expectNone("BEGIN");
        tester.expectNone("SET a 40");
        tester.expectResponse("GET a", "40");
        tester.expectNone("COMMIT");
        tester.expectResponse("GET a", "40");
        tester.expectError("ROLLBACK", "NO TRANSACTION");
    }

    @Test
    void deleteWithRollbackTest() {
        Database database = new Database();
        DatabaseClient client = new DatabaseClient(database);
        DBTester tester = new DBTester(client);

        tester.expectNone("SET a 50");
        tester.expectNone("BEGIN");
        tester.expectResponse("GET a", "50");
        tester.expectNone("SET a 60");
        tester.expectNone("BEGIN");
        tester.expectNone("DELETE a");
        tester.expectResponse("GET a", "NULL");
        tester.expectNone("ROLLBACK");
        tester.expectResponse("GET a", "60");
        tester.expectNone("COMMIT");
        tester.expectResponse("GET a", "60");
    }

    @Test
    void trxContTest() {
        Database database = new Database();
        DatabaseClient client = new DatabaseClient(database);
        DBTester tester = new DBTester(client);

        tester.expectNone("SET a 10");
        tester.expectNone("BEGIN");
        tester.expectResponse("COUNT 10", "1");
        tester.expectNone("BEGIN");
        tester.expectNone("DELETE a");
        tester.expectResponse("COUNT 10", "0");
        tester.expectNone("ROLLBACK");
        tester.expectResponse("COUNT 10", "1");




    }

}