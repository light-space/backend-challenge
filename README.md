# Approval workflow challenge SOLUTION

This solution is an implementation of a best-in-class invoice approval workflow application. 
The solution enables customers to configure each step of the approval process and define the workflow in terms of a set of rules. 
Each rule is responsible for sending an approval request to the desired company employee based on one or more constraints.

## Features

- Execute the application via command line by passing invoice's amount, department, and whether a manager approval is required as input fields
- Delete the current Workflow
- Add rules to the current Workflow
- Support for two ways to give approval: Slack and Email

## Command Line Executables

The solution consists of three command line executables.
1. `--submit-invoice <amount[Double]> <department[FINANCE|MARKETING] <manager_approval[Boolean]`:
   * Submits an invoice for approval. The invoice gets automatically processed by checking
against each rule in the workflow. A message is sent to the corresponding employee
via the specified contact method.
   * Example: 
     ```sh
       ./gradlew run --args="--submit-invoice 10000 finance false"
     ```
2. `--delete-workflow`:
   * **Deletes the current Workflow**. Destroys all tables and deletes the memory file.
   * Example:
     ```sh 
     ./gradlew run --args="--delete-workflow"
     ```
3. `--add-rule-to-workflow`:
   * Allows the user to create a new rule and insert it into the workflow. 
   The rule will be appended at the end of the workflow and will be checked last.
   In order to create a new rule, the user will be prompted with each valid rule constraint.
   Simply enter a valid constraint value or Skip by pressing Enter.
   If the rule is valid, it gets added to the Workflow and the user
   gets the option to add another rule or finish the program. Otherwise, an error 
   is thrown and the executable stops. The user can add more rules to the
   current Workflow by re-running this command line option.

   * Example:
     ```sh
     ./gradlew run --args="--add-rule-to-workflow"
     ```

If none of these commands is entered, the executable returns and error message 
and lists the available options. If one of these commands fail to execute,
an error message is printed informing of the correct usage.

## Initial Status

The workflow of fig.1 has been inserted into the database prior to handing off the solution
and should be available for testing. These are some suggested tests and the expected results:

```sh
$ ./gradlew run --args="--submit-invoice 7000 marketing false"
Send a message via SLACK to Finance team Member
```
```sh
$ ./gradlew run --args="--submit-invoice 400 finance true"
Send a message via SLACK to Finance team Member
```
```sh
$ ./gradlew run --args="--submit-invoice 8000 marketing true"
Send a message via EMAIL to Finance Manager
```

![code_exercise_diagram (2)](https://user-images.githubusercontent.com/112865589/191920630-6c4e8f8e-a8d9-42c2-b31e-ab2c881ed297.jpg)

Fig. 1: Initial Workflow.

## Database model


## Build and Run

### Run Tests

```sh
$ ./gradlew test
```

### Run Clean Build
```sh
./gradlew clean build
```

### Run Executables
```sh
./gradlew run --args="--option arg1 arg2 ..."
```