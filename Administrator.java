import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

class Administrator extends User {

    @Override
    void printMenu() {
        System.out.println();
        System.out.println("-----Operations for administrator menu-----");
        System.out.println("What kinds of operations would you like to perform?");
        System.out.println("1. Create all tables");
        System.out.println("2. Delete all tables");
        System.out.println("3. Load from datafile");
        System.out.println("4. Show number of records in each table");
        System.out.println("5. Return to the main menu");
        System.out.print("Enter Your Choice: ");
    }

    @Override
    boolean performOperation(int choice, Scanner scan) {
        switch (choice) {
            case 1:
                createTable();
                break;
            case 2:
                deleteTable();
                break;
            case 3:
                loadData(scan);
                break;
            case 4:
                showRecords();
                break;
            case 5:
                return true;
            default:
                System.out.println("Invalid Input");
        }

        return false;
    }

    private void createTable() {
        //System.out.println("createTable()");
        System.out.print("Processing...");

        try {
            Statement stmt = ConnectDatabase.getConn().createStatement();
            //drop the same table if existed
            stmt.executeUpdate("DROP TABLE IF EXISTS borrow;");
            stmt.executeUpdate("DROP TABLE IF EXISTS libuser;");
            stmt.executeUpdate("DROP TABLE IF EXISTS user_category;");
            stmt.executeUpdate("DROP TABLE IF EXISTS authorship;");
            stmt.executeUpdate("DROP TABLE IF EXISTS copy;");
            stmt.executeUpdate("DROP TABLE IF EXISTS book;");
            stmt.executeUpdate("DROP TABLE IF EXISTS book_category;");
            //create the tables
            stmt.execute("CREATE TABLE user_category (ucid INTEGER NOT NULL PRIMARY KEY, max INTEGER NOT NULL,  period INTEGER NOT NULL);");
            stmt.execute("CREATE TABLE libuser (libuid CHAR(10) NOT NULL PRIMARY KEY, name VARCHAR(25) NOT NULL, age INTEGER NOT NULL, address VARCHAR(100) NOT NULL, ucid INTEGER NOT NULL, FOREIGN KEY(ucid) REFERENCES user_category(ucid));");
            stmt.execute("CREATE TABLE book_category (bcid INTEGER NOT NULL PRIMARY KEY, bcname VARCHAR(30) NOT NULL);");
            stmt.execute("CREATE TABLE book (callnum VARCHAR(8) NOT NULL PRIMARY KEY, title VARCHAR(30) NOT NULL, publish DATE, rating FLOAT, tborrowed INTEGER NOT NULL, bcid INTEGER, FOREIGN KEY(bcid) REFERENCES book_category(bcid));"); // date format dd/mm/yy
            stmt.execute("CREATE TABLE copy (callnum VARCHAR(8) NOT NULL, copynum INTEGER NOT NULL, PRIMARY KEY (callnum, copynum), FOREIGN KEY (callnum) REFERENCES book(callnum));");
            stmt.execute("CREATE TABLE borrow (libuid CHAR(10) NOT NULL, callnum VARCHAR(8) NOT NULL, copynum INTEGER NOT NULL, checkout DATE NOT NULL, returndate DATE, PRIMARY KEY(libuid, callnum, copynum, checkout), FOREIGN KEY(libuid) REFERENCES libuser(libuid), FOREIGN KEY(callnum, copynum) REFERENCES copy(callnum, copynum));");
            stmt.execute("CREATE TABLE authorship(aname VARCHAR(25) NOT NULL, callnum VARCHAR(8) NOT NULL, PRIMARY KEY(aname, callnum), FOREIGN KEY(callnum) REFERENCES book(callnum));");
            //close statement
            stmt.close();
            System.out.println("Done. Database is initialized.");
        } catch (Exception e) {
            System.out.println("[Error]: " + e);
        }
    }

    private void deleteTable() { //wait for debug
        System.out.println("Processing...");
        try {
            Statement stmt = ConnectDatabase.getConn().createStatement();
            stmt.executeUpdate("DROP TABLE IF EXISTS borrow;");
            stmt.executeUpdate("DROP TABLE IF EXISTS libuser;");
            stmt.executeUpdate("DROP TABLE IF EXISTS user_category;");
            stmt.executeUpdate("DROP TABLE IF EXISTS authorship;");
            stmt.executeUpdate("DROP TABLE IF EXISTS copy;");
            stmt.executeUpdate("DROP TABLE IF EXISTS book;");
            stmt.executeUpdate("DROP TABLE IF EXISTS book_category;");
            stmt.close();
            System.out.println("Done. Database is removed.");
        } catch (Exception e) {
            System.out.println("[Error]: " + e + "Please make sure you have already created the database.");
        }
    }

    private void loadData(Scanner scan) {
        try {
            String folder_path;
            boolean missingFile = false;
            boolean failInsertData = false;
            System.out.println();             //a blank line according to the demo provided
            System.out.print("Please enter the folder path:");
            folder_path = scan.next();
            System.out.print("Processing...");

            //check book_category.txt exist
            try {
                File bookCategory = new File(System.getProperty("user.dir") + "/" + folder_path + "" + "/book_category.txt");
                Scanner scanFileData = new Scanner(bookCategory);
                scanFileData.close();
            } catch (Exception e) {
                System.out.print("\n[Error]: book_category.txt not found. Please make sure you have inputed the correct folder path.");
                missingFile = true;
            }

            //check book.txt exist
            try {
                File book = new File(System.getProperty("user.dir") + "/" + folder_path + "" + "/book.txt");
                Scanner scanFileData = new Scanner(book);
                scanFileData.close();
            } catch (Exception e) {
                System.out.print("\n[Error]: book.txt not found. Please make sure you have inputed the correct folder path.");
                missingFile = true;
            }

            //check check_out.txt exist
            try {
                File checkOut = new File(System.getProperty("user.dir") + "/" + folder_path + "" + "/check_out.txt");
                Scanner scanFileData = new Scanner(checkOut);
                scanFileData.close();
            } catch (Exception e) {
                System.out.print("\n[Error]: check_out.txt not found. Please make sure you have inputed the correct folder path.");
                missingFile = true;
            }

            //check user_category.txt exist
            try {
                File userCategory = new File(System.getProperty("user.dir") + "/" + folder_path + "" + "/user_category.txt");
                Scanner scanFileData = new Scanner(userCategory);
                scanFileData.close();
            } catch (Exception e) {
                System.out.print("\n[Error]: user_category.txt not found. Please make sure you have inputed the correct folder path.");
                missingFile = true;
            }

            //check user.txt exist
            try {
                File userData = new File(System.getProperty("user.dir") + "/" + folder_path + "" + "/user.txt");
                Scanner scanFileData = new Scanner(userData);
                scanFileData.close();
            } catch (Exception e) {
                System.out.print("\n[Error]: user.txt not found. Please make sure you have inputed the correct folder path.");
                missingFile = true;
            }
            //return if there is any file missing
            if (missingFile) {
                System.out.println();
                return;
            }

            //load book_category data
            try {
                File bookCategory = new File(System.getProperty("user.dir") + "/" + folder_path + "" + "/book_category.txt");
                Scanner scanFileData = new Scanner(bookCategory);
                Statement stmt = ConnectDatabase.getConn().createStatement();
                while (scanFileData.hasNextLine()) {
                    String[] bookCategoryData = scanFileData.nextLine().split("\\t");
                    stmt.executeUpdate("INSERT INTO book_category VALUES('" + Integer.parseInt(bookCategoryData[0]) + "',\"" + bookCategoryData[1] + "\");");
                }
                stmt.close();
                scanFileData.close();
            } catch (Exception e) {
                System.out.println("[Error]: Fail to insert data to book_category." + e);
                failInsertData = true;
            }

            //load user_category data
            try {
                File userCategory = new File(System.getProperty("user.dir") + "/" + folder_path + "" + "/user_category.txt");
                Scanner scanFileData = new Scanner(userCategory);
                Statement stmt = ConnectDatabase.getConn().createStatement();
                while (scanFileData.hasNextLine()) {
                    String[] userCategoryData = scanFileData.nextLine().split("\\t");
                    stmt.executeUpdate("INSERT INTO user_category VALUES('" + Integer.parseInt(userCategoryData[0]) + "','" + Integer.parseInt(userCategoryData[1]) + "','" + Integer.parseInt(userCategoryData[2]) + "');");
                }
                stmt.close();
                scanFileData.close();
            } catch (Exception e) {
                System.out.println("[Error]: Fail to insert data to user_category." + e);
                failInsertData = true;
            }

            //Load libuser data
            try {
                File userData = new File(System.getProperty("user.dir") + "/" + folder_path + "" + "/user.txt");
                Scanner scanFileData = new Scanner(userData);
                Statement stmt = ConnectDatabase.getConn().createStatement();
                while (scanFileData.hasNextLine()) {
                    String[] libuserData = scanFileData.nextLine().split("\\t");
                    stmt.executeUpdate("INSERT INTO libuser VALUES(\"" + libuserData[0] + "\",\"" + libuserData[1] + "\",'" + Integer.parseInt(libuserData[2]) + "',\"" + libuserData[3] + "\",'" + Integer.parseInt(libuserData[4]) + "');");
                }
                stmt.close();
                scanFileData.close();
            } catch (Exception e) {
                System.out.println("[Error]: Fail to insert data to libuser." + e);
                failInsertData = true;
            }

            //Load book
            try {
                File book = new File(System.getProperty("user.dir") + "/" + folder_path + "" + "/book.txt");
                Scanner scanFileData = new Scanner(book);
                Statement stmt = ConnectDatabase.getConn().createStatement();
                while (scanFileData.hasNextLine()) {
                    String[] bookData = scanFileData.nextLine().split("\\t");
                    if (bookData[4].equals("null")) {
                        stmt.executeUpdate("INSERT INTO book VALUES(\"" + bookData[0] + "\",\"" + bookData[2] + "\",\"" + bookData[4] + "\"," + (bookData[5].equals("null") ? "null" : Float.parseFloat(bookData[5])) + ",'" + Integer.parseInt(bookData[6]) + "','" + Integer.parseInt(bookData[7]) + "');");   //can insert without '', will show warning if insert 'null'/ "null" instead of null
                    } else {
                        stmt.executeUpdate("INSERT INTO book VALUES(\"" + bookData[0] + "\",\"" + bookData[2] + "\",STR_TO_DATE('" + bookData[4] + "', '%d/%m/%Y')," + (bookData[5].equals("null") ? "null" : Float.parseFloat(bookData[5])) + ",'" + Integer.parseInt(bookData[6]) + "','" + Integer.parseInt(bookData[7]) + "');");
                    }
                }
                stmt.close();
                scanFileData.close();
            } catch (Exception e) {
                System.out.println("[Error]: Fail to insert data to book." + e);
                failInsertData = true;
            }

            //Load authorship
            try {
                File book = new File(System.getProperty("user.dir") + "/" + folder_path + "" + "/book.txt");
                Scanner scanFileData = new Scanner(book);
                Statement stmt = ConnectDatabase.getConn().createStatement();
                while (scanFileData.hasNextLine()) {
                    String[] tmpData = scanFileData.nextLine().split("\\t");
                    String[] authorshipData = tmpData[3].split(",");
                    for (String author : authorshipData) {
                        stmt.executeUpdate("INSERT INTO authorship VALUES(\"" + author + "\",\"" + tmpData[0] + "\");");
                    }
                }
                stmt.close();
                scanFileData.close();
            } catch (Exception e) {
                System.out.println("[Error]: Fail to insert data to authorship." + e);
                failInsertData = true;
            }

            //Load copy
            try {
                File book = new File(System.getProperty("user.dir") + "/" + folder_path + "" + "/book.txt");
                Scanner scanFileData = new Scanner(book);
                Statement stmt = ConnectDatabase.getConn().createStatement();
                while (scanFileData.hasNextLine()) {
                    String[] tmpData = scanFileData.nextLine().split("\\t");
                    int tmpCopyNum = Integer.parseInt(tmpData[1]);
                    for (int i = 1; i <= tmpCopyNum; i++) {   //??? not sure whether this is correct or not
                        stmt.executeUpdate("INSERT INTO copy VALUES('" + tmpData[0] + "','" + i + "');");
                    }
                }
                stmt.close();
                scanFileData.close();
            } catch (Exception e) {
                System.out.println("[Error]: Fail to insert data to copy." + e);
                failInsertData = true;
            }

            //load borrow
            try {
                File checkOut = new File(System.getProperty("user.dir") + "/" + folder_path + "" + "/check_out.txt");
                Scanner scanFileData = new Scanner(checkOut);
                Statement stmt = ConnectDatabase.getConn().createStatement();

                while (scanFileData.hasNextLine()) {
                    String[] checkOutData = scanFileData.nextLine().split("\\t");
                    if (checkOutData[4].equals("null")) {
                        stmt.executeUpdate("INSERT INTO borrow VALUES(\"" + checkOutData[2] + "\",\"" + checkOutData[0] + "\",'" + Integer.parseInt(checkOutData[1]) + "',STR_TO_DATE('" + checkOutData[3] + "', '%d/%m/%Y')," + checkOutData[4] + ");");
                    } else {
                        stmt.executeUpdate("INSERT INTO borrow VALUES(\"" + checkOutData[2] + "\",\"" + checkOutData[0] + "\",'" + Integer.parseInt(checkOutData[1]) + "',STR_TO_DATE('" + checkOutData[3] + "', '%d/%m/%Y'),STR_TO_DATE('" + checkOutData[4] + "', '%d/%m/%Y'));");
                    }
                }
                stmt.close();
                scanFileData.close();
            } catch (Exception e) {
                System.out.println("[Error]: Fail to insert data to borrow." + e);
                failInsertData = true;
            }
            //return if fail to load any of the data
            if (failInsertData) {
                return;
            }

            //Show the process is done.
            System.out.println("Done. Data is inputted to the database.");
        } catch (Exception e) {
            System.out.println("[Error]: " + e + "Please make sure you have already created the database.");
        }
    }

    private void showRecords() {
        System.out.println("Number of records in each table:");
        //get recordNum of user_category
        try {
            Statement stmt = ConnectDatabase.getConn().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM user_category;");
            rs.next();
            int recordNum = rs.getInt(1);
            System.out.println("user_category: " + recordNum);
            stmt.close();
        } catch (Exception e) {
            System.out.println("[Error]: Fail to get the number of records of user_category. Please make sure the " +
                    "table is already created.");
        }
        //get recordNum of libuser
        try {
            Statement stmt = ConnectDatabase.getConn().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM libuser;");
            rs.next();
            int recordNum = rs.getInt(1);
            System.out.println("libuser: " + recordNum);
            stmt.close();
        } catch (Exception e) {
            System.out.println("[Error]: Fail to get the number of records of libuser. Please make sure the table " +
                    "is already created.");
        }
        //get recordNum of book_category
        try {
            Statement stmt = ConnectDatabase.getConn().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM book_category;");
            rs.next();
            int recordNum = rs.getInt(1);
            System.out.println("book_category: " + recordNum);
            stmt.close();
        } catch (Exception e) {
            System.out.println("[Error]: Fail to get the number of records of book_category. Please make sure the " +
                    "table is already created.");
        }
        //get recordNum of book
        try {
            Statement stmt = ConnectDatabase.getConn().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM book;");
            rs.next();
            int recordNum = rs.getInt(1);
            System.out.println("book: " + recordNum);
            stmt.close();
        } catch (Exception e) {
            System.out.println("[Error]: Fail to get the number of records of book. Please make sure the table is already created.");
        }
        //get recordNum of copy
        try {
            Statement stmt = ConnectDatabase.getConn().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM copy;");
            rs.next();
            int recordNum = rs.getInt(1);
            System.out.println("copy: " + recordNum);
            stmt.close();
        } catch (Exception e) {
            System.out.println("[Error]: Fail to get the number of records of copy. Please make sure the table is " +
                    "already created.");
        }
        //get recordNum of borrow
        try {
            Statement stmt = ConnectDatabase.getConn().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM borrow;");
            rs.next();
            int recordNum = rs.getInt(1);
            System.out.println("borrow: " + recordNum);
            stmt.close();
        } catch (Exception e) {
            System.out.println("[Error]: Fail to get the number of records of borrow. Please make sure the table is " +
                    "already created.");
        }
        //get recordNum of authorship
        try {
            Statement stmt = ConnectDatabase.getConn().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM authorship;");
            rs.next();
            int recordNum = rs.getInt(1);
            System.out.println("authorship: " + recordNum);
            stmt.close();
        } catch (Exception e) {
            System.out.println("[Error]: Fail to get the number of records of authorship. Please make sure the table " +
                    "is already created.");
        }
    }

    @Override
    public String toString() {
        return "Administrator";
    }
}
