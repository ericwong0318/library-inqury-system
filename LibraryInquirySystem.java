import java.util.Scanner;

public class LibraryInquirySystem {
    public static void main(String[] args) {
        User user;
        Scanner scan = new Scanner(System.in);
        int mainChoice;
        int userChoice;
        boolean returnMainMenu;
        ConnectDatabase.connectDB();
        printMainMenu();
        while (scan.hasNext()) {
            mainChoice = scan.nextInt(); //no input validation
            if (mainChoice == 1) {
                user = new Administrator();
            } else if (mainChoice == 2) {
                user = new LibraryUser();
            } else if (mainChoice == 3) {
                user = new Librarian();
            } else if (mainChoice == 4) {
                break;
            } else {
                System.out.println("Invalid input!");
                printMainMenu();
                continue;
            }
            // Choose the menu option
            do {
                user.printMenu();
                userChoice = scan.nextInt(); //no input validation
                returnMainMenu = user.performOperation(userChoice, scan);
            } while (!returnMainMenu);
            System.out.println();
            printMainMenu();
        }
        scan.close();
    }

    public static void printMainMenu() {
        System.out.println("Welcome to Library Inquiry System!");
        System.out.println();
        System.out.println("-----Main menu-----");
        System.out.println("What kinds of operations would you like to perform?");
        System.out.println("1. Operations for Administrator");
        System.out.println("2. Operations for Library User");
        System.out.println("3. Operations for Librarian");
        System.out.println("4. Exit this program");
        System.out.print("Enter Your Choice: ");
    }
}

