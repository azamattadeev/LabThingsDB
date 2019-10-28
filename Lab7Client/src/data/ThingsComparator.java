package data;

import java.util.Comparator;

public class ThingsComparator implements Comparator<Things> {
    @Override
    public int compare(Things things1, Things things2) {
        if ((things1.getOwner().compareTo(things2.getOwner())) != 0) {
            return things1.getOwner().compareTo(things2.getOwner());
        }
        return things1.getName().compareTo(things2.getName());
    }
}

