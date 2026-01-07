Feature: Pet Store API Tests
  Background:
  * url 'https://petstore3.swagger.io/api/v3'

  Scenario Outline: Get pets by status
    * def petStatusPending = read('classpath:examples/pets/pet-pending.json')
    * def petStatusSold = read('classpath:examples/pets/pet-sold.json')
    # Create pets with different statuses
    * print petStatusPending
    * print petStatusSold
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

  Scenario: Delete created pets
  * def petStatusAvailable =
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
        "status": "available"
      }
      """
    # Ensure pets exist before deletion
    Given path 'pet'
    And request petStatusAvailable
    When method post
    Then status 200
    * def createdPetId = response.id
    # Delete the created pets
    Given path 'pet', createdPetId
    When method delete
    Then status 200
  
  Scenario: Update pet information
    * def newPet =
      """
      {
        "id": 99009933,
        "category": {
          "id": 0,
          "name": "string"
        },
        "name": "Cattie",
        "photoUrls": [
          "string"
        ],
        "tags": [
          {
            "id": 0,
            "name": "string"
          }
        ],
        "status": "available"
      }
      """
    * def updatedPetInfo =
      """
      {
        "id": 99009933,
        "category": {
          "id": 0,
          "name": "string"
        },
        "name": "Cattelina",
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
    # Create a pet to update
    Given path 'pet'
    And request newPet
    When method post
    Then status 200
    # Update the pet's information
    Given path 'pet'
    And request updatedPetInfo
    When method put
    Then status 200
    And match response.name == updatedPetInfo.name
    And match response.status == updatedPetInfo.status