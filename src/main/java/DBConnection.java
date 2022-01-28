import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static Connection connection;
    
    private static final String URL = "jdbc:mysql://localhost:3306/search_engine";
    private static final String DB_USER = "search_engine";
    private static final String DB_PASSWORD = "9en2w0oc";
    
    public static void init() {
        try {
            connection = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
            connection.createStatement().execute("DROP TABLE IF EXISTS page");
            connection.createStatement().execute("CREATE TABLE page(" +
                    "id INT NOT NULL AUTO_INCREMENT, " +
                    "path TEXT NOT NULL, " +
                    "code INT NOT NULL, " +
                    "content MEDIUMTEXT CHARACTER SET utf8mb4 NOT NULL, " +
                    "PRIMARY KEY (id) ," +
                    "UNIQUE KEY (path(254))" +
                    ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static Connection getConnection() {
        return connection;
    }
    
    public static void insertPage(Page page) {
        String content = page.getContent() == null ? "" : page.getContent().toString()
                .replaceAll("\\s*\\n\\s*", "")  // удаление переносов строки
                .replaceAll("'", "\\\\'");      // экранирование символа
        String sql = "INSERT INTO page(path, code, content) " +
                "VALUES ('" + page.getPath() + "', " + page.getCode() + ", '" + content + "')";
        try {
            connection.createStatement().execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
