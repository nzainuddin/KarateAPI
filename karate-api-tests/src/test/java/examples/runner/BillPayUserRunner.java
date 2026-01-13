package examples.runner;

import com.intuit.karate.junit5.Karate;

public class BillPayUserRunner {

    // @BeforeAll
    // public static void setup() {
    //     Dotenv dotenv = Dotenv.load();
    //     System.setProperty("api.username", dotenv.get("demo"));
    //     System.setProperty("api.password", dotenv.get("password123"));
    // }
    @Karate.Test
    public Karate testBPayUser() {
        return Karate.run("classpath:examples/feature/billpay-user.feature");
    }
}
