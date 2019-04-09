package in.lubble.app.utils;

import java.util.ArrayList;
import java.util.Random;

public class ChatHelper {

    public static String getRandomGroupGreeting() {
        final ArrayList<String> stringList = new ArrayList<>();
        stringList.add("Hey y'all!");
        stringList.add("Hey all!");
        stringList.add("Hey people!");
        stringList.add("Hey peeps!");
        stringList.add("Hey!");
        stringList.add("Hi!");
        stringList.add("Hey folks!");

        return stringList.get(new Random().nextInt(stringList.size()));
    }

}
