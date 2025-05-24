import java.util.Scanner;

public class ProjekAlpro {
    static final int SIZE = 10;
    static char[][] map = new char[SIZE][SIZE];
    static int[][] jejak = new int[SIZE][SIZE];

    // Posisi rintangan
    static int[][] balok = {{1,3}, {4,2}, {7,8}};
    static int[][] api = {{4,7}, {6,2}, {8,5}};
    static int[][] air = {{4,8}, {7,3}, {2,7}};
    static int[][] heal = {{6,4}, {3,5}, {2,1}};
    static int[][] teleport = {{9,3}, {0,7}}; // 2 lokasi teleport

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        generateMap();

        while (true) {
            System.out.println("\n=== MENU ===");
            System.out.println("1. Buat MAP");
            System.out.println("2. Proses Perjalanan");
            System.out.println("3. Langsung Sampai");
            System.out.println("0. Keluar");
            System.out.print("Pilihanmu: ");
            int pilihan = sc.nextInt();

            if (pilihan == 1) {
                printMap(map);
            } else if (pilihan == 2) {
                // akan diisi nanti proses perjalanan step-by-step
                System.out.println("Proses perjalanan belum diimplementasi.");
            } else if (pilihan == 3) {
                // akan diisi nanti versi langsung
                System.out.println("Langsung sampai belum diimplementasi.");
            } else if (pilihan == 0) {
                break;
            } else {
                System.out.println("Pilihan tidak valid.");
            }
        }

        sc.close();
    }

    static void generateMap() {
        // Kosongkan map
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                map[i][j] = '-';
                jejak[i][j] = 0;
            }
        }

        // Start dan Finish
        map[SIZE-1][0] = 'S'; // Start (pojok kiri bawah)
        map[0][SIZE-1] = 'F'; // Finish (pojok kanan atas)

        // Tambah rintangan
        for (int[] b : balok) map[b[0]][b[1]] = 'B';
        for (int[] a : api) map[a[0]][a[1]] = 'A';
        for (int[] w : air) map[w[0]][w[1]] = 'a';
        for (int[] h : heal) map[h[0]][h[1]] = 'H';
        for (int[] t : teleport) map[t[0]][t[1]] = 'T';
    }

    static void printMap(char[][] peta) {
        System.out.println("\n=== MAP ===");

        for (int i = 0; i < SIZE; i++) {
            // Garis horizontal atas setiap baris
            for (int j = 0; j < SIZE; j++) {
                System.out.print("+---");
            }
            System.out.println("+");

            // Isi baris
            for (int j = 0; j < SIZE; j++) {
                char isi = peta[i][j];
                // Kalau kosong (masih '-'), tampilkan spasi
                if (isi == '-') {
                    System.out.print("|   ");
                } else {
                    System.out.print("| " + isi + " ");
                }
            }
            System.out.println("|");
        }

        // Garis bawah terakhir
        for (int j = 0; j < SIZE; j++) {
            System.out.print("+---");
        }
        System.out.println("+");
    }



}
