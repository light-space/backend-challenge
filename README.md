# Approval workflow challenge
At Light we want to implement the best in class invoice approval workflow application.
Every time one of our customer receives an invoice from a vendor, an approval request must be sent to the right employee.

Our customers will configure each step and define how the workflow will look like. A workflow can be seen as a set of rules.
Each rule will be responsible to send an approval request to the desired company's employee based on one or more constraints.
The constraints within each rule can only be based on:
- the invoice's amount 
- department the invoice is sent to
- whether the invoice requires manager approval

**Example of a rule:**

Send an approval request to the marketing team manager if the following constraints are true:

- the invoice is related to Marketing team expenses
- the invoice's amount is between 5000 and 10000 USD

**To successfully complete this coding challenge, the candidate must:**

- provide the database model to support the workflow configuration and execution (a jpeg of the database schema can be put in the README file)
- implement an application that is able to simulate the execution of the workflow based on the workflow inserted in the DB
- ensure the application supports two ways to give an approval:
    - Slack
    - Email
- provide the possibility to execute the application via command line, by passing invoice's amount, department and if a manager approval is required as input fields. Few requirements to consider about the input fields:
    - amount are expressed in USD
    - the departments to be supported are **Finance** and **Marketing**
- use preferably an in-memory database, unless the candidates wish to host a database of their choice
- insert the workflow in fig.1 into the database before the solution is handed off

![code_exercise_diagram (2)](https://user-images.githubusercontent.com/112865589/191920630-6c4e8f8e-a8d9-42c2-b31e-ab2c881ed297.jpg)

Fig. 1

While designing and implementing the solution the candidate must consider the following assumptions:

1. Each company will be able to define **only** one workflow. Each new invoice will go through that workflow.
2. A company should be able to modify their workflow at any point.

### We are providing a basic framework and libraries with the challenge, as well as the placeholders in the code for candidates to fill. However, both is just a suggestion, and candidates are welcome to try a different setup as well!

### How to build & run
```sh
./gradlew clean build
./gradlew run
```
