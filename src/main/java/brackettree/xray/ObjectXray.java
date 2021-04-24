package brackettree.xray;

import brackettree.writer.BracketTreeWriter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

public class ObjectXray implements Xray {
    transient Object object;
    transient int usages;
    String refId;

    public ObjectXray(Object object) {
        this.object = object;
        usages = 0;
    }

    public ObjectXray() {
    }

    @Override
    public boolean equals(Object o1) {
        if (this == o1) return true;
        if (o1 == null || getClass() != o1.getClass()) return false;
        ObjectXray that = (ObjectXray) o1;
        return object == that.object;
    }

    @Override
    public int hashCode() {
        return Objects.hash(object);
    }

    @Override
    public String toString(BracketTreeWriter writer) {
        return "#" + refId;
    }

    @Override
    public String toString() {
        return super.toString() + "{" + object + "}";
    }

    public Object getObject() {
        return object;
    }

    public int getUsages() {
        return usages;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public int use() {
        return usages++;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(refId);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        refId = in.readUTF();
    }
}
