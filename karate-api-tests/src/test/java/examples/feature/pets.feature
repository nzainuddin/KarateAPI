Feature: PetStore API tests

  Background:
    * url 'https://petstore3.swagger.io/api/v3'

  Scenario: Get pets by status
    Given path 'pet', 'findByStatus'
    And param status = 'available'
    When method get
    Then status 200
    And match response[0] contains { status: 'available' }


    * def first = response[0]
    * print 'First pet ID:', first.id
    * print 'First pet status:', first.status

  #   Given path 'users', first.id
  #   When method get
  #   Then status 200

  # Scenario: create a user and then get it by id
  #   * def user =
  #     """
  #     {
  #       "name": "Test User",
  #       "username": "testuser",
  #       "email": "test@user.com",
  #       "address": {
  #         "street": "Has No Name",
  #         "suite": "Apt. 123",
  #         "city": "Electri",
  #         "zipcode": "54321-6789"
  #       }
  #     }
  #     """

  #   Given url 'https://jsonplaceholder.typicode.com/users'
  #   And request user
  #   When method post
  #   Then status 201

  #   * def id = response.id
  #   * print 'created id is: ', id

  #   Given path id
  #   # When method get
  #   # Then status 200
  #   # And match response contains user


