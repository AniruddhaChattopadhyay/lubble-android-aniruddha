package in.lubble.app.chat.stories;

import java.util.ArrayList;
import java.util.HashMap;

public class StoryData {
    String storyName;
    String storyPic;
    private ArrayList<HashMap<String, Object>> story = new ArrayList<>();

    public StoryData() {

    }

    public String getStoryName() {
        return storyName;
    }

    public ArrayList<HashMap<String, Object>> getStory() {
        return story;
    }

    public void setStory(ArrayList<HashMap<String, Object>> story) {
        this.story = story;
    }

    public void setStoryName(String storyName) {
        this.storyName = storyName;
    }

    public String getStoryPic() {
        return storyPic;
    }

    public void setStoryPic(String storyPic) {
        this.storyPic = storyPic;
    }

}
