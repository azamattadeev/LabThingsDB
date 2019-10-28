package managers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import commands.Commands;
import data.Things;
import inet.client.Connector;
import json.JsonSyntaxMistakeException;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Created by azamat on 12.11.17.
 */
public class InternetCollectionManager extends JsonTreeSetCollectionManager {
    private Connector connector;

    public InternetCollectionManager(Connector connector){
        this.connector = connector;
        thingsTreeSet = new TreeSet<>();
    }

    @Override
    public String info(){
        return connector.send(Commands.createInfoCommand());
    }

    @Override
    public String add(Things thing){
        String res = connector.send(Commands.createAddCommand(thing));
        if (res.equals("Successfully!")) thingsTreeSet.add(thing);
        return res;
    }

    @Override
    public String remove(Things thingForRemove) {
        String res = connector.send(Commands.createRemoveCommand(thingForRemove));
        if (res.equals("Successfully!")) thingsTreeSet.remove(thingForRemove);
        return res;
    }



    @Override
    public String removeLower(Things thingForCompare) {
        String res = connector.send(Commands.createRemoveLowerCommand(thingForCompare));
        if (res.equals("Successfully!")){
            Iterator<Things> iterator = thingsTreeSet.iterator();
            Comparator comparator = thingsTreeSet.comparator();
            while (iterator.hasNext()) {
                Things anotherThings = iterator.next();
                if (comparator.compare(thingForCompare, anotherThings) > 0) {
                    iterator.remove();
                }
            }
        }
        return res;
    }

    @Override
    public String update(Things forRemove, Things forAdd){
        String res = connector.send(Commands.createUpdateCommand(forRemove, forAdd));
        if (res.equals("Successfully!")){
            if (thingsTreeSet.remove(forRemove)) thingsTreeSet.add(forAdd);
        }
        return res;
    }

    @Override
    public String addAll(Collection<Things> collection) {
        thingsTreeSet.addAll(collection);
        String res = connector.send(Commands.createAddAllCommand(collection));
        if (res.equals("Successfully!")){
            thingsTreeSet.addAll(collection);
        }
        return res;
    }

    @Override
    public String doImport(InputStream inpStr) {
        String data;
        try {
            data = readJsonFromStream(inpStr);
        }catch (IOException ioEx){
            return "Error. Input exception";
        }
        TreeSet<Things> treeSet = new TreeSet<>();
        try {
            treeSet.addAll(Things.jsonToThingsTreeSet(data));
        }catch (JsonSyntaxMistakeException jsmEx){
            return "Error. Incorrect format";
        }
        String res = addAll(treeSet);
        if (res.equals("Successfully!")){
            thingsTreeSet.addAll(treeSet);
        }
        return res;
    }

    @Override
    public TreeSet<Things> selectAll(){
        try {
            String json = connector.send(Commands.createSelectAllCommand());
            if(!json.equals("Server is not responding!")) {
                TreeSet<Things> treeSet = new TreeSet<>();
                try {
                    treeSet = Things.jsonToThingsTreeSet(json);
                    thingsTreeSet = treeSet;
                }catch (JsonSyntaxException e){
                    e.printStackTrace();
                }
                return thingsTreeSet;
            }else{
                JFrame frame = new JFrame();
                JOptionPane.showMessageDialog(frame,json);
                return new TreeSet<>();
            }
        }catch (JsonSyntaxMistakeException jsmEx){
            //jsmEx.printStackTrace();
            return thingsTreeSet;
        }
    }

    @Override
    public void close(){
        connector.close();
    }

}
