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


# Solution

### How to build & run

* Build project
```sh
  ./gradlew clean build
```
* Run project with default workflow & defualt invoice
```sh
  ./gradlew run
```

* Run project with default workflow but particular invoice
```sh
  ./gradlew run --args <path-to-file>/invoice.json
```
Invoice format
```json
{
  "id": {
    "id": "f6fad694-f39a-4043-81aa-48a7ec2250fc"
  },
  "companyId": {
    "id": "8c509ee1-8457-4884-a7b4-fc01776dd57c"
  },
  "status": "PENDING",
  "amount": {
    "value": 10001.00,
    "currency": "USD"
  },
  "department": "MARKETING",
  "requitesApproval": true
}
```


* Run project with particular workflow and invoice
```sh
  ./gradlew run --args='<path-to-file>/invoice.json <path-to-file>/workflow.json'
```
Workflow step format (you need to provide and array of them)
```json
{
    "id": {
      "id": "2e7858c8-0925-4ee5-b39c-32cf4a397997"
    },
    "workflowRule": {
      "companyId": {
        "id": "8c509ee1-8457-4884-a7b4-fc01776dd57c"
      },
      "type": "INVOICE_AMOUNT",
      "params": {
        "className": "org.light.challenge.data.models.InvoiceAmount",
        "lowerAmountLimit": {
          "value": 10000.00,
          "currency": "USD"
        },
        "lowerAmountLimitInclusive": false,
        "upperAmountLimit": null,
        "upperAmountLimitInclusive": null
      },
      "onSuccess": {
        "id": "62ae6db0-e2bc-444a-91a8-78eec5506f45"
      },
      "onFailure": {
        "id": "3a24e56d-d32b-42ef-8121-097b5870417b"
      },
      "startStep": true
    }
  }
```

There is also a programtic way of defining the workflow. In order to be able to use it you have to modify the
main of the app to use `storeExampleWorkflow(companyId: CompanyId)` function in line 62 of `App.kt`.

Default invoice and workflow can be found under `challenge-app` resources folder. 

## Implementation decisions

### Workflow rule parameters
I've decided to store them in the same table leveraging polymorphism and json because having separate them in 
separate tables could be areal mess. Still, this decision was made under current scenario, if the scenario changes
this decision should be revisited.
Eg: if at some point is required to do filtered searches over. In this scenario would make sense to have 
db columns to improve the performance of the search and the usage of DB resources.

### Use recursion
I've decided to use recursion because the problem is stated in a recursive way by nature. 
Would I do the same for a production use case?
Before going with a recursive algorithm I would consider the dept it can reach. For this case the customer
can define the workflow so, some security mechanism should be set in place to avoid unpleasant outcomes.

### Rest Api
I did not implement it, but to allow customers to define workflows and modify them a Rest API would be needed.

### Notification integrations & workflow step
I took notification integrations as business case and only log them. Even though this sounds naif I wanted to mention it.
From my understanding this integrations are not so different from each other that is why I hide them behind a single step 
with a dependency that will take care of the actual integration. If the parameters or something else changes, 
this decision might need to be revisited.

### Property workflow step
Sounded to me that what we needed to check was the value of a particular property, that is why I created a single step
and delegated the actual functionality of knowing how to get that information from the invoice to the configuration of 
the property.

### Integration tests
I did not create them because of lack of time but for a production ready code I think it is mandatory because they will 
not only ensure the code works, they will also ensure that the code will continue working over the changes that will come. 


