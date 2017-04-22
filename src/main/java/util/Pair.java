package util;

import java.util.Objects;

/**
 * Created by Evan on 4/16/2017.
 */
public class Pair<X,Y> {
    public final X _1;
    public final Y _2;
    protected boolean ordered;
    public Pair(X _1, Y _2) {
        this(_1,_2,true);
    }

    public Pair(X _1, Y _2, boolean ordered) {
        this._1=_1;
        this._2=_2;
        this.ordered=ordered;
    }

    @Override
    public int hashCode() {
        if(ordered) {
            return Objects.hash(_1,_2);
        } else {
            return Objects.hash(_1, _2) / 2 + Objects.hash(_2, _1) / 2;
        }
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Pair)) return false;

        Pair p2 = (Pair)other;
        if(ordered) {
            return (p2._1.equals(_1)&&p2._2.equals(_2));
        } else {
            return (p2._1.equals(_1) && p2._2.equals(_2)) || (p2._2.equals(_1) && p2._1.equals(_2));
        }
    }

    @Override
    public String toString() {
        return _1.toString()+";"+_2.toString();
    }

    public static void main(String[] args) {
        // tests
        if(new Pair<>("HI","MY NAME").equals(new Pair<>("MY NAME", "HI"))) {
            System.out.println("FAILED");
        }
        if(!(new Pair<>("HI","MY NAME",false).equals(new Pair<>("MY NAME", "HI",false)))) {
            System.out.println("FAILED");
        }
    }
}
