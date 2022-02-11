package repository;

import model.Index;
import model.Page;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DBConnection {

    private static Connection connection;
    
    private static final String URL = "jdbc:mysql://localhost:3306/search_engine";
    private static final String DB_USER = "search_engine";
    private static final String DB_PASSWORD = "9en2w0oc";
    
    public static void init() {
        try {
            List<String> sqlList = Files.readAllLines(Path.of("src/main/resources/schema-generation.sql"));
            connection = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
            for (String sql : sqlList) {
                connection.createStatement().execute(sql);
                System.out.println(sql);      // УДАЛИТЬ // logger
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static Map<String, Float> getFields() {
        Map<String, Float> fields = new HashMap<>();
        String sql = "SELECT _name, _selector, _weight FROM _field";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                String selector = resultSet.getString("_selector");
                Float weight = resultSet.getFloat("_weight");
                fields.put(selector, weight);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fields;
    }
    
    
    public static int getPageId(String path) {
        String sql = "SELECT _id FROM _page WHERE _path='" + path + "'";
        return executeGetIntQuery(sql);
    }
    
    public static int getLemmaId(String lemma) {
        String sql = "SELECT _id FROM _lemma WHERE _lemma='" + lemma + "'";
        return executeGetIntQuery(sql);
    }
    
    private static int executeGetIntQuery(String sql) {
        int result = 0;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()){
            resultSet.next();
            result = resultSet.getInt("_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    
    public static void insertPage(Page page) {
        String content = page.getContent()
                .replaceAll("\\s*\\n\\s*", "")  // удаление переносов строки
                .replaceAll("'", "\\\\'");      // экранирование символа
        String sql = "INSERT INTO _page(_path, _code, _content) " +
                "VALUES ('" + page.getPath() + "', " + page.getCode() + ", '" + content + "')";
        try {
            connection.createStatement().execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void insertLemmas(Set<String> lemmas) {
        synchronized (connection) {
            String sql = "INSERT INTO _lemma(_lemma, _frequency) " +
                    "VALUES " + buildLemmaValues(lemmas) +
                    " ON DUPLICATE KEY UPDATE _frequency = _frequency + 1";
            executeInsertQuery(sql);
        }
    }
    
    private static StringBuffer buildLemmaValues(Set<String> lemmas) {
        StringBuffer buffer = new StringBuffer();
        lemmas.forEach(lemma -> buffer.append("('")
                .append(lemma)
                .append("', 1),"));
        return buffer.deleteCharAt(buffer.length() - 1);
    }
    
    public static void insertIndexes(Set<Index> indexes) {
        synchronized (connection) {
            String sql = "INSERT INTO _index(_page_id, _lemma_id, _rank) " +
                    "VALUES " + buildIndexValues(indexes);
            executeInsertQuery(sql);
        }
    }
    
    private static StringBuffer buildIndexValues(Set<Index> indexes) {
        StringBuffer buffer = new StringBuffer();
        for (Index index : indexes) {
            buffer.append("(")
                    .append(index.getPageId())
                    .append(",")
                    .append(index.getLemmaId())
                    .append(",")
                    .append(index.getRank())
                    .append("),");
        }
        return buffer.deleteCharAt(buffer.length() - 1);
    }
    
    private static void executeInsertQuery(String sql) {
        try {
            connection.createStatement().execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
    }
}
