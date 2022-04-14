package main.exceptions;

public class ServerNotFoundException extends RuntimeException {
    public ServerNotFoundException(String url) {
        super("Server not found with url = " + url);
    }
}
