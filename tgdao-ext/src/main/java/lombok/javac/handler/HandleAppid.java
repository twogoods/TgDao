package lombok.javac.handler;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import me.ele.lombok.Appid;
import org.mangosdk.spi.ProviderFor;

@ProviderFor(JavacAnnotationHandler.class)
public class HandleAppid extends JavacAnnotationHandler<Appid> {

    @Override
    public void handle(AnnotationValues<Appid> annotation, JCAnnotation ast, JavacNode annotationNode) {

    }

}
