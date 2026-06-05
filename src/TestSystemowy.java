import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class TestSystemowy {

    @TempDir
    Path tempDir;

    private SystemZarzadzania system;
    private BazaLogowan baza;

    private Kurs testowyKurs;

    @BeforeEach
    void setUp() {
        Path plikBazy = tempDir.resolve("bazaTestowa.csv");
        baza = new BazaLogowan(plikBazy.toString());
        system = new SystemZarzadzania(baza);
    }

    @AfterEach
    void sprzataniePoTescie() throws IOException {
        if (testowyKurs != null) {
            Path folderKursu = Paths.get("data/courses/" + testowyKurs.getId());
            if (Files.exists(folderKursu)) {
                Files.walk(folderKursu)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        }
    }

    @org.junit.jupiter.api.Test
    void testPelnegoCykluZyciaTestu() throws Exception {
        Admin admin = new Admin("a111111", "admin", "haslo123");
        system.setZalogowanyUzytkownik(admin);
        system.zarejestrujUzytkownika("w111111", "haslo123", 1);
        system.zarejestrujUzytkownika("s123456", "haslo123", 0);

        Uzytkownik wykladowca = baza.sprawdzWPliku("w111111", "haslo123").get();
        system.setZalogowanyUzytkownik(wykladowca);
        system.utworzKurs("Programowanie", "haslo123");

        testowyKurs = ((Wykladowca) system.getZalogowanyUzytkownik()).getKursy().get(0);
        Kurs kurs = testowyKurs;

        system.wybierzObecnyKurs(kurs.getId());
        system.zmienWidocznoscObecnegoKursu();

        Path plikZrodlowyTestu = tempDir.resolve("pytania.txt");
        Files.write(plikZrodlowyTestu, List.of("Pytanie 1", "Pytanie 2"));
        system.dodajTest(plikZrodlowyTestu.toString(), "Test 1", LocalDate.now().plusDays(7));

        Uzytkownik student = baza.sprawdzWPliku("s123456", "haslo123").get();
        system.setZalogowanyUzytkownik(student);
        system.dolaczDoKursu(kurs.getId(), "haslo123");
        system.wybierzObecnyKurs(kurs.getId());
        system.wyslijRozwiazanieDoTestu("Test 1", "Odp1|Odp2");

        system.setZalogowanyUzytkownik(wykladowca);
        system.wybierzObecnyKurs(kurs.getId());
        system.ocenRozwiazanieTestu("Test 1", "s123456", 5.0f);

        Test test = kurs.getTesty().get(0);
        assertEquals("Odp1|Odp2", test.getRozwiazania().get("s123456"));
        assertEquals(5.0f, test.getOceny().get("s123456"));
    }
}