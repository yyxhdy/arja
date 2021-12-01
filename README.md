# ARJA-e: A System for Better Evolutionary Program Repair

ARJA-e is a new evolutionary repair system for Java. Compared to its 
predecessor ARJA (https://github.com/yyxhdy/arja), ARJA-e includes the following new features:
1. ARJA-e exploits a number of repair templates in addition to just exploiting statement-level 
redundancy assumption. So besides complex statement-level transformations,
ARJA-e can also conduct more targeted transformations (e.g., null
pointer check) or finer-grained transformations than statement-level (e.g., method name replacement), thereby making it 
have potential to fix more bugs. 
2. In ARJA-e, the statements
for replacement and statements for insertion are distinguished, and two context-related metrics
are introduced in order to select the most promising replacement and insertion statements respectively.
3. In ARJA-e, the repair templates is used in a novel way. That is, various template-based edits
(usually occurring at the expression level) are converted into two types of statement-level edits, so that all
kinds of edits can be decomposed into the same partial information, making it possible to
encode patches with a unified lower-granularity representation.
4. A new lower-granularity patch representation is introduced in ARJA-e, which is characterized by the
decoupling of statements for replacement and statements for insertion. So GP can 
evolve the two kinds of statements separately.
5. ARJA-e incorporates a finer-grained fitness function that can capture how close a program variant
satisfies each assertion in the unit test cases, which is expected to provide smoother gradients
for GP to traverse to find a solution.
6. ARJA-e includes a post-processing tool that can help to alleviate patch overfitting problem. 


## Requirements
1. Java JDK 1.7
2. Mac OS X or Linux

## How to Run

### Set Up

First, clone ARJA-e to the local computer:
```
$ git clone -b arja-e https://github.com/yyxhdy/arja
```
There are four subdirectories in the root directory of this system:
1. ../arja-e/src :  the source code of the core of the system
2. ../arja-e/lib :  the dependences of the core of the system (including a number of .jar files)
3. ../arja-e/external :  the external project that is used for the execution of test cases
4. ../arja-e/post-tool : the post-processing tool for alleviating patch overfitting 
5. ../arja-e/patches :  the obtained patches on 224 real bugs in Defects4J


### Build
The users should compile the core of the system first. Enter into the root directory of the system:
```
$ cd arja-e
```

Then, compile the source code of the core of the system, and the compiled classes are saved in the directory "bin":
```
$ mkdir bin
$ javac -cp lib/*: -d bin $(find src -name '*.java')
```

Similarly, the external project is compiled as follows:
```
$ cd external
$ mkdir bin
$ javac -cp lib/*: -d bin $(find src -name '*.java')
```

### Minimum Usage
Enter into the root directory of the system:
```
$ cd arja-e
```
Confirm that the current version of Java is JDK 1.7 and use the following command to run:
```
$ java -cp lib/*:bin us.msu.cse.repair.Main ArjaE -DsrcJavaDir path_to_directory_of_src_buggy \
						-DbinJavaDir path_to_directory_of_binary_source_buggy \
						-DbinTestDir path_to_directory_of_binary_test_buggy  \
						-Ddependences paths_to_dependences_buggy 
```
"ArjaE" means that the repair approach ARJA-e is run. Alternatively, "Arja", "GenProg", "RSRepair" and "Kali" can 
be used. Moreover, at least four parameters related with the buggy program are required.
1. -DsrcJavaDir  :  the path to the root directory of the source code 
2. -DbinJavaDir  : the path to the root directory of all the compiled classes of source code
3. -DbinTestDir  :  the path to the root directory of all the compiled classes of test code
4. -Ddependences :  the paths to the the dependences (jar files). If more than one, separated by ":"

In the above command, "path_to_directory_of_src_buggy" etc should be replaced with the actual 
absolute paths (the system currently only supports absolute paths). All the plausible 
patches found by the approach are saved in the directory arja-e/patches_$id$ by default. $id$ is a randomly 
generated string containing four characters. 


### Advanced Usage

The system provides the other parameters to configure the repair approaches. The following command
can be used to list all the parameters (including the description) available for each repair approach.
```
$ java -cp lib/*:bin us.msu.cse.repair.Main -listParameters
```

The following is an example to use more than four parameters:
```
$ java -cp lib/*:bin us.msu.cse.repair.Main ArjaE -DsrcJavaDir path_to_directory_of_src_buggy \
						-DbinJavaDir path_to_directory_of_binary_source_buggy \
						-DbinTestDir path_to_directory_of_binary_test_buggy \
						-Ddependences paths_to_dependences_buggy \
						-DpopulationSize value_of_population_size \
						-DgzoltarDataDir path_to_directory_of_Gzoltar_output \
```
In this command, the parameter -DpopulationSize sets the population size of ARJA-e to "value_of_population_size";
the parameter -DgzoltarDataDir specifies the path to the root directory of the Gzoltar 1.6.2 output (see https://github.com/GZoltar/gzoltar/tree/master/com.gzoltar.cli).


## Evaluation
ARJA-e has been evaluated on 224 real bugs in Defects4J v1.0.1 (https://github.com/rjust/defects4j/tree/v1.0.1). 
The results indicated that ARJA-e can fix 106 bugs in terms of passing all the test cases, and it can correctly fix
39 bugs (at least) according to the patches ranked first. The patches ranked first for the 106 bugs are available in the 
directory "../arja-e/patches". 


## Contact
For questions and feedback, please contact yyxhdy@gmail.com
