package com.tg.compile;

import com.tg.annotation.Data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Description:
 *
 * @author twogoods
 * @version 0.1
 * @since 2017-05-06
 */
@SupportedAnnotationTypes("com.tg.annotation.Data")
public class AutoWhenCompile extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        // 获得被该注解声明的元素
        Set<? extends Element> elememts = roundEnv.getElementsAnnotatedWith(Data.class);
        messager.printMessage(Diagnostic.Kind.WARNING, "twogoods");
        for (Element annotatedElement : elememts) {
            if (annotatedElement.getKind() == ElementKind.INTERFACE) {
            }
        }
        generateFile();
        return true;
    }

    private void generateFile() {
        File dir = new File(AutoWhenCompile.class.getResource("/").getPath());
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(dir, "test.txt");
        try {
            FileWriter fw = new FileWriter(file);
            fw.append("hahah \n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // @Override
    // public Set<String> getSupportedAnnotationTypes() {
    //     Set<String> set = super.getSupportedAnnotationTypes();
    //     if (set == null) {
    //         set = new HashSet<>();
    //     }
    //     set.add("com.tg.annotation.Data");
    //     return set;
    // }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
