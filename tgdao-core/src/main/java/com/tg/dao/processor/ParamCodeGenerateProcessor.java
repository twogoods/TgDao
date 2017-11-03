package com.tg.dao.processor;

import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Pair;
import com.tg.dao.annotation.Params;
import com.tg.dao.exception.TgDaoException;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by twogoods on 2017/11/3.
 */
@SupportedAnnotationTypes({"com.tg.dao.annotation.Params"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions({"com.tg.dao.annotation.Params"})
public class ParamCodeGenerateProcessor extends AbstractProcessor {


    public static final String PARAM_ANNOTATION_NAME = Params.class.getCanonicalName();

    private Messager messager;
    private Trees trees;
    private TreeMaker treeMaker;
    private Names names;
    private Enter enter;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        trees = Trees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context);
        enter = Enter.instance(context);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        annotations.stream()
                .filter(typeElement -> typeElement.toString().equals(PARAM_ANNOTATION_NAME))
                .forEach(typeElement -> roundEnv.getElementsAnnotatedWith(typeElement).forEach((this::handleTypeElement)));
        return true;
    }

    private void handleTypeElement(Element element) {
//        ElementFilter.methodsIn(element.getEnclosedElements())
//                .forEach(executableElement -> handleExecutableElement(executableElement));

        if (!(element.getKind() == ElementKind.INTERFACE)) {
            throw new TgDaoException("@Params only annotated Interface");
        }
        JCTree jcTree = (JCTree) trees.getTree(element);
        jcTree.accept(new ParamSwapper());
    }

    private void handleExecutableElement(ExecutableElement executableElement) {
        //JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) trees.getTree(executableElement);


    }

    private class ParamSwapper extends TreeTranslator {
        //这个方法会被循环调用，也就是说如果类中有两个方法，那么会分别调用两次这个方法来传入两个不同的方法进来，是按照先后顺序调用的，如果两个方法中间有成员变量的声明，那么会先去调用visitMethodDef（传入方法1），再去调用visitVarDef然后再回来掉用一次visitMethodDef（传入方法2）
        @Override
        public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
            super.visitMethodDef(jcMethodDecl);

            List<JCTree.JCVariableDecl> variables = jcMethodDecl.getParameters();
            variables.forEach(jcVariableDecl -> {
                JCTree.JCModifiers oldModifiers = jcVariableDecl.getModifiers();
                oldModifiers.annotations.forEach(jcAnnotation -> {
                    jcAnnotation.getAnnotationType();
                });

                Symbol.ClassSymbol stringSymbol = new Symbol.ClassSymbol(1L, names.fromString("java.lang.String"), null);
                Type.ClassType stringType = new Type.ClassType(null, List.nil(), stringSymbol);


                Symbol.ClassSymbol classSymbol = new Symbol.ClassSymbol(1L, names.fromString("org.apache.ibatis.annotations.Param"), null);
                Type.ClassType classType = new Type.ClassType(null, List.<Type>nil(), classSymbol);

                Type.MethodType methodType = new Type.MethodType(List.nil(),stringType,List.nil(),null);
                Symbol.MethodSymbol methodSymbol = new Symbol.MethodSymbol(1L, names.fromString("value"), methodType, classSymbol);



                Attribute.Constant constant = new Attribute.Constant(stringType, jcVariableDecl.getName().toString());

                List<Pair<Symbol.MethodSymbol, Attribute>> pairs = List.of(Pair.of(methodSymbol, constant));

                Attribute.Compound compound = new Attribute.Compound(classType, pairs);
                JCTree.JCAnnotation jcAnnotation = treeMaker.Annotation(compound);
                JCTree.JCModifiers newModifiers = treeMaker.Modifiers(oldModifiers.flags);
                treeMaker.VarDef(newModifiers, jcVariableDecl.getName(), jcVariableDecl.getNameExpression(), jcVariableDecl.getInitializer());
            });


            JCTree.JCMethodDecl methodDecl = treeMaker.MethodDef(jcMethodDecl.getModifiers(), jcMethodDecl.getName(),
                    treeMaker.TypeIdent(TypeTag.VOID), jcMethodDecl.getTypeParameters(), jcMethodDecl.getParameters(),
                    jcMethodDecl.getThrows(), jcMethodDecl.getBody(), jcMethodDecl.defaultValue);
            result = methodDecl;
        }

        private JCTree.JCBlock makeHelloBody() {
            JCTree.JCExpression printExpression = treeMaker.Ident(names.fromString("System"));
            printExpression = treeMaker.Select(printExpression, names.fromString("out"));
            printExpression = treeMaker.Select(printExpression, names.fromString("println"));
            List<JCTree.JCExpression> printArgs = List.from(new JCTree.JCExpression[]{treeMaker.Literal("Hello from HelloProcessor!")});
            printExpression = treeMaker.Apply(List.<JCTree.JCExpression>nil(), printExpression, printArgs);
            JCTree.JCStatement call = treeMaker.Exec(printExpression);
            List<JCTree.JCStatement> statements = List.from(new JCTree.JCStatement[]{call});
            return treeMaker.Block(0, statements);
        }
    }


}
