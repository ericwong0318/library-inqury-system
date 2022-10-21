import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Scanner;

class LibraryUser extends User {

    @Override
    void printMenu() {
        System.out.println();
        System.out.println("-----Operations for library user menu-----");
        System.out.println("What kinds of operations would you like to perform?");
        System.out.println("1. Search for Books");
        System.out.println("2. Show loan record of a user");
        System.out.println("3. Return to the main menu");
        System.out.print("Enter Your Choice: ");
    }

    @Override
    boolean performOperation(int choice, Scanner scan) {
        switch (choice) {
            case 1:
                searchBooks();
                break;
            case 2:
                showLoanRecords();
                break;
            case 3:
                return true;
            default:
                System.out.println("Invalid Input");
        }

        return false;
    }

    private void searchBooks() {
        System.out.println("Choose the Search criterion:");
        System.out.println("1. call number");
        System.out.println("2. title");
        System.out.println("3. author");
        System.out.print("Choose the search criterion: ");
        Scanner scan = new Scanner(System.in);
        int choice = scan.nextInt();
        scan.nextLine();
        if (choice < 1 || choice > 3) {
            System.out.println("Invalid input");
            return;
        }
        System.out.print("Type in the Search Keyword: ");
        String keyword = scan.nextLine();

        switch (choice) {
            /* Search by callnum */
            case 1:
                try {
                    Statement stmt = ConnectDatabase.getConn().createStatement();
                    ResultSet rs = stmt.executeQuery(
                            "SELECT B.callnum, B.title, BC.bcname, " +
                                    "GROUP_CONCAT(DISTINCT A.aname SEPARATOR ', '), B.rating, " +
                                    "COUNT(DISTINCT(C.copynum)) " +
                                    "FROM book B, copy C, book_category BC, authorship A " +
                                    "WHERE B.callnum = C.callnum AND B.bcid = BC.bcid AND B.callnum = A.callnum AND " +
                                    "B.callnum = '" +
                                    keyword +
                                    "' " +
                                    "HAVING B.callnum IS NOT NULL " +
                                    "ORDER BY B.callnum ASC;"
                    );
                    if (!rs.isBeforeFirst()) { // no records are found
                        System.out.println("Call Num is invalid");
                        return;
                    } else {
                        System.out.println(
                                "|Call Num|Title|Book Category|Author|Rating|Available No. of Copy|"
                        );
                        ResultSetMetaData rsmd = rs.getMetaData();
                        int colNum = rsmd.getColumnCount();
                        while (rs.next()) {
                            System.out.print("|");
                            for (int i = 1; i <= colNum; i++) {
                                String fieldString = rs.getString(i);
                                System.out.print(fieldString + "|");
                            }
                            System.out.println();
                        }
                        System.out.println("End of Query");
                    }
                } catch (Exception e) {
                    System.out.println("[Error]: " + e);
                    return;
                }
                break;
            /* Search by title */
            case 2:
                try {
                    Statement stmt = ConnectDatabase.getConn().createStatement();
                    ResultSet rs = stmt.executeQuery(
                            "SELECT B.callnum, B.title, BC.bcname, " +
                                    "GROUP_CONCAT(DISTINCT A.aname SEPARATOR ', '), B.rating, " +
                                    "COUNT(DISTINCT(C.copynum)) " +
                                    "FROM book B, copy C, book_category BC, authorship A " +
                                    "WHERE B.callnum = C.callnum AND B.bcid = BC.bcid AND B.callnum = A.callnum " +
                                    "AND B.title LIKE BINARY '%" +
                                    keyword +
                                    "%' " +
                                    "GROUP BY B.callnum " +
                                    "ORDER BY B.callnum ASC;"
                    );
                    if (!rs.isBeforeFirst()) { // no records are found
                        System.out.println("No partial match title");
                        return;
                    } else {
                        System.out.println(
                                "|Call Num|Title|Book Category|Author|Rating|Available No. of Copy|"
                        );
                        ResultSetMetaData rsmd = rs.getMetaData();
                        int colNum = rsmd.getColumnCount();
                        while (rs.next()) {
                            System.out.print("|");
                            for (int i = 1; i <= colNum; i++) {
                                String fieldString = rs.getString(i);
                                System.out.print(fieldString + "|");
                            }
                            System.out.println();
                        }
                        System.out.println("End of Query");
                    }
                } catch (Exception e) {
                    System.out.println("[Error]: " + e);
                    return;
                }
                break;
            /* Search by aname */
            case 3:
                try {
                    Statement stmt = ConnectDatabase.getConn().createStatement();
                    ResultSet rs = stmt.executeQuery(
                            "SELECT B.callnum, B.title, BC.bcname, " +
                                    "GROUP_CONCAT(DISTINCT A.aname SEPARATOR ', '), B.rating, " +
                                    "COUNT(DISTINCT(C.copynum)) " +
                                    "FROM book B, copy C, book_category BC, authorship A " +
                                    "WHERE B.callnum = C.callnum AND B.bcid = BC.bcid AND B.callnum = A.callnum " +
                                    "AND A.aname LIKE BINARY '%" +
                                    keyword +
                                    "%' " +
                                    "GROUP BY B.callnum " +
                                    "ORDER BY B.callnum ASC;"
                    );
                    if (!rs.isBeforeFirst()) { // no records are found
                        System.out.println("No partial match author name");
                        return;
                    } else {
                        System.out.println(
                                "|Call Num|Title|Book Category|Author|Rating|Available No. of Copy|"
                        );
                        ResultSetMetaData rsmd = rs.getMetaData();
                        int colNum = rsmd.getColumnCount();
                        while (rs.next()) {
                            System.out.print("|");
                            for (int i = 1; i <= colNum; i++) {
                                String fieldString = rs.getString(i);
                                System.out.print(fieldString + "|");
                            }
                            System.out.println();
                        }
                        System.out.println("End of Query");
                    }
                } catch (Exception e) {
                    System.out.println("[Error]: " + e);
                    return;
                }
                break;
            default:
                System.out.println("[Error]: Invalid input");
        }
    }

    private void showLoanRecords() {
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter The User ID: ");
        String userId = scan.nextLine();
        try {
            Statement stmt = ConnectDatabase.getConn().createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT BOR.callnum, BOR.copynum, B.title, GROUP_CONCAT(DISTINCT A.aname SEPARATOR ', '), " +
                            "BOR.checkout, " +
                            "CASE WHEN isNull(BOR.returndate) " +
                            "	THEN 'No' " +
                            "	ELSE 'Yes' " +
                            "END AS isreturned " +
                            "FROM book B, copy C, authorship A, libuser U, borrow BOR " +
                            "WHERE B.callnum = C.callnum AND B.callnum = A.callnum AND BOR.callnum = B.callnum " +
                            "AND U.libuid = BOR.libuid AND U.libuid = '" +
                            userId +
                            "' " +
                            "GROUP BY BOR.callnum " +
                            "ORDER BY BOR.checkout DESC;"
            );
            if (!rs.isBeforeFirst()) { // no record are found
                System.out.println("No loan record for this user");
            } else {
                System.out.println("Loan Record:");
                System.out.println(
                        "|CallNum|CopyNum|Title|Author|Check-out|Returned?|"
                );
                ResultSetMetaData rsmd = rs.getMetaData();
                int colNum = rsmd.getColumnCount();
                while (rs.next()) {
                    System.out.print("|");
                    for (int i = 1; i <= colNum; i++) {
                        String fieldString = rs.getString(i);
                        System.out.print(fieldString + "|");
                    }
                    System.out.println();
                }
                System.out.println("End of Query");
            }
        } catch (Exception e) {
            System.out.println("[Error]: " + e);
        }
    }

    @Override
    public String toString() {
        return "LibraryUser";
    }
}
