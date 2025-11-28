package examples.runner;

import com.intuit.karate.junit5.Karate;

class UsersMockRunner {

    @Karate.Test
    Karate testUsersMock() {
        return Karate.run("classpath:examples/feature/users-mock.feature");
    }
}
