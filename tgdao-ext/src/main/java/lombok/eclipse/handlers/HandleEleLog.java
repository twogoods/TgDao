package lombok.eclipse.handlers;

import lombok.core.AnnotationValues;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil.*;
import me.ele.lombok.Elelog;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.mangosdk.spi.ProviderFor;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static lombok.eclipse.Eclipse.fromQualifiedName;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;
import static me.ele.lombok.common.CommonUsage.ELOG_FLAG_USAGE;

@ProviderFor(EclipseAnnotationHandler.class)
public class HandleEleLog extends EclipseAnnotationHandler<Elelog> {

    @Override
    public void handle(AnnotationValues<Elelog> annotation, Annotation ast, EclipseNode annotationNode) {
        handleFlagUsage(annotationNode, ELOG_FLAG_USAGE, "@Elelog");
        String logName = annotation.getInstance().name();
        if (logName == null || logName.isEmpty()) {
            annotationNode.addError("failed because of no name");
            return;
        }

        EclipseNode owner = annotationNode.up();
        switch (owner.getKind()) {
        case TYPE:
            TypeDeclaration typeDecl = null;
            if (owner.get() instanceof TypeDeclaration)
                typeDecl = (TypeDeclaration) owner.get();
            int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;

            boolean notAClass = (modifiers & (ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation)) != 0;

            if (typeDecl == null || notAClass) {
                annotationNode.addError("@Elog is legal only on classes and enums.");
                return;
            }

            if (fieldExists(logName, owner) != MemberExistsResult.NOT_EXISTS) {
                annotationNode.addWarning("Field '" + logName + "' already exists.");
                return;
            }

            FieldDeclaration fieldDeclaration = createField(ast, logName);
            fieldDeclaration.traverse(new SetGeneratedByVisitor(ast), typeDecl.staticInitializerScope);
            injectField(owner, fieldDeclaration);
            owner.rebuild();
            break;
        default:
            annotationNode.addError("@Elelog is legal only on types.");
            break;
        }
    }

    private FieldDeclaration createField(Annotation ast, String logName) {
        int pS = ast.sourceStart, pE = ast.sourceEnd;
        long p = (long) pS << 32 | pE;

        // private static final <Type> elog = <factoryMethod>(<parameter>);
        FieldDeclaration fieldDecl = new FieldDeclaration(logName.toCharArray(), 0, -1);
        setGeneratedBy(fieldDecl, ast);
        fieldDecl.declarationSourceEnd = -1;
        fieldDecl.modifiers = Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL;

        fieldDecl.type = createTypeReference("me.ele.elog.Log", ast);

        MessageSend factoryMethodCall = new MessageSend();
        setGeneratedBy(factoryMethodCall, ast);

        factoryMethodCall.receiver = createNameReference("me.ele.elog.LogFactory", ast);
        factoryMethodCall.selector = "getLog".toCharArray();

        TypeReference copy = copyType(fieldDecl.type, ast);
        Expression parameter = new ClassLiteralAccess(ast.sourceEnd, copy);
        setGeneratedBy(parameter, ast);

        factoryMethodCall.arguments = new Expression[] { parameter };
        factoryMethodCall.nameSourcePosition = p;
        factoryMethodCall.sourceStart = pS;
        factoryMethodCall.sourceEnd = factoryMethodCall.statementEnd = pE;

        fieldDecl.initialization = factoryMethodCall;

        return fieldDecl;
    }

    private TypeReference createTypeReference(String typeName, Annotation ast) {
        int pS = ast.sourceStart, pE = ast.sourceEnd;
        long p = (long) pS << 32 | pE;

        TypeReference typeReference;
        if (typeName.contains(".")) {

            char[][] typeNameTokens = fromQualifiedName(typeName);
            long[] pos = new long[typeNameTokens.length];
            Arrays.fill(pos, p);

            typeReference = new QualifiedTypeReference(typeNameTokens, pos);
        } else {
            typeReference = null;
        }

        setGeneratedBy(typeReference, ast);
        return typeReference;
    }
}
