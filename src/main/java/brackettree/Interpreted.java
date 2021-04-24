package brackettree;

import brackettree.reader.StandardDiscoverer;
import brackettree.writer.StandardInterpreter;
import suite.suite.Subject;

public interface Interpreted {
    default Subject interpret() {
        return StandardInterpreter.interpret(this);
    }
    default void discover(Subject sub) {
        StandardDiscoverer.discover(this, sub);
    }
}
