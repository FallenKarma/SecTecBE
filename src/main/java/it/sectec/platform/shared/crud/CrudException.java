package it.sectec.platform.shared.crud;
public class CrudException extends RuntimeException {
    private final int status;
    public CrudException(int status, String code) { super(code); this.status = status; }
    public int status() { return status; }
}
