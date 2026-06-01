











import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class Wykladowca extends Uzytkownik {
	private ArrayList<Kurs> kursy;

	public Wykladowca(String id,String login, String haslo) {
		super(id,login, haslo);
		this.kursy = new ArrayList<>();
	}

	public List<Kurs> getKursy() {
		return kursy;
	}
}
