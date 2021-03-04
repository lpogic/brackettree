package brackettree;

import brackettree.reader.BracketTree;
import suite.suite.Suite;
import suite.suite.util.Sequence;

import java.util.Arrays;

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

    public static void main(String[] args) {
//        System.out.println(Integer[].class);

        var foo = new Foo(5, 6);
        System.out.println(BracketTree.writer().encode(Suite.inset(foo, Suite.set(foo, "a"))));
//        System.out.println(BracketTree.writer().encode(new Foo[]{new Foo(1, 2), new Foo(3, 4, foo), foo}));
//        BracketTree.parse("@[[int]][1][2][3]");
        System.out.println(BracketTree.parse("@[[int]][1][2][#1][#[1]3]").as(Integer[].class)[3]);
        int[][] i = new int[][]{{1}, {2, 3}};
    }
}
