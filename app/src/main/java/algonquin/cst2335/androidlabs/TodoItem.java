package algonquin.cst2335.androidlabs;

public class TodoItem {
    private long id;
    private String text;
    private boolean isUrgent;

    public TodoItem(long id, String text, boolean isUrgent) {
        this.id = id;
        this.text = text;
        this.isUrgent = isUrgent;
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public boolean isUrgent() {
        return isUrgent;
    }
}