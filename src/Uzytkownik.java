public class Uzytkownik {
	private String login;
	private String haslo;
	private String id;

	public Uzytkownik(String id,String login, String haslo) {
		this.id = id;
		this.login = login;
		this.haslo = haslo;
	}

    public String getLogin() {
        return login;
    }

	public String getId() { return id; }
}
