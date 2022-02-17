package brackettree;

import brackettree.reader.ObjectFactory;
import brackettree.writer.TreeDesigner;
import suite.suite.Subject;
import suite.suite.action.Statement;
import suite.suite.util.Cascade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static suite.suite.$uite.*;

public class Main {

    public static class Foo implements Interpreted {
        int a;
        Foo foo = null;

        public Foo() {
        }

        public Foo(int a, int b) {
            this.a = a;
        }

        public Foo(int a, int b, Foo foo) {
            this.a = a;
            this.foo = foo;
        }

        @Override
        public String toString() {
            return super.toString() + "{" +
                    "a=" + a +
                    '}';
        }
    }

    public static class Repo {
        List<Foo> foos = new ArrayList<>();

        public Repo(Foo ... foos) {
            this.foos.addAll(Arrays.asList(foos));
        }

        public static Subject decompose(Repo repo) {
            var $d = $();
            var c = new Cascade<>(repo.foos.iterator());
            $d.add(TreeDesigner.reserve(c.next()));
            for(var foo : c) {
                $d.add(foo);
            }
            return $d;
        }

        public static Subject compose(Subject $) {
            var repo = new Repo();
            for(var f : $.list().eachAs(Foo.class)) {
                repo.foos.add(f);
            }
            return $(repo);
        }

        public String toString() {
            return "Repo{" + foos.toString() + "}";
        }
    }

    static int a;
    static int b;

    public static void main(String[] args) {

        record Bar(int a, int b, Bar bar){}

        var bar = new Bar(1,2,null);
        var bara = new Bar(11,22,bar);
        var sub = $(bar, $(bar), 3, 4, $(bara));
        var str = BracketTree.encode(sub);
        System.out.println(str);

        var obj = BracketTree.parse(str, Subject.class);
        obj.print();

//        var obj = BracketTree.parse("""
//                @class[ serial ]
//                :<>=0005sr0011java.util.CollSerW8>:;;63:1;:811030001I0003tagxp00000001w0400000003sr0011java.lang.Integer12>2:0:4?7818738020001I0005valuexr0010java.lang.Number86:<951=0;94>08;020000xp00000001sq00~000200000002sq00~000200000003x[]
//                """, Object.class);
//        System.out.println(obj);
//        System.out.println(obj.getClass());

//        String tree = BracketTree.encode(new Bar(343, 434));
//        System.out.println(tree);
//        var bar = BracketTree.parse(tree, Bar.class);
//        var of = new ObjectFactory();
//        of.setClassAlias("Bar", Bar.class);
//        var bar = BracketTree.parse("[#1] [#1] @#1[ @class[ Bar ] a[ 5 ] b[ 10 ] ]", of, Object.class);
        /*
        var config = $(
                "alias", $(
                        "Bar", $(Bar.class)
                )
        );
        #config = [
          .alias[
            .Bar[ @Bar ]
          ]
        ]
        var config = BracketTree.alias("Bar", Bar.class)
        var bar = BracketTree.parse("[#1] [#1] @#1[ @class[ Bar ] a[ 5 ] b[ 10 ] ]", BracketTree.alias("Bar", Bar.class), Object.class);
         */
//        System.out.println(bar);
//        System.out.println(bar.getClass());
//        var sub = (Subject)bar;
//        sub.print();
//        System.out.println(sub.at(0).raw() == sub.at(1).raw());
    }
}
