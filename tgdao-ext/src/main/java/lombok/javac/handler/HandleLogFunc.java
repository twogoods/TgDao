package lombok.javac.handler;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import me.ele.lombok.LogFunc;

import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static me.ele.lombok.common.CommonUsage.ELOG_FLAG_USAGE;

public class HandleLogFunc extends JavacAnnotationHandler<LogFunc> {

    @Override
    public void handle(AnnotationValues<LogFunc> annotation, JCAnnotation ast, JavacNode annotationNode) {
        handleFlagUsage(annotationNode, ELOG_FLAG_USAGE, "@LogFunc");
    }
}
