import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class TestJednostkowy {

    @org.junit.jupiter.api.Test
    void testLogikiWewnetrznej() {
        List<String> pytania = List.of("Pytanie 1");
        Test testObj = new Test("dummy.txt", "Test 1", LocalDate.now(), pytania);

        testObj.zmienWidocznosc();
        assertTrue(testObj.isWidocznosc());

        testObj.dodajRozwiazanie("s123456", "Odp1");
        assertEquals("Odp1", testObj.getRozwiazania().get("s123456"));

        testObj.ocen("s123456", 5.0f);
        assertEquals(5.0f, testObj.getOceny().get("s123456"));
    }
}