import java.util.Scanner;

abstract class User {
    abstract void printMenu();  //print different menu for the user

    abstract boolean performOperation(int choice, Scanner scan);

    public abstract String toString();
}
