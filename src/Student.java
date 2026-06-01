











import java.util.ArrayList;
import java.util.List;

public class Student extends Uzytkownik {
	private ArrayList<Kurs> kursy;

	public Student(String id,String login, String haslo) {
		super(id,login, haslo);
		this.kursy = new ArrayList<>();
	}

	public List<Kurs> getKursy() {
		return kursy;
	}
}
