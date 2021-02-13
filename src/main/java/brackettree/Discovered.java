package brackettree;

import brackettree.reader.StandardDiscoverer;
import suite.suite.Subject;

public interface Discovered {
    default void discover(Subject sub) {
        StandardDiscoverer.discover(this, sub);
    }
}
