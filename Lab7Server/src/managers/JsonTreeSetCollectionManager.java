package managers;

import java.util.TreeSet;
import data.Things;

/**
 * Created by azamat on 10.11.17.
 */
public interface JsonTreeSetCollectionManager extends CollectionManager {

    public String getCollectionInJson();

    public TreeSet<Things> getTreeSetCopy();

    public Things[] getCollectionArray();

}
