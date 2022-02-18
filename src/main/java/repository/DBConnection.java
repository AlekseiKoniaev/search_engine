package repository;

import model.Index;
import model.Lemma;
import model.Page;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class DBConnection {

    private static Connection connection;
    
    private static final String URL = "jdbc:mysql://localhost:3306/search_engine";
    private static final String DB_USER = "search_engine";
    private static final String DB_PASSWORD = "9en2w0oc";
    
    public static void init() {
        connect();
        try {
            List<String> sqlList = Files.readAllLines(Path.of("src/main/resources/schema-generation.sql"));
            for (String sql : sqlList) {
                connection.createStatement().execute(sql);
                System.out.println(sql);      // УДАЛИТЬ // logger
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void connect() {
        try {
            connection = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
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
    
    
    public static Page getPage(String path) {
        String sql = "SELECT _id, _code, _content FROM _page WHERE _path='" + path + "'";
        Page result = new Page(path);
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()){
            resultSet.next();
            result.setId(resultSet.getInt("_id"));
            result.setCode(resultSet.getInt("_code"));
            result.setDocument(Jsoup.parse(resultSet.getString("_content")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public static Page getPage(int pageId) {
        String sql = "SELECT * FROM _page WHERE _id=" + pageId;
        Page page = new Page();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()){
            resultSet.next();
            page.setId(resultSet.getInt("_id"));
            page.setPath(resultSet.getString("_path"));
            page.setCode(resultSet.getInt("_code"));
            page.setDocument(Jsoup.parse(resultSet.getString("_content")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return page;
    }
    
    public static Lemma getLemma(String lemma) {
        String sql = "SELECT _id, _frequency FROM _lemma WHERE _lemma='" + lemma + "'";
        Lemma result = new Lemma();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()){
            resultSet.next();
            result.setId(resultSet.getInt("_id"));
            result.setLemma(lemma);
            result.setFrequency(resultSet.getInt("_frequency"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public static Set<Lemma> getLemmas(Set<String> lemmas) {
        if (lemmas == null || lemmas.isEmpty()) {
            return new HashSet<>();
        }
        StringBuffer sql = new StringBuffer("SELECT * FROM _lemma WHERE");
        lemmas.forEach(lemma -> sql.append(" _lemma='").append(lemma).append("' OR"));
        sql.delete(sql.length() - 3, sql.length());
        
        Set<Lemma> lemmaSet = new TreeSet<>();
        try (PreparedStatement statement = connection.prepareStatement(sql.toString());
             ResultSet resultSet = statement.executeQuery()){
            
            while(resultSet.next()) {
                Lemma lemma = new Lemma();
                lemma.setId(resultSet.getInt("_id"));
                lemma.setLemma(resultSet.getString("_lemma"));
                lemma.setFrequency(resultSet.getInt("_frequency"));
                lemmaSet.add(lemma);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lemmaSet;
    }
    
//    public static List<Integer> findPages(Set<Lemma> lemmas) {
//        StringBuffer sql = new StringBuffer();
//        lemmas.stream()
//                .mapToInt(Lemma::getId)
//                .forEach(lemmaId -> {
//                    if (sql.isEmpty()) {
//                        sql.append("SELECT _page_id FROM _index WHERE _lemma_id = ").append(lemmaId);
//                    } else {
//                        sql.insert(0, "SELECT _page_id FROM (")
//                                .append(")_index WHERE _lemma_id = ")
//                                .append(lemmaId);
//                    }
//                });
//
//        List<Integer> pages = new ArrayList<>();
//        try (PreparedStatement statement = connection.prepareStatement(sql.toString());
//             ResultSet resultSet = statement.executeQuery()){
//
//            while(resultSet.next()) {
//                pages.add(resultSet.getInt("_id"));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return pages;
//
//    }
    
    public static Set<Index> findIndexes(int id, String field) {
        String sql = "SELECT * FROM _index WHERE " + field + " = " + id;
        return executeFindIndex(sql);
    }
    
    private static Set<Index> executeFindIndex(String sql) {
        Set<Index> indexes = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
        
            while(resultSet.next()) {
                int pageId = resultSet.getInt("_page_id");
                int lemmaId = resultSet.getInt("_lemma_id");
                float rank = resultSet.getFloat("_rank");
                Index index = new Index(pageId, lemmaId, rank);
                indexes.add(index);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return indexes;
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
