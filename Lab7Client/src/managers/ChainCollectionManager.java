package managers;

import data.Things;

import java.util.TreeSet;

/**
 * Created by azamat on 10.11.17.
 */
public abstract class ChainCollectionManager extends JsonTreeSetCollectionManager implements CollectionManager {
    private ChainCollectionManager understudy;

    public ChainCollectionManager getUnderstudy() {
        return understudy;
    }

    public void setUnderstudy(ChainCollectionManager understudy){
        this.understudy = understudy;
        importUnderstudy();
    }

    public abstract TreeSet<Things> selectAll();

    public abstract void importUnderstudy();

}
