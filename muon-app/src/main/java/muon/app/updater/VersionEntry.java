package muon.app.updater;

public class VersionEntry implements Comparable<VersionEntry> {
    private String tagName;

    public VersionEntry() {
        // TODO Auto-generated constructor stub
    }

    public VersionEntry(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public int compareTo(VersionEntry o) {
        int v1 = getNumericValue();
        int v2 = o.getNumericValue();
        return v1 - v2;
    }

    public final int getNumericValue() {
        String[] arr = tagName.substring(1).split("\\.");
        int value = 0;
        int multiplier = 1;
        for (int i = arr.length - 1; i >= 0; i--) {
            value += Integer.parseInt(arr[i]) * multiplier;
            multiplier *= 10;
        }
        return value;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public String toString() {
        return "VersionEntry [tag_name=" + tagName + " value=" + getNumericValue() + "]";
    }
}
