import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class TestIntegracyjny {

    @TempDir
    Path tempDir;

    @org.junit.jupiter.api.Test
    void testZapisIOdczytZPliku() {
        Path plikTestu = tempDir.resolve("test.txt");

        List<String> pytania = List.of("Pytanie 1", "Pytanie 2");
        LocalDate dataKonca = LocalDate.now().plusDays(7);

        Test testDoZapisu = new Test(plikTestu.toString(), "Test 1", dataKonca, pytania);

        testDoZapisu.dodajRozwiazanie("s123456", "Odp1|Odp2");
        testDoZapisu.ocen("s123456", 5.0f);
        testDoZapisu.zmienWidocznosc();

        testDoZapisu.zapiszNaDysk();

        Test testOdczytany = new Test(plikTestu.toString());

        assertEquals("Test 1", testOdczytany.getTytul());
        assertEquals(dataKonca, testOdczytany.getDataKonca());
        assertTrue(testOdczytany.isWidocznosc());

        assertEquals(2, testOdczytany.getPytania().size());
        assertEquals("Pytanie 1", testOdczytany.getPytania().get(0));

        assertEquals("Odp1|Odp2", testOdczytany.getRozwiazania().get("s123456"));
        assertEquals(5.0f, testOdczytany.getOceny().get("s123456"));
    }
}