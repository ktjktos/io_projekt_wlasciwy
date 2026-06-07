public class Main {
    public static void main(String[] args) {
        BazaLogowan baza = new BazaLogowan("bazaDanych.csv");
        SystemZarzadzania system = new SystemZarzadzania(baza);
        ConsoleUI ui = new ConsoleUI(system, baza);
        ui.start();
    }
}