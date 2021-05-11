package fr.redstonneur1256.logic;

import fr.redstonneur1256.utils.RUtils;

public enum AdvancedLogicOp {

    FACTORIAL("fact", RUtils::factorial);

    public static final AdvancedLogicOp[] all = values();
    private String name;
    private int paramCount;
    private Op5 op;

    AdvancedLogicOp(String name, Op1 op) {
        this(name, 1, (a, b, c, d, e) -> op.execute(a));
    }

    AdvancedLogicOp(String name, Op2 op) {
        this(name, 2, (a, b, c, d, e) -> op.execute(a, b));

    }

    AdvancedLogicOp(String name, Op3 op) {
        this(name, 3, (a, b, c, d, e) -> op.execute(a, b, c));

    }

    AdvancedLogicOp(String name, Op4 op) {
        this(name, 4, (a, b, c, d, e) -> op.execute(a, b, c, d));

    }

    AdvancedLogicOp(String name, Op5 op) {
        this(name, 5, op);
    }

    AdvancedLogicOp(String name, int paramCount, Op5 op) {
        this.name = name;
        this.paramCount = paramCount;
        this.op = op;
    }

    public String getName() {
        return name;
    }

    public int getParamCount() {
        return paramCount;
    }

    public Op5 getOp() {
        return op;
    }

    @Override
    public String toString() {
        return name;
    }

    private interface Op1 {

        double execute(double a);

    }

    private interface Op2 {

        double execute(double a, double b);

    }

    private interface Op3 {

        double execute(double a, double b, double c);

    }

    private interface Op4 {

        double execute(double a, double b, double c, double d);

    }

    public interface Op5 {

        double execute(double a, double b, double c, double d, double e);

    }

}
