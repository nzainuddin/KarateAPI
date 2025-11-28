Feature: Users API tests

  Background:
    # allow per-feature mock: pass -Dmock.users=true to run against a local json-server
    # or set `karate.env=local-mock` to achieve the same effect from `karate-config.js`
    * def base = (karate.properties['mock.users'] == 'true' || karate.env == 'local-mock') ? 'http://localhost:3000' : 'https://jsonplaceholder.typicode.com'
    * url base

  Scenario: Get all users and then get the first user by id
    Given path 'users'
    When method get
    Then status 200
    * def first = response[0]
    Given path 'users', first.id
    When method get
    Then status 200

  Scenario: Create user and then get it by id
    * def newUserDetails = read('classpath:examples/users/user-request.json')
    Given path 'users'
    And request newUserDetails
    When method post
    Then status 201
    * print 'New User Response: ', response
    * def newUserId = response.id
    * print 'Created id: ', newUserId
    Given path newUserId
    When method get
    Then status 200
    And match response contains newUserDetails
    
  Scenario: Update user and then get it by id
    * def newUserDetails = read('classpath:examples/users/user-request.json')
    Given path 'users'
    And request newUserDetails
    When method post
    Then status 201
    * print 'New User Response: ', response
    * def newUserId = response.id
    * print 'Created id: ', newUserId
    * def updatedUserDetails = newUserDetails
    # Modify some details
    * updatedUserDetails.name = 'Maisarah Ahmad'
    * updatedUserDetails.email = 'mais.ahm@gmael.com'
    * updatedUserDetails.phone = '012-3336789'
    * updatedUserDetails.website = 'maisarahahmad.com'
    * print 'Updated Details: ', updatedUserDetails
    Given path 'users', newUserId
    And request updatedUserDetails
    When method put
    Then status 200
    * print 'Update Response: ', response
    Given path newUserId
    When method get
    Then status 200
    And match response contains updatedUserDetails
    


