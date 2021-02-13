package brackettree;

import brackettree.writer.StandardInterpreter;
import suite.suite.Subject;

public interface Interpreted {
    default Subject interpret() {
        return StandardInterpreter.interpret(this);
    }
}
