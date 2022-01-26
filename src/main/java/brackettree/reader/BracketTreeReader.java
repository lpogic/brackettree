package brackettree.reader;


import suite.suite.Subject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static suite.suite.$uite.$;

public class BracketTreeReader {

    public static Subject parse(String tree, ObjectFactory objectFactory) throws IOException {
        try(var bais = new ByteArrayInputStream(tree.getBytes())) {
            var btr = new BracketTreeReader(bais, objectFactory);
            return btr.next();
        }
    }

    public static Subject load(File file, ObjectFactory objectFactory) throws IOException {
        try(var fis = new FileInputStream(file)) {
            var btr = new BracketTreeReader(fis, objectFactory);
            return btr.next();
        }
    }

    public static Subject load(URL url, ObjectFactory objectFactory) throws IOException {
        try(var cis = url.openConnection().getInputStream()) {
            var btr = new BracketTreeReader(cis, objectFactory);
            return btr.next();
        }
    }

    private final InputStream inputStream;
    private final ObjectFactory factory;
    private Subject next;
    boolean hasNext;
    boolean hasNextFired;

    public BracketTreeReader(InputStream inputStream) {
        this(inputStream, new ObjectFactory(StandardDiscoverer.getAll()));
    }

    public BracketTreeReader(InputStream inputStream, ObjectFactory factory) {
        this.inputStream = inputStream;
        this.factory = factory;
    }

    public boolean hasNext() {
        if(hasNextFired) return hasNext;
        hasNextFired = true;
        BracketTreeProcessor processor = new BracketTreeProcessor();
        processor.getReady();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            int code = reader.read();
            if(code == -1) {
                next = null;
                return hasNext = false;
            }
            while (code != -1 && code != 23) {
                processor.advance(code);
                code = reader.read();
            }
            next = factory.load(processor.finish());
            return hasNext = true;
        }catch(Exception e) {
            throw new BracketTreeReadException(e);
        }
    }

    public Subject next() {
        if(!hasNextFired) hasNext();
        if(hasNext) {
            hasNextFired = false;
            var n = next;
            next = null;
            return n;
        } else {
            return $();
        }
    }
}
