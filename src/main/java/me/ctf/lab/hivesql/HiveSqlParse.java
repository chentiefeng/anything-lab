package me.ctf.lab.hivesql;

import com.google.common.base.Joiner;
import lombok.Data;
import org.apache.hadoop.hive.ql.lib.Node;
import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.ParseDriver;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.hadoop.hive.ql.parse.HiveParser.*;

/**
 * @author: chentiefeng[chentiefeng@linzikg.com]
 * @create: 2019-12-11 16:10
 */
public class HiveSqlParse {
    private ParseDriver pd = new ParseDriver();
    /**
     * 原始表(表名,别名)
     */
    private List<String[]> sourceTable = new ArrayList<>();
    /**
     * 插入表
     */
    private List<String> insertTables = new ArrayList<>();
    /**
     * 最外层列
     */
    private List<String> outermostColumns = new ArrayList<>();
    /**
     * 插入分区信息(分区列,分区值)
     */
    private Map<String, String> partitionMap = new HashMap<>();
    /**
     * 最外层Sel节点
     */
    private ASTNode outermostSelNode = null;
    /**
     * 最外层Insert节点
     */
    private ASTNode outermostInsertNode = null;
    /**
     * 放置 解析表栈
     */
    private Stack<HiveTableParseInfo> tableParseInfoSelStack = new Stack<>();
    private Stack<HiveTableParseInfo> tableParseInfoFromStack = new Stack<>();
    /**
     * 表关系解析信息，不包含原始表
     */
    private HiveTableParseInfo tableParseInfo = null;

    public HiveSqlParse() {
    }

    public HiveSqlParse(String sql) {
        parse(sql);
    }

    /**
     * sql解析
     *
     * @param sql
     */
    public void parse(String sql) {
        try {
            ASTNode ast = pd.parse(sql);
            parseNode(ast);
            insert(outermostInsertNode);
            outermostColumns(outermostSelNode);
            if (insertTables.size() > 0) {
                sourceTable.removeIf(arr -> arr[0].equals(insertTables.get(0)));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void parseNode(ASTNode ast) {
        if (Objects.nonNull(ast.getChildren()) && ast.getChildren().size() > 0) {
            for (Node child : ast.getChildren()) {
                ASTNode cc = (ASTNode) child;
                switch (cc.getToken().getType()) {
                    case TOK_INSERT:
                        outermostInsertNode = cc;
                        break;
                    case TOK_TABNAME:
                        String tableName = Joiner.on(".").join(cc.getChildren().stream().map(n -> ((ASTNode) n).getText()).collect(Collectors.toList()));
                        ASTNode ccChild = (ASTNode) cc.getParent().getChild(cc.getParent().getChildCount() - 1);
                        HiveTableParseInfo sourceTableParseInfo = new HiveTableParseInfo();
                        if (ccChild.getToken().getType() == TOK_TABNAME) {
                            sourceTable.add(new String[]{tableName, ""});
                            sourceTableParseInfo.setAlias("");
                        } else {
                            sourceTable.add(new String[]{tableName, ccChild.getText()});
                            sourceTableParseInfo.setAlias(ccChild.getText());
                        }
                        sourceTableParseInfo.setName(tableName);
                        if (!tableParseInfoFromStack.empty()) {
                            tableParseInfoFromStack.pop().getTables().add(sourceTableParseInfo);
                        }
                        break;
                    case TOK_QUERY:
                        ASTNode ccc = (ASTNode) cc.getParent().getChild(cc.getParent().getChildCount() - 1);
                        if (ccc.getToken().getType() != TOK_QUERY) {
                            HiveTableParseInfo table = new HiveTableParseInfo();
                            table.setAlias(ccc.getText());
                            tableParseInfoSelStack.push(table);
                            tableParseInfoFromStack.push(table);
                        }
                        break;
                    case TOK_SELECT:
                    case TOK_SELECTDI:
                        HiveTableParseInfo pop = tableParseInfoSelStack.pop();
                        if (!tableParseInfoSelStack.empty()) {
                            HiveTableParseInfo father = tableParseInfoSelStack.peek();
                            if (Objects.nonNull(father)) {
                                father.getTables().add(pop);
                            }
                        } else {
                            tableParseInfo = pop;
                        }
                        parseColumns(cc, pop);
                        continue;
                    default:
                }
                parseNode(cc);
            }
        }
    }

    private void insert(ASTNode cn) {
        if (CollectionUtils.isEmpty(cn.getChildren())) {
            return;
        }
        for (Node child : cn.getChildren()) {
            ASTNode cc = (ASTNode) child;
            switch (cc.getToken().getType()) {
                case TOK_INSERT_INTO:
                case TOK_DESTINATION:
                    insertTable(cn);
                    continue;
                case TOK_SELECT:
                    outermostSelNode = cn;
                    continue;
                default:
            }
            insert(cc);
        }
    }

    private void parseColumns(ASTNode cc, HiveTableParseInfo table) {
        for (Node node : cc.getChildren()) {
            ASTNode tokSelExpr = (ASTNode) node;
            HiveTableParseInfo.HiveTableColumnParseInfo column = new HiveTableParseInfo.HiveTableColumnParseInfo();
            String alias = getSelExprAlias(tokSelExpr);
            column.setName(alias);
            parseColumn(tokSelExpr, column);
            table.getColumns().add(column);
        }
    }


    private void parseColumn(ASTNode tokSelExpr, HiveTableParseInfo.HiveTableColumnParseInfo column) {
        if (CollectionUtils.isEmpty(tokSelExpr.getChildren())) {
            return;
        }
        for (Node child : tokSelExpr.getChildren()) {
            ASTNode cc = (ASTNode) child;
            if (cc.getToken().getType() == TOK_TABLE_OR_COL) {
                ASTNode ccc = (ASTNode) cc.getParent().getChild(cc.getParent().getChildCount() - 1);
                String[] item;
                if (ccc.getToken().getType() == TOK_TABLE_OR_COL) {
                    item = new String[]{cc.getChild(0).getText(), ""};
                } else {
                    item = new String[]{ccc.getText(), cc.getChild(0).getText()};
                }
                Optional<String[]> any = column.getSourceList().stream().filter(s -> Arrays.equals(item, s)).findAny();
                if (!any.isPresent()) {
                    column.getSourceList().add(item);
                }
                continue;
            }
            parseColumn(cc, column);
        }
    }

    /**
     * 插入信息
     *
     * @param cn
     */
    private void insertTable(ASTNode cn) {
        if (CollectionUtils.isEmpty(cn.getChildren())) {
            return;
        }
        for (Node child : cn.getChildren()) {
            ASTNode cc = (ASTNode) child;
            switch (cc.getToken().getType()) {
                case TOK_TABNAME:
                    String tableName = Joiner.on(".").join(cc.getChildren().stream().map(n -> ((ASTNode) n).getText()).collect(Collectors.toList()));
                    insertTables.add(tableName);
                    break;
                case TOK_PARTVAL:
                    if (cc.getChildCount() == 2) {
                        partitionMap.put(cc.getChild(0).getText(), cc.getChild(1).getText());
                    } else {
                        partitionMap.put(cc.getChild(0).getText(), null);
                    }
                    break;
                default:
            }
            insertTable(cc);
        }
    }

    /**
     * 最外层列
     *
     * @param cn
     */
    private void outermostColumns(ASTNode cn) {
        if (CollectionUtils.isEmpty(cn.getChildren())) {
            return;
        }
        for (Node cnChild : cn.getChildren()) {
            ASTNode cc = (ASTNode) cnChild;
            if (cc.getToken().getType() == TOK_SELEXPR) {
                String alias = getSelExprAlias(cc);
                outermostColumns.add(alias);
                continue;
            }
            outermostColumns(cc);
        }
    }

    /**
     * 列别名获取
     *
     * @param cc
     * @return
     */
    private String getSelExprAlias(ASTNode cc) {
        ASTNode child = (ASTNode) cc.getChild(cc.getChildCount() - 1);
        if (child.getToken().getType() == TOK_TABLE_OR_COL || child.getToken().getType() == DOT) {
            return child.getChild(child.getChildCount() - 1).getText();
        } else {
            return child.getText();
        }
    }

    public List<String> getOutermostColumns() {
        return outermostColumns;
    }

    public List<String> getSourceTables() {
        return sourceTable.stream().map(t -> t[0]).distinct().collect(Collectors.toList());
    }

    public String getInsertTable() {
        return Objects.nonNull(insertTables) && insertTables.size() > 0 ? insertTables.get(0) : null;
    }

    public Map<String, String> getPartition() {
        return partitionMap;
    }

    public HiveTableParseInfo getTableParseInfo() {
        return tableParseInfo;
    }

    /**
     * 表解析类
     */
    @Data
    public static class HiveTableParseInfo {
        /** 表别名 */
        private String alias;
        /** 表名称 */
        private String name;
        /** 列 */
        private Set<HiveTableColumnParseInfo> columns = new HashSet<>();
        /** 来源表 */
        private Set<HiveTableParseInfo> tables = new HashSet<>();

        /**
         * 列
         */
        @Data
        public static class HiveTableColumnParseInfo {
            /** 列名 */
            private String name;
            /** 来源信息 */
            private List<String[]> sourceList = new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        String sql23 = "SELECT \n" +
                "  temp.dd,\n" +
                "  COUNT(temp.url) pv, \n" +
                "  COUNT(DISTINCT temp.guid) uv  \n" +
                "FROM(\n" +
                "  SELECT\n" +
                "    SUBSTRING(trackTime, 0, 10) dd,\n" +
                "    url,\n" +
                "    guid\n" +
                "FROM\n" +
                "  db_web_data.track_log\n" +
                "WHERE\n" +
                "  length(url) > 0) temp\n" +
                "GROUP BY\n" +
                "  temp.dd";
        HiveSqlParse hiveSqlParse = new HiveSqlParse(sql23);
        System.out.println("==================");
        for (String table : hiveSqlParse.getSourceTables()) {
            System.out.println("原始表:"+table);
        }
        System.out.println("==================");
        for (String outermostColumn : hiveSqlParse.getOutermostColumns()) {
            System.out.println("最外层列:"+outermostColumn);
        }
        System.out.println("==================");
        System.out.println("插入表："+hiveSqlParse.getInsertTable());
        System.out.println("==================");
        System.out.println("分区信息："+hiveSqlParse.getPartition());
        System.out.println("==================");


        HiveTableParseInfo tableParseInfo = hiveSqlParse.getTableParseInfo();
        print(tableParseInfo);
    }

    private static void print(HiveTableParseInfo info) {
        System.out.println("======================");
        System.out.println("表："+info.alias+"."+info.name);
        if(Objects.nonNull(info.columns) && info.columns.size()>0){
            for (HiveTableParseInfo.HiveTableColumnParseInfo column : info.columns) {
                System.out.println("列:"+column.name);
                if(Objects.nonNull(column.sourceList) && column.sourceList.size()>0){
                    for (String[] strings : column.sourceList) {
                        System.out.println("来源信息:"+ Arrays.toString(strings));
                    }
                }
            }
        }
        if(Objects.nonNull(info.tables) && info.tables.size()>0){
            for (HiveTableParseInfo table : info.tables) {
                print(table);
            }
        }
    }
}
