import java.sql.*;
import java.util.Scanner;

public class Main {

    //preparing stuff
    static Connection myConn;
    static Statement st;
    //login stuff
    static String url = "jdbc:mysql://localhost:3306/";
    static String user = ""; //user = root
    static String pass = ""; //pass = 4580
    //stuff stuff
    static String db = "";
    static String input;
    static ResultSet rs;
    static ResultSetMetaData rsmd;

    public static void main(String[] args) {
        //preparing scanner and starting login
        Scanner scanner = new Scanner(System.in);
        login(scanner);
    }

    public static void login(Scanner scanner) {
        //user credentials and passing it to connection
        if (user.equals("")) {
            System.out.print("User: ");
            input = scanner.nextLine();
            user = input;
        }
        if (pass.equals("")) {
            System.out.print("Password: ");
            input = scanner.nextLine();
            pass = input;
        }
        connection(db, user, pass, scanner);
    }

    public static void connection(String db, String user, String pass, Scanner scanner) {
        //establishing connection to the server
        try {
            myConn = DriverManager.getConnection(url + db, user, pass);
            st = myConn.createStatement();
            database(db, user, pass, scanner);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void database(String db, String user, String pass, Scanner scanner) throws SQLException {
        //method to show databases/start creating a new database
        String newDb = ""; //temporary
        if (!db.equals(newDb) && !db.equals("")) {
            myConn = DriverManager.getConnection(url + db, user, pass);
            query(scanner);
        } else {
            System.out.print("--------\n" +
                             "Existing databases: \n\n");
            rs = st.executeQuery("show databases");
            while (rs.next()) {
                System.out.println(rs.getObject(1));
            }
            System.out.print("--------\n" +
                             "Database (if empty, makes new database): ");
            input = scanner.nextLine();
            newDb = input;
            if (newDb.equals("")) {
                newDatabase(scanner, user, pass);
            } else {
                myConn = DriverManager.getConnection(url + newDb, user, pass);
                query(scanner);
            }
        }
    }

    public static void query(Scanner scanner) throws SQLException {
        System.out.print("--------\n" +
                         "Commands: " +
                         "\nShow tables - 1 " +     //
                         "\nDescribe table - 2 " +      //
                         "\nCreate table - 3 " +        //
                         "\nInsert data - 4 " +     //
                         "\nUpdate table - 5 " +        //
                         "\nDelete table - 6 " +
                         "\nDrop table - 7 " +
                         "\nRead table data - 8" + "" +
                         "\n--------" + "" +
                         "\nCommand: ");
        input = scanner.nextLine();
        st = myConn.createStatement();

        //longer operation logic moved to it separate method
        if (input.equals("1")) {                    //Show tables
            st.executeQuery("show tables");
            rs = st.getResultSet();
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
        } else if (input.equals("2")) {             //Describe table
            descTable(scanner);

        } else if (input.equals("3")) {             //Create table
            System.out.print("Creating table...\n" + "New table name: ");
            input = scanner.nextLine();
            String table = input;
            st.executeUpdate("create table " + table + "(" + newColumns(scanner) + ")");
            System.out.println("Table created.");

        } else if (input.equals("4")) {             //Insert data
            insertingIntoTable(scanner);

        } else if (input.equals("5")) {             //Update table
            updateTable(scanner);

        } else if (input.equals("6")) {             //Delete table
            System.out.print("Table to delete: ");
            input = scanner.nextLine();
            st.executeUpdate("delete table " + input);

        } else if (input.equals("7")) {             //Drop table
            System.out.print("Table to drop: ");
            input = scanner.nextLine();
            st.executeUpdate("drop table " + input);

        } else if (input.equals("8")) {             //Read table data
            readTableData(scanner);
        }
        query(scanner);
    }

    public static String newColumns(Scanner scanner) { //method for making columns in the table
        String sql = "";
        while (true) {
            System.out.print("('close' to stop) \nColumn name: ");
            input = scanner.nextLine();
            String name = input;
            if (input.equals("close")) {
                break;
            }
            System.out.print("Datatype: ");
            input = scanner.nextLine();
            String data = input;
            System.out.print("Extra: ");
            input = scanner.nextLine();
            String extra = input;
            if (sql.equals("")) {
                sql = name + " " + data + " " + extra;
            } else {
                sql = sql + ", " + name + " " + data + " " + extra;
            }
            System.out.println(sql);
        }
        return sql;
    }

    public static void tableAddColumn(Scanner scanner) throws SQLException { //adding columns to the existing table
        System.out.print("Table name ('close' to stop): ");
        String table = scanner.nextLine();
        if (table.equals("close")) {
            query(scanner);
        }
        while (true) {
            String columns = newColumns(scanner);
            String sql = "ALTER TABLE " + table +
                    " ADD " + "(" + columns + ")";
            st.executeUpdate(sql);
            tableAddColumn(scanner);
        }
    }

    public static void newDatabase(Scanner scanner, String user, String pass) { //creating a new database
        System.out.print("New database name: ");
        input = scanner.nextLine();
        String db = input;
        String sql;
        try {
            sql = "CREATE DATABASE " + db;
            st.executeUpdate(sql);
            System.out.println("\n" +
                    "Making database...\n" +
                    "Database made.\n");
            database("", user, pass, scanner);
            } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void descTable(Scanner scanner) throws SQLException { //describes a table (column names and column datatype()
        System.out.print("Describe table: ");
        String table = scanner.nextLine();
        rs = st.executeQuery("select * from " + table);
        rsmd = rs.getMetaData();
        int column_count = rsmd.getColumnCount();
        String columns = "";

        for (int i = 1; i <= column_count; i++) {
            if (columns.equals("")) {
                columns = rsmd.getColumnName(i) + " " + rsmd.getColumnTypeName(i);
            } else {
                columns = columns + ", " + rsmd.getColumnName(i) + " " + rsmd.getColumnTypeName(i);
            }
        }
        if (column_count > 1) {
            System.out.println(column_count + " columns: " + columns);
        } else {
            System.out.println(column_count + " column: " + columns);
        }
    }

    public static void insertingIntoTable(Scanner scanner) throws SQLException {    //method to insert data into a table
        System.out.println("Table to insert data to: ");
        input = scanner.nextLine();
        //mozda se moze prebacit u metodu
        st.executeQuery("select * from " + input);
        rs = st.getResultSet();
        rsmd = rs.getMetaData();
        String columns = "";
        String columnsPrint = "";
        int column_count = rsmd.getColumnCount();
        for (int i = 1; i <= column_count; i++) {
            if (columns.equals("")) {
                columns = rsmd.getColumnName(i);
                columnsPrint = rsmd.getColumnName(i) + " (" + rsmd.getColumnTypeName(i) + ")";
            } else {
                columns = columns + ", " + rsmd.getColumnName(i);
                columnsPrint = columnsPrint + ", " + rsmd.getColumnName(i) + " (" + rsmd.getColumnTypeName(i) + ")";
            }
        }
        System.out.println("Columns: " + columnsPrint); //printing out existing columns for easier use
        while (true) {
            System.out.print("Values: ");
            String values = scanner.nextLine();
            if (values.equals("close")) {
                break;
            }
            st.executeUpdate("insert into " + input + " (" + columns + ")" + " values(" + values + ")");
        }
        query(scanner);
    }

    public static void updateTable(Scanner scanner) throws SQLException { //method to update data in the table
        System.out.println("add columns - 1 \nupdate rows - 2");
        input = scanner.nextLine();
        if (input.equals("1")) {
            tableAddColumn(scanner);
        } else if (input.equals("2")) {
            System.out.print("Table name: ");
            String table = scanner.nextLine();
            System.out.print("Column name: ");
            String column = scanner.nextLine();
            System.out.print("New data: ");
            String newData = scanner.nextLine();
            System.out.print("Where (" + column + "): ");
            String where = scanner.nextLine();
            if (where.equals("")) {
                where = column;
            }
            System.out.print(where + " is: ");
            String oldData = scanner.nextLine();
            st.executeUpdate("UPDATE " + table + " SET " + column + " = " + newData + " WHERE " + where + " = \"" + oldData + "\"");
            query(scanner);
        }
    }
        /*
        preimenovat u "read"
        ubaciti funkciju ako je table name "" onda radi select all/specific dio
        ako table name nije "", onda bi
         */
    public static void readTableData(Scanner scanner) throws SQLException {
        System.out.println("select all - 1\n select specific - 2");
        input = scanner.nextLine(); // 1 or 2
        System.out.print("Table name: ");
        String table = scanner.nextLine();
        String columns;
        String extra = "";
        if (input.equals("2")) {    //will be ignored if input is 1
            System.out.print("Columns to select: ");
            columns = scanner.nextLine();
            System.out.print("Extras: ");
            extra = scanner.nextLine();
        } else {                    //will be ignored if input is 2
            columns = "*";
        }
        st.executeQuery("select " + columns + " from " + table + " " + extra);
        rs = st.getResultSet();
        while (rs.next()) {
            rsmd = rs.getMetaData();
            int column_count = rsmd.getColumnCount();
            for (int i = 1; i <= column_count; i++) {
                if (i == column_count) {
                    System.out.print(rsmd.getColumnName(i) + ": " + rs.getString(i) + "\n");
                } else {
                    System.out.print(rsmd.getColumnName(i) + ": " + rs.getString(i) + ", ");
                }
            }
        }
    }
}