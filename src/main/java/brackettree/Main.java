package brackettree;

import brackettree.writer.TreeDesigner;
import suite.suite.Subject;
import suite.suite.util.Cascade;

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

    public static void main(String[] args) {

        record Bar(int a, int b){}

        String tree = BracketTree.encode(new Bar(343, 434));
        System.out.println(tree);
        var bar = BracketTree.parse(tree).as(Bar.class);
        System.out.println(bar);
    }
}
