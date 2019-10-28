package managers; /**
 *@author Azamat Tadeev
*/

import com.google.gson.Gson;
import json.JsonSyntaxMistakeException;

import java.io.*;
import java.util.*;
import data.Things;
import data.ThingsComparator;

public class ThingsCollectionManager extends ChainCollectionManager implements JsonTreeSetCollectionManager {
    private TreeSet<Things> thingsTreeSet;
    private Date initDate;

    public ThingsCollectionManager(InputStream inputStream) {
        this.initDate = new Date();
        thingsTreeSet = new TreeSet<Things>(new ThingsComparator());
        if(inputStream != null) {
            doImport(inputStream);
        }
    }

    public ThingsCollectionManager() {
        this.initDate = new Date();
        thingsTreeSet = new TreeSet<Things>(new ThingsComparator());
    }

    /**
     * Method shows information about storing collection
     */
    @Override
    synchronized public String info() {
        String s ="Collection has type TreeSet and contains Things objects.\n" +
        "it initialization data is " + initDate + "\n" +
        "it contains " + thingsTreeSet.size() + " elements now.\n";
        if (getUnderstudy() != null){
            s+= getUnderstudy().info();
        }
        return s;
    }

    /**
     * If collection contains this item (argument thingsForRemove) then delete it.
     * @param thingForRemove : (Things) - Object of class Things
     */
    @Override
    synchronized public String remove(Things thingForRemove) {
        String s;
        if (thingsTreeSet.remove(thingForRemove)) {
            s= "Successfully!";
            if(getUnderstudy() != null){
                getUnderstudy().remove(thingForRemove);
            }
        }
        else s= "Collection doesn't contain this object";
        return s;
    }

    /**
     * Method removes collection's items which lower than argument thingsForCompare.
     * @param thingForCompare (Things) - Object of class Things
     */
    @Override
    synchronized public String removeLower(Things thingForCompare) {
        Iterator<Things> iterator = thingsTreeSet.iterator();
        Comparator comparator = thingsTreeSet.comparator();
        int count = 0;
        while (iterator.hasNext()) {
            Things anotherThings = iterator.next();
            if (comparator.compare(thingForCompare, anotherThings) > 0) {
                iterator.remove();
                count++;
            }
        }
        if(getUnderstudy() != null){
            getUnderstudy().removeLower(thingForCompare);
        }
        return count + " Objects was deleted";
    }

    /**
     * Method adds item for storing collection.
     * @param thing : (Things) - Object of class Things
     */
    @Override
    synchronized public String add(Things thing) {
        String s;
        if (thingsTreeSet.add(thing)) {
            s = "Successfully!";
            if (getUnderstudy() != null){
                getUnderstudy().add(thing);
            }
        }else{
            s = "Error. This object before was added";
        }
        return s;
    }

    @Override
    synchronized public String update(Things forRemove, Things forAdd){
        if(thingsTreeSet.remove(forRemove)) {
            thingsTreeSet.add(forAdd);
            return getUnderstudy().update(forRemove, forAdd);
        }else return "Your data was outdated";
    }



    /**
     * Method imports items for storing collection from file.
     * @param inpStr:(java.io.File) - InputStream for reading
     */
    synchronized public String doImport(InputStream inpStr) {
        String rez = "";
        try{
            String JsonString = readJsonFromStream(inpStr);
            if (!(Things.jsonToThingsTreeSet(JsonString).isEmpty())){
                thingsTreeSet.addAll(Things.jsonToThingsTreeSet(JsonString));
                rez += "Completely members was added\n";
                if(getUnderstudy() != null) {
                    getUnderstudy().addAll(Things.jsonToThingsTreeSet(JsonString));
                }
            }else rez += "Nothing added, maybe this collection is empty\n";
        }catch (JsonSyntaxMistakeException ex){
            rez += "Incorrect format, check syntax, and try again\n";
        }catch (IOException ex){
            rez += ("Input Error: " + ex.getMessage());
        }
        return rez;
    }

    @Override
    synchronized public String addAll(Collection<Things> collection){
        if (thingsTreeSet.addAll(collection)){
            if(getUnderstudy() != null) {
                getUnderstudy().addAll(collection);
            }
            return "Elements was added";
        }else {
            return "Error. Collection has contain these elements yet";
        }
    }

    @Override
    public TreeSet<Things> selectAll(){
        return getTreeSetCopy();
    }

    @Override
    public void importUnderstudy(){
        if (getUnderstudy() != null){
            addAll(getUnderstudy().selectAll());
        }
    }

    /**
     * @param inputStream:(java.io.File) - stream for reading
     * @return string in format json (serialized object)
     * @throws IOException - throws Input-Output Exceptions
     */
    private String readJsonFromStream(InputStream inputStream) throws IOException {
        try(
                BufferedInputStream buffInpStream = new BufferedInputStream(inputStream);
        ) {
            LinkedList<Byte> collectionBytesList = new LinkedList<>();
            while (buffInpStream.available() > 0) {
                collectionBytesList.add((byte) buffInpStream.read());
            }
            char[] collectionChars = new char[collectionBytesList.size()];
            for (int i = 0; i < collectionChars.length; i++) {
                collectionChars[i] = (char) (byte) collectionBytesList.get(i);
            }
            return new String(collectionChars);
        }
    }

    /**
     * Gets collection in json format
     */
    synchronized public String getCollectionInJson() {
        return new Gson().toJson(thingsTreeSet);
    }

    synchronized public Things[] getCollectionArray(){
        Object[] objects = thingsTreeSet.toArray();
        Things[] things = new Things[objects.length];
        int count = 0;
        for(Object ob : objects){
            things[count++] = (Things) ob;
        }
        return things;
    }

    synchronized public TreeSet<Things> getTreeSetCopy(){
        return new TreeSet<>(thingsTreeSet);
    }

    @Override
    public void close(){
        try {
            if(getUnderstudy() != null) {
                getUnderstudy().close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
