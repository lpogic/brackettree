package brackettree.reader;

import brackettree.Discovered;
import suite.suite.Subject;
import suite.suite.Suite;
import suite.suite.action.Action;

import java.lang.reflect.Field;
import java.util.List;

public class StandardDiscoverer {

    public static Subject getAll() {
        return Suite.
                inset(Boolean.class, (Action) StandardDiscoverer::discoverBoolean).
                inset(Integer.class, (Action) StandardDiscoverer::discoverInteger).
                inset(Double.class, (Action) StandardDiscoverer::discoverDouble).
                inset(Float.class, (Action) StandardDiscoverer::discoverFloat).
                inset(Subject.class, (Action) StandardDiscoverer::discoverSubject).
                inset(String.class, (Action) StandardDiscoverer::discoverString).
                inset(Object.class, (Action) StandardDiscoverer::discoverObject).
                inset(List.class, (Action) StandardDiscoverer::discoverList)
                ;
    }

    public static Subject discoverBoolean(Subject $) {
        if($.is(String.class)) {
            String str = $.as(String.class);
            return Suite.set(Boolean.parseBoolean(str) || str.equals("+"));
        }
        if($.is(Boolean.class)) return $;
        return Suite.set(false);

    }

    public static Subject discoverInteger(Subject $) {
        if($.is(String.class)) return Suite.set(Integer.parseInt($.as(String.class)));
        if($.is(Number.class)) return Suite.set($.as(Number.class).intValue());
        return Suite.set();
    }

    public static Subject discoverDouble(Subject $) {
        if($.is(String.class)) return Suite.set(Double.parseDouble($.as(String.class)));
        if($.is(Number.class)) return Suite.set($.as(Number.class).doubleValue());
        return Suite.set();
    }

    public static Subject discoverFloat(Subject $) {
        if($.is(String.class)) return Suite.set(Float.parseFloat($.as(String.class)));
        if($.is(Number.class)) return Suite.set($.as(Number.class).floatValue());
        return Suite.set();
    }

    public static Subject discoverString(Subject $) {
        String str = $.as(String.class, "");
        boolean cutFront = str.startsWith("`"), cutBack = str.endsWith("`");
        return Suite.set(cutFront ? cutBack ? str.substring(1, str.length() - 1) : str.substring(1) :
                cutBack ? str.substring(0, str.length() - 1) : str);
    }

    public static Subject discoverObject(Subject $) {
        if($.absent()) return Suite.set();
        if($.size() == 1 && $.in().absent() && $.is(String.class)) return discoverString($);

        return discoverSubject($);
    }

    public static Subject discoverSubject(Subject $) {
        var $r = Suite.set();
        for(var $1 : $) {
            var o = $1.in().direct();
            if(o instanceof Subject) $r.set($1.direct(), (Subject) o);
            else $r.set($1.direct(), Suite.set(o));
        }
        return Suite.set($r);
    }

    public static Subject discoverList(Subject $) {
        return Suite.set($.eachIn().eachDirect().toList());
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

