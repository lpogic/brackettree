package brackettree.xray;

import brackettree.writer.BracketTreeWriter;

import java.io.ObjectInput;
import java.io.ObjectOutput;

public class AutoXray implements Xray {

    @Override
    public String toString(BracketTreeWriter writer) {
        return "";
    }

    @Override
    public String toString() {
        return super.toString() + "{}";
    }

    @Override
    public void writeExternal(ObjectOutput out) {}

    @Override
    public void readExternal(ObjectInput in) {}
}
