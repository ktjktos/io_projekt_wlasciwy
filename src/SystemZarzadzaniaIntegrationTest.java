import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import static org.junit.jupiter.api.Assertions.*;

public class SystemZarzadzaniaIntegrationTest {
    private final String NAZWA_BAZY_TESTOWEJ = "testowaBaza.csv";
    private Kurs utworzonyKurs;
    @Test
    public void testWybierzObecnyKurs_WykladowcaMaDostep() {
        BazaLogowan pustaBaza = new BazaLogowan(NAZWA_BAZY_TESTOWEJ);
        SystemZarzadzania system = new SystemZarzadzania(pustaBaza);
        Wykladowca wykladowca = new Wykladowca("id-wykladowcy-1", "dr_jurek", "secure");
        utworzonyKurs = new Kurs("test", "test123");
        system.setZalogowanyUzytkownik(wykladowca);
        wykladowca.getKursy().add(utworzonyKurs);
        boolean czyWejdzie = system.wybierzObecnyKurs(utworzonyKurs.getId());
        assertTrue(czyWejdzie, "Wykladowca powinien pomyslnie wejsc do swojego kursu.");
    }

    @AfterEach
    public void sprzataniePoTescie() {
        System.out.println("--- ROZPOCZYNANIE SPRZATANIA PO TESCIE ---");
        try {
            Files.deleteIfExists(Paths.get(NAZWA_BAZY_TESTOWEJ));
            System.out.println("Usunieto plik bazy: " + NAZWA_BAZY_TESTOWEJ);
        } catch (IOException e) {
            System.err.println("Nie udalo sie usunac pliku bazy testowej: " + e.getMessage());
        }
        if (utworzonyKurs != null) {
            Path folderKursu = Paths.get("data", "courses", utworzonyKurs.getId());
            try {
                if (Files.exists(folderKursu)) {
                    Files.walk(folderKursu)
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(java.io.File::delete);

                    System.out.println("usunieto folder testowy kursu: " + folderKursu);
                }
            } catch (IOException e) {
                System.err.println("Nie udalo sie oczyscic katalogu kursu: " + e.getMessage());
            }
        }
    }
}