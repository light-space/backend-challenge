# Approval workflow challenge
At Light, we want to implement the best in class invoice approval workflow application.
Every time one of our customer receives an invoice from a vendor, an approval request is sent to one more employees (approvers).

Our customers will configure each step and define how the workflow looks like for them. 

One possible interpretation (out of many!) of a workflow is to look at it as a set of rules.
Each rule can be responsible to send an approval request to the company's desired employee based on one or more constraints.

The decision making about whom to send the approval request can only be based of:
- the invoice amount 
- department the invoice is sent to
- whether the invoice requires manager approval

It could be all of these items, or any subset within them.

**Example of a rule:**

Send an approval request to the marketing team manager if the following constraints are true:

- the invoice is related to Marketing team expenses
- the invoice's amount is between 5000 and 10000 USD

**To successfully complete this coding challenge, the candidate should:**

- provide the database model to support the workflow configuration and execution (a jpeg of the database schema can be put in the README file)
- Note: it is NOT required to implement the database layer in the code; everything can be done in memory
- implement an application that is able to simulate the execution of the workflow
- ensure the application supports two ways to give an approval:
    - Slack
    - Email
    - both of these channels should be mocked, i.e. `println("sending approval via Slack")` is all that is needed
- it should be possible to run the application via CLI, by passing invoice amount, department and if a manager approval is required as input fields. 

Few requirements to consider about the input fields:
    - amount are expressed in USD (no need to support multiple currencies, or even introduce a concept of a currency)
    - the departments to be supported are **Finance** and **Marketing**
- insert the workflow in fig.1 into the database before the solution is handed off

![code_exercise_diagram (2)](https://user-images.githubusercontent.com/112865589/191920630-6c4e8f8e-a8d9-42c2-b31e-ab2c881ed297.jpg)

Fig. 1

While designing and implementing the solution the candidate must consider the following assumptions:

1. Each company will be able to define **only** one workflow. Each new invoice will go through that workflow.
2. A company should be able to modify their workflow at any point, e.g. when the **current** version of the workflow is in action.

### We are providing a basic framework and libraries with the challenge, as well as the placeholders in the code for candidates to fill. However, both is just a suggestion, and you're welcome to use a different structure!

### How to build & run
```sh
./gradlew clean build
./gradlew run
```
