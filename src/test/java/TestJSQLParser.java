import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.List;
import java.util.Objects;
//https://blog.csdn.net/qq_35986709/article/details/109806254?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7Edefault-18-109806254-blog-116025553.pc_relevant_multi_platform_whitelistv1_mlttest1&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7Edefault-18-109806254-blog-116025553.pc_relevant_multi_platform_whitelistv1_mlttest1&utm_relevant_index=21
@Slf4j
public class TestJSQLParser {
    static String sql1 = "select t1.f1,t1.f2,t2.id,count(*) from table t1 left join table1 t2 right join (select * from table2) t3 where t1.id='12121' or (t1.id between 1 and 3 and t1.id>'22112') group by t.f1 order by t.f1 desc,tf2 asc limit 1,20";
    static String sql2 = "insert into table(f1,f2) values (1,2)";
    static String sql2_1 = "insert into table(f1,f2) (select f1,f2 from table1)";
    static String sql3 = "update table set f1=2,f2=3 where f1=1212";
    static String sql3_1 = "insert into table(f1,f2) (select f1,f2 from table1)";
    static String sql4_1 = "delete from table where 1=1";

    public static void main(String[] args) {
        testSimpleSelectSql();
        testSimpleInsertSql(sql2);
        testSimpleInsertSql(sql2_1);
        testSimpleUpdateSql(sql3);
        testSimpleDeleteSql(sql4_1);
    }

    //解析sql
    public static void testSimpleSelectSql() {
        System.out.println("=================测试查询==================");
        try {
            Select select = (Select) CCJSqlParserUtil.parse(sql1);
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tableList = tablesNamesFinder.getTableList(select);
            PlainSelect plain = (PlainSelect) select.getSelectBody();
            List<Join> joins = plain.getJoins();
            for (Join join : joins) {
                FromItem rightItem = join.getRightItem();
                if (rightItem instanceof Table) {
                    Table table = (Table) (rightItem);
                    System.out.println("连接类型:" + joinTypeStr(join) + "         表：" + table.getName() + "           别名：" + table.getAlias());
                } else if (rightItem instanceof SubSelect) {
                    SubSelect subSelect = (SubSelect) (rightItem);
                    System.out.println("连接类型:" + joinTypeStr(join) + "         子查询：" + subSelect.getSelectBody() + "           别名：" + rightItem.getAlias());
                }
            }
            List<SelectItem> selectItems = plain.getSelectItems();
            for (SelectItem selectItem : selectItems) {
                SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
                Expression expression = selectExpressionItem.getExpression();
                //判断表达式是否是函数
                if (expression instanceof Function) {
                    Function function = (Function) expression;
                    NamedExpressionList namedParameters = function.getNamedParameters();
                    if (namedParameters != null) {
                        List<Expression> expressions = namedParameters.getExpressions();
                        System.out.println(expressions);
                    }
                    System.out.println("函数：" + ((Function) expression).getName());
                    boolean allColumns = function.isAllColumns();
                    System.out.println("传入的是全部列：" + allColumns);
                    //判断表达式是否是列
                } else if (expression instanceof Column) {
                    System.out.println("查询值：" + ((Column) expression).getColumnName());
                }
            }
            System.out.println("表名:" + tableList);
            Expression where = plain.getWhere();
            if (where != null) {
                System.out.println("条件:" + where);
            }

            //排序
            List<OrderByElement> orderByElements = plain.getOrderByElements();
            if (Objects.nonNull(orderByElements)) {
                for (OrderByElement orderByElement : orderByElements) {
                    Expression expression = orderByElement.getExpression();
                    if (expression instanceof Column) {
                        Column column = (Column) (expression);
                        System.out.println("排序字段:" + column.getColumnName() + "," + (orderByElement.isAsc() ? "正序" : "倒序"));
                    }
                }
            }


            //获取分组
            GroupByElement groupBy = plain.getGroupBy();
            if (Objects.nonNull(groupBy)) {
                List<Expression> groupByExpressions = groupBy.getGroupByExpressions();
                for (Expression groupByExpression : groupByExpressions) {
                    if (groupByExpression instanceof Column) {
                        Column column = (Column) (groupByExpression);
                        System.out.println("分组字段:" + column.getColumnName());
                    }
                }
            }

            //分页
            Limit limit = plain.getLimit();
            if (Objects.nonNull(limit)) {
                System.out.println("行:" + limit.getRowCount());
                System.out.println("偏移量:" + limit.getOffset());
            }
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        System.out.println("=================测试查询==================");
    }

    public static void testSimpleInsertSql(String sql) {
        System.out.println("=================测试插入sql==================");
        System.out.println("测试sql:" + sql);
        try {
            Insert insert = (Insert) CCJSqlParserUtil.parse(sql);
            System.out.println("插入的表" + insert.getTable());
            System.out.println("插入的列" + insert.getColumns());
            if (Objects.nonNull(insert.getSelect())) {
                SelectBody selectBody = insert.getSelect().getSelectBody();
                System.out.println("来自：" + selectBody);
            } else {
                System.out.println("普通插入");
                System.out.println("插入的值" + insert.getItemsList());
            }

        } catch (JSQLParserException e) {
            e.printStackTrace();
        }

        System.out.println("=================测试插入sql==================");
    }

    public static void testSimpleUpdateSql(String sql) {
        System.out.println("=================测试更新sql==================");
        System.out.println("测试sql:" + sql);
        try {
            Update update = (Update) CCJSqlParserUtil.parse(sql);
            System.out.println("更新的表" + update.getTable());
            System.out.println("更新的列" + update.getColumns());
            System.out.println("更新的值" + update.getExpressions());
            System.out.println("条件" + update.getWhere());
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }

        System.out.println("=================测试更新sql==================");
    }

    public static void testSimpleDeleteSql(String sql) {
        System.out.println("=================测试删除sql==================");
        System.out.println("测试sql:" + sql);
        try {
            Delete delete = (Delete) CCJSqlParserUtil.parse(sql);
            System.out.println("删除的表" + delete.getTable());
            System.out.println("条件的列" + delete.getWhere());
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }

        System.out.println("=================测试删除sql==================");
    }

    public static String joinTypeStr(Join join) {
        if (join.isLeft()) {
            return "左连接";
        }
        if (join.isRight()) {
            return "左连接";
        }
        if (join.isFull()) {
            return "全连接";
        }
        if (join.isCross()) {
            return "交叉连接";
        }
        return null;
    }

}
