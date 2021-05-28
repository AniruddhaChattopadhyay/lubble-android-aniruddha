package in.lubble.app.groups;

public interface ChatSearchListener {
    void toggleSliderVisibility(boolean isShown);
    void reInitGroupListCopy();
    void filterGroups(String query);
}
