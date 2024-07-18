package org.example;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;


import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Main  {
    public static void main(String[] args) {
        String sql = "WITH employee_ranking AS (\n" +
                "  SELECT\n" +
                "    employee_id,\n" +
                "    last_name,\n" +
                "    first_name,\n" +
                "    salary,\n" +
                "    NTILE(2) OVER (ORDER BY salary ) as ntile\n" +
                "  FROM employee\n" +
                ")\n" +
                "SELECT\n" +
                "  employee_id,\n" +
                "  last_name,\n" +
                "  first_name,\n" +
                "  salary\n" +
                "FROM employee_ranking\n" +
                "WHERE ntile = 1\n" +
                "ORDER BY salary";

        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            StatementVistors visitor = new StatementVistors();
            statement.accept(visitor);
            Map<String, List<String>> v = visitor.getColumnTable();
            for (Map.Entry<String, List<String>> entry : v.entrySet()) {
                boolean cteTable =false;
                String table = entry.getKey();
                List<String> column = entry.getValue();
                for (String cte : visitor.withlists()) {
                    if(Objects.equals(cte, table)) {
                        cteTable = true;
                        break;
                    }
                }
                if(!cteTable){
                    System.out.println("Table: " + table + " Column:" + column);
                }
            }
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }

}