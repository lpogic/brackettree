package brackettree;

import brackettree.reader.StandardDiscoverer;
import suite.suite.Subject;

public interface Discovered {
    default void set(Subject sub) {
        StandardDiscoverer.discover(this, sub);
    }
}
