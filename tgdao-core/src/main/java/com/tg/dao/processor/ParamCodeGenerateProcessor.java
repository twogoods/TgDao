package com.tg.dao.processor;

import com.sun.source.util.Trees;
import com.sun.tools.javac.code.*;
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
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Iterator;
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

    private Symbol.ClassSymbol mybatisParam;
    private Symbol.MethodSymbol valueMethod;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        trees = Trees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        annotations.stream().filter(typeElement -> typeElement.toString().equals(PARAM_ANNOTATION_NAME)).forEach(typeEle -> {
            Symbol.ClassSymbol paramAnnotation = (Symbol.ClassSymbol) typeEle;
            Symbol.MethodSymbol tagMethod = (Symbol.MethodSymbol) paramAnnotation.members().elems.sym;
            Type.ArrayType resType = (Type.ArrayType) tagMethod.getReturnType();
            mybatisParam = (Symbol.ClassSymbol) resType.elemtype.asElement();
            valueMethod = (Symbol.MethodSymbol) mybatisParam.members().elems.sym;
        });
        if (mybatisParam == null || valueMethod == null) {
            messager.printMessage(Diagnostic.Kind.WARNING, "can't find class  org.apache.ibatis.annotations.Param , check have import mybatis jar");
            return true;
        }
        annotations.stream().filter(typeElement -> typeElement.toString().equals(PARAM_ANNOTATION_NAME))
                .forEach(typeElement -> roundEnv.getElementsAnnotatedWith(typeElement).forEach((this::handleTypeElement)));
        return true;
    }

    private void handleTypeElement(Element element) {
        if ((element.getKind() == ElementKind.METHOD)) {
            Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) element;
            if (methodSymbol.getEnclosingElement().getAnnotation(Params.class) != null) {
                return;
            }
            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) trees.getTree(element);
            handleMethod(jcMethodDecl);
            return;
        }
        if ((element.getKind() != ElementKind.INTERFACE)) {
            throw new TgDaoException("@Params only annotated Interface or Method");
        }
        JCTree jcTree = (JCTree) trees.getTree(element);
        jcTree.accept(new ParamWapper());
    }

    private void handleMethod(JCTree.JCMethodDecl jcMethodDecl) {
        List<JCTree.JCVariableDecl> variables = jcMethodDecl.getParameters();
        variables.stream().forEach(jcVariableDecl -> {
            JCTree.JCModifiers modifiers = jcVariableDecl.getModifiers();
            JCTree.JCAnnotation jcAnnotation = generateParamAnnotation(jcVariableDecl.getName().toString());
            if (modifiers.getAnnotations().size() == 0) {
                modifiers.annotations = List.of(jcAnnotation);
            } else {
                Iterator<JCTree.JCAnnotation> iterator = modifiers.getAnnotations().iterator();
                while (iterator.hasNext()) {
                    JCTree.JCAnnotation item = iterator.next();
                    if ("org.apache.ibatis.annotations.Param".equals(item.type.toString())) {
                        return;
                    }
                }
                modifiers.annotations = modifiers.getAnnotations().append(jcAnnotation);
            }
        });
    }

    private JCTree.JCAnnotation generateParamAnnotation(String paramName) {
        Symbol.ClassSymbol stringSymbol = new Symbol.ClassSymbol(17L, names.fromString("java.lang.String"), null);
        Type.ClassType stringType = new Type.ClassType(Type.noType, List.nil(), stringSymbol);
        Type.ClassType mybatisParamType = new Type.ClassType(Type.noType, List.nil(), mybatisParam);
        Attribute.Constant constant = new Attribute.Constant(stringType, paramName);
        Attribute.Compound compound = new Attribute.Compound(mybatisParamType, List.of(Pair.of(valueMethod, constant)));
        return treeMaker.Annotation(compound);
    }

    private class ParamWapper extends TreeTranslator {
        @Override
        public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
            super.visitMethodDef(jcMethodDecl);
            handleMethod(jcMethodDecl);
            JCTree.JCMethodDecl methodDecl = treeMaker.MethodDef(jcMethodDecl.getModifiers(), jcMethodDecl.getName(),
                    (JCTree.JCExpression) jcMethodDecl.getReturnType(), jcMethodDecl.getTypeParameters(), jcMethodDecl.getParameters(),
                    jcMethodDecl.getThrows(), jcMethodDecl.getBody(), jcMethodDecl.defaultValue);
            result = methodDecl;
        }
    }
}
