public enum Relation {
    EQUAL, NOT_EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL;
    public String toString() {
        switch(this) {
            case EQUAL: return "==";
            case NOT_EQUAL: return "!=";
            case LESS: return "<";
            case LESS_EQUAL: return "<=";
            case GREATER: return ">";
            case GREATER_EQUAL: return ">=";
        }
        throw new UnsupportedOperationException("Unhandled operator");
    }
}

