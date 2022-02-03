package brackettree.reader;


import suite.suite.Subject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static suite.suite.$uite.$;

public class BracketTreeReader {

    public static Subject parse(String tree) throws IOException {
        try(var bais = new ByteArrayInputStream(tree.getBytes())) {
            var btr = new BracketTreeReader(bais);
            return btr.next();
        }
    }

    public static Subject load(File file) throws IOException {
        try(var fis = new FileInputStream(file)) {
            var btr = new BracketTreeReader(fis);
            return btr.next();
        }
    }

    public static Subject load(URL url) throws IOException {
        try(var cis = url.openConnection().getInputStream()) {
            var btr = new BracketTreeReader(cis);
            return btr.next();
        }
    }

    public static Subject parse(String tree, ObjectFactory objectFactory) throws IOException {
        try(var bais = new ByteArrayInputStream(tree.getBytes())) {
            var btr = new BracketTreeReader(bais, new BracketTreeProcessor() {
                @Override
                public Subject finish() {
                    return objectFactory.load(super.finish());
                }
            });
            return btr.next();
        }
    }

    public static Subject load(File file, ObjectFactory objectFactory) throws IOException {
        try(var fis = new FileInputStream(file)) {
            var btr = new BracketTreeReader(fis, new BracketTreeProcessor() {
                @Override
                public Subject finish() {
                    return objectFactory.load(super.finish());
                }
            });
            return btr.next();
        }
    }

    public static Subject load(URL url, ObjectFactory objectFactory) throws IOException {
        try(var cis = url.openConnection().getInputStream()) {
            var btr = new BracketTreeReader(cis, new BracketTreeProcessor() {
                @Override
                public Subject finish() {
                    return objectFactory.load(super.finish());
                }
            });
            return btr.next();
        }
    }

    private final InputStream inputStream;
    private final BracketTreeProcessor processor;
    private Subject next;
    boolean hasNext;
    boolean hasNextFired;

    public BracketTreeReader(InputStream inputStream) {
        this(inputStream, new BracketTreeProcessor());
    }

    public BracketTreeReader(InputStream inputStream, BracketTreeProcessor processor) {
        this.inputStream = inputStream;
        this.processor = processor;
    }

    public boolean hasNext() {
        if(hasNextFired) return hasNext;
        hasNextFired = true;
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
            next = processor.finish();
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
