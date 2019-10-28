package data;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import json.JsonSyntaxMistakeException;
import java.util.TreeSet;

/**
 *@author Azamat Tadeev
 */

public class Things implements Comparable<Things> {
    private String name;
    private Person owner;

    public Things(String name, Person owner){
        this.name = name;
        this.owner = owner;
    }

    public String getName(){
        return this.name;
    }

    public Person getOwner(){
        return this.owner;
    }

    public boolean equals(Object ob){
        if (ob.getClass() != this.getClass()) return false;
        Things thing2 = (Things) ob;
        return (this.name == thing2.getName()) && (this.owner.equals(thing2.getOwner()));
    }

    public String getJson(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public int compareTo(Things thing2) {
        return (owner.compareTo(thing2.getOwner()) != 0) ? owner.compareTo(thing2.getOwner()) : this.name.compareTo(thing2.getName());
    }

    public static TreeSet<Things> jsonToThingsTreeSet(String jsonThingsTreeSet) throws JsonSyntaxMistakeException {
        try {
            Gson gson = new Gson();
            TreeSet<Things> thingsTreeSet = new TreeSet<>(new ThingsComparator());
            int noInitializedCount = 0;
            if (jsonThingsTreeSet.length() != 0) {
                Things[] thingsArray = gson.fromJson(jsonThingsTreeSet, Things[].class);
                for (Things i : thingsArray) {
                    if ((i != null) && (i.getName() != null) && (i.getOwner() != null) && (i.getOwner().getName() != null)) {
                        thingsTreeSet.add(i);
                    }else noInitializedCount++;
                }
            }
            if (noInitializedCount > 0) System.out.println("Найдено " + noInitializedCount + " не полностью инициализированных элементов");
            return thingsTreeSet;
        }catch (JsonSyntaxException ex){
            ex.printStackTrace();
            throw new JsonSyntaxMistakeException();
        }
    }

}



