package brackettree.writer;

import brackettree.Interpreted;
import brackettree.xray.*;
import brackettree.xray.formal.BinaryXray;
import brackettree.xray.formal.SpecialXray;
import brackettree.xray.formal.Xray;
import suite.suite.SolidSubject;
import suite.suite.Subject;
import static suite.suite.$uite.*;

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

    static final Xray idXray = new SpecialXray("@id");
    static final Xray classXray = new SpecialXray("@class");
    static final Xray reservationsXray = new SpecialXray("@reserved");

    static final Subject $classAliasBox = Suite.
            put(Short.class, "short").
            put(short.class, "short").
            put(Integer.class, "int").
            put(int.class, "int").
            put(Long.class, "long").
            put(long.class, "long").
            put(Float.class, "float").
            put(float.class, "float").
            put(Double.class, "double").
            put(double.class, "double").
            put(List.class, "list").
            put(SolidSubject.class, "subject").
            put(String.class, "string").
            put(Serializable.class, "serial");

    Subject $refs = $();
    Subject $decompositions = $();
    Subject $reservations = $();

    Subject $decomposers = $();
    Function<Object, Subject> elementaryDecomposer;
    boolean attachingTypes;
    Subject $classAliases = $();

    public TreeDesigner() {
        setDecomposers(StandardInterpreter.getAllSupported());
        $classAliases.alter($classAliasBox);
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
        $decompositions.inset(new ObjectXray(o), $);
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

    public static Reservation reserve(Object o) {
        return new Reservation(o);
    }

    int id;

    public Subject load(Object o) {
        $refs = $();
        var $printedObjectRefs = $();
        id = 0;
        var xray = xray(o);
        var $reservations = Suite.alter(this.$reservations);
        Subject $xRoot;
        if(xray instanceof ObjectXray x) {
            if(x.getUsages() > 1) {
                if(x.getRefId() == null) x.setRefId("" + id++);
                $xRoot = $(idXray, $(new StringXray(x.getRefId()))).alter($refs.in(x).get()).set(reservationsXray);
            } else {
                $xRoot = $().alter($refs.in(x).get()).set(reservationsXray);
            }
        } else {
            $xRoot = $(xray, reservationsXray);
        }
        for(var $i : preDfs$($($xRoot)).eachIn()) {
            for(var $i1 : $i) {
                if($i1.is(ObjectXray.class)) {
                    ObjectXray x = $i1.asExpected();
                    boolean isLeaf = $i.size() == 1 && $i1.in().absent();

                    if (x.getUsages() < 2 && isLeaf) {
                        $i.unset().alter($refs.in(x));
                        $printedObjectRefs.set(x);
                    } else {
                        var $reservation = $reservations.in(x).get();

                        if ($printedObjectRefs.absent(x) && ($reservation.absent() || $i == $reservation.raw())) {
                            $reservations.unset(x);
                            $printedObjectRefs.set(x);
                            if(x.getRefId() == null) x.setRefId("" + id++);
                            var $r = $refs.in(x).get();
                            if (isLeaf) {
                                $i.unset().put(idXray, new StringXray(x.getRefId())).alter($r);
                            } else {
                                $i.swap(x, new SpecialXray("#" + x.getRefId()));
                                $xRoot.inset(new SpecialXray("@#" + x.getRefId()), $r);
                            }
                        } else if(!isLeaf) {
                            if(x.getRefId() == null) x.setRefId("" + id++);
                            $i.swap(x, new SpecialXray("#" + x.getRefId()));
                        }
                    }
                } else if($i1.raw() == reservationsXray) {
                    $i.unset(reservationsXray);
                }
            }
        }
        if(xray instanceof ObjectXray && ((ObjectXray) xray).getUsages() > 1) return $xRoot.at(1);
        else return $xRoot;
    }

    public static class Reservation {
        Object o;

        public Reservation(Object o) {
            this.o = o;
        }
    }

    public Xray xray(Object o) {
        if(o instanceof Reservation r) o = r.o;
        if(o instanceof Xray) return (Xray) o;
        var $prim = elementaryDecomposer.apply(o);
        if($prim.present()) return new StringXray($prim.asExpected());
        if(o instanceof String s) return new StringXray(s);
        if(o instanceof Suite.Auto) return new AutoXray();

        ObjectXray xray = $refs.sate(new ObjectXray(o)).asExpected();
        if(xray.use() < 1) {
            var $ = decompose(o, xray);
            $refs.inset(xray, $);
            for(var $i : preDfs$($($)).eachIn()) {
                for(var i : $i.each()) {
                    if(i instanceof Reservation r && $i.size() == 1 && $i.in().absent()) {
                        var x = xray(r.o);
                        $i.swap(i, x);
                        $reservations.put(x, $i);
                    } else {
                        $i.swap(i, xray(i));
                    }
                }
            }
        }

        return xray;
    }

    Subject decompose(Object o, Xray xray) {

        var $decomposition = $decompositions.in(xray).get();
        if($decomposition.present()) return $decomposition;

        Class<?> type = o.getClass();

        var $decomposer = $decomposers.in(type).get();
        if($decomposer.present()) {
            if ($decomposer.is(Action.class)) {

                Action decomposer = $decomposer.asExpected();
                var $r = decomposer.play(Suite.set(o));
                if(isAttachingTypes()) attachType($r, type);
                $decompositions.inset(xray, $r);
                return $r;
            } else if ($decomposer.is(BiConsumer.class)) {
                BiConsumer<Object, TreeDesigner> consumer = $decomposer.asExpected();
                consumer.accept(o, this);
                return $decompositions.in(xray).get();
            }
        } else if(type.isArray()) {
            var $r = interpretArray(o);
            if(isAttachingTypes()) attachType($r, type);
            $decompositions.inset(xray, $r);
            return $r;
        } else {
            try {
                Method method = type.getMethod("decompose", type, TreeDesigner.class);
                if(method.trySetAccessible()) {
                    int modifiers = method.getModifiers();
                    if(Subject.class.isAssignableFrom(method.getReturnType()) && Modifier.isStatic(modifiers)) {
                        return (Subject)method.invoke(null, type.cast(o), this);
                    }
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {

            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Method method = type.getMethod("decompose", type);
                if(method.trySetAccessible()) {
                    int modifiers = method.getModifiers();
                    if(Subject.class.isAssignableFrom(method.getReturnType()) && Modifier.isStatic(modifiers)) {
                        var $r = (Subject)method.invoke(null, type.cast(o));
                        if(attachingTypes) attachType($r, type);
                        $decompositions.inset(xray, $r);
                        return $r;
                    }
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {

            } catch (Exception e) {
                e.printStackTrace();
            }
            if(o instanceof Interpreted interpreted) {
                var $r = interpreted.interpret();
                if(attachingTypes) attachType($r, type);
                $decompositions.inset(xray, $r);
                return $r;
            }

            if(type.isRecord()) {
                var $r = StandardInterpreter.interpret(o);
                if(isAttachingTypes()) attachType($r, type);
                $decompositions.inset(xray, $r);
                return $r;
            }

            if(o instanceof Serializable) {
                var baos = new ByteArrayOutputStream();
                var $replacementType = $();
                var $r = $();
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
                $decompositions.inset(xray, $r);
                return $r;
            }
        }
        System.err.println("Can't decompose " + o);
        return $();
    }

    public void attachType(Subject $, Class<?> type) {
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
