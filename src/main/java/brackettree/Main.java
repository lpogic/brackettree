package brackettree;

import brackettree.reader.BracketTree;

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

        System.out.println(BracketTree.writer().encode(new Foo[]{new Foo(1, 2), new Foo(3, 4, new Foo(5, 6))}));
//        BracketTree.parse("#[[int]][1][2][3]");
        System.out.println(BracketTree.parse("#[ [ [ int ] ] ][ [ 1 ] ][ [ 2 ][ 3 ] ]").direct().getClass());
        int[][] i = new int[][]{{1}, {2, 3}};
    }
}
