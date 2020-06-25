package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseLogic {
    private int id;
    public static final String DB_FIELD_ID = "Id";
    private static final String DISTINCT_SYMBOL_COUNT = "Distinct_Symbol_Count";
    private List<String>companyList;
    private int quantity;
    public static final String DB_FIELD_QUANTITY = "Quantity";
    private String dateOfInvestment;//I have changed this from Date to String because of a persistent exception
    public static final String DB_FIELD_DATE_OF_INVESTMENT= "DateOfInvestment";
    private int shares_bought_sold;
    public static final String DB_FIELD_SHARES_BOUGHT_SOLD = "Shares_Bought_Sold";
    private String symbol;
    public static final String DB_FIELD_SYMBOL= "Symbol";
    private static final String URL= "jdbc:sqlite:financialportfoliomanager.db";
    public static final String TABLE_TRANSACTIONS = "tblTransactions";

    private String fileName;
    private String getNumRowsAffected(PreparedStatement preparedStatement)throws SQLException{//Getter method
        int numRowsAffected = preparedStatement.executeUpdate();
        return numRowsAffected > 0 ? numRowsAffected + " were affected" : "No rows were affected";
    }

    public static String getURL() {//Getter method
        return URL;
    }

    public int getId() {//Getter method
        return id;
    }
    public List<String>getCompanyList(){
        return companyList;
    }
    public void setCompanyList(List<String>companyList){
        this.companyList = companyList;
    }
    public int getQuantity() {//Getter method
        return quantity;
    }

    public int getShares_bought_sold() {//Getter Method
        return shares_bought_sold;
    }

    public String getDateOfInvestment() {//Getter method
        return dateOfInvestment;
    }

    public String getSymbol() {//Getter method
        return symbol;
    }

    public void setDateOfInvestment(String dateOfInvestment) {//Setter method
        this.dateOfInvestment = dateOfInvestment;
    }

    public void setId(int id) {//Setter method
        this.id = id;
    }

    public void setQuantity(int quantity) {//Setter method
        this.quantity = quantity;
    }

    public void setShares_bought_sold(int shares_bought_sold) {//Setter method
        this.shares_bought_sold = shares_bought_sold;
    }

    public void setSymbol(String symbol) {//Setter method
        this.symbol = symbol;
    }

    public DatabaseLogic(){
        companyList = new ArrayList<>();
    }
    public void createNewDatabase() {//Create new database
        try(Connection connection = DriverManager.getConnection(URL)){
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            System.out.println("Driver Name is: "+ databaseMetaData.getDriverName());
            System.out.println("New Database has been created");
        }
        catch (SQLException ex){
            System.out.print(ex.getMessage());
        }
    }
    public void createTable(String tableName){//Create table to store data
        try(Connection connection = DriverManager.getConnection(URL);
            PreparedStatement tableCreationStatement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS "+ tableName + "("
                            + DB_FIELD_ID + " INTEGER PRIMARY KEY,"
                            + DB_FIELD_QUANTITY + " INTEGER NOT NULL,"
                            + DB_FIELD_DATE_OF_INVESTMENT + " VARCHAR(20) NOT NULL,"
                            + DB_FIELD_SHARES_BOUGHT_SOLD + " INTEGER NOT NULL,"
                            + DB_FIELD_SYMBOL + " VARCHAR(30) NOT NULL"
                            + ");")){
            System.out.println("Status of query: " + tableCreationStatement.execute());
        }
        catch (SQLException e){
            System.out.println("An exception occurred: "+ e.getMessage());
        }
    }
    public int retrieveAllCompanies(String tableName){
        try(Connection connection = DriverManager.getConnection(URL);
            PreparedStatement distinctCompanyStatement = connection.prepareStatement("SELECT COUNT (*) AS "+ DISTINCT_SYMBOL_COUNT +" FROM "+"(SELECT DISTINCT " + DB_FIELD_SYMBOL + " FROM " + tableName +");");
            PreparedStatement companyStatement = connection.prepareStatement("SELECT DISTINCT " + DB_FIELD_SYMBOL + " FROM " + tableName);
            ResultSet distinctResultSet = distinctCompanyStatement.executeQuery();
            ResultSet companyResultSet = companyStatement.executeQuery()){
            while (companyResultSet.next()){
                companyList.add(companyResultSet.getString(DB_FIELD_SYMBOL));
            }
            return distinctResultSet.getInt(DISTINCT_SYMBOL_COUNT);
        }
        catch (SQLException e){
            System.out.println("An Exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }
    public void createRecord(String tableName) {//Create new record
        try(Connection connection = DriverManager.getConnection(URL);
            PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO " + tableName + "('"
                            + DB_FIELD_QUANTITY +"','"
                            + DB_FIELD_DATE_OF_INVESTMENT + "','"
                            + DB_FIELD_SHARES_BOUGHT_SOLD + "','"
                            + DB_FIELD_SYMBOL
                            + "')"
                            +"VALUES"
                            + "('"+ quantity +"','"+ dateOfInvestment +"','" + shares_bought_sold +"','"+symbol + "');")) {
            System.out.println("Created: " + getNumRowsAffected(insertStatement));
        }
        catch (SQLException e){
            System.out.println("An exception occurred: " + e.getMessage());
        }
    }
    public void retrieveRecord(String tableName) {//Retrieve record
        try(Connection connection = DriverManager.getConnection(URL);
            PreparedStatement retrieveStatement = connection.prepareStatement("SELECT * FROM "+ tableName + " WHERE "
                    + DB_FIELD_ID + "='" + id
                    + "'AND "
                    + DB_FIELD_QUANTITY + "='" + quantity
                    + "'AND "
                    + DB_FIELD_DATE_OF_INVESTMENT+ "='" + dateOfInvestment
                    + "'AND "
                    + DB_FIELD_SYMBOL + "='" + symbol
                    + "';"
            ); ResultSet resultSet = retrieveStatement.executeQuery()) {
            int rowsRetrieved = 0;
            while(resultSet.next()){
                id  = resultSet.getInt(DB_FIELD_ID);
                dateOfInvestment = resultSet.getString(DB_FIELD_DATE_OF_INVESTMENT);
                quantity = resultSet.getInt(DB_FIELD_QUANTITY);
                shares_bought_sold = resultSet.getInt(DB_FIELD_SHARES_BOUGHT_SOLD);
                symbol = resultSet.getString(DB_FIELD_SYMBOL);
                System.out.println("ID: "+ id);
                System.out.println("Date of Investment: " + dateOfInvestment);
                System.out.println("Quantity: " + quantity);
                System.out.println("Shares bought and sold: "+ shares_bought_sold);
                System.out.println("Symbol: " + symbol);
                rowsRetrieved++;
            }
            System.out.println("Retrieved: "+ rowsRetrieved);
        }
        catch (SQLException e){
            System.out.println("An exception occurred: " + e.getMessage());
        }
    }
    public void deleteRecord(String tableName){//Delete Record
        try(Connection connection = DriverManager.getConnection(URL);
            PreparedStatement deleteStatement =
                    connection.prepareStatement("DELETE FROM "+ tableName +" WHERE "
                            + DB_FIELD_QUANTITY + "='" + quantity
                            + "'AND "
                            + DB_FIELD_DATE_OF_INVESTMENT + "='"+ dateOfInvestment
                            + "'AND "
                            + DB_FIELD_SYMBOL + "='" + symbol
                            + "';")) {
            System.out.println("Deleted: " + getNumRowsAffected(deleteStatement));
        }
        catch (SQLException e){
            System.out.println("An error occurred whilst attempting to delete a record. " + e.getMessage());
        }
    }

    public String getFileName(){//Getter method
        return fileName;
    }
    public void setFileName(String fileName){//Setter method
        this.fileName = fileName;
    }
}
