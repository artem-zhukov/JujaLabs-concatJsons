/**
 * Created by Artem on 17.06.2017.
 */
public class User {
    private String from;
    private String to;
    private String sendDate;
    private int points;
    private String desc;
    private String type;

    public User(String from, String to, String sendDate, int points, String desc, String type) {
        this.from = from;
        this.to = to;
        this.sendDate = sendDate;
        this.points = points;
        this.desc = desc;
        this.type = type;
    }
}
