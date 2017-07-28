package com.tg.compile;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.tg.annotation.*;
import com.tg.constant.Attach;
import com.tg.util.StringUtils;
import com.tg.util.XmlUtils;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Description:
 *
 * @author twogoods
 * @version 0.1
 * @since 2017-05-06
 */
@SupportedAnnotationTypes({"com.tg.annotation.DaoGen", "com.tg.annotation.Table"})
public class AutoWhenCompile extends AbstractProcessor {

    public static final String DAOGENANNOTATIONNAME = DaoGen.class.getCanonicalName();
    public static final String TABLEANNOTATIONNAME = Table.class.getCanonicalName();

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
        //annotations的值是通过@SupportedAnnotationTypes声明的且目标源代码拥有的所有Annotations
        Messager messager = processingEnv.getMessager();
        annotations.stream()
                .filter(typeElement -> typeElement.toString().equals(TABLEANNOTATIONNAME))
                .forEach(typeElement -> {
                    roundEnv.getElementsAnnotatedWith(typeElement).forEach((this::handleTableElement));
                });

        annotations.stream()
                .filter(typeElement -> typeElement.toString().equals(DAOGENANNOTATIONNAME))
                .forEach(typeElement -> {
                    roundEnv.getElementsAnnotatedWith(typeElement).forEach((this::handleDaoGenElement));
                });
        //generateFile();
        return true;
    }

    public void handleTableElement(Element element) {
        Symbol.ClassSymbol classSymbol = (Symbol.ClassSymbol) element;
        TableMapping tableMapping = new TableMapping();
        tableMapping.setClassName(classSymbol.getQualifiedName().toString());
        Table table = classSymbol.getAnnotation(Table.class);
        String tableName = table.name();
        tableMapping.setTableName(tableName);
        Map<String, String> fieldToColumn = new HashMap<>();
        Map<String, String> columnToField = new HashMap<>();
        List<Symbol> list = classSymbol.getEnclosedElements();

        list.stream().filter(symbol -> symbol.getKind() == ElementKind.FIELD)
                .forEach(symbol -> {
                    String columnName = parseColumnAnnotation(symbol);
                    String fieldName = symbol.getSimpleName().toString();
                    fieldToColumn.put(fieldName, StringUtils.isEmpty(columnName) ? fieldName : columnName);
                    columnToField.put(StringUtils.isEmpty(columnName) ? fieldName : columnName, fieldName);
                });
        System.out.println(fieldToColumn);
    }

    public String parseColumnAnnotation(Element element) {
        Column column = element.getAnnotation(Column.class);
        return column == null ? null : column.value();
    }

    public void handleDaoGenElement(Element element) {
        //TODO 编译的warning提示
        if (!(element.getKind() == ElementKind.INTERFACE)) return;

        /*
        element.getAnnotationMirrors().forEach(annotationMirror -> {
            if (DaoGen.class.getName().equals(annotationMirror.getAnnotationType().toString())) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> map = annotationMirror.getElementValues();
                Type.ClassType classType = null;
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : map.entrySet()) {
                    if ("model".equals(entry.getKey().getSimpleName().toString())) {
                        classType = (Type.ClassType) entry.getValue().getValue();
                        break;
                    }
                }
                Boolean flag = classType.isAnnotated();
                List<Attribute.TypeCompound> list = classType.getAnnotationMirrors();
                Annotation[] ans = classType.getAnnotationsByType(Table.class);
            }
        });
        */

        DaoGen daoGen = element.getAnnotation(DaoGen.class);
        daoGen.model();

        Symbol.ClassSymbol classSymbol = (Symbol.ClassSymbol) element;
        String daoName = classSymbol.getQualifiedName().toString();
        List<? extends ExecutableElement> executableElements = ElementFilter.methodsIn(element.getEnclosedElements());

        try {
            org.dom4j.Element rootElement = XmlUtils.generateMybatisXmlFrame(daoName);
            for (ExecutableElement executableElement : executableElements) {
                handleExecutableElement(executableElement, rootElement);

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleExecutableElement(ExecutableElement executableElement, org.dom4j.Element root) {
        List<? extends AnnotationMirror> annotationInMethod = executableElement.getAnnotationMirrors();
        Select select = executableElement.getAnnotation(Select.class);
        if (select != null) {
            selectSql(select, executableElement, root);
            return;
        }
        Insert insert = executableElement.getAnnotation(Insert.class);
        if (insert != null) {
            insertSql(insert, executableElement, root);
            return;
        }
        Update update = executableElement.getAnnotation(Update.class);
        if (update != null) {
            updateSql(update, executableElement, root);
            return;
        }
        Delete delete = executableElement.getAnnotation(Delete.class);
        if (delete != null) {
            deleteSql(delete, executableElement, root);
        }
    }

    public void selectSql(Select select, ExecutableElement executableElement, org.dom4j.Element root) {
        List<? extends VariableElement> variableElements = executableElement.getParameters();
        StringBuilder selectSql = new StringBuilder();
        String columns = select.columns();
        if (StringUtils.isEmpty(columns)) {
            selectSql.append("select * from ");
        }

        for (VariableElement variableElement : variableElements) {
            String varName = variableElement.getSimpleName().toString();
            List<? extends AnnotationMirror> list = variableElement.getAnnotationMirrors();
            Condition condition = variableElement.getAnnotation(Condition.class);
            String column = condition.column();
            String conds = condition.value();
            Attach attach = condition.attach();


            String s = "";
        }
    }

    public void insertSql(Insert insert, ExecutableElement executableElement, org.dom4j.Element root) {

    }

    public void updateSql(Update update, ExecutableElement executableElement, org.dom4j.Element root) {

    }

    public void deleteSql(Delete delete, ExecutableElement executableElement, org.dom4j.Element root) {

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
    //     set.add("com.tg.annotation.DaoGen");
    //     return set;
    // }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
