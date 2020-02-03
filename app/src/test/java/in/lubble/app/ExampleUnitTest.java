package in.lubble.app;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void dm_quality_filter() throws Exception {
        String name = "pikachu";
        String filteredText = ("this is regarding the rwa membership plz accept me " + name).toLowerCase();
        filteredText = filteredText.replaceAll("hi+", "");
        filteredText = filteredText.replaceAll("hello+", "");
        filteredText = filteredText.replaceAll("yo+", "");
        filteredText = filteredText.replaceAll("hey+", "");
        filteredText = filteredText.replaceAll(name, "");
        assertFalse(filteredText.length() >= 10);

    }
}