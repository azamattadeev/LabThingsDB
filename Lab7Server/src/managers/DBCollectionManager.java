package managers;

import com.sun.rowset.FilteredRowSetImpl;

import javax.sql.rowset.FilteredRowSet;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import data.*;
import org.postgresql.util.PSQLException;

/**
 * Created by azamat on 10.11.17.
 */
public class DBCollectionManager extends ChainCollectionManager {
    private String dbURL;
    private String username;
    private Connection dbConnection;
    private CallableStatement addCallStat;
    private CallableStatement removeLowerCallStat;
    private CallableStatement removeCallStat;
    private CallableStatement containsThingCallStat;
    private LinkedList<AutoCloseable> forClose;
    private int maxPersonID;
    private int maxThingID;
    private String getAllDataQuery = "Select things.id, things.name, person.id AS person_id, person.name AS person_name, person.courage AS person_courage From things join person on things.owner_id=person.id;";

    private DBCollectionManager(Connection connection, String dbURL, String username){
        this.dbURL = dbURL;
        maxPersonID = Integer.MIN_VALUE;
        maxThingID = Integer.MIN_VALUE;
        this.username = username;
        forClose = new LinkedList<>();
        try {
            dbConnection = connection;
            dbConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            addCallStat = dbConnection.prepareCall("{call add(?,?,?,?,?)}");
            removeLowerCallStat = dbConnection.prepareCall("{call remove_lower(?,?,?)}");
            removeCallStat = dbConnection.prepareCall("{call remove(?,?,?)}");
            containsThingCallStat = dbConnection.prepareCall("{call containsThing(?,?,?)}");
            forClose.add(dbConnection);
            forClose.add(addCallStat);
            forClose.add(removeCallStat);
            forClose.add(containsThingCallStat);
            forClose.add(removeLowerCallStat);
            try(Statement statement = dbConnection.createStatement()) {
                ResultSet rs = statement.executeQuery("SELECT max(id) FROM things");
                rs.next();
                maxThingID = rs.getInt(1);
                rs = statement.executeQuery("SELECT max(id) FROM person");
                rs.next();
                maxPersonID = rs.getInt(1);
            }
        }catch (SQLException sqlEx){
            sqlEx.printStackTrace();
            close();
        }
    }

    public static DBCollectionManager createDBCollectionManager(String dbURL, String username, String password) {
        try{
            Connection connection = DriverManager.getConnection(dbURL, username, password);
            return new DBCollectionManager(connection, dbURL, username);
        }catch (SQLException sqlEx){
            System.out.println("Database isn't responding!");
            return null;
        }
    }

    @Override
    public String info(){
        String rez = "";
        rez+= "Database url: " + this.dbURL + ".\n";
        rez+= "DBMS: PostgreSQL.";
        return rez;
    }

    @Override
    synchronized public String remove(Things thingsForRemove) {
        try {
            if(containsThing(thingsForRemove)) {
                removeCallStat.setString(1, thingsForRemove.getName());
                removeCallStat.setString(2, thingsForRemove.getOwner().getName());
                removeCallStat.setInt(3, thingsForRemove.getOwner().getCourage());
                removeCallStat.execute();
                return "Successfully!";
            }else{
                return "Error. Database doesn't contain this thing";
            }
        }catch (SQLException sqle){
            sqle.printStackTrace();
            return "Error. thing didn't delete";
        }
    }

    @Override
    synchronized public String removeLower(Things thingForCompare) {
        try {
            removeLowerCallStat.setString(1, thingForCompare.getName());
            removeLowerCallStat.setString(2, thingForCompare.getOwner().getName());
            removeLowerCallStat.setInt(3, thingForCompare.getOwner().getCourage());
            removeLowerCallStat.execute();
            return "Successfully!";
        }catch (SQLException sqlEx){
            sqlEx.printStackTrace();
            return "Error. things didn't delete";
        }
    }

    @Override
    synchronized public String add(Things things) {
        try {
            if (!containsThing(things)) {
                int thingID = ++maxThingID, personID = ++maxPersonID;
                addCallStat.setInt(1, personID);
                addCallStat.setString(2, things.getOwner().getName());
                addCallStat.setInt(3, things.getOwner().getCourage());
                addCallStat.setInt(4, thingID);
                addCallStat.setString(5, things.getName());
                addCallStat.execute();
                return "Successfully!";
            }else {
                return "Error! Database has contain this thing yet";
            }
        }catch (SQLException ex){
            ex.printStackTrace();
            maxThingID--;
            maxPersonID--;
            return "Error. Thing didn't was added.";
        }
    }

    @Override
    public String update(Things forRemove, Things forAdd){
        if (remove(forRemove).trim().equals("Successfully!")){
            String addRez = add(forAdd);
            if (addRez.equals("Successfully!")) {
                return "Update: Successfully!";
            }else {
                return "Your data was outdated";
            }
        }else {
            return "Your data was outdated";
        }
    }

    @Override
    synchronized public String addAll(Collection<Things> collection) {
        try{
            dbConnection.setAutoCommit(false);
            Iterator<Things> iterator = collection.iterator();
            int count = 1;
            int successCount = 0;
            while (iterator.hasNext()){
                Savepoint savepoint = dbConnection.setSavepoint("s" + count++);
                try {
                    Things thing = iterator.next();
                    int thingID = ++maxThingID, personID = ++maxPersonID;
                    if (!containsThing(thing)) {
                        addCallStat.setInt(1, personID);
                        addCallStat.setString(2, thing.getOwner().getName());
                        addCallStat.setInt(3, thing.getOwner().getCourage());
                        addCallStat.setInt(4, thingID);
                        addCallStat.setString(5, thing.getName());
                        addCallStat.execute();
                        successCount++;
                    }
                }catch (SQLException sqlEx){
                    sqlEx.printStackTrace();
                    dbConnection.rollback(savepoint);
                    maxThingID--;
                    maxPersonID--;
                }
            }
            dbConnection.commit();
            dbConnection.setAutoCommit(true);
            return successCount + " things were added!";
        }catch (SQLException sqlEx){
            System.out.println("Unsuccessfully! Can't deactivate auto-commit");
            sqlEx.printStackTrace();
            return "Unsuccessfully! Transaction was failed";
        }
    }

    @Override
    synchronized public String doImport(InputStream inpStr) {
        return "Use import across client app";
    }

    @Override
    synchronized public TreeSet<Things> selectAll(){
        TreeSet<Things> thingsTreeSet = new TreeSet<>();
        try(Statement statement = dbConnection.createStatement()) {
            FilteredRowSet filteredRowSet = new FilteredRowSetImpl();
            filteredRowSet.populate(statement.executeQuery(getAllDataQuery));
            while(filteredRowSet.next()){
                int thingID = filteredRowSet.getInt("id");
                if (maxThingID < thingID) maxThingID = thingID;
                String thingName = filteredRowSet.getString("name");
                int personID = filteredRowSet.getInt("person_id");
                if (maxPersonID < personID) maxPersonID = personID;
                String personName = filteredRowSet.getString("person_name");
                int personCourage = filteredRowSet.getInt("person_courage");
                Things thing = new Things(thingName,new Person(personName,personCourage));
                thingsTreeSet.add(thing);
            }
        }catch (SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        return thingsTreeSet;
    }

    @Override
    public void importUnderstudy(){
        if (getUnderstudy() != null){
            addAll(getUnderstudy().selectAll());
        }
    }

    public Boolean containsThing(Things thing){
        try {
            containsThingCallStat.setString(1, thing.getName());
            containsThingCallStat.setString(2, thing.getOwner().getName());
            containsThingCallStat.setInt(3, thing.getOwner().getCourage());
            containsThingCallStat.execute();
            ResultSet rs = containsThingCallStat.getResultSet();
            rs.next();
            return rs.getBoolean(1);
        }catch (SQLException sqlEx){
            sqlEx.printStackTrace();
            return null;
        }
    }

    @Override
    synchronized public void close(){
        Iterator<AutoCloseable> iterator = forClose.iterator();
        while (iterator.hasNext()){
            try{
                iterator.next().close();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        try {
            if(getUnderstudy() != null) {
                getUnderstudy().close();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


}