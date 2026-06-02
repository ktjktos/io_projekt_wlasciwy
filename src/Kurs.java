import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Kurs {
	private String plik; 
	private String id; 
	private String tytul;
	private String haslo; 
	private ArrayList<String> uzytkownicyID; 
	private ArrayList<Material> materialy;
	private ArrayList<Test> testy;
	private String sylabus; 
	private boolean widocznosc;

	
	public Kurs(String tytul, String haslo) {
		this.id = UUID.randomUUID().toString();
		this.tytul = tytul;
		this.haslo = hashujSHA256(haslo); 
		this.widocznosc = false; 
		this.plik = "data/courses/" + this.id;
		this.sylabus = Paths.get(this.plik, "sylabus.txt").toString();

		this.uzytkownicyID = new ArrayList<>();
		this.materialy = new ArrayList<>();
		this.testy = new ArrayList<>();

		inicjalizujKatalogi();
	}

	
	private Kurs(String id, String plik, String tytul, String haslo, boolean widocznosc) {
		this.id = id;
		this.plik = plik;
		this.tytul = tytul;
		this.haslo = haslo;
		this.widocznosc = widocznosc;
		this.sylabus = Paths.get(plik, "sylabus.txt").toString();

		this.uzytkownicyID = new ArrayList<>();
		this.materialy = new ArrayList<>();
		this.testy = new ArrayList<>();
	}

	
	public static Kurs wczytajZKatalogu(Path sciezkaKatalogu) throws IOException {
		String sciezkaString = sciezkaKatalogu.toString();
		String wyciagnieteId = sciezkaKatalogu.getFileName().toString();

		List<String> linieInfo = Files.readAllLines(sciezkaKatalogu.resolve("info.txt"));
		String tytul = linieInfo.get(0);
		String haslo = linieInfo.get(1);
		boolean widocznosc = Boolean.parseBoolean(linieInfo.get(2));

		Kurs wczytanyKurs = new Kurs(wyciagnieteId, sciezkaString, tytul, haslo, widocznosc);

		List<String> linieCsv = Files.readAllLines(sciezkaKatalogu.resolve("uzytkownicy.csv"));
		for (String linia : linieCsv) {
			if (!linia.trim().isEmpty()) {
				wczytanyKurs.uzytkownicyID.add(linia.trim());
			}
		}

		Path plikCsvMaterialy = sciezkaKatalogu.resolve("materialy.csv");
		if (Files.exists(plikCsvMaterialy)) {
			linieCsv = Files.readAllLines(plikCsvMaterialy);
			for (String linia : linieCsv) {
				if (!linia.trim().isEmpty()) {
					String[] tokeny = linia.split(";");

					String nazwaPliku = tokeny[0];
					String matTytul = tokeny[1];
					String data = tokeny[2];
					boolean matWidocznosc = Boolean.parseBoolean(tokeny[3]);

					String pelnaSciezka = sciezkaKatalogu.resolve("materialy").resolve(nazwaPliku).toString();
					wczytanyKurs.materialy.add(new Material(pelnaSciezka, matTytul, data, matWidocznosc));
				}
			}
		}

		Path katalogTesty = sciezkaKatalogu.resolve("testy");
		if (Files.exists(katalogTesty)) {
			try (var stream = Files.list(katalogTesty)) {
				stream.forEach(path -> {
					if (Files.isRegularFile(path)) {
						wczytanyKurs.testy.add(new Test(path.toString()));
					}
				});
			}
		}

		return wczytanyKurs;
	}

	private void inicjalizujKatalogi() {
		try {
			Files.createDirectories(Paths.get(this.plik));
			Files.createDirectories(Paths.get(this.plik, "testy"));
			Files.createDirectories(Paths.get(this.plik, "materialy"));

			Path sciezkaSylabus = Paths.get(this.plik, "sylabus.txt");
			if (Files.notExists(sciezkaSylabus)) {
				Files.createFile(sciezkaSylabus);
			}

			Path sciezkaMaterialy = Paths.get(this.plik, "materialy.csv");
			if (Files.notExists(sciezkaMaterialy)) {
				Files.createFile(sciezkaMaterialy);
			}

			Path sciezkaUzytkownicy = Paths.get(this.plik, "uzytkownicy.csv");
			if (Files.notExists(sciezkaUzytkownicy)) {
				Files.createFile(sciezkaUzytkownicy);
			}

			Path sciezkaInfo = Paths.get(this.plik, "info.txt");
			if (Files.notExists(sciezkaInfo)) {
				zapiszInfoTxt();
			}

		} catch (IOException e) {
			throw new RuntimeException("Blad krytyczny: Nie mozna utworzyc folderu kursu " + this.plik, e);
		}
	}

	private String hashujSHA256(String jawneHaslo) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(jawneHaslo.getBytes(StandardCharsets.UTF_8));

			StringBuilder hexString = new StringBuilder();
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Blad krytyczny SHA-256: ", e);
		}
	}

	public boolean zweryfikujHaslo(String wpisaneHaslo) {
		return this.haslo.equals(hashujSHA256(wpisaneHaslo.trim()));
	}

	public void zmienHaslo(String noweHaslo) {
		this.haslo = hashujSHA256(noweHaslo);
		zapiszInfoTxt();
	}

	public void dodajMaterial(String sciezkaZrodlowa, String tytul, String data, boolean widocznosc) throws IOException {
		Path zrodlo = Paths.get(sciezkaZrodlowa);
		String nazwaPliku = zrodlo.getFileName().toString();

		Path cel = Paths.get(this.plik, "materialy", nazwaPliku);
		Files.copy(zrodlo, cel, StandardCopyOption.REPLACE_EXISTING);

		Material nowyMaterial = new Material(cel.toString(), tytul, data, widocznosc);
		this.materialy.add(nowyMaterial);

		zapiszMaterialyCsv();
	}

	public void usunMaterialy(String tytul) {
		for (Material m : this.materialy) {
			if (m.getTytul().equals(tytul)) {
				try {
					Path sciezkaFizyczna = Paths.get(m.getPlik());
					Files.deleteIfExists(sciezkaFizyczna);
				} catch (IOException e) {
					System.err.println("Blad Oasis podczas fizycznego usuwania pliku materialu: " + m.getPlik() + " -> " + e.getMessage());
				}
			}
		}

		boolean usunieto = materialy.removeIf(material -> material.getTytul().equals(tytul));
		if (usunieto) {
			zapiszMaterialyCsv();
		}
	}

	public void wyswietlMaterialy() {
		for (Material material: this.materialy) {
			String path = material.getPlik();
			System.out.println("Plik: " + path);
			try {
				List<String> linie = Files.readAllLines(Path.of(path));
				for (String linia : linie) {
					System.out.println(linia);
				}
			} catch (IOException e) {
				System.out.println("Blad w odczytywaniu pliku: " + path);
			}
			System.out.println();
		}
	}

	public void zapiszUzytkownika(String uzytkownikID) {
		if (!this.uzytkownicyID.contains(uzytkownikID)) {
			this.uzytkownicyID.add(uzytkownikID);
			zapiszUzytkownikowCsv();
		}
	}

	public void wypiszUzytkownika(String uzytkownikID) {
		if (this.uzytkownicyID.remove(uzytkownikID)) {
			zapiszUzytkownikowCsv();
		}
	}

	
	public void dodajTest(String sciezkaPliku, String tytul, LocalDate datakonca) {
		try {
			Path zrodlo = Paths.get(sciezkaPliku);
			if (Files.notExists(zrodlo) || !Files.isRegularFile(zrodlo)) {
				System.out.println("Blad: Wskazany plik zrodlowy testu nie istnieje: " + sciezkaPliku);
				return;
			}

			
			List<String> pytania = Files.readAllLines(zrodlo, StandardCharsets.UTF_8);

			String nazwaPliku = zrodlo.getFileName().toString();
			String sciezkaTestu = Paths.get(this.plik, "testy", nazwaPliku).toString();

			
			Test nowyTest = new Test(sciezkaTestu, tytul, datakonca, pytania);
			this.testy.add(nowyTest);
			nowyTest.zapiszNaDysk();
		} catch (IOException e) {
			System.out.println("Blad podczas dodawania testu (odczyt/zapis): " + e.getMessage());
		}
	}

	
	public boolean ocenTest(String nazwaTestu, String login, Float ocena) {
		for (Test test : testy) {
			if (test.getTytul().trim().equalsIgnoreCase(nazwaTestu.trim())) {
				if (test.getOceny().containsKey(login.trim())) {
					System.out.println("Blad: Ten student zostal juz oceniony z tego testu!");
					return false;
				}
				test.ocen(login.trim(), ocena);
				test.zapiszNaDysk();
				return true;
			}
		}
		System.out.println("Blad: Nie znaleziono testu o podanym tytule.");
		return false;
	}

	
	public void rozwiazTest(String nazwaTestu, String login, String rozwiazanie) {
		for (Test test : testy) {
			if (test.getTytul().trim().equalsIgnoreCase(nazwaTestu.trim())) {
				if (test.getRozwiazania().containsKey(login.trim())) {
					System.out.println("Blad: Juz rozwiazales ten test!");
					return;
				}
				test.dodajRozwiazanie(login.trim(), rozwiazanie.trim());
				test.zapiszNaDysk();
				return;
			}
		}
		System.out.println("Blad: Nie znaleziono testu o podanym tytule.");
	}

	
	public void wyswietlZawartoscTestu(String tytul) {
		for (Test t : testy) {
			if (t.getTytul().trim().equalsIgnoreCase(tytul.trim())) {
				System.out.println("\n--- TRESC TESTU: " + t.getTytul() + " ---");
				if (t.getPytania().isEmpty()) {
					System.out.println("[Ten test nie zawiera zadnych pytan]");
				} else {
					for (String linia : t.getPytania()) {
						System.out.println(linia);
					}
				}
				System.out.println("--------------------------------");
				return;
			}
		}
		System.out.println("Blad: Nie znaleziono testu o podanym tytule.");
	}

	public void wyswietlTesty() {
		for (Test test: this.testy) {
			System.out.println("Test: " + test.getTytul() + " (Plik: " + test.getPlik() + ")");
			System.out.println("Termin oddania: " + test.getDataKonca());
			System.out.println("Widocznosc: " + (test.isWidocznosc() ? "Tak" : "Nie"));
			System.out.println("Oceny studentow:");
			test.getOceny().forEach((login, ocena) -> System.out.println("  - " + login + ": " + ocena));
			System.out.println("Rozwiazania studentow:");
			test.getRozwiazania().forEach((login, odp) -> System.out.println("  - " + login + ": " + odp));
			System.out.println();
		}
	}

	public void zmienWidocznoscKursu() {
		this.widocznosc = !widocznosc;
		zapiszInfoTxt();
	}

	public boolean isWidoczny() {
		return this.widocznosc;
	}

	public void setSylabus(String plik) {
		this.sylabus = plik;
	}

	public List<Material> getMaterialy() {
		return materialy;
	}

	public List<Test> getTesty() {
		return testy;
	}

	public String getTytul() {
		return tytul;
	}

	public String getId() {
		return id;
	}

	public List<String> getUzytkownicy() {
		return uzytkownicyID;
	}

	public String getSylabus() {
		return sylabus;
	}

	private void zapiszInfoTxt() {
		try {
			ArrayList<String> linieInfo = new ArrayList<>();
			linieInfo.add(this.tytul);
			linieInfo.add(this.haslo);
			linieInfo.add(String.valueOf(this.widocznosc));
			Files.write(Paths.get(this.plik, "info.txt"), linieInfo);
		} catch (IOException e) {
			throw new RuntimeException("Blad zapisu info.txt dla kursu: " + this.id, e);
		}
	}

	private void zapiszMaterialyCsv() {
		try {
			ArrayList<String> linieCsv = new ArrayList<>();
			for (Material m : this.materialy) {
				linieCsv.add(m.toCsv());
			}
			Files.write(Paths.get(this.plik, "materialy.csv"), linieCsv);
		} catch (IOException e) {
			throw new RuntimeException("Blad zapisu materialy.csv dla kursu: " + this.id, e);
		}
	}

	private void zapiszUzytkownikowCsv() {
		try {
			Files.write(Paths.get(this.plik, "uzytkownicy.csv"), this.uzytkownicyID);
		} catch (IOException e) {
			throw new RuntimeException("Blad zapisu uzytkownicy.csv dla kursu: " + this.id, e);
		}
	}
}