package com.tg.dao.processor;

import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import com.tg.dao.annotation.Params;

import java.util.Set;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import static com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import static com.sun.tools.javac.tree.JCTree.JCBlock;
import static com.sun.tools.javac.tree.JCTree.JCClassDecl;
import static com.sun.tools.javac.tree.JCTree.JCExpression;
import static com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import static com.sun.tools.javac.tree.JCTree.JCIdent;
import static com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import static com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import static com.sun.tools.javac.tree.JCTree.JCModifiers;
import static com.sun.tools.javac.tree.JCTree.JCNewClass;
import static com.sun.tools.javac.tree.JCTree.JCStatement;
import static com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import static com.sun.tools.javac.tree.JCTree.JCVariableDecl;

/**
 * Created by twogoods on 2017/11/3.
 */
@SupportedAnnotationTypes({"com.tg.dao.annotation.Params"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions({"com.tg.dao.annotation.Params"})
public class Hello extends AbstractProcessor {
    private Trees mTrees;
    private TreeMaker mTreeMaker;
    private Names names;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        mTreeMaker = TreeMaker.instance(context);
        mTrees = Trees.instance(processingEnv);
        names = Names.instance(context);
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        Set<? extends Element> elements = env.getElementsAnnotatedWith(Params.class);
        if (elements.size() > 0) {
            for (Element element : elements) {
                JCClassDecl classDecl = (JCClassDecl) mTrees.getTree(element);
                addMainMethod(classDecl, element.toString());
                addHelloMethod(classDecl);
            }
        }
        return true;
    }

    private void addMainMethod(JCClassDecl classDecl, String className) {
        JCModifiers modifiers = mTreeMaker.Modifiers(Flags.PUBLIC | Flags.STATIC);
        JCExpression returnType = mTreeMaker.TypeIdent(TypeTag.VOID);
        List<JCVariableDecl> parameters = makeMainParameters();
        List<JCTypeParameter> generics = List.nil();
        List<JCExpression> throwz = List.nil();
        JCBlock methodBody = makeMainBody(className);
        Name methodName = getName("main");
        JCMethodDecl mainMethodDecl = mTreeMaker.MethodDef(modifiers, methodName, returnType,
                generics, parameters, throwz, methodBody, null);
        classDecl.defs = classDecl.defs.append(mainMethodDecl);
    }

    private List<JCVariableDecl> makeMainParameters() {
        JCIdent paramType = mTreeMaker.Ident(getName("String"));
        JCArrayTypeTree paramArray = mTreeMaker.TypeArray(paramType);
        JCVariableDecl paramDecl = mTreeMaker.VarDef(mTreeMaker.Modifiers(Flags.PARAMETER),
                getName("args"), paramArray, null);
        return List.from(new JCVariableDecl[]{paramDecl});
    }

    private JCBlock makeMainBody(String className) {
        String[] strings = className.split("\\.");
        JCExpression classNameIdent = mTreeMaker.Ident(getName(strings[0]));
        for (int i = 1; i < strings.length; i++) {
            classNameIdent = mTreeMaker.Select(classNameIdent, getName(strings[i]));
        }
        JCNewClass classObj = mTreeMaker.NewClass(null, List.<JCExpression>nil(), classNameIdent,
                List.<JCExpression>nil(), null);
        JCFieldAccess printHello = mTreeMaker.Select(classObj, getName("printHello"));
        JCMethodInvocation printHelloInv = mTreeMaker.Apply(List.<JCExpression>nil(), printHello, List.<JCExpression>nil());
        JCStatement exec = mTreeMaker.Exec(printHelloInv);
        List<JCStatement> statements = List.of(exec);
        return mTreeMaker.Block(0, statements);
    }

    private void addHelloMethod(JCClassDecl classDecl) {
        JCModifiers modifiers = mTreeMaker.Modifiers(Flags.PRIVATE | Flags.FINAL);
        JCExpression returnType = mTreeMaker.TypeIdent(TypeTag.VOID);
        List<JCVariableDecl> parameters = List.nil();
        List<JCTypeParameter> generics = List.nil();
        Name methodName = getName("printHello");
        List<JCExpression> throwz = List.nil();
        JCBlock methodBody = makeHelloBody();
        JCMethodDecl helloMethodDecl = mTreeMaker.MethodDef(modifiers, methodName, returnType,
                generics, parameters, throwz, methodBody, null);
        classDecl.defs = classDecl.defs.append(helloMethodDecl);
    }

    private JCBlock makeHelloBody() {
        JCExpression printExpression = mTreeMaker.Ident(getName("System"));
        printExpression = mTreeMaker.Select(printExpression, getName("out"));
        printExpression = mTreeMaker.Select(printExpression, getName("println"));
        List<JCExpression> printArgs = List.from(new JCExpression[]{mTreeMaker.Literal("Hello from HelloProcessor!")});
        printExpression = mTreeMaker.Apply(List.<JCExpression>nil(), printExpression, printArgs);
        JCStatement call = mTreeMaker.Exec(printExpression);
        List<JCStatement> statements = List.from(new JCStatement[]{call});
        return mTreeMaker.Block(0, statements);
    }

    private Name getName(String string) {
        return names.fromString(string);
    }
}