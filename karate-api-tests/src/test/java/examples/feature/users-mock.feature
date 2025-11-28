Feature: Users API mock tests

  Background:
    # run this feature against a local mock with -Dmock.users=true or -Dkarate.env=local-mock
    * def base = (karate.properties['mock.users'] == 'true' || karate.env == 'local-mock') ? 'http://localhost:3000' : 'https://jsonplaceholder.typicode.com'
    * url base

  Scenario: Mock: Create then update user details (recommended to run with the mock)
    # Run this scenario against the local mock by starting json-server and using
    # `-Dmock.users=true` or `-Dkarate.env=local-mock`. If you don't run the mock,
    # the scenario will exercise the real API endpoint defined in the Background.
    * def newUserDetails = read('classpath:examples/users/user-request.json')
    Given path 'users'
    And request newUserDetails
    When method post
    Then status 201
    * def createdId = response.id
    * print 'createdId:', createdId
    # prepare updated payload
    * def updated = newUserDetails
    * updated.name = 'Mock Updated Name'
    * updated.email = 'mock.updated@example.com'
    Given path 'users', createdId
    And request updated
    When method put
    Then status 200
    And match response contains updated
    # verify via GET
    Given path 'users', createdId
    When method get
    Then status 200
    And match response contains updated
