import main.lemmatizer.Lemmatizer;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class LemmatizerTest {

    @Test
    public void testGetLemmasRussianCorrect() {
        final String text = "Повторное появление леопарда в Осетии позволяет предположить, что " +
                "леопард постоянно обитает в некоторых районах Северного Кавказа.";

        Lemmatizer lemmatizer = new Lemmatizer(text);
        Map<String, Integer> expected = new HashMap<>();
        expected.putAll(Map.of("предположить", 1, "северный", 1,
                "район", 1, "кавказ", 1, "повторный", 1,
                "появление", 1, "осетия", 1, "постоянно", 1,
                "позволять", 1, "леопард", 2));
        expected.put("обитать", 1);

        Map<String, Integer> actual = lemmatizer.getLemmas();

        assertEquals(expected.size(), actual.size());
        for (String normalForm : expected.keySet()) {
            assertTrue(actual.containsKey(normalForm));
            assertEquals(expected.get(normalForm), actual.get(normalForm));
        }
    }

    @Test
    public void testGetLemmasEnglishCorrect() {
        final String text = "Female domestic cats can have kittens from spring to late autumn, " +
                "with litter sizes often ranging from two to five kittens.";

        Lemmatizer lemmatizer = new Lemmatizer(text);
        Map<String, Integer> expected = new HashMap<>();
        expected.putAll(Map.of("often", 1, "two", 1,
                "domestic", 1, "spring", 1, "ranging", 1,
                "can", 1, "kitten", 2, "late", 1,
                "size", 1, "lit", 1));
        expected.putAll(Map.of("cat", 1, "have", 1,
                "autumn", 1, "female", 1, "five", 1));

        Map<String, Integer> actual = lemmatizer.getLemmas();

        assertEquals(expected.size(), actual.size());
        for (String normalForm : expected.keySet()) {
            assertTrue(actual.containsKey(normalForm));
            assertEquals(expected.get(normalForm), actual.get(normalForm));
        }
    }
}
