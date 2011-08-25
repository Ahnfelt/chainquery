public abstract class Type {

    public abstract <R, A> R accept(A argument, Visitor<R, A> visitor);
    
    public interface Visitor<R, A> {
        public R visit(A argument, Double value);
        public R visit(A argument, String value);
    }
}

