import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import static org.junit.jupiter.api.Assertions.*;

public class KursUnitTest {
    private Kurs kurs;
    @Test
    public void testZweryfikujHaslo_Sukces() {
        kurs = new Kurs("Programowanie Obiektowe", "jakieshaslo123");
        boolean wynik = kurs.zweryfikujHaslo("  jakieshaslo123  ");
        assertTrue(wynik, "Metoda powinna zatwierdzic poprawne haslo po odcieciu spacji.");
    }
    @Test
    public void testZweryfikujHaslo_Blad() {
        kurs = new Kurs("Programowanie Obiektowe", "jakieshaslo123");
        boolean wynik = kurs.zweryfikujHaslo("aaaaaa");
        assertFalse(wynik, "Metoda nie powinna wpuscic uzytkownika z blednym haslem.");
    }
    @AfterEach
    public void sprzatajPoTescie() {
        if (kurs != null) {
            Path folderKursu = Paths.get("data", "courses", kurs.getId());
            try {
                if (Files.exists(folderKursu)) {
                    Files.walk(folderKursu)
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(java.io.File::delete);

                    System.out.println("Usunieto katalog testowy: " + folderKursu);
                }
            } catch (IOException e) {
                System.err.println("Nie udalo sie oczyszczyc dysku po tescie jednostkowym: " + e.getMessage());
            }
        }
    }
}