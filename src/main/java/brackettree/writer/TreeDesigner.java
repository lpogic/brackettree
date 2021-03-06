package brackettree.writer;

import brackettree.Interpreted;
import suite.suite.SolidSubject;
import suite.suite.Subject;
import static suite.suite.$uite.*;

import suite.suite.Suite;
import suite.suite.action.Action;
import suite.suite.util.Series;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class TreeDesigner {

    interface Xray {
        String toString(BracketTreeWriter writer);
    }

    static class ObjectXray implements Xray {
        Object o;
        int usages;
        String refId;

        public ObjectXray(Object o) {
            this.o = o;
            usages = 0;
        }

        @Override
        public boolean equals(Object o1) {
            if (this == o1) return true;
            if (o1 == null || getClass() != o1.getClass()) return false;
            ObjectXray that = (ObjectXray) o1;
            return o == that.o;
        }

        @Override
        public int hashCode() {
            return Objects.hash(o);
        }

        @Override
        public String toString(BracketTreeWriter writer) {
            return "#" + refId;
        }

        @Override
        public String toString() {
            return super.toString() + "{" + o + "}";
        }
    }

    static class AutoXray implements Xray {

        @Override
        public String toString(BracketTreeWriter writer) {
            return "";
        }

        @Override
        public String toString() {
            return super.toString() + "{}";
        }
    }

    static class StringXray implements Xray {
        String str;

        public StringXray(String str) {
            this.str = str;
        }

        @Override
        public String toString(BracketTreeWriter writer) {
            return writer.escaped(str);
        }

        @Override
        public String toString() {
            return super.toString() + "{" + str + "}";
        }
    }

    static class SpecialXray implements Xray {
        String str;

        public SpecialXray(String str) {
            this.str = str;
        }

        @Override
        public String toString(BracketTreeWriter writer) {
            return str;
        }

        @Override
        public String toString() {
            return super.toString() + "{" + str + "}";
        }
    }

    static final Xray idXray = new SpecialXray("#");
    static final Xray classXray = new SpecialXray("@");

    Subject $refs = $();
    Subject $decompositions = $();

    Subject $decomposers = $();
    Function<Object, Subject> elementaryDecomposer;
    boolean attachingTypes;
    Subject $classAliases = $();

    public TreeDesigner() {
        setDecomposers(StandardInterpreter.getAllSupported());
        $classAliases.alter(Suite.
                put(Integer.class, "int").
                put(int.class, "int").
                put(Double.class, "double").
                put(double.class, "double").
                put(Float.class, "float").
                put(float.class, "float").
                put(List.class, "list").
                put(SolidSubject.class, "subject").
                put(String.class, "string")
        );
        elementaryDecomposer = o -> {
            if(o == null) return $("null");
            if(o instanceof String) return $(o);
            if(o instanceof Integer) return $(o.toString());
            if(o instanceof Double) return $(o.toString());
            if(o instanceof Float) return $(o.toString());
            if(o instanceof Boolean) return $(o.toString());
            return $();
        };
        attachingTypes = true;
    }

    public boolean isAttachingTypes() {
        return attachingTypes;
    }

    public void setAttachingTypes(boolean attachingTypes) {
        this.attachingTypes = attachingTypes;
    }

    public void setDecomposition(Object o, Subject $) {
        $decompositions.inset(o, $);
    }

    public void setDecomposer(Class<?> type, Action decomposer) {
        $decomposers.put(type, decomposer);
    }

    public<T> void setDecomposer(Class<T> type, BiConsumer<T, TreeDesigner> decomposer) {
        $decomposers.put(type, decomposer);
    }

    public void setDecomposers(Series $decomposers) {
        this.$decomposers.alter($decomposers.select(v -> v.is(Class.class) &&
                (v.in().is(Action.class) || v.in().is(BiConsumer.class))
        ));
    }

    public void setElementaryDecomposer(Function<Object, Subject> elementaryDecomposer) {
        this.elementaryDecomposer = elementaryDecomposer;
    }

    public void setClassAlias(Class<?> aClass, String alias) {
        $classAliases.in(aClass).set(alias);
    }

    public Subject load(Object o) {
        $refs = $();
        var xray = xray(o);
        var $xRoot = $(xray);
        int id = 0;
        for(var $i : preDfs$(inset$($xRoot)).eachIn()) {
            for(var $i1 : $i) {
                if($i1.is(ObjectXray.class)) {
                    ObjectXray x = $i1.asExpected();

                    if (x.usages < 2 && $i.size() == 1 && $i1.in().absent()) {
                        $i.unset().alter($refs.in(x));
                    } else {
                        if (x.refId == null) {
                            x.refId = "" + id++;
                            var $r = $refs.in(x).get();
                            if($i.size() == 1 && $i1.in().absent()) {
                                $r.aimedInset($r.first().raw(), idXray, $(new StringXray(x.refId)));
                                $i.unset().alter($r);
                            } else {
                                $i.shift(x, new SpecialXray("##" + x.refId));
                                $xRoot.inset(new SpecialXray("#" + x.refId), $r);
                            }
                        } else if(!($i.size() == 1 && $i1.in().absent())) {
                            $i.shift(x, new SpecialXray("##" + x.refId));
                        }
                    }
                }
            }
        }
        if(xray instanceof ObjectXray && ((ObjectXray) xray).usages > 1) return $xRoot.at(1);
        else return $xRoot;
    }

    Xray xray(Object o) {
        if(o instanceof Xray) return (Xray) o;
        var $prim = elementaryDecomposer.apply(o);
        if($prim.present()) return new StringXray($prim.asExpected());
        if(o instanceof String) return new StringXray($prim.asExpected());
        if(o instanceof Suite.Auto) return new AutoXray();

        ObjectXray xray = $refs.sate(new ObjectXray(o)).asExpected();
        if(xray.usages++ < 1) {
            var $ = decompose(o);
            $refs.inset(xray, $);
            for(var $i : preDfs$(inset$($)).eachIn()) {
                for(var i : $i.eachRaw()) {
                    $i.shift(i, xray(i));
                }
            }
        }

        return xray;
    }

    Subject decompose(Object o) {

        if($decompositions.present(o)) return $decompositions.in(o).get();

        Class<?> type = o.getClass();

        var $decomposer = $decomposers.in(type).get();
        if($decomposer.present()) {
            if ($decomposer.is(Action.class)) {

                Action decomposer = $decomposer.asExpected();
                var $r = decomposer.play($(o));
                if(isAttachingTypes()) attachType($r, type);
                $decompositions.inset(o, $r);
                return $r;
            } else if ($decomposer.is(BiConsumer.class)) {
                BiConsumer<Object, TreeDesigner> consumer = $decomposer.asExpected();
                consumer.accept(o, this);
                return $decompositions.in(o).get();
            }
        } else if(type.isArray()) {
            var $r = interpretArray(o);
            if(isAttachingTypes()) attachType($r, type);
            $decompositions.inset(o, $r);
            return $r;
        } else {
            try {
                Method method = type.getDeclaredMethod("decompose", Subject.class, TreeDesigner.class);
                if(method.trySetAccessible()) {
                    int modifiers = method.getModifiers();
                    if(Subject.class.isAssignableFrom(method.getReturnType()) && Modifier.isStatic(modifiers)) {
                        return (Subject)method.invoke(null, $(o), this);
                    }
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
            try {
                Method method = type.getMethod("decompose", Subject.class);
                if(method.trySetAccessible()) {
                    int modifiers = method.getModifiers();
                    if(Subject.class.isAssignableFrom(method.getReturnType()) && Modifier.isStatic(modifiers)) {
                        var $r = (Subject)method.invoke(null, $(o));
                        if(attachingTypes) attachType($r, type);
                        $decompositions.inset(o, $r);
                        return $r;
                    }
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
            if(o instanceof Interpreted) {
                var $r = ((Interpreted)o).interpret();
                if(attachingTypes) attachType($r, type);
                $decompositions.inset(o, $r);
                return $r;
            }
        }
        System.err.println("Can't decompose " + o);
        return $();
    }

    void attachType(Subject $, Class<?> type) {
        $.aimedInset($.raw(), classXray, wrapType(type));
    }

    Subject wrapType(Class<?> type) {
        Subject $wrappedType = $();
        var $1 = $classAliases.in(type).get();
        if($1.present()) $wrappedType.set(new StringXray($1.asExpected()));
        else {
            if(type.isArray()) {
                $wrappedType.inset(new AutoXray(), wrapType(type.getComponentType()));
            } else {
                $wrappedType.set(new StringXray(type.getName()));
            }
        }
        return $wrappedType;
    }

    Subject interpretArray(Object array) {
        Class<?> type = array.getClass().getComponentType();
        var $ = $();

        if (type.isPrimitive()) {
            if (type == Integer.TYPE) {
                int[] a = (int[]) array;
                for(var i : a) $.add(i);
            } else if (type == Byte.TYPE) {
                byte[] a = (byte[]) array;
                for(var i : a) $.add(i);
            } else if (type == Long.TYPE) {
                long[] a = (long[]) array;
                for(var i : a) $.add(i);
            } else if (type == Float.TYPE) {
                float[] a = (float[]) array;
                for(var i : a) $.add(i);
            } else if (type == Double.TYPE) {
                double[] a = (double[]) array;
                for(var i : a) $.add(i);
            } else if (type == Short.TYPE) {
                short[] a = (short[]) array;
                for(var i : a) $.add(i);
            } else if (type == Character.TYPE) {
                char[] a = (char[]) array;
                for(var i : a) $.add(i);
            } else if (type == Boolean.TYPE) {
                boolean[] a = (boolean[]) array;
                for(var i : a) $.add(i);
            } else {
                throw new InternalError();
            }
        } else {
            Object[] a = (Object[]) array;
            for(var i : a) $.add(i);
        }
        return $;
    }

}
