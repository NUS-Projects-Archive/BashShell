# Setup Project Structure
1. Go to [File] > [Project Structure]... (or press _Ctrl+Alt+Shift+S_)\
   ![Screenshot of File to Project Structure](img/img.png)
2. On the side, under Project Settings, select [Project]
3. For _SDK_, select one that is **Java 11**
4. Leave _Language Level_ as "SDK Default"
5. You should have something that looks like this:\
   ![Project Settings - Project](img/img_2.png)
6. Click [Apply] to save changes
7. Next, on the side, under Project Settings, select [Modules]
8. You should have _cs4218_shell_2024_ as the only module
9. Under the _Sources_ tab, you should see a list of directories
10. Mark the following as ![Test](img/img_5.png):
    - integration_tests
    - public_tests
    - system_tests
    - testing_utilities
    - unit_tests
11. Mark the "resources" directory as ![Test Resources](img/img_6.png)
12. You should have something that looks like this:\
    ![Project Settings - Modules](img/img_4.png)
13. Click [Apply] to save changes, and then [OK] to exit


# Automated Testing Tools

In our project we used 2 automatic testing tools
- [Pitest](automated_tests/pitest/README.md)
- [EvoSuite](automated_tests/evosuite-tests/README.md)

> [!TIP]
> Click on the hyperlink to see the README of the respective tools

In our report, we explained how we used these tools to improve the robustness
of our tests.