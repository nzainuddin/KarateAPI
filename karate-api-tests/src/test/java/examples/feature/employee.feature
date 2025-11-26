Feature: Employee API tests

  Background:
    * url 'https://dummy.restapiexample.com/api/v1'

    Scenario: Get all employee
        Given path 'employees'
        When method get
        Then status 200
        And assert response.status == 'success'
        And assert response.data.length > 0
        And print response.data[0].employee_name

    Scenario: Get single employee by ID
        Given path 'employee', '1'
        When method get
        Then status 200
        And assert response.status == 'success'
        And assert response.data.id == 1
        And print response.data.employee_name

    Scenario: Create a new employee
        * def newEmployee =
          """
          {
            "name": "Sarimah Ibrahim",
            "salary": "5000",
            "age": "29"
          }
          """
        Given path 'create'
        And request newEmployee
        When method post
        Then status 200
        And assert response.status == 'success'
        And assert response.data.name == 'Sarimah Ibrahim'
        And print 'Created Employee ID:', response.data.id

    Scenario: Update an existing employee
        * def newEmployee =
            """
            {
            "name": "Liyana Jasmay",
            "salary": "4000",
            "age": "29"
            }
            """
        Given path 'create'
        And request newEmployee
        When method post
        Then status 200
        * def createdId = response.data.id
        * def updatedEmployee =
          """
          {
            "name": "Lyana Jasmine",
            "salary": "6000",
            "age": "30"
          }
          """
        Given path 'update', 'createdId'
        And request updatedEmployee
        When method put
        Then status 200
        And assert response.status == 'success'
        And assert response.data.name == 'Lyana Jasmine'
        And assert response.data.salary == '6000'
        And assert response.data.age == '30'
        And assert response.data.id == createdId

    Scenario: Delete an employee
        * def newEmployee =
          """
          {
            "name": "Aiman Hakim",
            "salary": "4500",
            "age": "28"
          }
          """
        Given path 'create'
        And request newEmployee 
        When method post
        Then status 200
        * def createdId = response.data.id
        Given path 'delete', 'createdId'
        When method delete
        Then status 200
        And assert response.status == 'success'
        And assert response.data == 'successfully! deleted Records'
        And print 'Deleted Employee ID:', createdId