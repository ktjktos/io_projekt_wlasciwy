import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {
	private String plik; 
	private String tytul;
	private LocalDate dataWyslania;
	private LocalDate dataKonca;
	private List<String> pytania; 
	private HashMap<String, Float> oceny; 
	private HashMap<String, String> rozwiazania; 
	private boolean widocznosc;

	
	public Test(String plik, String tytul, LocalDate dataKonca) {
		this(plik, tytul, dataKonca, new ArrayList<>());
	}

	
	public Test(String plik, String tytul, LocalDate dataKonca, List<String> pytania) {
		this.plik = plik;
		this.tytul = tytul;
		this.dataWyslania = LocalDate.now();
		this.dataKonca = dataKonca;
		this.widocznosc = false;
		this.pytania = new ArrayList<>(pytania);
		this.oceny = new HashMap<>();
		this.rozwiazania = new HashMap<>();
	}

	
	public Test(String plik) {
		this.plik = plik;
		this.oceny = new HashMap<>();
		this.rozwiazania = new HashMap<>();
		this.pytania = new ArrayList<>();
		wczytajNaDysk(); 
	}

	
	public void zapiszNaDysk() {
		try {
			ArrayList<String> linie = new ArrayList<>();
			
			linie.add(this.tytul);
			linie.add(this.dataWyslania.toString());
			linie.add(this.dataKonca.toString());
			linie.add(String.valueOf(this.widocznosc));

			
			linie.add("---pytania---");
			for (String pytanie : pytania) {
				linie.add(pytanie);
			}

			
			linie.add("---oceny---");
			for (Map.Entry<String, Float> entry : oceny.entrySet()) {
				linie.add(entry.getKey() + ";" + entry.getValue());
			}

			
			linie.add("---rozwiazania---");
			for (Map.Entry<String, String> entry : rozwiazania.entrySet()) {
				linie.add(entry.getKey() + ";" + entry.getValue());
			}

			Files.write(Paths.get(this.plik), linie);
		} catch (IOException e) {
			throw new RuntimeException("Blad zapisu pliku testu: " + this.plik, e);
		}
	}

	
	private void wczytajNaDysk() {
		try {
			Path path = Paths.get(this.plik);
			if (!Files.exists(path)) {
				return;
			}

			List<String> linie = Files.readAllLines(path);
			if (linie.size() < 4) return;

			
			this.tytul = linie.get(0);
			this.dataWyslania = LocalDate.parse(linie.get(1));
			this.dataKonca = LocalDate.parse(linie.get(2));
			this.widocznosc = Boolean.parseBoolean(linie.get(3));

			String tryb = "METADANE";
			for (int i = 4; i < linie.size(); i++) {
				String linia = linie.get(i).trim();
				if (linia.isEmpty()) continue;

				
				if (linia.equals("---pytania---")) {
					tryb = "PYTANIA";
					continue;
				} else if (linia.equals("---oceny---")) {
					tryb = "OCENY";
					continue;
				} else if (linia.equals("---rozwiazania---")) {
					tryb = "ROZWIAZANIA";
					continue;
				}

				if (tryb.equals("PYTANIA")) {
					this.pytania.add(linia);
				} else {
					String[] tokeny = linia.split(";");
					if (tokeny.length == 2) {
						String login = tokeny[0];
						if (tryb.equals("OCENY")) {
							this.oceny.put(login, Float.parseFloat(tokeny[1]));
						} else if (tryb.equals("ROZWIAZANIA")) {
							this.rozwiazania.put(login, tokeny[1]);
						}
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Blad odczytu pliku testu: " + this.plik, e);
		}
	}

	

	public void ocen(String login, Float ocena) {
		this.oceny.put(login, ocena);
	}

	public void dodajRozwiazanie(String login, String rozwiazanie) {
		this.rozwiazania.put(login, rozwiazanie);
	}

	public void zmienWidocznosc() {
		this.widocznosc = !this.widocznosc;
	}

	
	public String getPlik() { return plik; }
	public String getTytul() { return tytul; }
	public LocalDate getDataWyslania() { return dataWyslania; }
	public LocalDate getDataKonca() { return dataKonca; }
	public boolean isWidocznosc() { return widocznosc; }
	public List<String> getPytania() { return pytania; }
	public HashMap<String, Float> getOceny() { return oceny; }
	public HashMap<String, String> getRozwiazania() { return rozwiazania; }
}