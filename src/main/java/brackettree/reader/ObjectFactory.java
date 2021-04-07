package brackettree.reader;

import brackettree.Discovered;
import suite.suite.Subject;
import suite.suite.Suite;
import suite.suite.Vendor;
import suite.suite.action.Action;
import suite.suite.util.Series;

import java.lang.reflect.*;
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

    public ObjectFactory(Series $composers) {
        setComposers(StandardDiscoverer.getAll());
        setComposers($composers);
        $classAliases.alter(Suite.
                put("int", Integer.class).
                put("double", Double.class).
                put("float", Float.class).
                put("list", List.class).
                put("subject", Subject.class).
                put("string", String.class)
        );
        elementaryComposer = str -> $();
    }

    public FactoryVendor load(Subject $root) {
        $references = $();
        $inferredTypes = $();
        for(var $1 : postDfs$(in$($root), $ -> $.exclude($$ -> {
            var o = $$.raw();
            return "#".equals(o) || "@".equals(o);
        })).eachIn()) {
            var $hash = $1.take("#");
            var $at = $1.take("@");
            if($at.present()) inferType($1, $at.in().get());
            if($hash.present()) $references.inset($hash.in().raw(), $1);

            for(var $2 : $1) {
                var str = $2.asString("");
                if(str.startsWith("#") && !str.startsWith("##")) {
                    var $in2 = $2.in().get();
                    if($in2.present()) {
                        $1.unset(str);
                        $references.inset(str.substring(1), $in2);
                    }
                }
            }
        }
        $references.alter($externalReferences);
        return factoryVendor($root);
    }

    public void setComposition(String ref, Object param) {
        if(ref.startsWith("#")) ref = ref.substring(1);
        var $s = $();
        $externalReferences.put(ref, $s);
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

        if (isReference($)) {
            $ = findReferred($);
        }

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
                System.err.println("Expected type (" + expectedType +
                        ") is not assignable from inferred type (" + inferredType + ")");
                $v = compose($, expectedType);
            }
        } else {
            $v = compose($, expectedType);
        }

        return $v;
    }

    boolean isReference(Subject $) {
        return $.size() == 1 && $.in().absent() && $.as(String.class, "").startsWith("#");
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
                if($type.present()) return $.shift($type.raw());
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
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}

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
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}

        if(Discovered.class.isAssignableFrom(type)) {

            try {
                Constructor<?> constructor = type.getDeclaredConstructor();
                Discovered reformable = (Discovered)constructor.newInstance();
                $compositions.in($).set(reformable);
                reformable.set(factoryVendorRoot($));
                return $(reformable);
            } catch (NoSuchMethodException | IllegalAccessException |
                    InstantiationException | InvocationTargetException ignored) {
                System.err.println("Can't create object. Check access modifiers");
            }
        }

        return $();
    }

    Subject composeArray(Subject $, Class<?> componentType) {
        Object[] a = (Object[])Array.newInstance(componentType, $.size());
        int i = 0;
        for(var $up : $.eachIn()) {
            a[i++] = $up.asExpected();
        }
        return $((Object)a);
    }

    FactoryVendorRoot factoryVendorRoot(Subject $sub) {
        for(var $ : $sub) {
            if($.is(String.class)) {
                String str = $.asExpected();
                if(str.startsWith("#")) {
                    $sub.shift(str, get(findReferred($), Object.class).asExpected());
                } else {
                    var $prim = elementaryComposer.apply(str);
                    if($prim.present()) $sub.shift(str, $prim.raw());
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
