package lombok.eclipse.handlers;

import lombok.core.AnnotationValues;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import me.ele.lombok.Appid;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.mangosdk.spi.ProviderFor;

import java.util.Arrays;

import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static lombok.eclipse.Eclipse.fromQualifiedName;
import static lombok.eclipse.handlers.EclipseHandlerUtil.createNameReference;
import static lombok.eclipse.handlers.EclipseHandlerUtil.setGeneratedBy;
import static me.ele.lombok.common.CommonUsage.APPID_FLAG_USAGE;

@ProviderFor(EclipseAnnotationHandler.class)
public class HandleAppid extends EclipseAnnotationHandler<Appid> {

    @Override
    public void handle(AnnotationValues<Appid> annotation, Annotation ast, EclipseNode annotationNode) {
        handleFlagUsage(annotationNode, APPID_FLAG_USAGE, "@Appid");
        String value = annotation.getInstance().value();
        if (value == null || value.isEmpty()) {
            annotationNode.addError("failed because of no value");
            return;
        }
        EclipseNode owner = annotationNode.up();
        switch (owner.getKind()) {
        case METHOD:
            AbstractMethodDeclaration blockNode = (AbstractMethodDeclaration) owner.get();
            int pS = ast.sourceStart, pE = ast.sourceEnd;
            LocalDeclaration appid = createAppidLocal(pS, pE, ast);

            StringLiteral unknown = new StringLiteral("unknown".toCharArray(), pS, pE, 0);
            setGeneratedBy(unknown, ast);

            SingleNameReference appidLocal = getNameReference("appid", ast);
            EqualExpression appidIsNull = new EqualExpression(appidLocal, unknown, OperatorIds.EQUAL_EQUAL);
            setGeneratedBy(appidIsNull, ast);

            long position = (long) ast.sourceStart << 32 | ast.sourceEnd;
            MessageSend set = setAppid(ast, pS, pE, position, true, value);
            IfStatement ifappidIsNull = new IfStatement(appidIsNull, set, pS, pE);
            setGeneratedBy(ifappidIsNull, ast);

            Statement[] statements = new Statement[2];
            statements[0] = appid;
            statements[1] = ifappidIsNull;

            blockNode.statements = statements;
            owner.rebuild();
            break;
        default:
            annotationNode.addError("@Appid is legal only on methods.");
            break;
        }
    }

    private MessageSend setAppid(final Annotation ast, final int pS, final int pE, final long position, final boolean isSuccess, String value) {
        long p = (long) pS << 32 | pE;
        MessageSend methodCall = new MessageSend();
        setGeneratedBy(methodCall, ast);
        methodCall.receiver = createNameReference("me.ele.arch.etrace.agent.config.AgentConfiguration", ast);
        methodCall.selector = "setAppId".toCharArray();
        methodCall.arguments = new Expression[] { new StringLiteral(value.toCharArray(), pS, pE, 0) };
        methodCall.nameSourcePosition = p;
        methodCall.sourceStart = pS;
        methodCall.sourceEnd = methodCall.statementEnd = pE;
        return methodCall;
    }

    private SingleNameReference getNameReference(String name, Annotation ast) {
        SingleNameReference ref = new SingleNameReference(name.toCharArray(), 0);
        setGeneratedBy(ref, ast);
        return ref;
    }

    private LocalDeclaration createAppidLocal(int pS, int pE, Annotation ast) {
        long p = (long) pS << 32 | pE;
        LocalDeclaration valDecl = new LocalDeclaration("appid".toCharArray(), pS, pE);
        setGeneratedBy(valDecl, ast);
        valDecl.type = createTypeReference("java.lang.String", ast);
        MessageSend methodCall = new MessageSend();
        setGeneratedBy(methodCall, ast);

        methodCall.receiver = createNameReference("me.ele.arch.etrace.agent.config.AgentConfiguration", ast);
        methodCall.selector = "getAppId".toCharArray();

        methodCall.nameSourcePosition = p;
        methodCall.sourceStart = pS;
        methodCall.sourceEnd = methodCall.statementEnd = pE;

        valDecl.initialization = methodCall;

        return valDecl;
    }

    private TypeReference createTypeReference(final String typeName, final Annotation ast) {
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
