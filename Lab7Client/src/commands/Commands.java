package commands;

import com.google.gson.Gson;
import data.Things;

import java.util.Collection;

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

    public static String createUpdateCommand(Things forRemove, Things forAdd){
        Things[] things = {forRemove, forAdd};
        Gson gson = new Gson();
        return "update " + gson.toJson(things);
    }

    public static String createAddAllCommand(Collection<Things> treeSet){
        Gson gson = new Gson();
        return "addAll " + gson.toJson(treeSet);
    }

    public static String createInfoCommand(){
        return "info";
    }

    public static String createSelectAllCommand(){
        return "selectAll";
    }

    public static String createConnectCommand(){
        return "connect";
    }

}
