# CS4244 Project 1 - SAT Solver
This is a project repository for CS4244 Project 1.

## Project Structure
- <b>`src`</b><br>
This directory consists of all the source code of the SAT Solver.
- <b>`input`</b><br>
This directory consists of all the input files that are used to test the SAT Solver.

## FAQ
<b>1. What version of `Java` is used?</b><br>
This project is done using `Java` version `9`.

<b>2. How to run the `SATSolver`?</b><br>
If you're using IDE like `IntelliJ`, you have to go to `Run > Edit Configurations` to add a new `Application` 
and checked the option on `Redirect Input From` and choose the `CNF` file that you would like to run with. 
Then, now, you can click on `run`.<br><br>
If you're not using IDE, you could just run the command `javac *.java` from the Directory `src` in your `terminal` to 
compile the `java` files.Then, you could run the following command `java SATSolver < <INPUT_FILE>`. 
Eg: `java SATSolver < input/example1.cnf`.
