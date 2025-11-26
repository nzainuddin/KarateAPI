package examples.runner;

import com.intuit.karate.junit5.Karate;

class PetsRunner {

    @Karate.Test
    Karate testPets() {
        return Karate.run("classpath:examples/feature/pets.feature");
    }
}
