import java.util.Comparator;

public class TimeOfEventComparator implements Comparator<Event> {
    @Override
    public int compare(Event x, Event y) {
        if (x.time < y.time) {
            return -1;
        } 

        return 0;
    }
}