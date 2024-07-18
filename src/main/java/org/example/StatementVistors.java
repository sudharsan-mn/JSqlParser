package org.example;

import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.WithItem;

import java.util.List;
import java.util.Map;

public class StatementVistors extends StatementVisitorAdapter {
    ColumnTableFinder vistor = new ColumnTableFinder();
    public void visit(Select select) {
        List<WithItem> withItemsList = select.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            for (WithItem withItem : withItemsList) {
                withItem.accept((SelectVisitor) vistor);
            }
        }
        select.accept((SelectVisitor) vistor);
    }
    public Map<String, List<String> > getColumnTable() {
        return vistor.getColumnTableMap();
    }
    public List<String> withlists() {
        return vistor.withLists();
    }
}
