package fr.redstonneur1256.logic;

import arc.func.Func;
import arc.graphics.Color;
import fr.redstonneur1256.logic.statements.AdvancedOperationStatement;
import fr.redstonneur1256.logic.statements.CommentStatement;
import mindustry.gen.LogicIO;
import mindustry.logic.LAssembler;
import mindustry.logic.LStatement;

public class RedLogic {

    public static final Color color = new Color(0xFF0000FF);

    public RedLogic() {
        register("comment", CommentStatement::new);
        register("redOp", AdvancedOperationStatement::new);
    }

    private void register(String name, Func<String[], LStatement> provider) {
        LAssembler.customParsers.put(name, provider);
        LogicIO.allStatements.add(() -> provider.get(null));
    }

}
