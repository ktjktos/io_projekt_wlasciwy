










import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class SystemZarzadzania {
	private ArrayList<Kurs> kursy;
	private ArrayList<Uzytkownik> uzytkownicy;
	private Kurs obecnyKurs;
	private Uzytkownik zalogowanyUzytkownik;
	private BazaLogowan bazaLogowan;

	public SystemZarzadzania(BazaLogowan bazaLogowan) {
		this.kursy = new ArrayList<>();
		this.uzytkownicy = new ArrayList<>();
		this.bazaLogowan = bazaLogowan;

		wczytajKursyZDysku();
		wczytajUzytkownikowZBazyLogowan();
		powiazUzytkownikowZKursami();
	}

	private void wczytajKursyZDysku() {
		Path glownyKatalogKursow = Paths.get("data/courses");
		if (Files.notExists(glownyKatalogKursow)) {
			return;
		}

		try (Stream<Path> stream = Files.list(glownyKatalogKursow)) {
			stream.filter(Files::isDirectory)
					.forEach(sciezkaFolderu -> {
						try {
							Kurs wczytany = Kurs.wczytajZKatalogu(sciezkaFolderu);
							this.kursy.add(wczytany);
						} catch (IOException e) {
							System.err.println("Nie udalo sie wczytac kursu z katalogu: " + sciezkaFolderu + " -> " + e.getMessage());
						}
					});
		} catch (IOException e) {
			System.err.println("Blad podczas skanowania katalogu kursow: " + e.getMessage());
		}
	}

	private void wczytajUzytkownikowZBazyLogowan() {
		if (this.bazaLogowan != null && this.bazaLogowan.getUzytkownicy() != null) {
			this.uzytkownicy.addAll(this.bazaLogowan.getUzytkownicy());
		}
	}

	private void powiazUzytkownikowZKursami() {
		for (Kurs kurs : this.kursy) {
			for (String studentId : kurs.getUzytkownicy()) {
				Uzytkownik uzytkownik = znajdzUzytkownikaPoId(studentId);
				if (uzytkownik instanceof Student) {
					Student student = (Student) uzytkownik;
					if (!student.getKursy().contains(kurs)) {
						student.getKursy().add(kurs);
					}
				} else if (uzytkownik instanceof Wykladowca) {
					Wykladowca wykladowca = (Wykladowca) uzytkownik;
					if (!wykladowca.getKursy().contains(kurs)) {
						wykladowca.getKursy().add(kurs);
					}
				}
			}
		}
	}

	private Uzytkownik znajdzUzytkownikaPoId(String id) {
		for (Uzytkownik u : this.uzytkownicy) {
			if (u.getId().equals(id)) {
				return u;
			}
		}
		return null;
	}


	


	public boolean zarejestrujUzytkownika(String login, String haslo, int typ) {
		if (!(zalogowanyUzytkownik instanceof Admin)) {
			System.out.println("Blad: Tylko administrator moze rejestrowac uzytkownikow.");
			return false;
		}

		String idUzytkownika = UUID.randomUUID().toString();
		boolean zapisanyDoPliku = bazaLogowan.zarejestrujUzytkownika(idUzytkownika, login, haslo, typ);

		if (zapisanyDoPliku) {
			Uzytkownik nowyUzytkownik;
			switch (typ) {
				case 1:
					nowyUzytkownik = new Wykladowca(idUzytkownika, login, haslo);
					break;
				case 0:
					nowyUzytkownik = new Student(idUzytkownika, login, haslo);
					break;
				default:
					nowyUzytkownik = new Admin(idUzytkownika, login, haslo);
					break;
			}
			uzytkownicy.add(nowyUzytkownik);
			return true;
		}
		return false;
	}

	public boolean usunUzytkownika(String login) {
		if (!(zalogowanyUzytkownik instanceof Admin)) {
			System.out.println("Blad: Brak uprawnien administratora.");
			return false;
		}

		boolean usunietyZPliku = bazaLogowan.usunUzytkownika(login);

		if (usunietyZPliku) {
			uzytkownicy.removeIf(u -> u.getLogin().equals(login));
			return true;
		}
		return false;
	}


	


	public void utworzKurs(String nazwa, String haslo) {
		if (!(zalogowanyUzytkownik instanceof Wykladowca)) {
			System.out.println("Blad: Ta akcja wymaga konta wykladowcy.");
			return;
		}

		Wykladowca wykladowca = (Wykladowca) zalogowanyUzytkownik;
		Kurs nowyKurs = new Kurs(nazwa, haslo);

		nowyKurs.zapiszUzytkownika(wykladowca.getId());

		this.kursy.add(nowyKurs);
		wykladowca.getKursy().add(nowyKurs);
		System.out.println("Kurs " + nazwa + " zostal pomyslnie utworzony.");
	}

	public void zmienSylabus(String sciezkaPlikuZrodlowego) {
		if (zalogowanyUzytkownik instanceof Wykladowca && obecnyKurs != null) {
			try {
				Path zrodlo = Paths.get(sciezkaPlikuZrodlowego);

				if (Files.notExists(zrodlo) || !Files.isRegularFile(zrodlo)) {
					System.out.println("Blad: Wskazany plik zrodlowy sylabusa nie istnieje: " + sciezkaPlikuZrodlowego);
					return;
				}

				Path cel = Paths.get(obecnyKurs.getSylabus());
				Files.copy(zrodlo, cel, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Sylabus zostal zaktualizowany na podstawie pliku: " + sciezkaPlikuZrodlowego);
			} catch (IOException e) {
				System.out.println("Blad kopiowania sylabusa na dysku: " + e.getMessage());
			}
		} else {
			System.out.println("Blad: Musisz być wykladowcą i wybrac kurs.");
		}
	}

	public void dodajMaterialy(String sciezkaPlikuZrodlowego, String tytul) {
		if (zalogowanyUzytkownik instanceof Wykladowca && obecnyKurs != null) {
			try {
				obecnyKurs.dodajMaterial(sciezkaPlikuZrodlowego, tytul, LocalDate.now().toString(), true);
				System.out.println("Dodano material i skopiowano plik: " + tytul);
			} catch (IOException e) {
				System.out.println("Blad podczas dodawania materialu (kopiowania pliku): " + e.getMessage());
			}
		}
	}

	public void usunMaterialyPlik(String tytul) {
		if (zalogowanyUzytkownik instanceof Wykladowca && obecnyKurs != null) {
			obecnyKurs.usunMaterialy(tytul);
			System.out.println("Zlecono usuniecie materialu o tytule: " + tytul);
		}
	}

	public void dodajTest(String nazwaPliku, String tytul, LocalDate datakonca) {
		if (zalogowanyUzytkownik instanceof Wykladowca && obecnyKurs != null) {
			obecnyKurs.dodajTest(nazwaPliku, tytul, datakonca);
			System.out.println("Dodano nowy test na dysku: " + tytul);
		}
	}

	public void sprawdzOceny() {
		if (zalogowanyUzytkownik instanceof Wykladowca && obecnyKurs != null) {
			System.out.println("Oceny dla kursu: " + obecnyKurs.getTytul());
			for (Test test : obecnyKurs.getTesty()) {
				System.out.println("Test: " + test.getTytul() + " -> " + test.getOceny());
			}
		}
	}

	public String pobierzRozwiazanie(String tytulTestu, String loginStudenta) {
		if (obecnyKurs != null) {
			Test test = obecnyKurs.getTesty().stream()
					.filter(t -> t.getTytul().equalsIgnoreCase(tytulTestu))
					.findFirst()
					.orElse(null);

			if (test != null) {
				return test.getRozwiazania().get(loginStudenta);
			}
		}
		return null;
	}

	public void ocenRozwiazanieTestu(String tytulTestu, String loginStudenta, float ocena) {
		if (zalogowanyUzytkownik instanceof Wykladowca && obecnyKurs != null) {
			boolean sukces = obecnyKurs.ocenTest(tytulTestu, loginStudenta, ocena);
			if (sukces) {
				System.out.println("Sukces: Wystawiono ocene " + ocena + " studentowi " + loginStudenta + " za test: " + tytulTestu);
			}
		} else {
			System.out.println("Blad: Musisz byc zalogowany jako wykladowca i wybrac aktywny kurs.");
		}
	}

	
	public void zmienWidocznoscObecnegoKursu() {
		if (zalogowanyUzytkownik instanceof Wykladowca && obecnyKurs != null) {
			obecnyKurs.zmienWidocznoscKursu();
			System.out.println("Sukces: Widocznosc kursu '" + obecnyKurs.getTytul() + "' zostala zmieniona. Obecnie jest: " + (obecnyKurs.isWidoczny() ? "WIDOCZNY" : "NIEWIDOCZNY") + ".");
		} else {
			System.out.println("Blad: Brak uprawnien lub nie wybrano kursu.");
		}
	}


	


	
	
	
	public void dolaczDoKursu(String id, String wpisaneHaslo) {
		if (!(zalogowanyUzytkownik instanceof Student)) return;

		Student student = (Student) zalogowanyUzytkownik;
		id = id.trim().replace(" ", ""); 

		String finalId = id;
		Kurs wybrany = kursy.stream().filter(k -> k.getId().equals(finalId)).findFirst().orElse(null);

		if (wybrany != null) {
			if (!wybrany.isWidoczny()) {
				System.out.println("Blad: Ten kurs jest obecnie niewidoczny w systemie.");
				return;
			}

			if (student.getKursy().contains(wybrany)) {
				System.out.println("Blad: Jestes juz zapisany/a na ten kurs!");
				return;
			}

			if (!wybrany.zweryfikujHaslo(wpisaneHaslo)) {
				System.out.println("Blad: Nieprawidlowe haslo dostepu do kursu!");
				return;
			}

			student.getKursy().add(wybrany);
			wybrany.zapiszUzytkownika(student.getId());
			System.out.println("Dolaczono do kursu: " + wybrany.getTytul());
		} else {
			System.out.println("Blad: Nie znaleziono kursu o podanym ID.");
		}
	}

	public boolean wybierzObecnyKurs(String id) {
		id = id.trim().replace(" ", ""); 
		String finalId = id;

		if (zalogowanyUzytkownik instanceof Student) {
			Student student = (Student) zalogowanyUzytkownik;
			Kurs wybrany = student.getKursy().stream().filter(k -> k.getId().equals(finalId)).findFirst().orElse(null);

			if (wybrany != null) {
				this.obecnyKurs = wybrany;
				System.out.println("Aktywny kurs ustawiony na: " + obecnyKurs.getTytul());
				return true;
			} else {
				System.out.println("Nie masz dostepu do tego kursu.");
				return false;
			}
		} else if (zalogowanyUzytkownik instanceof Wykladowca) {
			Wykladowca wykladowca = (Wykladowca) zalogowanyUzytkownik;
			Kurs wybrany = wykladowca.getKursy().stream().filter(k -> k.getId().equals(finalId)).findFirst().orElse(null);

			if (wybrany != null) {
				this.obecnyKurs = wybrany;
				System.out.println("Aktywny kurs ustawiony na: " + obecnyKurs.getTytul());
				return true;
			} else {
				System.out.println("Nie prowadzisz tego kursu lub nie masz do niego dostepu.");
				return false;
			}
		} else {
			System.out.println("Blad: Brak autoryzacji.");
			return false;
		}
	}

	public void wyslijRozwiazanieDoTestu(String tytulTestu, String odp) {
		if (zalogowanyUzytkownik instanceof Student && obecnyKurs != null) {
			obecnyKurs.rozwiazTest(tytulTestu, zalogowanyUzytkownik.getLogin(), odp);
			System.out.println("Rozwiazanie testu zostalo wyslane.");
		}
	}

	public void sprawdzOceneTestu(String tytul) {
		if (zalogowanyUzytkownik instanceof Student && obecnyKurs != null) {
			Test test = obecnyKurs.getTesty().stream().filter(t -> t.getTytul().equalsIgnoreCase(tytul)).findFirst().orElse(null);
			if (test != null) {
				Float ocena = test.getOceny().get(zalogowanyUzytkownik.getLogin());
				System.out.println("Twoja ocena z " + tytul + " to: " + (ocena != null ? ocena : "Brak oceny"));
			} else {
				System.out.println("Blad: Nie znaleziono testu o podanej nazwie.");
			}
		}
	}

	public boolean wyswietlPodgladDoOceny(String tytulTestu, String loginStudenta) {
		if (obecnyKurs == null) {
			System.out.println("Blad: Wybierz najpierw kurs.");
			return false;
		}
		Test test = obecnyKurs.getTesty().stream()
				.filter(t -> t.getTytul().trim().equalsIgnoreCase(tytulTestu.trim()))
				.findFirst().orElse(null);
		if (test == null) {
			System.out.println("Blad: Nie znaleziono testu o podanym tytule.");
			return false;
		}

		String studentTrimmed = loginStudenta.trim();
		if (test.getOceny().containsKey(studentTrimmed)) {
			System.out.println("Blad: Ten student zostal juz oceniony z tego testu! (Aktualna ocena: " + test.getOceny().get(studentTrimmed) + ")");
			return false;
		}

		System.out.println("\n=== PODGLAD PYTAN TESTU: " + test.getTytul() + " ===");
		if (test.getPytania().isEmpty()) {
			System.out.println("[Brak pytan w tescie]");
		} else {
			for (String pyt : test.getPytania()) {
				System.out.println(pyt);
			}
		}

		System.out.println("\n=== ROZWIAZANIE STUDENTA: " + studentTrimmed + " ===");
		String odp = test.getRozwiazania().get(studentTrimmed);
		if (odp == null) {
			System.out.println("[Warning] Student nie przeslal jeszcze rozwiazania tego testu!");
		} else {
			System.out.println("Odpowiedz studenta: " + odp);
		}
		System.out.println("===========================================\n");
		return true;
	}

	
	public void wyswietlZawartoscTestu(String tytulTestu) {
		if (obecnyKurs != null) {
			obecnyKurs.wyswietlZawartoscTestu(tytulTestu);
		} else {
			System.out.println("Blad: Brak wybranego kursu.");
		}
	}


	


	public void sprawdzMaterialy() {
		if (obecnyKurs == null) {
			System.out.println("Wybierz najpierw kurs!");
			return;
		}
		System.out.println("Materialy dla kursu " + obecnyKurs.getTytul() + ":");
		for (Material m : obecnyKurs.getMaterialy()) {
			if (m.getWidocznosc() || zalogowanyUzytkownik instanceof Wykladowca) {
				System.out.println("- " + m.getTytul() + " (Plik: " + m.getPlik() + ")");
			}
		}
	}

	public void sprawdzTesty() {
		if (obecnyKurs == null) return;
		System.out.println("Testy w kursie:");
		for (Test t : obecnyKurs.getTesty()) {
			System.out.println("- " + t.getTytul() + " (Termin do: " + t.getDataKonca() + ")");
		}
	}

	public void sprawdzSylabus() {
		if (this.obecnyKurs != null) {
			try {
				Path sciezkaSylabus = Paths.get(this.obecnyKurs.getSylabus());
				if (Files.exists(sciezkaSylabus)) {
					List<String> linie = Files.readAllLines(sciezkaSylabus, StandardCharsets.UTF_8);
					System.out.println("\n--- SYLABUS KURSU: " + this.obecnyKurs.getTytul().toUpperCase() + " ---");
					if (linie.isEmpty() || (linie.size() == 1 && linie.get(0).trim().isEmpty())) {
						System.out.println("[Syllabus tego kursu jest pusty]");
					} else {
						for (String linia : linie) {
							System.out.println(linia);
						}
					}
					System.out.println("----------------------------------------\n");
				} else {
					System.out.println("Blad: Brak pliku sylabusa na dysku.");
				}
			} catch (IOException e) {
				System.out.println("Blad podczas odczytu pliku sylabusa: " + e.getMessage());
			}
		}
	}

	public void sprawdzSyllabus() {
		this.sprawdzSylabus();
	}

	public ArrayList<Kurs> getKursy(){
		return kursy;
	}

	public Uzytkownik getZalogowanyUzytkownik() {
		return zalogowanyUzytkownik;
	}

	public void setZalogowanyUzytkownik(Uzytkownik uzytkownik) {
		if (uzytkownik == null) {
			this.zalogowanyUzytkownik = null;
			return;
		}
		Uzytkownik powiazanyUzytkownik = znajdzUzytkownikaPoId(uzytkownik.getId());
		if (powiazanyUzytkownik != null) {
			this.zalogowanyUzytkownik = powiazanyUzytkownik;
		} else {
			this.zalogowanyUzytkownik = uzytkownik;
		}
	}
}