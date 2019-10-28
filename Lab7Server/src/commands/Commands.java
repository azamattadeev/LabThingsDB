package commands;

import com.google.gson.Gson;
import data.Things;

import java.util.TreeSet;

/**
 * Created by azamat on 12.11.17.
 */

public class Commands {
    public static String createAddCommand(Things thing){
        return "add " + thing.getJson();
    }

    public static String createRemoveCommand(Things things){
        return "remove " + things.getJson();
    }

    public static String createRemoveLowerCommand(Things thing){
        return "remove_lower " + thing.getJson();
    }

    public static String updateCommand(Things remThing, Things addThing){
        return "remove" + remThing.getJson() + "\n" + "add " + addThing.getJson();
    }

    public static String addAllCommand(TreeSet<Things> treeSet){
        Gson gson = new Gson();
        return "addAll " + gson.toJson(treeSet);
    }

}
