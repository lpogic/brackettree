package brackettree.writer;

import brackettree.Interpreted;
import suite.suite.SolidSubject;
import suite.suite.Subject;
import suite.suite.Suite;
import suite.suite.action.Action;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class StandardInterpreter {

    public static Subject getAllSupported() {
        return Suite.
                setUp(Integer.class, (Action)StandardInterpreter::interpretPrimitive).
                setUp(ArrayList.class, (Action)StandardInterpreter::interpretCollection).
                setUp(HashSet.class, (Action)StandardInterpreter::interpretCollection).
                setUp(HashMap.class, (Action)StandardInterpreter::interpretMap).
                setUp(File.class, (Action)StandardInterpreter::interpretFile).
                setUp(SolidSubject.class, (Action)StandardInterpreter::interpretSubject)
                ;
    }

    public static Subject interpret(Interpreted interpreted) {
        var $ = Suite.set();
        for(Class<?> aClass = interpreted.getClass(); aClass != Object.class; aClass = aClass.getSuperclass()) {
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                try {
                    int modifiers = field.getModifiers();
                    if (!Modifier.isTransient(modifiers) && !Modifier.isStatic(modifiers)) {
                        field.setAccessible(true);
                        $.setIf(field.getName(), Suite.set(field.get(interpreted)), $::absent);
                    }
                } catch (Exception ex) {
                    System.err.println("Cant get '" + field.getName() + "' from " + aClass);
                }
            }
        }
        return $;
    }

    public static Subject interpretPrimitive(Subject $in) {
        return Suite.set(Objects.toString($in.direct()));
    }

//    public static void interpretPrimitive(Object o, TreeDesigner designer) {
//        var $ = Suite.set();
//        if(designer.isAttachingTypes()) designer.attachType($, o.getClass());
//        $.set(new TreeDesigner.StringXray(o.toString()));
//        designer.setDecomposition(o, Suite.set(new TreeDesigner.PrimitiveXray($)));
//    }

    public static Subject interpretCollection(Subject $in) {
        Collection<?> collection = $in.asExpected();
        var $ = Suite.set();
        collection.forEach(o -> $.up().set(o));
        return $;
    }

    public static Subject interpretMap(Subject $in) {
        Map<?, ?> map = $in.asExpected();
        var $ = Suite.set();
        map.forEach((key, value) -> $.up(key).set(value));
        return $;
    }

    public static Subject interpretSubject(Subject $in) {
        return $in.asExpected();
    }

    public static Subject interpretFile(Subject $in) {
        File file = $in.asExpected();
        return Suite.set(file.getPath());
    }

}
