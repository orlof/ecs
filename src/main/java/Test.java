abstract class A {
    public static int cid = 0;

    public int get() throws NoSuchFieldException, IllegalAccessException {
        return getClass().getField("cid").getInt(this);
    }
}

class AA extends A {
    public static int cid = 1;
}

public class Test {
    public static void main(String[] args) throws Exception {
        A aa = new AA();
        System.out.println("aa.cid = " + aa.cid);
        System.out.println("A.cid = " + A.cid);
        System.out.println("AA.cid = " + AA.cid);
        System.out.println("aa.getClass().getField(\"cid\").getInt(null) = " + aa.getClass().getField("cid").getInt(null));
        System.out.println("aa.getClass().getField(\"cid\").getInt(aa) = " + aa.getClass().getField("cid").getInt(aa));
        System.out.println("aa.get() = " + aa.get());
    }
}
