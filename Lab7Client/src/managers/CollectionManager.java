package managers;

import data.Things;

import java.io.Closeable;
import java.io.InputStream;
import java.util.Collection;
import java.util.TreeSet;

/**
 * Created by azamat on 10.11.17.
 */
public interface CollectionManager extends Closeable {

    public String info();

    public String remove(Things thingsForRemove);

    public String removeLower(Things thingForCompare);

    public String add(Things things);

    public String addAll(Collection<Things> c);

    public String update(Things forRemove, Things forAdd);

    public String doImport(InputStream inpStr);

    public TreeSet<Things> selectAll();

}
