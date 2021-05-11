package fr.redstonneur1256.logic.statements;

import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import fr.redstonneur1256.logic.AdvancedLogicOp;
import mindustry.logic.LAssembler;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import mindustry.ui.Styles;

import java.util.Arrays;

import static fr.redstonneur1256.logic.RedLogic.color;

public class AdvancedOperationStatement extends LStatement {

    private AdvancedLogicOp op;
    private String dest;
    private String[] parameters;

    public AdvancedOperationStatement(String[] tokens) {
        if(tokens == null) {
            tokens = new String[0];
        }

        op = tokens.length < 2 ? AdvancedLogicOp.all[0] : AdvancedLogicOp.valueOf(tokens[1]);
        dest = tokens.length < 3 ? "result" : tokens[2];
        parameters = new String[5];
        Arrays.fill(parameters, "0");

        for(int i = 0; i < 5 && i < tokens.length - 4; i++) {
            parameters[i] = tokens[3 + i];
        }
    }

    @Override
    public void build(Table table) {
        table.clearChildren();

        field(table, dest, value -> dest = value);
        table.add(" = ");

        table.button(button -> {
            button.label(() -> op.getName());
            button.clicked(() -> showSelect(button, AdvancedLogicOp.all, op, o -> {
                op = o;
                build(table);
            }));
        }, Styles.logict, () -> {
        }).size(64, 40).pad(4).color(table.color);

        for(int i = 0; i < op.getParamCount(); i++) {
            int index = i;
            field(table, parameters[i], value -> parameters[index] = value);
        }
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append("redOp ").append(op.name()).append(' ').append(dest);
        for(String parameter : parameters) {
            builder.append(' ').append(parameter);
        }
    }

    @Override
    public Color color() {
        return color;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        int[] output = new int[parameters.length];
        for(int i = 0; i < parameters.length; i++) {
            output[i] = builder.var(parameters[i]);
        }
        return new AdvancedOperationInstruction(op, builder.var(dest), output);
    }

    public static class AdvancedOperationInstruction implements LExecutor.LInstruction {

        private AdvancedLogicOp op;
        private int dest;
        private int[] parameters;

        public AdvancedOperationInstruction(AdvancedLogicOp op, int dest, int[] parameters) {
            this.op = op;
            this.dest = dest;
            this.parameters = parameters;
        }

        @Override
        public void run(LExecutor exec) {
            double a = exec.num(parameters[0]);
            double b = exec.num(parameters[1]);
            double c = exec.num(parameters[2]);
            double d = exec.num(parameters[3]);
            double e = exec.num(parameters[4]);

            exec.setnum(dest, op.getOp().execute(a, b, c, d, e));
        }

    }

}
