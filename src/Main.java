import java.util.Optional;
import java.util.Scanner;

public void main(String[] args) {
    SystemZarzadzania system = new SystemZarzadzania();
    Admin admin = new Admin("GOD"); // raczej do zmiany xd
    BazaLogowan baza = new BazaLogowan("test123");
    admin.zarejestrujUzytkownika("olafbog","lubieplacki",0);
    admin.zarejestrujUzytkownika("olafbog2","lubieuczyc",1);

    Scanner scanner = new Scanner(System.in);
    Optional<Uzytkownik> user = Optional.empty();
    short proby=0;
    while (user.isEmpty()) {
        if (proby>=3){
            //TODO co ma być po przekroczeniu 3 próby?
        }
        System.out.println("Wpisz login oraz haslo"); // pierw patrz login czy istnieje potem haslo
        String login,haslo;
        login = scanner.nextLine().replace(" ", "");
        haslo = scanner.nextLine().replace(" ", "");

        user = baza.sprawdzWPliku(login,haslo);
        proby++;
    }

    switch (user.orElse(null)) {
        case Admin a -> {
            System.out.println("--- ZALOGOWANO JAKO ADMIN ---");
            while(user.isPresent()){
                System.out.println("1 Zarejestruj uzytkownika");
                System.out.println("2 Usun uzytkownika");
                System.out.println("3 Wyloguj");
                System.out.print("Wybierz opcje: ");

                int wybor = scanner.nextInt();
                scanner.nextLine();

                switch (wybor) {
                    case 1 -> {
                        System.out.print("Podaj login: ");
                        String log = scanner.nextLine();
                        System.out.print("Podaj haslo: ");
                        String pas = scanner.nextLine();
                        System.out.print("Podaj typ (2-Admin, 1-Wykladowca, 0-Student): ");
                        int typ = scanner.nextInt();

                        a.zarejestrujUzytkownika(log, pas, typ);
                    }
                    case 2 -> {
                        System.out.print("Podaj login uzytkownika do usuniecia: ");
                        String log = scanner.nextLine();
                        a.usunUzytkownika(log);
                    }
                    case 3 -> {
                        user = Optional.empty();
                    }
                    default -> System.out.println("Niepoprawny wybor.");
                }
            }
        }

        case Wykladowca w -> {
            System.out.println("--- ZALOGOWANO JAKO WYKLADOWCA ---");
            while(user.isPresent()){
                System.out.println("1 Utworz kurs");
                System.out.println("2 Wyswietl liste moich kursow");
                System.out.println("3 Wyloguj");

                int wybor = scanner.nextInt();
                scanner.nextLine();

                switch (wybor){
                    case 1:
                        //TODO tworzenie kursu
                        break;
                    case 2:
                        System.out.println("podaj tytul kursu aby wejsc do kursu");
                        System.out.println("~~~lista kursow~~~");//TODO
                        int wybor1 = scanner.nextInt();
                        scanner.nextLine();
                        //TODO otwórz kurs i reszta niżej
                        System.out.println("1 Zmień sylabus");
                        System.out.println("2 Dodaj materiały");
                        System.out.println("3 Usuń materiały");
                        System.out.println("4 Sprawdź materiały");
                        System.out.println("5 Dodaj test");
                        System.out.println("6 Sprawdź oceny");
                        System.out.println("7 Sprawdź sylabus");
                        break;
                    case 3:
                        user = Optional.empty();
                        break;
                }
            }
        }

        case Student s -> {
            System.out.println("--- ZALOGOWANO JAKO STUDENT ---");
            while(user.isPresent()){
                System.out.println("1 Wyswietl liste wszystkich kursow");
                System.out.println("2 Wyswietl liste dolaczonych? kursow");
                System.out.println("3 Wyloguj");

                int wybor = scanner.nextInt();
                scanner.nextLine();

                switch(wybor){
                    case 1 -> {
                        System.out.println("Podaj tytul i haslo aby dolaczyc");
                        System.out.println("~~~lista kursow~~~");//TODO
                        //TODO dołączanie
                    }
                    case 2 -> {
                        System.out.println("Podaj tytul aby otworzyc");
                        System.out.println("~~~lista kursow~~~");//TODO
                        //TODO otwieranie kursu
                        System.out.println("1 Wyślij rozwiązanie do testu");
                        System.out.println("2 Sprawdź ocenę testu");
                        System.out.println("3 Sprawdź materiały");
                    }
                    case 3 -> {
                        user = Optional.empty();
                    }
                }
            }
        }

        case null, default -> {
            System.out.println("Nieznany typ użytkownika - BŁĄD");
        }
    }
}