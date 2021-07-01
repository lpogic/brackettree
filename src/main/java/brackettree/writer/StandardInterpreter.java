package brackettree.writer;

import brackettree.Interpreted;

import suite.suite.SolidSubject;
import suite.suite.Subject;
import static suite.suite.$uite.*;
import suite.suite.action.Action;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class StandardInterpreter {

    public static Subject getAllSupported() {
        return set$(
                $(Integer.class, (Action)StandardInterpreter::interpretPrimitive),
                $(ArrayList.class, (Action)StandardInterpreter::interpretCollection),
                $(HashSet.class, (Action)StandardInterpreter::interpretCollection),
                $(HashMap.class, (Action)StandardInterpreter::interpretMap),
                $(File.class, (Action)StandardInterpreter::interpretFile),
                $(SolidSubject.class, (Action)StandardInterpreter::interpretSubject)
        );
    }

    public static Subject interpret(Object interpreted) {
        var $ = set$();
        for(Class<?> aClass = interpreted.getClass(); aClass != Object.class; aClass = aClass.getSuperclass()) {
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                try {
                    int modifiers = field.getModifiers();
                    if (!Modifier.isTransient(modifiers) && !Modifier.isStatic(modifiers)) {
                        field.setAccessible(true);
                        var $1 = $.in(field.getName());
                        if($1.absent()) {
                            $1.set(field.get(interpreted));
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Cant get '" + field.getName() + "' from " + aClass);
                }
            }
        }
        return $;
    }

    public static Subject interpretPrimitive(Subject $in) {
        return set$(Objects.toString($in.raw()));
    }

//    public static void interpretPrimitive(Object o, TreeDesigner designer) {
//        var $ = Suite.set();
//        if(designer.isAttachingTypes()) designer.attachType($, o.getClass());
//        $.set(new TreeDesigner.StringXray(o.toString()));
//        designer.setDecomposition(o, Suite.set(new TreeDesigner.PrimitiveXray($)));
//    }

    public static Subject interpretCollection(Subject $in) {
        Collection<?> collection = $in.asExpected();
        var $ = set$();
        collection.forEach($::add);
        return $;
    }

    public static Subject interpretMap(Subject $in) {
        Map<?, ?> map = $in.asExpected();
        var $ = set$();
        map.forEach($::put);
        return $;
    }

    public static Subject interpretSubject(Subject $in) {
        return $in.asExpected();
    }

    public static Subject interpretFile(Subject $in) {
        File file = $in.asExpected();
        return set$(file.getPath());
    }

}
