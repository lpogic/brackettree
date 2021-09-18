package brackettree.reader;

import brackettree.Interpreted;

import suite.suite.Subject;
import static suite.suite.$uite.*;

import suite.suite.Suite;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class StandardDiscoverer {

    public static Subject getAll() {
        return $(
                Boolean.class, $(StandardDiscoverer::discoverBoolean),
                Integer.class, $(StandardDiscoverer::discoverInteger),
                Double.class, $(StandardDiscoverer::discoverDouble),
                Float.class, $(StandardDiscoverer::discoverFloat),
                Subject.class, $(StandardDiscoverer::discoverSubject),
                String.class, $(StandardDiscoverer::discoverString),
                Object.class, $(StandardDiscoverer::discoverObject), // primitive discoverer
                ArrayList.class, $(StandardDiscoverer::discoverList)
        );
    }

    public static Subject discoverBoolean(Subject $) {
        if($.is(String.class)) {
            String str = $.as(String.class);
            return $(Boolean.parseBoolean(str) || str.equals("+"));
        }
        if($.is(Boolean.class)) return $;
        return $(false);

    }

    public static Subject discoverInteger(Subject $) {
        if($.is(String.class)) return $(Integer.valueOf($.as(String.class)));
        if($.is(Number.class)) return $($.as(Number.class).intValue());
        return $();
    }

    public static Subject discoverDouble(Subject $) {
        if($.is(String.class)) return $(Double.valueOf($.as(String.class)));
        if($.is(Number.class)) return $($.as(Number.class).doubleValue());
        return $();
    }

    public static Subject discoverFloat(Subject $) {
        if($.is(String.class)) return $(Float.valueOf($.as(String.class)));
        if($.is(Number.class)) return $($.as(Number.class).floatValue());
        return $();
    }

    public static Subject discoverString(Subject $) {
        String str = $.as(String.class, "");
        boolean cutFront = str.startsWith("'"), cutBack = str.endsWith("'");
        return $(cutFront ? cutBack ? str.substring(1, str.length() - 1) : str.substring(1) :
                cutBack ? str.substring(0, str.length() - 1) : str);
    }

    public static Subject discoverObject(Subject $) {
        if($.absent()) return $();
        if($.size() == 1 && $.in().absent() && $.is(String.class)) {
//            return discoverString($); // skip elementary

            String str = $.as(String.class);
            boolean cutFront = str.startsWith("'"), cutBack = str.endsWith("'");
            if(cutFront) {
                return $(cutBack ? str.substring(1, str.length() - 1) : str.substring(1));
            } else if(Character.isDigit(str.codePointAt(0))) {
                return $(Integer.valueOf(str));
            }


        }

        return discoverSubject($);
    }

    public static Subject discoverSubject(Subject $) {
        var $r = $();
        for(var $1 : $) {
            var o = $1.in().raw();
            if(o instanceof Subject) $r.inset($1.raw(), (Subject) o);
            else $r.inset($1.raw(), $(o));
        }
        return Suite.set($r);
    }

    public static Subject discoverList(Subject $) {
        return $($.eachIn().each().toList());
    }

    static Subject $boxer = $(
            int.class, $(Integer.class),
            float.class, $(Float.class),
            double.class, $(Double.class),
            boolean.class, $(Boolean.class)
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

