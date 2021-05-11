package fr.redstonneur1256.logic.statements;

import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import fr.redstonneur1256.logic.NullInstruction;
import mindustry.logic.LAssembler;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import mindustry.ui.Styles;

import static fr.redstonneur1256.logic.RedLogic.color;

public class CommentStatement extends LStatement {

    private String comment;

    public CommentStatement(String[] tokens) {
        comment = tokens == null ? "" : tokens[1].substring(1, tokens[1].length() - 1);
    }

    @Override
    public void build(Table table) {
        table.area(comment, Styles.nodeArea, v -> comment = v).growX().height(90f).padLeft(2).padRight(6).color(table.color);
    }

    @Override
    public Color color() {
        return color;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new NullInstruction();
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append("comment \"").append(comment).append('"');
    }

}
