import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BazaLogowan {
	private String plik;

	public BazaLogowan(String plik) {
		try {
			Path path = Paths.get(plik);

			if (!Files.exists(path) || Files.isDirectory(path)) {
				throw new FileNotFoundException("Plik nie istnieje lub jest katalogiem: " + plik);
			}

			this.plik = plik;

		} catch (FileNotFoundException e) {
			System.err.println("Błąd: " + e.getMessage());
			this.plik = "-BRAK SCIEZKI- -BLAD KRYTYCZNY-";
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
			String oczekiwanyPoczatek = login + ";" + hexString + ";";

			List<String> linie = Files.readAllLines(Paths.get(this.plik));
			for (String linia : linie) {
				if (linia.startsWith(oczekiwanyPoczatek)) {
					String[] czesci = linia.split(";");
					if (czesci.length == 3) {
						int poziomUprawnien = Integer.parseInt(czesci[2]);
						Uzytkownik uzytkownik = stworzUzytkownika(login, poziomUprawnien);
						return Optional.of(uzytkownik);
					}
				}
			}
		} catch (IOException | NoSuchAlgorithmException | NumberFormatException e) {
			System.err.println("Blad: " + e.getMessage());
		}
		return Optional.empty();
	}

	private Uzytkownik stworzUzytkownika(String login, int poziomUprawnien) {
		return switch (poziomUprawnien) {
			case 2 -> new Admin(login);
			case 1 -> new Wykladowca(login);
			case 0 -> new Student(login);
			default -> throw new IllegalArgumentException("Nieznany poziom uprawnień: " + poziomUprawnien);
		};
	}

	public boolean zarejestrujUzytkownika(String login, String haslo, int poziomUprawnien) {
		try {
			if (!Files.exists(Paths.get(this.plik))) {
				throw new Exception("brak pliku");
			}

			List<String> linie = Files.readAllLines(Paths.get(this.plik));
			for (String linia : linie) {
				if (linia.startsWith(login + ";")) {
					return false;
				}
			}

			String hexString = hashujHaslo(haslo);
			String nowaLinia = login + ";" + hexString + ";" + poziomUprawnien + System.lineSeparator();
			Files.write(Paths.get(this.plik), nowaLinia.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
			return true;

		} catch (IOException | NoSuchAlgorithmException e) {
			System.err.println("Blad: " + e.getMessage());
			return false;
		} catch (Exception e) {
			System.err.println("Blad: " + e.getMessage());
		}
		return false;
	}

	public boolean usunUzytkownika(String login) {
		try {
			List<String> linie = Files.readAllLines(Paths.get(this.plik));
			List<String> noweLinie = new ArrayList<>();
			boolean usunieto = false;

			for (String linia : linie) {
				if (linia.startsWith(login + ";")) {
					usunieto = true;
				} else {
					noweLinie.add(linia);
				}
			}

			if (usunieto) {
				Files.write(Paths.get(this.plik), noweLinie);
				return true;
			}

		} catch (IOException e) {
			System.err.println("Blad: " + e.getMessage());
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

			List<String> linie = Files.readAllLines(Paths.get(this.plik));
			List<String> noweLinie = new ArrayList<>();
			boolean zmieniono = false;

			for (String linia : linie) {
				String[] czesci = linia.split(";");
				if (czesci.length == 3 && czesci[0].equals(login) && czesci[1].equals(stareHex)) {
					noweLinie.add(login + ";" + noweHex + ";" + czesci[2]);
					zmieniono = true;
				} else {
					noweLinie.add(linia);
				}
			}

			if (zmieniono) {
				Files.write(Paths.get(this.plik), noweLinie);
				return true;
			}

		} catch (IOException | NoSuchAlgorithmException e) {
			System.err.println("Blad: " + e.getMessage());
		}
		return false;
	}
}
