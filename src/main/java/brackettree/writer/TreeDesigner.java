package brackettree.writer;

import brackettree.Interpreted;
import brackettree.xray.*;
import brackettree.xray.formal.BinaryXray;
import brackettree.xray.formal.SpecialXray;
import suite.suite.SolidSubject;
import suite.suite.Subject;
import static suite.suite.$.*;

import suite.suite.Suite;
import suite.suite.action.Action;
import suite.suite.util.Series;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class TreeDesigner {

    static final Xray idXray = new SpecialXray("#");
    static final Xray classXray = new SpecialXray("@");

    Subject $refs = set$();
    Subject $decompositions = set$();

    Subject $decomposers = set$();
    Function<Object, Subject> elementaryDecomposer;
    boolean attachingTypes;
    Subject $classAliases = set$();

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
                put(String.class, "string").
                put(Serializable.class, "serial")
        );
        elementaryDecomposer = o -> {
            if(o == null) return set$("null");
            if(o instanceof String) return set$(o);
            if(o instanceof Integer) return set$(o.toString());
            if(o instanceof Double) return set$(o.toString());
            if(o instanceof Float) return set$(o.toString());
            if(o instanceof Boolean) return set$(o.toString());
            return set$();
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

    int id;
    Subject $xRoot;

    public Subject load(Object o) {
        $refs = set$();
        var $printedObjectRefs = set$();
        id = 0;
        $xRoot = set$();
        var xray = xray(o);
        $xRoot.aimedSet($xRoot.first().raw(), xray);
        for(var $i : preDfs$(list$($xRoot)).eachIn()) {
            for(var $i1 : $i) {
                if($i1.is(ObjectXray.class)) {
                    ObjectXray x = $i1.asExpected();

                    if (x.getUsages() < 2 && $i.size() == 1 && $i1.in().absent()) {
                        $i.unset().alter($refs.in(x));
                        $printedObjectRefs.set(x);
                    } else {
                        if ($printedObjectRefs.absent(x)) {
                            $printedObjectRefs.set(x);
                            if(x.getRefId() == null) x.setRefId("" + id++);
                            var $r = $refs.in(x).get();
                            if($i.size() == 1 && $i1.in().absent()) {
                                $r.aimedInset($r.first().raw(), idXray, set$(new StringXray(x.getRefId())));
                                $i.unset().alter($r);
                            } else {
                                $i.shift(x, new SpecialXray("##" + x.getRefId()));
                                $xRoot.inset(new SpecialXray("#" + x.getRefId()), $r);
                            }
                        } else if(!($i.size() == 1 && $i1.in().absent())) {
                            $i.shift(x, new SpecialXray("##" + x.getRefId()));
                        }
                    }
                }
            }
        }
        if(xray instanceof ObjectXray && ((ObjectXray) xray).getUsages() > 1) return $xRoot.at(1);
        else return $xRoot;
    }

    Xray xray(Object o) {
        if(o instanceof Xray) return (Xray) o;
        var $prim = elementaryDecomposer.apply(o);
        if($prim.present()) return new StringXray($prim.asExpected());
        if(o instanceof String) return new StringXray($prim.asExpected());
        if(o instanceof Suite.Auto) return new AutoXray();

        ObjectXray xray = $refs.sate(new ObjectXray(o)).asExpected();
        if(xray.use() < 1) {
            var $ = decompose(o);
            $refs.inset(xray, $);
            for(var $i : preDfs$(list$($)).eachIn()) {
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
                var $r = decomposer.play(Suite.set(o));
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
                        return (Subject)method.invoke(null, Suite.set(o), this);
                    }
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
            try {
                Method method = type.getMethod("decompose", Subject.class);
                if(method.trySetAccessible()) {
                    int modifiers = method.getModifiers();
                    if(Subject.class.isAssignableFrom(method.getReturnType()) && Modifier.isStatic(modifiers)) {
                        var $r = (Subject)method.invoke(null, Suite.set(o));
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
            if(o instanceof Serializable) {
                var baos = new ByteArrayOutputStream();
                var $replacementType = set$();
                var $r = set$();
                try(var oos = new ObjectOutputStream(baos) {
                    {
                        enableReplaceObject(true);
                    }

                    @Override
                    protected Object replaceObject(Object obj) {
                        if($replacementType.absent()) {
                            $replacementType.set(obj.getClass());
                            return obj;
                        }
                        var xray = xray(obj);
                        if(xray instanceof ObjectXray objectXray) {
                            if(objectXray.getRefId() == null) {
                                objectXray.setRefId("" + id++);
                            }
                            $r.add(obj);
                            return xray;
                        } else return obj;
                    }
                }) {
                    oos.writeObject(o);
                } catch (IOException e) {
//                    e.printStackTrace();
                }
                $r.aimedSet($r.first().raw(), new BinaryXray(baos.toByteArray()));
                if(attachingTypes) attachType($r, Serializable.class);
                $decompositions.inset(o, $r);
                return $r;
            }
        }
        System.err.println("Can't decompose " + o);
        return set$();
    }

    void attachType(Subject $, Class<?> type) {
        $.aimedInset($.raw(), classXray, wrapType(type));
    }

    Subject wrapType(Class<?> type) {
        Subject $wrappedType = set$();
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
        var $ = set$();

        if (type.isPrimitive()) {
            if (type == Integer.TYPE) {
                for(var i : (int[]) array) $.add(i);
            } else if (type == Byte.TYPE) {
                for(var i : (byte[]) array) $.add(i);
            } else if (type == Long.TYPE) {
                for(var i : (long[]) array) $.add(i);
            } else if (type == Float.TYPE) {
                for(var i : (float[]) array) $.add(i);
            } else if (type == Double.TYPE) {
                for(var i : (double[]) array) $.add(i);
            } else if (type == Short.TYPE) {
                for(var i : (short[]) array) $.add(i);
            } else if (type == Character.TYPE) {
                for(var i : (char[]) array) $.add(i);
            } else if (type == Boolean.TYPE) {
                for(var i : (boolean[]) array) $.add(i);
            } else {
                throw new InternalError();
            }
        } else {
            for(var i : (Object[]) array) $.add(i);
        }
        return $;
    }
}
