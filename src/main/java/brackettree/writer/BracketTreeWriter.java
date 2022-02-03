package brackettree.writer;

import brackettree.xray.formal.Xray;
import suite.suite.Subject;
import static suite.suite.$uite.*;

import suite.suite.Suite;
import suite.suite.action.Action;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class BracketTreeWriter {

    private TreeDesigner designer;
    private boolean compact;
    private boolean root;
    private String extendSign = "[";
    private String closeSign = "]";
    private String fenceSign = "\"";

    public BracketTreeWriter() {
        this(new TreeDesigner());
    }

    public BracketTreeWriter(TreeDesigner designer) {
        this.designer = designer;
        compact = false;
        root = false;
    }

    public void save(Object o, File file) throws IOException, BracketTreeWriteException {
        try(var fos = new FileOutputStream(file)) {
            write(o, fos);
            fos.flush();
        }
    }

    public void save(Object o, URL url) throws IOException, BracketTreeWriteException {
        try(var cos = url.openConnection().getOutputStream()) {
            write(o, cos);
        }
    }

    public String encode(Object o) {
        try(var outputStream = new ByteArrayOutputStream()) {
            write(o, outputStream);
            return outputStream.toString();
        } catch (IOException | BracketTreeWriteException e) {
            e.printStackTrace();
            return "";
        }
    }

    public TreeDesigner getDesigner() {
        return designer;
    }

    public void setDesigner(TreeDesigner designer) {
        this.designer = designer;
    }

    public int getExtendSign() {
        return extendSign.codePointAt(0);
    }

    public void setExtendSign(int extendSign) {
        this.extendSign = new String(new int[]{extendSign}, 0, 1);
    }

    public int getCloseSign() {
        return closeSign.codePointAt(0);
    }

    public void setCloseSign(int closeSign) {
        this.closeSign = new String(new int[]{closeSign}, 0, 1);
    }

    public int getFenceSign() {
        return fenceSign.codePointAt(0);
    }

    public void setFenceSign(int fenceSign) {
        this.fenceSign = new String(new int[]{fenceSign}, 0, 1);
    }

    public BracketTreeWriter withDecomposer(Class<?> type, Action decomposer) {
        this.designer.setDecomposer(type, decomposer);
        return this;
    }

    public BracketTreeWriter withDecomposition(Object o, Subject $decomposition) {
        this.designer.setDecomposition(o, $decomposition);
        return this;
    }

    public BracketTreeWriter withElementaryDecomposer(Function<Object, Subject> decomposer) {
        this.designer.setElementaryDecomposer(decomposer);
        return this;
    }

    public BracketTreeWriter withAlias(Class<?> aClass, String alias) {
        this.designer.setClassAlias(aClass, alias);
        return this;
    }

    public BracketTreeWriter with(Subject $params) {
        if($params.present("root")) root = $params.in("root").as(Boolean.class, true);
        if($params.present("compact")) compact = $params.in("compact").as(Boolean.class, true);
        if($params.present("attachingTypes"))
            designer.setAttachingTypes($params.in("attachingTypes").as(Boolean.class, true));
        return this;
    }

    public boolean isCompact() {
        return compact;
    }

    public void setCompact(boolean compact) {
        this.compact = compact;
    }

    public boolean isRoot() {
        return root;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public void write(Object o, OutputStream output) throws BracketTreeWriteException, IOException {

        OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);

        var $ = designer.load(o);

        Suite.export($, writer, root, this::stringify, compact);
        writer.flush();
    }

    public String stringify(Object object) {
        return ((Xray)object).toString(this);
    }

    public String escaped(String str) {
        if(str.startsWith("@") || str.startsWith("#") || str.trim().length() < str.length() ||
                str.contains(extendSign) || str.contains(closeSign)) {
            int i = 0;
            while(str.contains(fenceSign + "^".repeat(i))) ++i;
            return "^".repeat(i) + "\"" + str + "\"" + "^".repeat(i);
        }
        return str;
    }
}
