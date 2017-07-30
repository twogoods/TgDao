package com.tg.compile;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.tg.annotation.*;
import com.tg.constant.Attach;
import com.tg.constant.Criterions;
import com.tg.util.StringUtils;
import com.tg.util.XmlUtils;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.element.Element;
import javax.lang.model.type.MirroredTypeException;
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

    private Map<String, TableMapping> nameModelMapping = new HashMap<>();

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
        return true;
    }

    public void handleTableElement(Element element) {
        //TODO TableMapping 里的属性 空值检查
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
        tableMapping.setColumnToField(columnToField);
        tableMapping.setFieldToColumn(fieldToColumn);
        nameModelMapping.put(tableMapping.getClassName(), tableMapping);
        System.out.println(nameModelMapping);
    }

    public String parseColumnAnnotation(Element element) {
        Column column = element.getAnnotation(Column.class);
        return column == null ? null : column.value();
    }

    public void handleDaoGenElement(Element element) {
        //TODO 编译的warning提示
        if (!(element.getKind() == ElementKind.INTERFACE)) return;
        String modelClass = getAnnotatedClassForDaoGen(element);

        Symbol.ClassSymbol classSymbol = (Symbol.ClassSymbol) element;
        String daoName = classSymbol.getQualifiedName().toString();
        List<? extends ExecutableElement> executableElements = ElementFilter.methodsIn(element.getEnclosedElements());

        try {
            org.dom4j.Element rootElement = XmlUtils.generateMybatisXmlFrame(daoName);
            for (ExecutableElement executableElement : executableElements) {
                handleExecutableElement(executableElement, rootElement, modelClass);
            }
            OutputFormat format = OutputFormat.createPrettyPrint();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            XMLWriter writer = new XMLWriter(outputStream, format);
            writer.write(rootElement);
            writer.flush();
            generateXmlFile(classSymbol.getSimpleName().toString(), daoName.substring(0, daoName.lastIndexOf(".")), outputStream.toString("UTF-8"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private String getAnnotatedClassForDaoGen(Element element) {
        DaoGen daoGen = element.getAnnotation(DaoGen.class);
        TypeMirror typeMirror = null;
        try {
            daoGen.model();
        } catch (MirroredTypeException mirroredTypeException) {
            //see https://area-51.blog/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/
            typeMirror = mirroredTypeException.getTypeMirror();
        }
        //TODO null 编译报错
        return typeMirror.toString();
    }

    public void handleExecutableElement(ExecutableElement executableElement, org.dom4j.Element root, String modelClass) {
//        List<? extends AnnotationMirror> annotationInMethod = executableElement.getAnnotationMirrors();
        Select select = executableElement.getAnnotation(Select.class);
        if (select != null) {
            selectSql(select, executableElement, root, modelClass);
            return;
        }
        Insert insert = executableElement.getAnnotation(Insert.class);
        if (insert != null) {
            insertSql(insert, executableElement, root, modelClass);
            return;
        }
        Update update = executableElement.getAnnotation(Update.class);
        if (update != null) {
            updateSql(update, executableElement, root, modelClass);
            return;
        }
        Delete delete = executableElement.getAnnotation(Delete.class);
        if (delete != null) {
            deleteSql(delete, executableElement, root, modelClass);
        }
    }

    public void selectSql(Select select, ExecutableElement executableElement, org.dom4j.Element root, String modelClass) {
        StringBuilder selectSql = new StringBuilder();
        String columns = select.columns();
        TableMapping tableInfo = nameModelMapping.get(modelClass);
        if (StringUtils.isEmpty(columns)) {
            selectSql.append("select * from ");
        } else {
            selectSql.append("select ");
            selectSql.append(columns);
            selectSql.append(" from ");

        }
        selectSql.append(tableInfo.getTableName());

        List<? extends VariableElement> variableElements = executableElement.getParameters();
        if (variableElements.size() > 0) {
            selectSql.append(" where ");
        }
        for (int i = 0; i < variableElements.size(); i++) {
            //List<? extends AnnotationMirror> list = variableElement.getAnnotationMirrors();
            VariableElement variableElement = variableElements.get(i);
            String varName = variableElement.getSimpleName().toString();
            Condition condition = variableElement.getAnnotation(Condition.class);
            if (condition == null) {
                selectSql.append(varName).append(" = ").append("#{").append(i).append("} ");
                continue;
            }
            String column = condition.column();
            Criterions criterion = condition.value();
            Attach attach = condition.attach();

            selectSql.append(attach.name())
                    .append(StringUtils.BLANK)
                    .append(StringUtils.isEmpty(column) ? varName : column)
                    .append(StringUtils.BLANK);
            if (criterion == Criterions.IN || criterion == Criterions.NOT_IN) {
                selectSql.append(criterion.getCriterion());
                //TODO

            } else {
                selectSql.append(criterion.getCriterion()).append(" #{").append(i).append("} ");
            }
        }
        org.dom4j.Element selectElement = root.addElement("select");
        selectElement.addAttribute("id", executableElement.getSimpleName().toString());
        selectElement.addAttribute("resultType", tableInfo.getClassName());
        selectElement.setText(selectSql.toString());
        String s = "";
    }

    private void generateWhereParams() {

    }

    public void insertSql(Insert insert, ExecutableElement executableElement, org.dom4j.Element root, String modelClass) {

    }

    public void updateSql(Update update, ExecutableElement executableElement, org.dom4j.Element root, String modelClass) {

    }

    public void deleteSql(Delete delete, ExecutableElement executableElement, org.dom4j.Element root, String modelClass) {

    }


    private void generateXmlFile(String fileName, String relativePath, String content) {
        File dir = new File(AutoWhenCompile.class.getResource("/").getPath() + relativePath.replace(".", "/"));
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(dir, fileName + ".xml");
        try {
            FileWriter fw = new FileWriter(file);
            fw.append(content);
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
