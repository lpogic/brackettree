package brackettree.xray;

import brackettree.writer.BracketTreeWriter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class SpecialXray implements Xray {
    String str;

    public SpecialXray(String str) {
        this.str = str;
    }

    public SpecialXray() {
    }

    @Override
    public String toString(BracketTreeWriter writer) {
        return str;
    }

    @Override
    public String toString() {
        return super.toString() + "{" + str + "}";
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(str);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        str = in.readUTF();
    }
}
