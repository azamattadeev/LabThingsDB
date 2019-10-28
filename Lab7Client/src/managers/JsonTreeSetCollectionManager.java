package managers;

import com.google.gson.Gson;
import data.Things;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.TreeSet;

/**
 * Created by azamat on 10.11.17.
 */
public abstract class JsonTreeSetCollectionManager implements CollectionManager {
    TreeSet<Things> thingsTreeSet;

    public String getCollectionInJson() {
        return new Gson().toJson(thingsTreeSet);
    }

    public TreeSet<Things> getTreeSetCopy(){
        return new TreeSet<>(thingsTreeSet);
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

    /**
     * @param inputStream:(java.io.File) - stream for reading
     * @return string in format json (serialized object)
     * @throws IOException - throws Input-Output Exceptions
     */
    String readJsonFromStream(InputStream inputStream) throws IOException {
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

}
