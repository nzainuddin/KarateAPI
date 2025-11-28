package examples.runner;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.File;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class UsersMockServerRunner {

    private static HttpServer server;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static JsonSchema userSchema = null;
    private static List<Map<String, Object>> users = new ArrayList<>();
    private static final File persistFile = new File("target/mock-users.json");
    private static final AtomicInteger nextId = new AtomicInteger(1);

    @BeforeAll
    public static void startMock() throws Exception {
        // ensure feature picks up local mock base
        System.setProperty("mock.users", "true");
        System.setProperty("karate.env", "local-mock");

        // load persisted file if present, otherwise seed from resources
        if (persistFile.exists()) {
            users = mapper.readValue(persistFile, new TypeReference<List<Map<String, Object>>>() {});
        } else {
            InputStream is = UsersMockServerRunner.class.getResourceAsStream("/examples/mock-data/users-list.json");
            if (is != null) {
                users = mapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
            }
        }

        // compute nextId
        int max = 0;
        for (Map<String, Object> u : users) {
            Object idObj = u.get("id");
            if (idObj != null) {
                try { int id = Integer.parseInt(idObj.toString()); if (id > max) max = id; } catch (Exception ignored) {}
            }
        }
        nextId.set(max + 1);

        // load JSON Schema for payload validation (optional). Uses networknt validator.
        InputStream sis = UsersMockServerRunner.class.getResourceAsStream("/examples/schemas/user-schema.json");
        if (sis != null) {
            try (InputStream schemaIs = sis) {
                JsonNode schemaNode = mapper.readTree(schemaIs);
                JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
                userSchema = factory.getSchema(schemaNode);
            } catch (Exception e) {
                System.err.println("Failed to load user schema: " + e.getMessage());
                userSchema = null;
            }
        }

        server = HttpServer.create(new InetSocketAddress(3000), 0);
        server.createContext("/users", new UsersHandler());
        server.createContext("/users/", new UsersHandler());
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
        System.out.println("Embedded mock server started on port 3000");
    }

    @AfterAll
    public static void stopMock() {
        if (server != null) {
            server.stop(0);
            System.out.println("Embedded mock server stopped");
        }
    }

    @Karate.Test
    Karate testUsersMock() {
        return Karate.run("classpath:examples/feature/users-mock.feature");
    }

    static class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();
                String[] segs = path.split("/");
                exchange.getResponseHeaders().add("Content-Type", "application/json");

                if (segs.length >= 3 && !segs[2].isEmpty()) {
                    // /users/{id}
                    int id = parseId(segs[2]);
                    if ("GET".equalsIgnoreCase(method)) {
                        Map<String,Object> user = findUserById(id);
                        if (user != null) send(exchange,200, mapper.writeValueAsString(user)); else send(exchange,404,"{}");
                        return;
                    } else if ("PUT".equalsIgnoreCase(method)) {
                        Map<String,Object> payload = mapper.readValue(exchange.getRequestBody(), new TypeReference<Map<String,Object>>(){ });
                        Map<String,Object> existing = findUserById(id);
                        if (existing != null) {
                            // schema validation if available
                            if (userSchema != null) {
                                JsonNode node = mapper.valueToTree(payload);
                                java.util.Set<ValidationMessage> errs = userSchema.validate(node);
                                if (errs != null && !errs.isEmpty()) {
                                    send(exchange,400, mapper.writeValueAsString(Map.of("error","schema validation failed","details", errs.stream().map(ValidationMessage::getMessage).toArray())));
                                    return;
                                }
                            } else {
                                String missing = validatePayload(payload);
                                if (missing != null) { send(exchange,400, mapper.writeValueAsString(Map.of("error","missing field: "+missing))); return; }
                            }
                            payload.put("id", id);
                            replaceUser(id, payload);
                            persist();
                            send(exchange,200, mapper.writeValueAsString(payload));
                        } else {
                            send(exchange,404,"{}");
                        }
                        return;
                    } else if ("PATCH".equalsIgnoreCase(method)) {
                        Map<String,Object> payload = mapper.readValue(exchange.getRequestBody(), new TypeReference<Map<String,Object>>(){ });
                        Map<String,Object> existing = findUserById(id);
                        if (existing != null) {
                            existing.putAll(payload);
                            persist();
                            send(exchange,200, mapper.writeValueAsString(existing));
                        } else {
                            send(exchange,404,"{}");
                        }
                        return;
                    } else if ("DELETE".equalsIgnoreCase(method)) {
                        boolean removed = removeUser(id);
                        if (removed) { persist(); send(exchange,200, "{}"); } else send(exchange,404,"{}");
                        return;
                    }
                } else {
                    // /users
                    if ("GET".equalsIgnoreCase(method)) {
                        // support query params, e.g. /users?username=Bret or /users?name=Leanne
                        String query = exchange.getRequestURI().getQuery();
                        List<Map<String,Object>> result = users;
                        if (query != null && !query.isEmpty()) {
                            // parse a single or multiple query params (key=value&...)
                            Map<String,String> params = java.util.Arrays.stream(query.split("&"))
                                    .map(s -> s.split("=",2))
                                    .filter(arr -> arr.length==2)
                                    .collect(Collectors.toMap(a->a[0], a->a[1], (a,b)->b));
                            // filter users by all params (contains match)
                            result = users.stream().filter(u -> {
                                for (Map.Entry<String,String> e : params.entrySet()) {
                                    Object v = u.get(e.getKey());
                                    if (v==null) return false;
                                    if (!v.toString().toLowerCase().contains(e.getValue().toLowerCase())) return false;
                                }
                                return true;
                            }).collect(Collectors.toList());
                        }
                        send(exchange,200, mapper.writeValueAsString(result));
                        return;
                    } else if ("POST".equalsIgnoreCase(method)) {
                        Map<String,Object> payload = mapper.readValue(exchange.getRequestBody(), new TypeReference<Map<String,Object>>(){ });
                        // prefer schema validation if provided
                        if (userSchema != null) {
                            JsonNode node = mapper.valueToTree(payload);
                            java.util.Set<ValidationMessage> errs = userSchema.validate(node);
                            if (errs != null && !errs.isEmpty()) {
                                send(exchange,400, mapper.writeValueAsString(Map.of("error","schema validation failed","details", errs.stream().map(ValidationMessage::getMessage).toArray())));
                                return;
                            }
                        } else {
                            String missing = validatePayload(payload);
                            if (missing != null) { send(exchange,400, mapper.writeValueAsString(Map.of("error","missing field: "+missing))); return; }
                        }
                        int id = nextId.getAndIncrement();
                        payload.put("id", id);
                        users.add(payload);
                        persist();
                        send(exchange,201, mapper.writeValueAsString(payload));
                        return;
                    }
                }

                send(exchange,404,"{}");
            } catch (Exception e) {
                try { send(exchange,500, "{}"); } catch (Exception ignored) {}
            }
        }

        private int parseId(String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException e) { return -1; }
        }

        private Map<String,Object> findUserById(int id) {
            for (Map<String,Object> u : users) {
                Object idObj = u.get("id");
                if (idObj != null) {
                    try { if (Integer.parseInt(idObj.toString()) == id) return u; } catch (NumberFormatException ignored) {}
                }
            }
            return null;
        }

        private void replaceUser(int id, Map<String,Object> payload) {
            for (int i=0;i<users.size();i++) {
                Object idObj = users.get(i).get("id");
                if (idObj != null) {
                    try {
                        if (Integer.parseInt(idObj.toString()) == id) { users.set(i, payload); return; }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        private boolean removeUser(int id) {
            for (int i=0;i<users.size();i++) {
                Object idObj = users.get(i).get("id");
                if (idObj != null) {
                    try {
                        if (Integer.parseInt(idObj.toString()) == id) { users.remove(i); return true; }
                    } catch (NumberFormatException ignored) {}
                }
            }
            return false;
        }

        private String validatePayload(Map<String,Object> payload) {
            if (payload == null) return "payload";
            String[] required = new String[]{"name","username","email"};
            for (String r : required) {
                Object v = payload.get(r);
                if (v == null || v.toString().trim().isEmpty()) return r;
            }
            return null;
        }

        private void persist() {
            try { mapper.writerWithDefaultPrettyPrinter().writeValue(persistFile, users); } catch (java.io.IOException ignored) {}
        }

        private void send(HttpExchange exchange, int status, String body) throws Exception {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (var os = exchange.getResponseBody()) { os.write(bytes); }
        }
    }
}
