package com.tg.generator.sql.primary;

import com.tg.generator.model.TableMapping;
import com.tg.generator.sql.AbstractSqlGen;
import com.tg.generator.sql.SqlGen;
import com.tg.generator.sql.suffix.SuffixSqlGen;
import com.tg.generator.sql.where.ModelWhereSqlGen;
import com.tg.generator.sql.where.WhereSqlGen;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by twogoods on 2017/7/31.
 */
public abstract class PrimarySqlGen extends AbstractSqlGen implements SqlGen {
    protected WhereSqlGen whereSqlGen;
    protected SuffixSqlGen suffixSqlGen;

    public PrimarySqlGen(ExecutableElement executableElement, TableMapping tableInfo) {
        super(executableElement, tableInfo);
    }

    public PrimarySqlGen(ExecutableElement executableElement, TableMapping tableInfo, WhereSqlGen whereSqlGen, SuffixSqlGen suffixSqlGen) {
        super(executableElement, tableInfo);
        this.whereSqlGen = whereSqlGen;
        this.suffixSqlGen = suffixSqlGen;
    }

    @Override
    public void generateSql(Element root) {
        checkAnnotatedRule();
        Element sqlElement = generateBaseSql(root);
        generateParamType(sqlElement);
        generateWhereSql(sqlElement);
        generateSuffixSql(sqlElement);
    }

    protected abstract void checkAnnotatedRule();

    protected abstract Element generateBaseSql(Element root);

    private void generateParamType(Element sqlElement) {
        if (whereSqlGen instanceof ModelWhereSqlGen) {
            sqlElement.addAttribute("parameterType", executableElement.getParameters().get(0).asType().toString());
        }
    }

    private void generateWhereSql(Element sqlElement) {
        if (whereSqlGen != null) {
            whereSqlGen.generateWhereSql(sqlElement);
        }
    }

    private void generateSuffixSql(Element sqlElement) {
        if (suffixSqlGen != null) {
            suffixSqlGen.generateSuffixSql(sqlElement);
        }
    }
}
