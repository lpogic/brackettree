package brackettree;

import brackettree.reader.BracketTreeReader;
import brackettree.reader.ObjectFactory;
import brackettree.reader.StandardDiscoverer;
import brackettree.writer.BracketTreeWriteException;
import brackettree.writer.BracketTreeWriter;
import brackettree.writer.TreeDesigner;
import suite.suite.Subject;

import java.io.*;
import java.net.URL;

import static suite.suite.$uite.$;

public class BracketTree {

    public static Subject read(String filePath) {
        return read(new File(filePath));
    }

    public static Subject read(File file) {
        return read(file, new ObjectFactory(StandardDiscoverer.getAll()));
    }

    public static Subject read(String filePath, ObjectFactory objectFactory) {
        return read(new File(filePath), objectFactory);
    }

    public static Subject read(File file, ObjectFactory objectFactory) {
        try {
            return BracketTreeReader.load(file, objectFactory);
        } catch (Exception e) {
            e.printStackTrace();
            return $();
        }
    }

    public static Subject parse(String tree) {
        return parse(tree, new ObjectFactory(StandardDiscoverer.getAll()));
    }

    public static Subject parse(String tree, ObjectFactory objectFactory) {
        try {
            return BracketTreeReader.parse(tree, objectFactory);
        } catch (Exception e) {
            e.printStackTrace();
            return $();
        }
    }

    public static boolean write(Object object, String filePath) {
        return write(object, new File(filePath), new TreeDesigner());
    }

    public static boolean write(Object object, File file) {
        return write(object, file, new TreeDesigner());
    }

    public static boolean write(Object object, URL url) {
        return write(object, url, new TreeDesigner());
    }

    public static String encode(Object o) {
        return encode(o, new TreeDesigner());
    }

    public static boolean write(Object object, String filePath, TreeDesigner treeDesigner) {
        return write(object, new File(filePath), treeDesigner);
    }

    public static boolean write(Object object, File file, TreeDesigner treeDesigner) {
        try {
            new BracketTreeWriter(treeDesigner).save(object, file);
            return true;
        } catch (IOException | BracketTreeWriteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean write(Object object, URL url, TreeDesigner treeDesigner) {
        try {
            new BracketTreeWriter(treeDesigner).save(object, url);
            return true;
        } catch (IOException | BracketTreeWriteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String encode(Object o, TreeDesigner treeDesigner) {
        return new BracketTreeWriter(treeDesigner).encode(o);
    }

    public static BracketTreeWriter writer() {
        return new BracketTreeWriter(new TreeDesigner());
    }
}
