package brackettree.reader;

import brackettree.Interpreted;

import suite.suite.Subject;
import static suite.suite.$uite.*;

import suite.suite.Suite;
import suite.suite.action.Action;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class StandardDiscoverer {

    public static Subject getAll() {
        return set$(
                $(Boolean.class, (Action) StandardDiscoverer::discoverBoolean),
                $(Integer.class, (Action) StandardDiscoverer::discoverInteger),
                $(Double.class, (Action) StandardDiscoverer::discoverDouble),
                $(Float.class, (Action) StandardDiscoverer::discoverFloat),
                $(Subject.class, (Action) StandardDiscoverer::discoverSubject),
                $(String.class, (Action) StandardDiscoverer::discoverString),
                $(Object.class, (Action) StandardDiscoverer::discoverObject), // primitive discoverer
                $(ArrayList.class, (Action) StandardDiscoverer::discoverList)
        );
    }

    public static Subject discoverBoolean(Subject $) {
        if($.is(String.class)) {
            String str = $.as(String.class);
            return set$(Boolean.parseBoolean(str) || str.equals("+"));
        }
        if($.is(Boolean.class)) return $;
        return set$(false);

    }

    public static Subject discoverInteger(Subject $) {
        if($.is(String.class)) return set$(Integer.valueOf($.as(String.class)));
        if($.is(Number.class)) return set$($.as(Number.class).intValue());
        return set$();
    }

    public static Subject discoverDouble(Subject $) {
        if($.is(String.class)) return set$(Double.valueOf($.as(String.class)));
        if($.is(Number.class)) return set$($.as(Number.class).doubleValue());
        return set$();
    }

    public static Subject discoverFloat(Subject $) {
        if($.is(String.class)) return set$(Float.valueOf($.as(String.class)));
        if($.is(Number.class)) return set$($.as(Number.class).floatValue());
        return set$();
    }

    public static Subject discoverString(Subject $) {
        String str = $.as(String.class, "");
        boolean cutFront = str.startsWith("'"), cutBack = str.endsWith("'");
        return set$(cutFront ? cutBack ? str.substring(1, str.length() - 1) : str.substring(1) :
                cutBack ? str.substring(0, str.length() - 1) : str);
    }

    public static Subject discoverObject(Subject $) {
        if($.absent()) return set$();
        if($.size() == 1 && $.in().absent() && $.is(String.class)) {
//            return discoverString($); // skip elementary

            String str = $.as(String.class);
            boolean cutFront = str.startsWith("'"), cutBack = str.endsWith("'");
            if(cutFront) {
                return set$(cutBack ? str.substring(1, str.length() - 1) : str.substring(1));
            } else if(Character.isDigit(str.codePointAt(0))) {
                return set$(Integer.valueOf(str));
            }


        }

        return discoverSubject($);
    }

    public static Subject discoverSubject(Subject $) {
        var $r = set$();
        for(var $1 : $) {
            var o = $1.in().raw();
            if(o instanceof Subject) $r.inset($1.raw(), (Subject) o);
            else $r.inset($1.raw(), set$(o));
        }
        return Suite.set($r);
    }

    public static Subject discoverList(Subject $) {
        return set$($.eachIn().eachRaw().toList());
    }

    static Subject $boxer = set$(
            $(int.class, Integer.class),
            $(float.class, Float.class),
            $(double.class, Double.class),
            $(boolean.class, Boolean.class)
    );

    public static void discover(Interpreted reformable, Subject $) {
        for(Class<?> aClass = reformable.getClass(); aClass != Object.class; aClass = aClass.getSuperclass()) {
            try {
                Field[] fields = aClass.getDeclaredFields();
                for (Field field : fields) {
                    if ($.present(field.getName())) {
                        field.setAccessible(true);
                        Class<?> fieldType = field.getType();
                        if(fieldType.isPrimitive()) {
                            Class<Object> type = $boxer.in(fieldType).asExpected();
                            field.set(reformable, $.in(field.getName()).as(type));
                        } else {
                            var o = $.in(field.getName()).as(fieldType, null);
                            field.set(reformable, o);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

