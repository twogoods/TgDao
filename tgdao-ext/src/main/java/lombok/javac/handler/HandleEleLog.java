package lombok.javac.handler;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil.*;
import me.ele.lombok.Elelog;
import org.mangosdk.spi.ProviderFor;

import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static lombok.javac.handlers.JavacHandlerUtil.*;
import static me.ele.lombok.common.CommonUsage.ELOG_FLAG_USAGE;

@ProviderFor(JavacAnnotationHandler.class)
public class HandleEleLog extends JavacAnnotationHandler<Elelog> {

    @Override
    public void handle(AnnotationValues<Elelog> annotation, JCAnnotation ast, JavacNode annotationNode) {
        handleFlagUsage(annotationNode, ELOG_FLAG_USAGE, "@Elelog");

        String logName = annotation.getInstance().name();
        if (logName == null || logName.isEmpty()) {
            annotationNode.addError("failed because of no name");
            return;
        }

        JavacNode typeNode = annotationNode.up();
        switch (typeNode.getKind()) {
        case TYPE:
            if ((((JCClassDecl) typeNode.get()).mods.flags & Flags.INTERFACE) != 0) {
                annotationNode.addError("@Elelog is legal only on classes and enums.");
                return;
            }
            if (fieldExists(logName, typeNode) != MemberExistsResult.NOT_EXISTS) {
                annotationNode.addWarning("Field '" + logName + "' already exists.");
                return;
            }

            JavacTreeMaker maker = typeNode.getTreeMaker();
            Name name = ((JCClassDecl) typeNode.get()).name;
            JCFieldAccess loggingType = maker.Select(maker.Ident(name), typeNode.toName("class"));

            createField(typeNode, loggingType, annotationNode.get(), logName, ast);
            break;
        default:
            annotationNode.addError("@Elelog is legal only on types.");
            break;
        }

    }

    private void createField(JavacNode typeNode, JCFieldAccess loggingType, JCTree jcTree, String logName, JCTree ast) {
        JavacTreeMaker maker = typeNode.getTreeMaker();

        JCExpression loggerType = chainDotsString(typeNode, "me.ele.elog.Log");
        JCExpression factoryMethod = chainDotsString(typeNode, "me.ele.elog.LogFactory.getLog");

        JCMethodInvocation factoryMethodCall = maker.Apply(List.<JCExpression> nil(), factoryMethod, List.<JCExpression> of(loggingType));

        JCVariableDecl fieldDecl = recursiveSetGeneratedBy(
                maker.VarDef(maker.Modifiers(Flags.PRIVATE | Flags.FINAL | Flags.STATIC), typeNode.toName(logName), loggerType, factoryMethodCall), ast,
                typeNode.getContext());

        injectFieldAndMarkGenerated(typeNode, fieldDecl);
    }
}
