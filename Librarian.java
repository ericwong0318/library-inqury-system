import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Scanner;

class Librarian extends User {

    @Override
    void printMenu() {
        System.out.println();
        System.out.println("-----Operations for librarian menu-----");
        System.out.println("What kinds of operations would you like to perform?");
        System.out.println("1. Book Borrowing");
        System.out.println("2. Book Returning");
        System.out.println("3. List all un-returned book copies which are checked-out within a period");
        System.out.println("4. Return to the main menu");
        System.out.print("Enter Your Choice: ");
    }

    @Override
    boolean performOperation(int choice, Scanner scan) {
        switch (choice) {
            case 1:
                borrowBook();
                break;
            case 2:
                returnBook();
                break;
            case 3:
                listUnreturnBook();
                break;
            case 4:
                return true;
            default:
                System.out.println("Invalid Input");
        }

        return false;
    }

    private void borrowBook() {

        // get borrow information
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter The User ID: ");
        String userID = sc.nextLine();
        System.out.print("Enter The Call Number: ");
        String callNum = sc.nextLine();
        System.out.print("Enter The Copy Number: ");
        int copyNum = sc.nextInt();

        // check whether the book is available
        try {
            Connection conn = ConnectDatabase.getConn();

            // get all borrow records with no return date for this copy
            PreparedStatement pstmtBorrowed = conn.prepareStatement("SELECT * FROM borrow WHERE callnum = ? " +
                    "AND copynum = ? AND returndate IS NULL");
            pstmtBorrowed.setString(1, callNum);
            pstmtBorrowed.setInt(2, copyNum);

            ResultSet borrowedCopies = pstmtBorrowed.executeQuery();

            // see if the book is available
            if (borrowedCopies.next()) {
                // the copy was already borrowed, thus not available
                System.out.println("The book is not available.");
            } else {
                // the copy was not yet borrowed, perform borrowing

                // create new borrow record
                PreparedStatement pstmtToBorrow = conn.prepareStatement("INSERT INTO borrow (libuid, callnum, " +
                        "copynum, checkout, returndate) VALUES (?, ?, ?, ?, NULL)");

                pstmtToBorrow.setString(1, userID);
                pstmtToBorrow.setString(2, callNum);
                pstmtToBorrow.setInt(3, copyNum);
                pstmtToBorrow.setDate(4, java.sql.Date.valueOf(java.time.LocalDate.now()));
                pstmtToBorrow.execute();

                // finish
                System.out.println("Book borrowing performed successfully.");
            }
        } catch (Exception e) {
            System.out.println("Error in borrowing book: " + e);
        }
    }

    private void returnBook() {

        // get return information
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter The User ID: ");
        String userID = sc.nextLine();
        System.out.print("Enter The Call Number: ");
        String callNum = sc.nextLine();
        System.out.print("Enter The Copy Number: ");
        int copyNum = sc.nextInt();
        System.out.print("Enter Your Rating of the Book: ");
        int rating = sc.nextInt();

        // check whether the book is being borrowed
        try {
            Connection conn = ConnectDatabase.getConn();

            // search for the borrow record
            PreparedStatement pstmtIsBorrowed = conn.prepareStatement("SELECT * FROM borrow WHERE libuid = ? " +
                    "AND callnum = ? AND copynum = ? AND returndate IS NULL");
            pstmtIsBorrowed.setString(1, userID);
            pstmtIsBorrowed.setString(2, callNum);
            pstmtIsBorrowed.setInt(3, copyNum);

            ResultSet borrowingCopies = pstmtIsBorrowed.executeQuery();

            // check whether the borrow record exists
            if (borrowingCopies.next()) {
                // pending record exists, perform return
                PreparedStatement pstmtReturn = conn.prepareStatement("UPDATE borrow SET returndate = ? " +
                        "WHERE libuid = ? AND callnum = ? AND copynum = ? AND returndate IS NULL");
                pstmtReturn.setDate(1, java.sql.Date.valueOf(java.time.LocalDate.now()));
                pstmtReturn.setString(2, userID);
                pstmtReturn.setString(3, callNum);
                pstmtReturn.setInt(4, copyNum);
                pstmtReturn.executeUpdate();

                // update rating of the book

                // see if the book has rating or not
                PreparedStatement pstmtCheckRating = conn.prepareStatement("SELECT rating FROM book " +
                        "WHERE callnum = ?");
                pstmtCheckRating.setString(1, callNum);
                ResultSet bookRating = pstmtCheckRating.executeQuery();
                bookRating.next();
                String ratingString = bookRating.getString(1);

                if (ratingString == null) {
                    // no rating, set rating as initial rating
                    PreparedStatement pstmtInitRating = conn.prepareStatement("UPDATE book SET rating = ? " +
                            "WHERE callnum = ?");
                    pstmtInitRating.setFloat(1, (float) rating);
                    pstmtInitRating.setString(2, callNum);
                    pstmtInitRating.executeUpdate();
                } else {
                    // rating exists, update rating
                    PreparedStatement pstmtUpdateRating = conn.prepareStatement("UPDATE book SET rating = " +
                            "(rating * tborrowed + ?) / (tborrowed + 1) WHERE callnum = ?");
                    pstmtUpdateRating.setFloat(1, (float) rating);
                    pstmtUpdateRating.setString(2, callNum);
                    pstmtUpdateRating.executeUpdate();
                }

                // increase number of times the book borrowed
                PreparedStatement pstmtIncreaseBorrowedCount = conn.prepareStatement("UPDATE book SET tborrowed " +
                        "= tborrowed + 1 WHERE callnum = ?");
                pstmtIncreaseBorrowedCount.setString(1, callNum);
                pstmtIncreaseBorrowedCount.executeUpdate();

                System.out.println("Book returning performed successfully.");
            } else {
                // no borrowed record found
                System.out.println("No borrowed record is found");
            }
        } catch (Exception e) {
            System.out.println("Error in returning book: " + e);
        }
    }

    private void listUnreturnBook() {

        // get date range
        Scanner sc = new Scanner(System.in);
        System.out.print("Type in the starting date [dd/mm/yyyy]: ");
        String startDateDMY = sc.nextLine();
        System.out.print("Type in the ending date [dd/mm/yyyy]: ");
        String endDateDMY = sc.nextLine();

        // query for all unreturned copies within specified date inclusively
        try {
            java.text.DateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy");
            java.util.Date startDate = df.parse(startDateDMY);
            java.util.Date endDate = df.parse(endDateDMY);

            Connection conn = ConnectDatabase.getConn();

            PreparedStatement pstmtUnreturned = conn.prepareStatement("SELECT libuid, callnum, copynum, checkout " +
                    "FROM borrow WHERE checkout BETWEEN ? AND ? AND returndate IS NULL ORDER BY checkout DESC ");
            pstmtUnreturned.setDate(1, new java.sql.Date(startDate.getTime()));
            pstmtUnreturned.setDate(2, new java.sql.Date(endDate.getTime()));

            ResultSet unreturnedCopies = pstmtUnreturned.executeQuery();

            if (!unreturnedCopies.isBeforeFirst()) {
                // empty result
                System.out.println("No unreturned books");
            } else {
                // display list of unreturned copies
                System.out.println("|LibUID|CallNum|CopyNum|Checkout|");
                ResultSetMetaData rsmd = unreturnedCopies.getMetaData();
                int colNum = rsmd.getColumnCount();
                while (unreturnedCopies.next()) {
                    System.out.print("|");
                    for (int i = 1; i <= colNum; i++) {
                        String fieldString = unreturnedCopies.getString(i);
                        System.out.print(fieldString + "|");
                    }
                    System.out.println();
                }
                System.out.println("End of Query");
            }

        } catch (Exception e) {
            System.out.println("Error in listing unreturned books: " + e);
        }

    }

    @Override
    public String toString() {
        return "Librarian";
    }
}
