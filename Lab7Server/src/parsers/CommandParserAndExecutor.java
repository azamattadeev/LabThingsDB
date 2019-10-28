package parsers;
/**
 *@author Azamat Tadeev
 */

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import json.JsonSyntaxMistakeException;
import managers.CollectionManager;
import data.*;

import java.util.Collection;
import java.util.TreeSet;

public class CommandParserAndExecutor implements CommandParser {
    private CollectionManager collectionManager;
    private Gson gson;

    public CommandParserAndExecutor(CollectionManager collectionManager){
        this.collectionManager = collectionManager;
        gson = new Gson();
    }

    @Override
    synchronized public String parse(String command){
        String[] commandParts = command.split(" ");
        /*
        Things thingForAction = null;
        if ((fullCommand[0].equals("import") || fullCommand[0].equals("add") || fullCommand[0].equals("remove") || fullCommand[0].equals("remove_lower"))) {
            if(fullCommand.length == 1) {
                System.out.println("Error, " + fullCommand[0] + " must have argument.");
            }
            if((fullCommand.length == 2) && !(fullCommand[0].equals("import"))){
                //Following code will be done if command uses argument in format Json
                try{
                    Gson gson = new Gson();
                    thingForAction = gson.fromJson(fullCommand[1], Things.class);
                    if ((thingForAction == null) || (thingForAction.getName() == null) || (thingForAction.getOwner() == null) || (thingForAction.getOwner().getName() == null)){
                        System.out.println("Error, member for collection is incorrect, maybe you didn't initialized all field");
                    }
                }catch(JsonSyntaxException ex) {
                    System.out.println("Error, member for collection is incorrect");
                }
            }
        }*/
        String argument;
        switch (commandParts[0]){
            case "info":
                return collectionManager.info();
            case "add":
                argument = "";
                for(int i = 1; i < commandParts.length; i++) {
                    argument = argument + commandParts[i];
                }
                return collectionManager.add(gson.fromJson(argument, Things.class));
            case "remove":
                argument = "";
                for(int i = 1; i < commandParts.length; i++) {
                    argument = argument + commandParts[i];
                }
                return collectionManager.remove(gson.fromJson(argument, Things.class));
            case "remove_lower":
                argument = "";
                for(int i = 1; i < commandParts.length; i++) {
                    argument = argument + commandParts[i];
                }
                return collectionManager.removeLower(gson.fromJson(argument, Things.class));
            case "addAll":
                argument = "";
                for(int i = 1; i < commandParts.length; i++) {
                    argument = argument + commandParts[i];
                }
                TreeSet<Things> treeSet = new TreeSet<>();
                try {
                    treeSet = Things.jsonToThingsTreeSet(argument);
                }catch (JsonSyntaxMistakeException jsmEx){
                    System.out.println("Json syntax exception");
                }
                return collectionManager.addAll(treeSet);
            case "selectAll":
                String forRet = gson.toJson(collectionManager.selectAll());
                return forRet;
            case "update":
                argument = "";
                for (int i = 1; i < commandParts.length; i++) {
                    argument = argument + commandParts[i];
                }
                Things[] things = gson.fromJson(argument, Things[].class);
                return collectionManager.update(things[0], things[1]);
            case "connect":
                return "ready";
            //case "exit":
              //  break;
            default:
                System.err.println("Error, unknown command");
                return "Error, unknown command";
        }
    }

}