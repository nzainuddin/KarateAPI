Feature: Pet Store API Tests
  Background:
  * url 'https://petstore3.swagger.io/api/v3'

  Scenario Outline: Get pets by status
    * def petStatusPending =
      """
      {
        "id": 990099,
        "category": {
          "id": 0,
          "name": "string"
        },
        "name": "doggie",
        "photoUrls": [
          "string"
        ],
        "tags": [
          {
            "id": 0,
            "name": "string"
          }
        ],
        "status": "pending"
      }
      """
    * def petStatusSold =
      """
      {
        "id": 990100,
        "category": {
          "id": 0,
          "name": "string"
        },
        "name": "kitty",
        "photoUrls": [
          "string"
        ],
        "tags": [
          {
            "id": 0,
            "name": "string"
          }
        ],
        "status": "sold"
      }
      """
    # Create pets with different statuses
    Given path 'pet'
    And request petStatusPending
    When method post
    Then status 200
    Given path 'pet'
    And request petStatusSold
    When method post
    Then status 200
    Given path 'pet', 'findByStatus'
    And param status = '<status>'
    When method get
    Then status 200
    And match response[0].status == '<status>'
    Examples:
      | status     |
      | available  |
      | pending    |
      | sold       |