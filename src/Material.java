import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

public class Material {
	private String plik;
	private String tytul;
	private LocalDate data;
	private boolean widocznosc;
	public Material(String plik, String tytul) {
		this.plik = plik;
		this.tytul = tytul;
		this.data = LocalDate.now();
		this.widocznosc = false;
	}

	public Material(String plik, String tytul, String data, boolean widocznosc) {
		this.plik = plik;
		this.tytul = tytul;
		this.data = LocalDate.parse(data);
		this.widocznosc = widocznosc;
	}

	public String toCsv() {
		Path path = Paths.get(this.plik);
		String nazwaPliku = path.getFileName().toString();
		return nazwaPliku + ";" + tytul + ";" + data.toString() + ";" + widocznosc;
	}

	public String getPlik() {
		return this.plik;
	}
	
	public String getTytul() {
		return this.tytul;
	}
	
	public LocalDate getData() {
		return this.data;
	}
	
	public boolean getWidocznosc() {
		return this.widocznosc;
	}
	
	public void zmienWidocznosc() {
		this.widocznosc = !this.widocznosc;
	}
}
