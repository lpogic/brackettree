package brackettree.xray;

import brackettree.writer.BracketTreeWriter;

import java.io.Externalizable;

public interface Xray extends Externalizable {
    String toString(BracketTreeWriter writer);
}
