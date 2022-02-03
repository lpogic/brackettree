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
        try {
            return BracketTreeReader.load(file);
        } catch (Exception e) {
            e.printStackTrace();
            return $();
        }
    }

    public static<T> T read(String filePath, Class<T> outputClass) {
        return read(new File(filePath), new ObjectFactory(StandardDiscoverer.getAll()), outputClass);
    }

    public static<T> T read(File file, Class<T> outputClass) {
        return read(file, new ObjectFactory(StandardDiscoverer.getAll()), outputClass);
    }

    public static<T> T read(String filePath, ObjectFactory objectFactory, Class<T> outputClass) {
        return read(new File(filePath), objectFactory, outputClass);
    }

    public static<T> T read(File file, ObjectFactory objectFactory, Class<T> outputClass) {
        try {
            return BracketTreeReader.load(file, objectFactory).as(outputClass);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Subject parse(String tree) {
        try {
            return BracketTreeReader.parse(tree);
        } catch (Exception e) {
            e.printStackTrace();
            return $();
        }
    }

    public static<T> T parse(String tree, Class<T> outputClass) {
        return parse(tree, new ObjectFactory(StandardDiscoverer.getAll()), outputClass);
    }

    public static<T> T parse(String tree, ObjectFactory objectFactory, Class<T> outputClass) {
        try {
            return BracketTreeReader.parse(tree, objectFactory).as(outputClass);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
