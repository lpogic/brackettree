package brackettree.xray;

import brackettree.writer.BracketTreeWriter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class StringXray implements Xray {
    String value;

    public StringXray(String value) {
        this.value = value;
    }

    public StringXray() {
    }

    @Override
    public String toString(BracketTreeWriter writer) {
        return writer.escaped(value);
    }

    @Override
    public String toString() {
        return super.toString() + "{" + value + "}";
    }

    public String getValue() {
        return value;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(value);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        value = in.readUTF();
    }
}
