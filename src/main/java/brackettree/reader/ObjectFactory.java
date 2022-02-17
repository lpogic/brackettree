package brackettree.reader;

import brackettree.Interpreted;

import brackettree.xray.AutoXray;
import brackettree.xray.ObjectXray;
import brackettree.xray.StringXray;
import brackettree.xray.formal.BinaryXray;
import suite.suite.Subject;
import suite.suite.Suite;
import suite.suite.Vendor;
import suite.suite.action.Action;
import suite.suite.util.Series;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static suite.suite.$uite.*;

public class ObjectFactory {

    Subject $references = $();
    Subject $inferredTypes = $();

    Subject $externalReferences = $();

    Subject $compositions = $();
    Subject $composers = $();
    Subject $classAliases = $();
    Function<String, Subject> elementaryComposer;

    public ObjectFactory() {
        this(StandardDiscoverer.getAll());
    }

    public ObjectFactory(Series $composers) {
        setComposers($composers);
        $classAliases.alter(Suite.
                put("int", Integer.class).
                put("double", Double.class).
                put("float", Float.class).
                put("list", List.class).
                put("subject", Subject.class).
                put("string", String.class).
                put("serial", Serializable.class)
        );
        elementaryComposer = this::discoverElementary;
    }

    public FactoryVendor load(Subject $root) {
        $references = $();
        $inferredTypes = $();
        for(var $1 : postDfs$($($root)).eachIn()) {
            for(var s : $1.each(String.class).select(s -> s.startsWith("@"))) {
                switch (s) {
                    case "@id" -> $references.inset($1.in(s).raw(), $1);
                    case "@class" -> inferType($1, $1.in(s).get());
                    default -> {
                        if (s.startsWith("@#")) {
                            $references.inset(s.substring(2), $1.in(s).get());
                        }
                    }
                }
                $1.unset(s);
            }
        }
        $references.alter($externalReferences);
        return factoryVendor($root);
    }

    public void setComposition(String ref, Object param) {
        if(ref.startsWith("#")) ref = ref.substring(1);
        var $s = $();
        $externalReferences.inset(ref, $s);
        $compositions.in($s).set(param);
    }

    public void setClassAlias(String alias, Class<?> aClass) {
        $classAliases.in(alias).set(aClass);
    }

    public void setComposer(Class<?> type, Action constructor) {
        $composers.in(type).set(constructor);
    }

    public void setComposer(Class<?> type, BiConsumer<Subject, ObjectFactory> constructor) {
        $composers.in(type).set(constructor);
    }

    public void setComposers(Series composers) {
        $composers.alter(composers.select(v -> v.is(Class.class) &&
                (v.in().is(Action.class) || v.in().is(BiConsumer.class))
        ));
    }

    public void setElementaryComposer(Function<String, Subject> elementaryComposer) {
        this.elementaryComposer = elementaryComposer;
    }

    public Subject get(Subject $, Class<?> expectedType) {

        var $v = $compositions.in($).get();
        if($v.present()) {
            if($v.is(expectedType) || $v.raw() == null) return $v;
            else  System.err.println("Expected type (" + expectedType +
                    ") is not supertype of backed object type (" + $v.raw().getClass() + ")");
        }

        var $inferredType = $inferredTypes.in($).get();

        if($inferredType.is(Class.class)) {
            Class<?> inferredType = $inferredType.asExpected();
            if(expectedType.isAssignableFrom(inferredType)) {
                $v = compose($, inferredType);
            } else {
                if(inferredType != Serializable.class) {
                    System.err.println("Expected type (" + expectedType +
                            ") is not assignable from inferred type (" + inferredType + ")");
                }
                $v = compose($, expectedType);
            }
        } else {
            $v = compose($, expectedType);
        }

        return $v;
    }

    boolean isReference(Subject $) {
        if($.size() != 1 || $.in().present()) return false;
        var str = $.as(String.class, "");
        return str.startsWith("#") && !str.startsWith("##");
    }

    Subject findReferred(Subject $) {
        do {
            String str = $.asExpected();
            $ = $references.in(str.substring(1)).get();
        } while (isReference($));
        return $;
    }

    void inferType(Subject $root, Subject $typeTree) {
        $typeTree.introspect($ -> {
            if($.is(String.class)) {
                var $type = findType($.asString());
                if($type.present()) return $.swap($.raw(), $type.raw());
                else return $();
            } else return $;
        });
        inferTypeRq(Series.of($root), $typeTree);
    }

    Class<?> inferTypeRq(Series $s, Subject $typeTree) {
        Class<?> type = Object.class;
        for(var $ : $s) {
            Class<?> componentType = Object.class;
            if($typeTree.in().present()) {
                componentType = inferTypeRq($.eachIn(), $typeTree.in().get());
            }
            type = $typeTree.is(Class.class) ? $typeTree.asExpected() : componentType.arrayType();
            if($inferredTypes.absent($)) {
                $inferredTypes.put($, type);
            }
        }
        return type;
    }

    Subject findType(String type) {
        if ($classAliases.present(type)) {
            return $classAliases.in(type).get();
        }
        try {
            return $(Class.forName(type));
        } catch (ClassNotFoundException e) {
            System.err.println("ObjectFactory: class '" + type + "' not found");
        }

        return $();
    }

    Subject compose(Subject $, Class<?> type) {

        var $composer = $composers.in(type).get();

        if ($composer.is(Action.class)) {

            Action constructor = $composer.asExpected();
            var $r = constructor.play(factoryVendorRoot($));
            if ($r.present()) {
                $compositions.in($).set($r.raw());
                return $r;
            }
        }

        if ($composer.is(BiConsumer.class)) {

            BiConsumer<Vendor, ObjectFactory> consumer = $composer.asExpected();
            consumer.accept(factoryVendorRoot($), this);
            if($compositions.present($)) {
                return $compositions.in($).get();
            }
        }

        if(type.isArray()) {

            var $r = composeArray(factoryVendorRoot($), type.getComponentType());
            if ($r.present()) {
                $compositions.in($).set($r.raw());
                return $r;
            }
        }

        try {
            Method method = type.getMethod("compose", Subject.class);
            if(method.trySetAccessible()) {
                int modifiers = method.getModifiers();
                if(Subject.class.isAssignableFrom(method.getReturnType()) && Modifier.isStatic(modifiers)) {
                    var $r = (Subject)method.invoke(null, factoryVendorRoot($));
                    if ($r.present()) {
                        $compositions.in($).set($r.raw());
                        return $r;
                    }
                }
            }
        } catch(NoSuchMethodException ignored) {
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        try {
            Method method = type.getMethod("compose", Subject.class, ObjectFactory.class);
            if(method.trySetAccessible()) {
                int modifiers = method.getModifiers();
                if(Modifier.isStatic(modifiers)) {
                    method.invoke(null, factoryVendorRoot($), this);
                    if($compositions.present($)) {
                        return $compositions.in($).get();
                    }
                }
            }
        } catch(NoSuchMethodException ignored) {
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        if(Interpreted.class.isAssignableFrom(type)) {

            try {
                Constructor<?> constructor = type.getDeclaredConstructor();
                if(constructor.trySetAccessible()) {
                    Interpreted reformable = (Interpreted) constructor.newInstance();
                    $compositions.in($).set(reformable);
                    reformable.discover(factoryVendorRoot($));
                    return $(reformable);
                }
            } catch(NoSuchMethodException ignored) {
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        if(type.isRecord()) {
            var $r = composeRecord(factoryVendorRoot($), type);
            if ($r.present()) {
                $compositions.in($).set($r.raw());
                return $r;
            }
        }

        if(Serializable.class.isAssignableFrom(type)) {
            try {
                var $r = composeSerial(factoryVendorRoot($));
                if ($r.present()) {
                    $compositions.in($).set($r.raw());
                    return $r;
                }
            } catch (IOException | ClassNotFoundException ignored) {
//                ignored.printStackTrace();
                System.err.println("Problem with Serializable");
            }
        }

        if($.present()) {
            System.err.println("Uncomposable element!");
            System.err.println(toString$($));
        }
        return $();
    }

    Subject composeArray(Subject $, Class<?> componentType) {
        if($.size() == 1 && $.in().absent() && $.raw() == null) return $;

        Object[] a = (Object[])Array.newInstance(componentType, $.size());
        int i = 0;
        for(var $up : $.eachIn()) {
            a[i++] = $up.asExpected();
        }
        return $(a);
    }

    Subject composeRecord(Subject $, Class<?> recordClass) {
        if($.size() == 1 && $.in().absent() && $.raw() == null) return $;

        try {
            var components = recordClass.getRecordComponents();
            var classes = new Class<?>[components.length];
            var objects = new Object[components.length];
            for(int i = 0; i < components.length; ++ i) {
                classes[i] = components[i].getType();
                objects[i] = $.in(components[i].getName()).as(classes[i].isPrimitive() ?
                        $primBox.in(classes[i]).asExpected() : classes[i]);
            }

            Constructor<?> constructor = recordClass.getDeclaredConstructor(classes);
            if(constructor.trySetAccessible()) {
                return $(constructor.newInstance(objects));
            }
        } catch(NoSuchMethodException ignored) {
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return $();
    }

    Subject composeSerial(Subject $) throws IOException, ClassNotFoundException {
        if($.size() == 1 && $.in().absent() && $.raw() == null) return $;

        try(var ois = new ObjectInputStream(new ByteArrayInputStream(BinaryXray.utf8DecodePrintable($.asString()))) {
            {
                enableResolveObject(true);
            }

            @Override
            protected Object resolveObject(Object obj) {
                if(obj instanceof AutoXray) return new Suite.Auto();
                if(obj instanceof StringXray xray) return get(Suite.set(xray.getValue()), Object.class).raw();
                if(obj instanceof ObjectXray xray) return get(Suite.set("#" + xray.getRefId()), Object.class).raw();
                return obj;
            }
        }) {
            return Suite.set(ois.readObject());
        }
    }

    static final Subject $primBox = Suite
            .put(boolean.class, Boolean.class)
            .put(byte.class, Byte.class)
            .put(char.class, Character.class)
            .put(double.class, Double.class)
            .put(float.class, Float.class)
            .put(int.class, Integer.class)
            .put(long.class, Long.class)
            .put(short.class, Short.class)
            .put(void.class, Void.class);


    public Subject discoverElementary(String str) {
        if(str.startsWith("\"")) return $(str.substring(1, str.length() - 1));
        if("null".equals(str)) return $(null);
        if(str.startsWith("#")) return get($references.in(str.substring(1)).get(), Object.class);
        if("+".equals(str) || "true".equals(str)) return $(true);
        if("-".equals(str) || "false".equals(str)) return $(false);
        try {
            // TODO
            if (str.matches("^[-+]?\\d*\\.\\d*")) return $(Double.parseDouble(str));
            if (str.matches("^[-+]?\\d*")) return $(Integer.parseInt(str));
        } catch (Exception ignored) {}

        return $(str);
    }

    FactoryVendorRoot factoryVendorRoot(Subject $sub) {
        for(var $ : $sub) {
            if($.is(String.class)) {
                String str = $.asExpected();
                var $prim = elementaryComposer.apply(str);
                var o = $prim.raw();
                if(!str.equals(o)) {
                    if ($sub.present(o)) {
                        $sub.unset(o);
                        System.err.println("Duplicate key found: " + o);
                    }
                    $sub.swap(str, o);
                }
            }
        }
        return new FactoryVendorRoot(this, $sub);
    }

    FactoryVendor factoryVendor(Subject $sub) {
        if(isReference($sub)) $sub = findReferred($sub);
        return new FactoryVendor(this, $sub);
    }
}
