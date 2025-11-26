package examples.runner;

import com.intuit.karate.junit5.Karate;

class EmployeeRunner {

    @Karate.Test
    Karate testEmployee() {
        return Karate.run("classpath:examples/feature/employee.feature");
    }
}
