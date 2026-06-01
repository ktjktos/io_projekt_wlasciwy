import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BazaLogowan {
	private String plik;
	private List<String> bazaDanych;

	public BazaLogowan(String plik) {
		try {
			if (!plik.toLowerCase().endsWith(".csv")) {
				throw new IllegalArgumentException("Plik bazy danych musi mieć rozszerzenie .csv");
			}

			Path path = Paths.get(plik);

			if (Files.exists(path) && Files.isDirectory(path)) {
				throw new IllegalArgumentException("Podana ścieżka jest katalogiem: " + plik + ".");
			}

			this.plik = plik;
			this.bazaDanych = new ArrayList<>();

			if (!Files.exists(path)) {
				System.err.println("Ostrzezenie: Plik bazy danych '" + plik + "' nie istnieje. Tworzenie nowego pliku z domyslnymi uzytkownikami...");
				try {
					Path parent = path.getParent();

					if (parent != null && !Files.exists(parent)) {
						Files.createDirectories(parent);
					}

					Files.createFile(path);

					String adminHash = hashujHaslo("admin");
					String wykladowcaHash = hashujHaslo("wykladowca");
					String studentHash = hashujHaslo("student");

					
					this.bazaDanych.add(UUID.randomUUID().toString() + ";admin;" + adminHash + ";2");
					this.bazaDanych.add(UUID.randomUUID().toString() + ";wykladowca;" + wykladowcaHash + ";1");
					this.bazaDanych.add(UUID.randomUUID().toString() + ";student;" + studentHash + ";0");

					zapiszDoPliku();
					System.out.println("Utworzono domyślny plik bazy danych CSV z użytkownikami: admin, wykladowca, student.");
				} catch (IOException | NoSuchAlgorithmException e) {
					System.err.println("Blad podczas generowania domyślnej bazy danych: " + e.getMessage());
				}
			} else {
				odczytajZPliku();
			}

		} catch (IllegalArgumentException e) {
			System.err.println("Blad: " + e.getMessage());
			this.plik = "Blad krytyczny - program nie ma pliku bazy danych, nie pozwoli na zalogowanie";
			this.bazaDanych = new ArrayList<>();
		}
	}

	private void odczytajZPliku() {
		try {
			Path path = Paths.get(this.plik);
			if (Files.exists(path)) {
				this.bazaDanych = Files.readAllLines(path, StandardCharsets.UTF_8);
			} else {
				this.bazaDanych = new ArrayList<>();
			}
		} catch (IOException e) {
			System.err.println("Blad podczas odczytu z pliku CSV: " + e.getMessage());
			this.bazaDanych = new ArrayList<>();
		}
	}

	private void zapiszDoPliku() {
		try {
			Path path = Paths.get(this.plik);
			Files.write(path, this.bazaDanych, StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.println("Blad podczas zapisu do pliku CSV: " + e.getMessage());
		}
	}

	private String hashujHaslo(String haslo) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] encodedhash = digest.digest(haslo.getBytes(StandardCharsets.UTF_8));
		StringBuilder hexString = new StringBuilder();
		for (byte b : encodedhash) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1) hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}

	
	public Optional<Uzytkownik> sprawdzWPliku(String login, String haslo) {
		try {
			String hexString = hashujHaslo(haslo);
			for (String linia : this.bazaDanych) {
				String[] czesci = linia.split(";");
				if (czesci.length == 4) {
					String id = czesci[0];
					String l = czesci[1];
					String h = czesci[2];
					int poziomUprawnien = Integer.parseInt(czesci[3]);

					if (l.equals(login) && h.equals(hexString)) {
						Uzytkownik uzytkownik = stworzUzytkownika(id, login, hexString, poziomUprawnien);
						return Optional.of(uzytkownik);
					}
				}
			}
		} catch (NoSuchAlgorithmException | NumberFormatException e) {
			System.err.println("Blad: " + e.getMessage());
		}
		return Optional.empty();
	}

	
	private Uzytkownik stworzUzytkownika(String id, String login, String haslo, int poziomUprawnien) {
		return switch (poziomUprawnien) {
			case 2 -> new Admin(id, login, haslo);
			case 1 -> new Wykladowca(id, login, haslo);
			case 0 -> new Student(id, login, haslo);
			default -> throw new IllegalArgumentException("Nieznany poziom uprawnien: " + poziomUprawnien);
		};
	}

	
	public boolean zarejestrujUzytkownika(String id, String login, String haslo, int poziomUprawnien) {
		try {
			if (!Files.exists(Paths.get(this.plik))) {
				throw new Exception("brak pliku");
			}

			
			for (String linia : this.bazaDanych) {
				String[] czesci = linia.split(";");
				if (czesci.length >= 2 && czesci[1].equals(login)) {
					return false;
				}
			}

			String hexString = hashujHaslo(haslo);
			String nowaLinia = id + ";" + login + ";" + hexString + ";" + poziomUprawnien;

			this.bazaDanych.add(nowaLinia);
			zapiszDoPliku();
			return true;

		} catch (NoSuchAlgorithmException e) {
			System.err.println("Blad hashowania: " + e.getMessage());
			return false;
		} catch (Exception e) {
			System.err.println("Blad: " + e.getMessage());
		}
		return false;
	}

	public boolean usunUzytkownika(String login) {
		List<String> noweLinie = new ArrayList<>();
		boolean usunieto = false;

		for (String linia : this.bazaDanych) {
			String[] czesci = linia.split(";");
			if (czesci.length >= 2 && czesci[1].equals(login)) {
				usunieto = true;
			} else {
				noweLinie.add(linia);
			}
		}

		if (usunieto) {
			this.bazaDanych = noweLinie;
			zapiszDoPliku();
			return true;
		}
		return false;
	}

	public boolean zmienHaslo(String login, String haslo, String noweHaslo) {
		try {
			if (!Files.exists(Paths.get(this.plik))) {
				return false;
			}

			String stareHex = hashujHaslo(haslo);
			String noweHex = hashujHaslo(noweHaslo);

			List<String> noweLinie = new ArrayList<>();
			boolean zmieniono = false;

			for (String linia : this.bazaDanych) {
				String[] czesci = linia.split(";");
				
				if (czesci.length == 4 && czesci[1].equals(login) && czesci[2].equals(stareHex)) {
					noweLinie.add(czesci[0] + ";" + login + ";" + noweHex + ";" + czesci[3]);
					zmieniono = true;
				} else {
					noweLinie.add(linia);
				}
			}

			if (zmieniono) {
				this.bazaDanych = noweLinie;
				zapiszDoPliku();
				return true;
			}

		} catch (NoSuchAlgorithmException e) {
			System.err.println("Blad hashowania: " + e.getMessage());
		}
		return false;
	}

	
	public List<Uzytkownik> getUzytkownicy() {
		List<Uzytkownik> lista = new ArrayList<>();
		for (String linia : this.bazaDanych) {
			String[] czesci = linia.split(";");
			if (czesci.length == 4) {
				try {
					String id = czesci[0];
					String login = czesci[1];
					String hash = czesci[2];
					int poziom = Integer.parseInt(czesci[3]);
					lista.add(stworzUzytkownika(id, login, hash, poziom));
				} catch (IllegalArgumentException e) {
					System.err.println("Blad podczas odtwarzania uzytkownika: " + e.getMessage());
				}
			}
		}
		return lista;
	}
}