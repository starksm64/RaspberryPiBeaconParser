import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestUptime {
    public static void main(String[] args) throws Exception {
        Process uptimeProc = Runtime.getRuntime().exec("uptime");
        BufferedReader in = new BufferedReader(new InputStreamReader(uptimeProc.getInputStream()));
        String line = in.readLine();
        long uptime;
        String loadAvgs;
        if (line != null) {
            // 18:25  up 3 days,  9:01, 12 users, load averages: 1.78 2.10 2.23
            // 01:25:06 up 1 day,  7:03,  2 users,  load average: 0.09, 0.16, 0.14
            System.out.printf("Parsing: %s\n", line);
            Pattern parse = Pattern.compile("((\\d+) day[s]?,)?\\s+(\\d+):(\\d+),.*(load average[s]?:.*)");
            Matcher matcher = parse.matcher(line);
            if (matcher.find()) {
                String _days = matcher.group(2);
                String _hours = matcher.group(3);
                String _minutes = matcher.group(4);
                String avgs = matcher.group(5);
                int days = _days != null ? Integer.parseInt(_days) : 0;
                int hours = _hours != null ? Integer.parseInt(_hours) : 0;
                int minutes = _minutes != null ? Integer.parseInt(_minutes) : 0;
                uptime = (minutes * 60000) + (hours * 60000 * 60) + (days * 6000 * 60 * 24);
                System.out.printf("uptime=%d, avgs=%s\n", uptime, avgs);
            }
        }

    }
}
