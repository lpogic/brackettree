package brackettree.reader;

import brackettree.Discovered;

import suite.suite.Subject;
import static suite.suite.$uite.*;
import suite.suite.action.Action;

import java.lang.reflect.Field;
import java.util.List;

public class StandardDiscoverer {

    public static Subject getAll() {
        return set$(
                arm$(Boolean.class, (Action) StandardDiscoverer::discoverBoolean),
                arm$(Integer.class, (Action) StandardDiscoverer::discoverInteger),
                arm$(Double.class, (Action) StandardDiscoverer::discoverDouble),
                arm$(Float.class, (Action) StandardDiscoverer::discoverFloat),
                arm$(Subject.class, (Action) StandardDiscoverer::discoverSubject),
                arm$(String.class, (Action) StandardDiscoverer::discoverString),
                arm$(Object.class, (Action) StandardDiscoverer::discoverObject),
                arm$(List.class, (Action) StandardDiscoverer::discoverList)
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
        if($.is(String.class)) return set$(Integer.parseInt($.as(String.class)));
        if($.is(Number.class)) return set$($.as(Number.class).intValue());
        return set$();
    }

    public static Subject discoverDouble(Subject $) {
        if($.is(String.class)) return set$(Double.parseDouble($.as(String.class)));
        if($.is(Number.class)) return set$($.as(Number.class).doubleValue());
        return set$();
    }

    public static Subject discoverFloat(Subject $) {
        if($.is(String.class)) return set$(Float.parseFloat($.as(String.class)));
        if($.is(Number.class)) return set$($.as(Number.class).floatValue());
        return set$();
    }

    public static Subject discoverString(Subject $) {
        String str = $.as(String.class, "");
        boolean cutFront = str.startsWith("`"), cutBack = str.endsWith("`");
        return set$(cutFront ? cutBack ? str.substring(1, str.length() - 1) : str.substring(1) :
                cutBack ? str.substring(0, str.length() - 1) : str);
    }

    public static Subject discoverObject(Subject $) {
        if($.absent()) return set$();
        if($.size() == 1 && $.in().absent() && $.is(String.class)) return discoverString($);

        return discoverSubject($);
    }

    public static Subject discoverSubject(Subject $) {
        var $r = set$();
        for(var $1 : $) {
            var o = $1.in().raw();
            if(o instanceof Subject) $r.inset($1.raw(), (Subject) o);
            else $r.inset($1.raw(), set$(o));
        }
        return set$($r);
    }

    public static Subject discoverList(Subject $) {
        return set$($.eachIn().eachRaw().toList());
    }

    public static void discover(Discovered reformable, Subject $) {
        for(Class<?> aClass = reformable.getClass(); aClass != Object.class; aClass = aClass.getSuperclass()) {
            try {
                Field[] fields = aClass.getDeclaredFields();
                for (Field field : fields) {
                    if ($.present(field.getName())) {
                        field.setAccessible(true);
                        Class<?> fieldType = field.getType();
                        if(fieldType.isPrimitive()) {
                            if(fieldType.equals(int.class)) {
                                field.setInt(reformable, $.in(field.getName()).as(Integer.class, 0));
                            }
                        } else {
                            field.set(reformable, $.in(field.getName()).as(fieldType, null));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

