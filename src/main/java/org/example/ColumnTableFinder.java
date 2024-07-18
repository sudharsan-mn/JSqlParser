package org.example;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColumnTableFinder extends ExpressionVisitorAdapter implements SelectVisitor,
        FromItemVisitor,SelectItemVisitor {
    private final Map<String, List<String>> columnTableMap = new HashMap<>();
    public List<String> withList = new ArrayList<>();
    public List<String> withLists() {return withList;}
    private String currentTable ;
    private final Map<String, String> aliasMap = new HashMap<>();

    @Override
    public void visit(Column column) {
        String columnName = column.getColumnName();
        String tableName = column.getTable() != null ? aliasMap.get(column.getTable().getName() ): currentTable;
        if(columnTableMap.containsKey(tableName)){
            List<String> retrievedList = columnTableMap.get(tableName);
            if (!retrievedList.contains(columnName)) {
                retrievedList.add(columnName);
                columnTableMap.put(tableName, retrievedList);
            }
        }else{
            List<String> finalColumnNames = columnTableMap.computeIfAbsent(tableName, k -> new ArrayList<>());
            if (!finalColumnNames.contains(columnName)){
                finalColumnNames.add(columnName);
            }
        }
    }
    @Override
    public void visit(ParenthesedSelect selectBody) {
        List<WithItem> withItemsList = selectBody.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            for (WithItem withItem : withItemsList) {
                withItem.accept((SelectVisitor) this);
            }
        }
        selectBody.getSelect().accept((SelectVisitor) this);
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        List<WithItem> withItemsList = plainSelect.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            for (WithItem withItem : withItemsList) {
                withItem.accept((SelectVisitor) this);
            }
        }
        if (plainSelect.getFromItem() != null) {
            plainSelect.getFromItem().accept(this);
        }
        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                join.getFromItem().accept(this);
                join.getRightItem().accept(this);
                for (Expression expression : join.getOnExpressions()) {
                    expression.accept(this);
                }
            }
        }
        if (plainSelect.getSelectItems() != null) {
            for (SelectItem<?> item : plainSelect.getSelectItems()) {
                item.accept(this);
            }
        }
        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(this);
        }

    }
    @Override
    public void visit(Select selectBody) {
        selectBody.accept((SelectVisitor) this);
    }

    @Override
    public void visit(SetOperationList list) {
        List<WithItem> withItemsList = list.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            for (WithItem withItem : withItemsList) {
                withItem.accept((SelectVisitor) this);
            }
        }
        for (Select selectBody : list.getSelects()) {
            selectBody.accept((SelectVisitor) this);
        }
    }

    @Override
    public void visit(WithItem withItem) {
        withList.add(withItem.getAlias().getName());
        withItem.getSelect().accept((SelectVisitor) this);
    }

    @Override
    public void visit(Values aThis) {

    }

    @Override
    public void visit(Table tableName) {
        String table = tableName.getName();
        currentTable = table;
        String alias = tableName.getAlias() != null ? tableName.getAlias().getName() : table;
        if(alias == null){
            aliasMap.put(table,table);
        }else{
            aliasMap.put(alias,table);
        }
        columnTableMap.computeIfAbsent(table, k -> new ArrayList<>());
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {

        lateralSubSelect.getSelect().accept((SelectVisitor) this);
    }

    @Override
    public void visit(TableFunction tableFunction) {
        visit(tableFunction.getFunction());
    }

    @Override
    public void visit(ParenthesedFromItem aThis) {

    }

    @Override
    public void visit(TableStatement tableStatement) {
        tableStatement.getTable().accept(this);
    }
    public Map<String, List<String> > getColumnTableMap() {
        return columnTableMap;
    }
}
