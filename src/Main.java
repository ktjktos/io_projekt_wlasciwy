import java.util.Optional;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        BazaLogowan baza = new BazaLogowan("bazaDanych.csv");
        SystemZarzadzania system = new SystemZarzadzania(baza);
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== SYSTEM WSPOMAGAJACY NAUKE ===");

        Optional<Uzytkownik> user = Optional.empty();
        short proby = 0;

        while (user.isEmpty()) {
            if (proby >= 3) {
                System.out.println("\nBlad: Wykorzystano 3 proby logowania. Zamykanie systemu.");
                return;
            }

            System.out.println("\n--- PANEL LOGOWANIA (Proba " + (proby + 1) + "/3) ---");
            String login = wczytajNiepustyTekst(scanner, "Wpisz login: ").replace(" ", "");
            String haslo = wczytajNiepustyTekst(scanner, "Wpisz haslo: ").replace(" ", "");

            user = baza.sprawdzWPliku(login, haslo);
            if (user.isEmpty()) {
                System.out.println("Blad: Niepoprawny login lub haslo. Sprobuj ponownie.");
            }
            proby++;
        }

        system.setZalogowanyUzytkownik(user.get());
        Uzytkownik zalogowany = system.getZalogowanyUzytkownik();

        switch (zalogowany) {
            case Admin a -> {
                System.out.println("\nZalogowano pomyslnie jako Administrator.");

                while (user.isPresent()) {
                    wyswietlMenuAdmina();
                    int wybor = wczytajLiczbe(scanner, "Wybierz opcje: ");

                    switch (wybor) {
                        case 1 -> {
                            System.out.println("\n--- REJESTRACJA NOWEGO UZYTKOWNIKA ---");
                            String log = wczytajNiepustyTekst(scanner, "Podaj login: ");
                            String pas = wczytajNiepustyTekst(scanner, "Podaj haslo: ");
                            System.out.println("Typ uprawnien (2-Admin, 1-Wykladowca, 0-Student):");
                            int typ = wczytajLiczbe(scanner, "Wybierz typ (0-2): ");

                            boolean sukces = system.zarejestrujUzytkownika(log, pas, typ);
                            if (sukces) {
                                System.out.println("Sukces: Uzytkownik '" + log + "' zostal zarejestrowany.");
                            } else {
                                System.out.println("Blad: Rejestracja nie powiodla sie (login moze byc zajety).");
                            }
                        }
                        case 2 -> {
                            System.out.println("\n--- USUNIECIE UZYTKOWNIKA ---");
                            String log = wczytajNiepustyTekst(scanner, "Podaj login uzytkownika do usuniecia: ");

                            boolean sukces = system.usunUzytkownika(log);
                            if (sukces) {
                                System.out.println("Sukces: Uzytkownik '" + log + "' zostal usuniety.");
                            } else {
                                System.out.println("Blad: Nie udalo sie usunac uzytkownika.");
                            }
                        }
                        case 3 -> {
                            System.out.println("Wylogowano.");
                            user = Optional.empty();
                        }
                        default -> System.out.println("Niepoprawny wybor! Wybierz liczbe od 1 do 3.");
                    }
                }
            }

            case Wykladowca w -> {
                System.out.println("\nZalogowano pomyslnie jako Wykladowca.");

                while (user.isPresent()) {
                    wyswietlMenuWykladowcy();
                    int wybor = wczytajLiczbe(scanner, "Wybierz opcje: ");

                    switch (wybor) {
                        case 1:
                            System.out.println("\n--- TWORZENIE NOWEGO KURSU ---");
                            String nazwa = wczytajNiepustyTekst(scanner, "Podaj nazwe kursu: ");
                            String haslo = wczytajNiepustyTekst(scanner, "Podaj haslo kursu: ");
                            system.utworzKurs(nazwa, haslo);
                            break;

                        case 2:
                            if (w.getKursy().isEmpty()) {
                                System.out.println("Informacja: Nie prowadzisz zadnych kursow.");
                                break;
                            }

                            System.out.println("\n--- MOJE KURSY ---");
                            for (Kurs kurs : w.getKursy()) {
                                System.out.println("ID: " + kurs.getId() + " | Tytul: " + kurs.getTytul() + " | Widocznosc: " + (kurs.isWidoczny() ? "WIDOCZNY" : "NIEWIDOCZNY"));
                            }
                            String wybraneId = wczytajNiepustyTekst(scanner, "Podaj ID kursu, aby do niego wejsc: ").replace(" ", "");

                            boolean udaloSieWejsc = system.wybierzObecnyKurs(wybraneId);
                            if (udaloSieWejsc) {
                                trueloop:
                                while (true) {
                                    wyswietlPanelZarzadzaniaKursem();
                                    int wybor1 = wczytajLiczbe(scanner, "Wybierz funkcje: ");

                                    switch (wybor1) {
                                        case 1:
                                            String sylabusWybor = wczytajNiepustyTekst(scanner, "Podaj sciezke do pliku z nowym sylabusem: ");
                                            system.zmienSylabus(sylabusWybor);
                                            break;
                                        case 2:
                                            String matsy = wczytajNiepustyTekst(scanner, "Podaj sciezke do pliku materialu: ");
                                            String tytul = wczytajNiepustyTekst(scanner, "Podaj tytul materialu: ");
                                            system.dodajMaterialy(matsy, tytul);
                                            break;
                                        case 3:
                                            String doUsuniecia = wczytajNiepustyTekst(scanner, "Podaj tytul materialu do usuniecia: ");
                                            system.usunMaterialyPlik(doUsuniecia);
                                            break;
                                        case 4:
                                            system.sprawdzMaterialy();
                                            break;
                                        case 5:
                                            String plik = wczytajNiepustyTekst(scanner, "Podaj sciezke do pliku z testem: ");
                                            String tytul1 = wczytajNiepustyTekst(scanner, "Podaj tytul testu: ");
                                            LocalDate dataKonca = wczytajDate(scanner, "Podaj date zakonczenia (RRRR-MM-DD): ");

                                            system.dodajTest(plik, tytul1, dataKonca);
                                            break;
                                        case 6:
                                            system.sprawdzOceny();
                                            break;
                                        case 7:
                                            
                                            System.out.println("\n--- OCENIANIE ROZWIAZANIA ---");
                                            String testOceniany = wczytajNiepustyTekst(scanner, "Podaj tytul testu: ");
                                            String studentLogin = wczytajNiepustyTekst(scanner, "Podaj login studenta: ");

                                            boolean podgladOk = system.wyswietlPodgladDoOceny(testOceniany, studentLogin);
                                            if (podgladOk) {
                                                float ocenaFloat = wczytajOcene(scanner, "Wystaw ocene (np. 4.5): ");
                                                system.ocenRozwiazanieTestu(testOceniany, studentLogin, ocenaFloat);
                                            }
                                            break;
                                        case 8:
                                            system.zmienWidocznoscObecnegoKursu();
                                            break;
                                        case 9:
                                            system.sprawdzSylabus();
                                            break;
                                        default:
                                            System.out.println("Powrot do menu glownego.");
                                            break trueloop;
                                    }
                                }
                            }
                            break;

                        case 3:
                            System.out.println("Wylogowano.");
                            user = Optional.empty();
                            break;

                        default:
                            System.out.println("Niepoprawny wybor!");
                    }
                }
            }

            case Student s -> {
                System.out.println("\nZalogowano pomyslnie jako Student.");

                while (user.isPresent()) {
                    wyswietlMenuStudenta();
                    int wybor = wczytajLiczbe(scanner, "Wybierz opcje: ");

                    switch (wybor) {
                        case 1 -> {
                            if (s.getKursy().isEmpty()) {
                                System.out.println("Informacja: Nie nalezysz do zadnego kursu. Dolacz do kursu (opcja 2).");
                                break;
                            }

                            System.out.println("\n--- MOJE KURSY ---");
                            for (Kurs kurs : s.getKursy()) {
                                System.out.println("ID: " + kurs.getId() + " | Tytul: " + kurs.getTytul());
                            }
                            String wybraneId = wczytajNiepustyTekst(scanner, "Podaj ID kursu, ktory chcesz otworzyc: ").replace(" ", "");

                            boolean udaloSieWejsc = system.wybierzObecnyKurs(wybraneId);

                            if (udaloSieWejsc) {
                                trueloop:
                                while (true) {
                                    wyswietlPanelStudentaKursu();
                                    int wybor2 = wczytajLiczbe(scanner, "Wybierz opcje: ");

                                    switch (wybor2) {
                                        case 1:
                                            system.sprawdzSylabus();
                                            break;
                                        case 2:
                                            system.sprawdzMaterialy();
                                            break;
                                        case 3:
                                            system.sprawdzTesty();
                                            break;
                                        case 4:
                                            System.out.println("\nDostepne testy w tym kursie:");
                                            system.sprawdzTesty();
                                            String tytulWyswietlany = wczytajNiepustyTekst(scanner, "Podaj tytul testu, którego tresc chcesz wyswietlic: ");
                                            system.wyswietlZawartoscTestu(tytulWyswietlany);
                                            break;
                                        case 5:
                                            System.out.println("\nDostepne testy w tym kursie:");
                                            system.sprawdzTesty();
                                            String tytulTestu = wczytajNiepustyTekst(scanner, "\nPodaj tytul testu do rozwiazania: ");
                                            String odp = wczytajNiepustyTekst(scanner, "Wpisz swoje odpowiedzi: ");

                                            system.wyslijRozwiazanieDoTestu(tytulTestu, odp);
                                            break;
                                        case 6:
                                            String tytulOcena = wczytajNiepustyTekst(scanner, "Wpisz tytul testu, aby zobaczyc ocene: ");
                                            system.sprawdzOceneTestu(tytulOcena);
                                            break;
                                        default:
                                            System.out.println("Wyjscie z kursu.");
                                            break trueloop;
                                    }
                                }
                            }
                        }
                        case 2 -> {
                            System.out.println("\n--- DOSTEPNE KURSY ---");
                            ArrayList<Kurs> temp = system.getKursy();
                            boolean saNoweKursy = false;

                            for (Kurs k : temp) {
                                if (k.isWidoczny() && !s.getKursy().contains(k)) {
                                    System.out.println("ID: " + k.getId() + " | Tytul: " + k.getTytul());
                                    saNoweKursy = true;
                                }
                            }

                            if (!saNoweKursy) {
                                System.out.println("Brak nowych, widocznych kursow w systemie.");
                                break;
                            }

                            String idDoDolaczenia = wczytajNiepustyTekst(scanner, "Podaj ID kursu, do ktorego chcesz dolaczyc: ").replace(" ", "");
                            String hasloKursu = wczytajNiepustyTekst(scanner, "Podaj haslo zabezpieczajace kurs: ");

                            system.dolaczDoKursu(idDoDolaczenia, hasloKursu);
                        }
                        case 3 -> {
                            System.out.println("Wylogowano.");
                            user = Optional.empty();
                        }
                        default -> System.out.println("Niepoprawny wybor!");
                    }
                }
            }

            case null, default -> {
                System.out.println("Blad krytyczny: Nieznany typ uzytkownika.");
            }
        }
    }

    

    private static void wyswietlMenuAdmina() {
        System.out.println("\n--- MENU ADMINISTRATORA ---");
        System.out.println("1. Zarejestruj nowego uzytkownika");
        System.out.println("2. Usun uzytkownika");
        System.out.println("3. Wyloguj");
    }

    private static void wyswietlMenuWykladowcy() {
        System.out.println("\n--- MENU WYKLADOWCY ---");
        System.out.println("1. Utworz nowy kurs");
        System.out.println("2. Wybierz i zarzadzaj kursem");
        System.out.println("3. Wyloguj");
    }

    private static void wyswietlPanelZarzadzaniaKursem() {
        System.out.println("\n--- PANEL ZARZADZANIA KURSEM ---");
        System.out.println("1. Zmien sylabus kursu (z pliku)");
        System.out.println("2. Dodaj materialy");
        System.out.println("3. Usun materialy");
        System.out.println("4. Wyswietl liste materialow");
        System.out.println("5. Dodaj test");
        System.out.println("6. Sprawdz oceny i odpowiedzi");
        System.out.println("7. Ocen rozwiazanie testu");
        System.out.println("8. Zmien widocznosc kursu");
        System.out.println("9. Wyswietl sylabus");
        System.out.println("10. Wyjdz do menu glownego");
    }

    private static void wyswietlMenuStudenta() {
        System.out.println("\n--- MENU STUDENTA ---");
        System.out.println("1. Wejdz do swojego kursu");
        System.out.println("2. Wyswietl dostepne kursy i dolacz");
        System.out.println("3. Wyloguj");
    }

    private static void wyswietlPanelStudentaKursu() {
        System.out.println("\n--- MENU KURSU ---");
        System.out.println("1. Wyswietl sylabus");
        System.out.println("2. Wyswietl materialy");
        System.out.println("3. Sprawdz dostepne testy");
        System.out.println("4. Wyswietl tresc wybranego testu");
        System.out.println("5. Przeslij rozwiazanie testu");
        System.out.println("6. Sprawdz swoja ocene");
        System.out.println("7. Wyjdz z kursu");
    }

    private static int wczytajLiczbe(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Blad: Podana wartosc nie jest poprawna liczba! Sprobuj ponownie.");
            }
        }
    }

    private static LocalDate wczytajDate(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Blad: Niepoprawny format daty! Uzyj formatu RRRR-MM-DD.");
            }
        }
    }

    private static String wczytajNiepustyTekst(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Blad: Wartosc nie moze byc pusta! Sprobuj ponownie.");
            } else {
                return input;
            }
        }
    }

    
    private static float wczytajOcene(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().replace(",", ".");
            if (input.isEmpty()) {
                System.out.println("Blad: Ocena nie moze byc pusta!");
                continue;
            }
            try {
                return Float.parseFloat(input);
            } catch (NumberFormatException e) {
                System.out.println("Blad: Podana ocena nie jest poprawna liczba! Sprobuj ponownie (np. 4.5).");
            }
        }
    }
}