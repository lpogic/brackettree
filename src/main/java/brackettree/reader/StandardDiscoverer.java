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
                Object.class, $(StandardDiscoverer::discoverObject),
                Boolean.class, $(StandardDiscoverer::discoverBoolean),
                Number.class, $(StandardDiscoverer::discoverNumber),
                Short.class, $(StandardDiscoverer::discoverShort),
                Integer.class, $(StandardDiscoverer::discoverInteger),
                Long.class, $(StandardDiscoverer::discoverLong),
                Float.class, $(StandardDiscoverer::discoverFloat),
                Double.class, $(StandardDiscoverer::discoverDouble),
                Subject.class, $(StandardDiscoverer::discoverSubject),
                String.class, $(StandardDiscoverer::discoverString),
                ArrayList.class, $(StandardDiscoverer::discoverList)
        );
    }

    public static Subject discoverObject(Subject $) {
        if($.absent()) return $();
        if($.size() == 1 && $.in().absent()) {
            return Suite.set($.raw());
        }

        return StandardDiscoverer.discoverSubject($);
    }

    public static Subject discoverBoolean(Subject $) {
        if($.is(String.class)) {
            String str = $.as(String.class);
            return $(Boolean.parseBoolean(str) || str.equals("+"));
        }
        if($.is(Boolean.class)) return $;
        return $(false);

    }

    public static Subject discoverShort(Subject $) {
        var $n = discoverNumber($);
        if($n.present()) return $($n.asShort());
        return $();
    }

    public static Subject discoverInteger(Subject $) {
        var $n = discoverNumber($);
        if($n.present()) return $($n.asInt());
        return $();
    }

    public static Subject discoverLong(Subject $) {
        var $n = discoverNumber($);
        if($n.present()) return $($n.asLong());
        return $();
    }

    public static Subject discoverDouble(Subject $) {
        var $n = discoverNumber($);
        if($n.present()) return $($n.asDouble());
        return $();
    }

    public static Subject discoverFloat(Subject $) {
        var $n = discoverNumber($);
        if($n.present()) return $($n.asFloat());
        return $();
    }

    public static Subject discoverNumber(Subject $) {
        if($.is(String.class)) {
            var str = $.asString();
            if(str.contains(".")) return $(Double.valueOf(str));
            return $(Long.valueOf(str));
        }
        if($.is(Number.class)) return $($.raw());
        return $(0);
    }

    public static Subject discoverString(Subject $) {
        String str = $.as(String.class, "");
        boolean cutFront = str.startsWith("\""), cutBack = str.endsWith("\"");
        return $(cutFront ? cutBack ? str.substring(1, str.length() - 1) : str.substring(1) :
                cutBack ? str.substring(0, str.length() - 1) : str);
    }


    public static Subject discoverSubject(Subject $) {
        var $r = $();
        for(var $1 : $) {
            if($1.in().present()) {
                var o = $1.in().raw();
                if (o instanceof Subject) $r.inset($1.raw(), (Subject) o);
                else $r.inset($1.raw(), $(o));
            } else {
                $r.set($1.raw());
            }
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

