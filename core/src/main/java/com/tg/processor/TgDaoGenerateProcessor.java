package com.tg.processor;

import com.sun.tools.javac.code.Symbol;
import com.tg.annotation.*;
import com.tg.generator.model.TableMapping;
import com.tg.generator.sql.*;
import com.tg.util.StringUtils;

import java.util.*;
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
public class TgDaoGenerateProcessor extends AbstractProcessor {

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
        annotations.stream()
                .filter(typeElement -> typeElement.toString().equals(TABLEANNOTATIONNAME))
                .forEach(typeElement -> roundEnv.getElementsAnnotatedWith(typeElement).forEach((this::handleTableElement)));

        annotations.stream()
                .filter(typeElement -> typeElement.toString().equals(DAOGENANNOTATIONNAME))
                .forEach(typeElement -> roundEnv.getElementsAnnotatedWith(typeElement).forEach((this::handleDaoGenElement)));
        return true;
    }

    private void handleTableElement(Element element) {
        //TODO TableMapping 里的属性 空值检查
        Symbol.ClassSymbol classSymbol = (Symbol.ClassSymbol) element;
        TableMapping tableMapping = new TableMapping();
        tableMapping.setClassName(classSymbol.getQualifiedName().toString());
        Table table = classSymbol.getAnnotation(Table.class);
        String tableName = table.name();
        tableMapping.setTableName(tableName);

        Map<String, String> fieldToColumn = new HashMap<>();
        Map<String, String> columnToField = new HashMap<>();
        List<Symbol> symbols = classSymbol.getEnclosedElements();
        symbols.stream().filter(symbol -> symbol.getKind() == ElementKind.FIELD)
                .filter(symbol -> symbol.getAnnotation(Ignore.class) == null)
                .forEach(symbol -> {
                    Id id = symbol.getAnnotation(Id.class);
                    if (id != null) {
                        tableMapping.setIdColumn(id.value());
                        tableMapping.setIdField(symbol.getSimpleName().toString());
                    } else {
                        String columnName = parseColumnAnnotation(symbol);
                        String fieldName = symbol.getSimpleName().toString();
                        fieldToColumn.put(fieldName, StringUtils.isEmpty(columnName) ? fieldName : columnName);
                        columnToField.put(StringUtils.isEmpty(columnName) ? fieldName : columnName, fieldName);
                    }
                });
        tableMapping.setColumnToField(columnToField);
        tableMapping.setFieldToColumn(fieldToColumn);
        nameModelMapping.put(tableMapping.getClassName(), tableMapping);
    }

    private String parseColumnAnnotation(Element element) {
        Column column = element.getAnnotation(Column.class);
        return column == null ? null : column.value();
    }

    private void handleDaoGenElement(Element element) {
        //TODO 编译的warning提示
        if (!(element.getKind() == ElementKind.INTERFACE)) return;
        String modelClass = getAnnotatedClassForDaoGen(element);
        Symbol.ClassSymbol classSymbol = (Symbol.ClassSymbol) element;
        List<SqlGen> sqlGens = new ArrayList<>();
        ElementFilter.methodsIn(element.getEnclosedElements()).forEach(executableElement -> {
            SqlGen sqlGen = handleExecutableElement(executableElement, modelClass);
            if (sqlGen != null) {
                sqlGens.add(sqlGen);
            }
        });
        try {
            GenerateHelper.generate(classSymbol.getQualifiedName().toString(), sqlGens);
        } catch (Exception e) {
            //TODO 错误
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

    public SqlGen handleExecutableElement(ExecutableElement executableElement, String modelClass) {
        Select select = executableElement.getAnnotation(Select.class);
        if (select != null) {
            return new SelectSql(executableElement, nameModelMapping.get(modelClass), select);
        }
        Count count = executableElement.getAnnotation(Count.class);
        if (count != null) {
            return new SelectCountSql(executableElement, nameModelMapping.get(modelClass), count);
        }
        Insert insert = executableElement.getAnnotation(Insert.class);
        if (insert != null) {
            return new InsertSql(executableElement, nameModelMapping.get(modelClass), insert);
        }
        BatchInsert batchInsert = executableElement.getAnnotation(BatchInsert.class);
        if (batchInsert != null) {
            return new BatchInsertSql(executableElement, nameModelMapping.get(modelClass), batchInsert);
        }
        Update update = executableElement.getAnnotation(Update.class);
        if (update != null) {
            return null;
        }
        Delete delete = executableElement.getAnnotation(Delete.class);
        if (delete != null) {
            return null;
        }
        return null;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
