import java.util.*; // Import library utilitas Java (untuk Scanner, Arrays, List, dsb)

public class ProjekAlpro {
    // Ukuran papan, HP awal, dan HP maksimum
    static final int UKURAN = 10, HP_AWAL = 10, HP_MAKS = 10;

    // Daftar koordinat untuk balok (B), api (A), air (a), heal (H), dan teleport (T)
    static final int[][] daftarBalok = {{1,3}, {4,2}, {7,8}};
    static final int[][] daftarApi = {{4,7}, {6,2}, {8,5}};
    static final int[][] daftarAir = {{4,8}, {7,3}, {2,7}};
    static final int[][] daftarHeal = {{6,4}, {3,5}, {2,1}};
    static final int[][] lokasiTeleport = {{0,7}, {9,3}};

    // Delta baris dan kolom untuk pergerakan (atas, bawah, kiri, kanan)
    static final int[] deltaBaris = {-1, 1, 0, 0}, deltaKolom = {0, 0, -1, 1};
    static final String[] namaArah = {"ATAS", "BAWAH", "KIRI", "KANAN"};

    // Koordinat start (S) dan finish (F)
    static final int mulaiBaris = UKURAN-1, mulaiKolom = 0, akhirBaris = 0, akhirKolom = UKURAN-1;

    // Papan karakter utama (isi map)
    static char[][] papanKarakter = new char[UKURAN][UKURAN];

    // Untuk menyimpan solusi pertama yang ditemukan
    static int[][] solusiPertama = null;
    static int hpSolusiPertama, langkahSolusiPertama, iterasiSolusiPertama;

    // Flag untuk status pencarian solusi
    static boolean solusiDitemukan, hentikanSemua, tampilLangkah;

    // Jeda antar langkah (ms) jika mode tampil langkah diaktifkan
    static int jedaMs = 500;

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in); // Untuk input dari user
        buatPapan(); // Inisialisasi papan/map

        // Menu utama program
        while (true) {
            System.out.println("\n=== MENU ===");
            System.out.println("1. Tampilkan MAP Dasar");
            System.out.println("2. Proses Perjalanan (Stop di F pertama, variasi jalur)");
            System.out.println("3. Langsung Hasil Akhir (Stop di F pertama, variasi jalur)");
            System.out.println("0. Keluar");
            System.out.print("Pilihanmu: ");
            int menu = input.nextInt(); input.nextLine();

            resetSolusi(); // Reset solusi sebelum mulai pencarian baru
            int[][] papanAwal = new int[UKURAN][UKURAN]; // Papan penanda jalur (path)

            if (menu == 1) {
                // Tampilkan map dasar tanpa jalur
                tampilkanPapan(papanKarakter, papanAwal, HP_AWAL, 0, false);
            } else if (menu == 2 || menu == 3) {
                // Mode proses perjalanan (tampil langkah) atau langsung hasil akhir
                tampilLangkah = (menu == 2);
                if (tampilLangkah) {
                    System.out.print("Masukkan jeda antar langkah (ms): ");
                    jedaMs = input.nextInt(); input.nextLine();
                }
                // Mulai pencarian jalur dengan backtracking
                findPath(0, papanAwal, mulaiBaris, mulaiKolom, HP_AWAL, 1, papanKarakter[mulaiBaris][mulaiKolom]);
                if (solusiDitemukan && solusiPertama != null) {
                    // Jika solusi ditemukan, tampilkan hasilnya
                    System.out.println(menu == 2 ? "\n=== PROSES SELESAI ===" : "\n=== HASIL AKHIR ===");
                    tampilkanPapan(papanKarakter, solusiPertama, hpSolusiPertama, langkahSolusiPertama, true);
                    System.out.println("Sisa HP: " + hpSolusiPertama);
                    System.out.println("Jumlah Sel di Path Solusi: " + langkahSolusiPertama);
                    System.out.println("Mencapai Finish pada iterasi: " + iterasiSolusiPertama);
                } else {
                    System.out.println("\nTidak ada solusi yang ditemukan.");
                }
                tampilLangkah = false;
            } else if (menu == 0) break; // Keluar program
            else System.out.println("Pilihan tidak valid.");
        }
        input.close();
    }

    // Inisialisasi papan/map dengan semua elemen (S, F, B, A, a, H, T)
    static void buatPapan() {
        for (char[] row : papanKarakter) Arrays.fill(row, '-'); // Isi semua dengan '-'
        papanKarakter[mulaiBaris][mulaiKolom] = 'S'; // Start
        papanKarakter[akhirBaris][akhirKolom] = 'F'; // Finish
        for (int[] b : daftarBalok) papanKarakter[b[0]][b[1]] = 'B'; // Balok
        for (int[] a : daftarApi) papanKarakter[a[0]][a[1]] = 'A'; // Api
        for (int[] w : daftarAir) papanKarakter[w[0]][w[1]] = 'a'; // Air
        for (int[] h : daftarHeal) papanKarakter[h[0]][h[1]] = 'H'; // Heal
        for (int[] t : lokasiTeleport) papanKarakter[t[0]][t[1]] = 'T'; // Teleport
    }

    // Reset semua variabel solusi sebelum pencarian baru
    static void resetSolusi() {
        solusiPertama = null;
        hpSolusiPertama = langkahSolusiPertama = iterasiSolusiPertama = -1;
        solusiDitemukan = hentikanSemua = false;
    }

    // Membuat salinan papan (untuk path/jalur)
    static int[][] copyPapan(int[][] papan) {
        int[][] baru = new int[UKURAN][UKURAN];
        for (int i = 0; i < UKURAN; i++) baru[i] = Arrays.copyOf(papan[i], UKURAN);
        return baru;
    }

    // Mengecek apakah posisi (baris, kolom) aman untuk dilalui
    static boolean aman(int baris, int kolom, int[][] papan, int iterasi) {
        return baris >= 0 && baris < UKURAN && kolom >= 0 && kolom < UKURAN &&
               papanKarakter[baris][kolom] != 'B' && papan[baris][kolom] != iterasi;
    }

    // Menghitung jumlah langkah (sel yang sudah dilewati)
    static int hitungLangkah(int[][] papan) {
        int jumlah = 0;
        for (int[] row : papan)
            for (int v : row) if (v != 0) jumlah++;
        return jumlah;
    }

    // Fungsi utama pencarian jalur (backtracking)
    // depth: kedalaman rekursi, papanSebelum: papan path sebelum langkah ini
    // baris, kolom: posisi sekarang, hp: HP sekarang, iterasi: nomor langkah, prevChar: karakter sebelumnya
    static void findPath(int depth, int[][] papanSebelum, int baris, int kolom, int hp, int iterasi, char prevChar) {
        if (hentikanSemua) return; // Jika sudah ketemu solusi, hentikan semua rekursi

        int[][] papan = copyPapan(papanSebelum); // Salin papan path
        papan[baris][kolom] = iterasi; // Tandai posisi sekarang dengan nomor iterasi

        // Jika mode tampil langkah aktif, tampilkan info langkah sekarang
        if (tampilLangkah) {
            System.out.println("\n------------------------------------------");
            System.out.println("Iterasi: " + iterasi + ", Depth: " + (depth+1) + " di (" + baris + "," + kolom + ")");
            System.out.println("HP: " + hp + ", Dari: '" + prevChar + "' -> '" + papanKarakter[baris][kolom] + "'");
            tampilkanPapan(papanKarakter, papan, hp, depth+1, false);
            try { Thread.sleep(jedaMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        // Jika sudah sampai finish, catat solusi pertama
        if (baris == akhirBaris && kolom == akhirKolom) {
            solusiDitemukan = true;
            int langkah = hitungLangkah(papan);
            if (solusiPertama == null) {
                hpSolusiPertama = hp;
                langkahSolusiPertama = langkah;
                solusiPertama = copyPapan(papan);
                iterasiSolusiPertama = iterasi;
                hentikanSemua = true; // Hentikan semua pencarian setelah solusi pertama
            }
            return;
        }

        // Batas iterasi agar tidak infinite loop
        if (iterasi > UKURAN*UKURAN*10) return;

        // Randomisasi urutan arah agar jalur bervariasi
        List<Integer> urutanArah = Arrays.asList(0,1,2,3);
        Collections.shuffle(urutanArah);
        boolean sudahBergerak = false;

        // Coba semua arah (atas, bawah, kiri, kanan)
        for (int idx : urutanArah) {
            int nb = baris + deltaBaris[idx], nk = kolom + deltaKolom[idx];
            if (!aman(nb, nk, papan, iterasi)) continue; // Skip jika tidak aman
            sudahBergerak = true;
            int nextHp = hp;
            char now = papanKarakter[baris][kolom], tujuan = papanKarakter[nb][nk];
            int nb2 = nb, nk2 = nk;
            boolean teleport = false;

            // Efek cell tujuan
            if (tujuan == 'a') nextHp--; // Air: -1 HP
            else if (tujuan == 'A') nextHp -= 2; // Api: -2 HP
            else if (tujuan == 'H') nextHp = Math.min(nextHp+1, HP_MAKS); // Heal: +1 HP (maksimal HP_MAKS)
            else if (tujuan == 'T') { // Teleport
                if (nb == lokasiTeleport[0][0] && nk == lokasiTeleport[0][1]) {
                    nb2 = lokasiTeleport[1][0]; nk2 = lokasiTeleport[1][1];
                } else {
                    nb2 = lokasiTeleport[0][0]; nk2 = lokasiTeleport[0][1];
                }
                teleport = true; now = 'T';
            }
            // Efek combo api-air
            if (!teleport && ((now == 'A' && tujuan == 'a') || (now == 'a' && tujuan == 'A'))) nextHp = 0;

            // Jika HP habis, mulai iterasi baru dari posisi sekarang (reset HP)
            if (nextHp <= 0) {
                int[][] papanBaru = copyPapan(papan);
                papanBaru[nb][nk] = iterasi;
                if (teleport) papanBaru[nb2][nk2] = iterasi;
                findPath(0, papanBaru, teleport ? nb2 : nb, teleport ? nk2 : nk, HP_AWAL, iterasi+1, papanKarakter[teleport ? nb2 : nb][teleport ? nk2 : nk]);
                return;
            } else {
                // Lanjutkan ke langkah berikutnya
                findPath(depth+1, papan, teleport ? nb2 : nb, teleport ? nk2 : nk, nextHp, iterasi, now);
            }
        }

        // Jika tidak bisa bergerak ke mana-mana (stuck), mulai iterasi baru dari posisi sekarang
        if (!sudahBergerak && !(baris == akhirBaris && kolom == akhirKolom)) {
            findPath(0, papan, baris, kolom, hp, iterasi+1, papanKarakter[baris][kolom]);
        }
    }

    // Menampilkan papan/map ke layar
    // dasar: papan karakter, jalur: papan path, hp: HP sekarang, langkah: jumlah langkah, solusiAkhir: apakah ini solusi akhir
    static void tampilkanPapan(char[][] dasar, int[][] jalur, int hp, int langkah, boolean solusiAkhir) {
        System.out.println("\n=== MAP (HP: " + hp + ", Langkah: " + langkah + ") ===");
        if (solusiAkhir && iterasiSolusiPertama != -1)
            System.out.println("--- Solusi Pertama pada Iterasi: " + iterasiSolusiPertama + " ---");
        for (int i = 0; i < UKURAN; i++) {
            for (int j = 0; j < UKURAN; j++) System.out.print("+---");
            System.out.println("+");
            for (int j = 0; j < UKURAN; j++) {
                char ch = dasar[i][j];
                int mark = (jalur != null) ? jalur[i][j] : 0;
                // Jika cell adalah S, F, B, a, H, T tampilkan hurufnya
                if ("SFBaHTA".indexOf(ch) >= 0) System.out.print("| " + ch + " ");
                // Jika cell adalah bagian jalur, tampilkan nomor langkahnya
                else if (mark != 0) System.out.printf("|%3d", mark);
                // Jika kosong, tampilkan spasi
                else System.out.print("|   ");
            }
            System.out.println("|");
        }
        for (int j = 0; j < UKURAN; j++) System.out.print("+---");
        System.out.println("+");
    }
}