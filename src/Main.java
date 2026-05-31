import java.util.Optional;
import java.util.Scanner;
import java.time.format.DateTimeParseException;

public void main(String[] args) {
    SystemZarzadzania system = new SystemZarzadzania();
    BazaLogowan baza = new BazaLogowan("test123");

    Scanner scanner = new Scanner(System.in);
    Optional<Uzytkownik> user = Optional.empty();
    short proby=0;
    while (user.isEmpty()) {
        if (proby>=3){
            System.out.println("Wykorzystano 3 proby logowania.");
            return;
        }
        System.out.println("Wpisz login oraz haslo");
        String login,haslo;
        login = scanner.nextLine().replace(" ", "");
        haslo = scanner.nextLine().replace(" ", "");

        user = baza.sprawdzWPliku(login,haslo);
        proby++;
    }

    system.setZalogowanyUzytkownik(user.get());

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

                        system.zarejestrujUzytkownika(log, pas, typ);
                    }
                    case 2 -> {
                        System.out.print("Podaj login uzytkownika do usuniecia: ");
                        String log = scanner.nextLine();
                        system.usunUzytkownika(log);
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
                        System.out.println("Podaj nazwe kursu");
                        String nazwa = scanner.nextLine();
                        System.out.println("Podaj haslo kursu");
                        String haslo = scanner.nextLine();
                        system.utworzKurs(nazwa,haslo);
                        break;
                    case 2:
                        System.out.println("~~~lista kursow~~~");
                        for (Kurs kurs: w.getKursy()) {
                            System.out.println(kurs.getId() + " " + kurs.getTytul());
                        }
                        System.out.println("Podaj id kursu aby wejsc do kursu");
                        system.wybierzObecnyKurs(scanner.nextInt());

                        int wybor1 = scanner.nextInt();

                        trueloop:
                        while (true) {
                            System.out.println("1 Zmień sylabus");
                            System.out.println("2 Dodaj materiały");
                            System.out.println("3 Usuń materiały");
                            System.out.println("4 Sprawdź materiały");
                            System.out.println("5 Dodaj test");
                            System.out.println("6 Sprawdź oceny");
                            System.out.println("7 Sprawdź sylabus");

                            switch (wybor1) {
                                case 1:
                                    System.out.println("Wprowadz sciezke do nowego sylabusa.");
                                    system.zmienSylabus(scanner.nextLine());
                                    break;
                                case 2:
                                    System.out.println("Wprowadz sciezke do materialu ktory chcesz dodac");
                                    String matsy = scanner.nextLine();
                                    System.out.println("Wprowadz tytul jaki ma posiadac dodany material");
                                    String tytul = scanner.nextLine();
                                    system.dodajMaterialy(matsy,tytul);
                                    break;
                                case 3:
                                    System.out.println("Wprowadz sciezke do materialu ktory chcesz usunac");
                                    system.usunMaterialyPlik(scanner.nextLine());
                                    break;
                                case 4:
                                    system.sprawdzMaterialy();
                                    break;
                                case 5:
                                    System.out.print("Podaj tytul testu: ");
                                    String tytul1 = scanner.nextLine();
                                    System.out.print("Wprowadz sciezke do pliku z testem: ");
                                    String plik = scanner.nextLine();

                                    LocalDate dataKonca = null;
                                    while (dataKonca == null) {
                                        System.out.print("Podaj date zakonczenia (format: RRRR-MM-DD, np. 2026-06-15): ");
                                        String dataInput = scanner.nextLine();

                                        try {
                                            dataKonca = LocalDate.parse(dataInput);
                                        } catch (DateTimeParseException e) {
                                            System.out.println("Blad: Niepoprawny format daty! Uzyj formatu RRRR-MM-DD.");
                                        }
                                    }
                                    system.dodajTest(plik, tytul1, dataKonca);
                                    break;
                                case 6:
                                    system.sprawdzOceny();
                                    break;
                                case 7:
                                    system.sprawdzSyllabus();
                                    break;
                                default:
                                    break trueloop;
                            }
                        }
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
                System.out.println("1 Wyswietl liste dolaczonych? kursow");
                System.out.println("2 Wyswietl liste wszystkich kursow");
                System.out.println("3 Wyloguj");

                int wybor = scanner.nextInt();
                scanner.nextLine();

                switch(wybor){
                    case 1 -> {
                        System.out.println("~~~lista kursow~~~");
                        for (Kurs kurs: s.getKursy()) {
                            System.out.println(kurs.getId() + " " + kurs.getTytul());
                        }
                        System.out.println("Podaj tytul aby otworzyc");
                        String tytul = scanner.nextLine();

                        boolean znalezionoKurs = false;
                        for (Kurs kurs: s.getKursy()) {
                            znalezionoKurs = kurs.getTytul().equals(tytul);
                            if (znalezionoKurs) break;
                        }

                        if (znalezionoKurs) {

                            trueloop:
                            while(true) {
                                System.out.println("1 Wyślij rozwiązanie do testu");
                                System.out.println("2 Sprawdź ocenę testu");
                                System.out.println("3 Sprawdź materiały");
                                int wybor2 = scanner.nextInt();
                                switch (wybor2) {
                                    case 1:
                                        System.out.println("Wprowadz tytul testu");
                                        String tytul2 = scanner.nextLine(); // TODO: wyswietlic test xd
                                        System.out.println("Wprowadz odpowiedzi w formacie \"A B C...\"");
                                        String odp = scanner.nextLine();
                                        system.wyslijRozwiazanieDoTestu(tytul2,odp);
                                        break;
                                    case 2:
                                        System.out.println("Wprowadz tytul testu");
                                        String tytul3 = scanner.nextLine();
                                        system.sprawdzOceneTestu(tytul3);
                                        break;
                                    case 3:
                                        system.sprawdzMaterialy();
                                        break;
                                    default:
                                        break trueloop;
                                }
                            }

                        } else {
                            System.out.println("Nie znaleziono podanego kursu.");
                        }
                    }
                    case 2 -> {
                        System.out.println("~~~lista kursow~~~");
                        ArrayList<Kurs> temp = system.getKursy();
                        for (Kurs k : temp) {
                            if (!s.getKursy().contains(k)) {
                                System.out.println("ID: "+k.getId()+" Tytul: "+k.getTytul());
                            }
                        }
                        System.out.println("Podaj tytul i haslo aby dolaczyc");
                        int idDoDolaczenia = scanner.nextInt();
                        system.dolaczDoKursu(idDoDolaczenia);
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