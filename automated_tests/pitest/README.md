# PITest Mutation Testing

#### This guide outlines the steps to generate mutation testing reports using PITest in a Java project.

## Prerequisites
- Maven is installed in your system

## Steps to Generate PITest Reports
1. Open your terminal and navigate to the directory containing the pom.xml file of your Maven project.
2. Run PITest with the command: `mvn test-compile org.pitest:pitest-maven:mutationCoverage`
3. Reports should be generated under `automated_tests/pitest`
4. To view the reports: run the command `start ./automated_test/pitest/index.html`
