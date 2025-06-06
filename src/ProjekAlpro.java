import java.util.Scanner;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class ProjekAlpro {
    // NOTE: Konstanta ukuran papan dan HP
    static final int UKURAN = 10;
    static final int HP_AWAL = 10;
    static final int HP_MAKS = 10;
    static int jedaMs = 500;

    // NOTE: Papan karakter utama
    static char[][] papanKarakter = new char[UKURAN][UKURAN];

    // NOTE: Titik mulai dan akhir
    static final int mulaiBaris = UKURAN - 1;
    static final int mulaiKolom = 0;
    static final int akhirBaris = 0;
    static final int akhirKolom = UKURAN - 1;

    // NOTE: Daftar posisi objek khusus di papan
    static int[][] daftarBalok = {{1,3}, {4,2}, {7,8}};
    static int[][] daftarApi = {{4,7}, {6,2}, {8,5}};
    static int[][] daftarAir = {{4,8}, {7,3}, {2,7}};
    static int[][] daftarHeal = {{6,4}, {3,5}, {2,1}};
    static int[][] lokasiTeleport = {{0,7}, {9,3}};

    // NOTE: Arah gerak (atas, bawah, kiri, kanan)
    static int[] deltaBaris = {-1, 1, 0, 0};
    static int[] deltaKolom = {0, 0, -1, 1};
    static String[] namaArah = {"ATAS", "BAWAH", "KIRI", "KANAN"};

    // NOTE: Variabel untuk menyimpan solusi pertama yang ditemukan
    static int[][] solusiPertama = null;
    static int hpSolusiPertama = -1;
    static int langkahSolusiPertama = -1;
    static int iterasiSolusiPertama = -1;
    static boolean hentikanSemua = false;

    static boolean solusiDitemukan = false;
    static boolean tampilLangkah = false;

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        buatPapan(); // NOTE: Inisialisasi papan

        while (true) {
            // NOTE: Menu utama
            System.out.println("\n=== MENU ===");
            System.out.println("1. Tampilkan MAP Dasar");
            System.out.println("2. Proses Perjalanan (Berhenti di F pertama, variasi jalur)");
            System.out.println("3. Langsung Hasil Akhir (Berhenti di F pertama, variasi jalur)");
            System.out.println("0. Keluar");
            System.out.print("Pilihanmu: ");
            int pilihanMenu = input.nextInt();
            input.nextLine();

            // NOTE: Reset variabel solusi setiap proses baru
            solusiPertama = null;
            hpSolusiPertama = -1;
            langkahSolusiPertama = -1;
            iterasiSolusiPertama = -1;
            hentikanSemua = false;
            solusiDitemukan = false;

            int[][] papanAwal = new int[UKURAN][UKURAN];

            if (pilihanMenu == 1) {
                tampilkanPapan(papanKarakter, papanAwal, HP_AWAL, 0, false);
            } else if (pilihanMenu == 2) {
                tampilLangkah = true;
                System.out.println("\nMemulai proses perjalanan (berhenti di F pertama, jalur akan bervariasi)...");
                System.out.print("Masukkan jeda antar langkah (ms), misal 500: ");
                jedaMs = input.nextInt(); input.nextLine();
                // NOTE: Mulai proses backtracking dari titik awal
                findPath(0, papanAwal, mulaiBaris, mulaiKolom, HP_AWAL, 1, papanKarakter[mulaiBaris][mulaiKolom]);

                if (solusiDitemukan && solusiPertama != null) {
                    System.out.println("\n=== PROSES SELESAI (Berhenti di F pertama) ===");
                    System.out.println("Solusi pertama yang ditemukan mencapai Finish:");
                    tampilkanPapan(papanKarakter, solusiPertama, hpSolusiPertama, langkahSolusiPertama, true);
                    System.out.println("Sisa HP: " + hpSolusiPertama);
                    System.out.println("Jumlah Sel di Path Solusi: " + langkahSolusiPertama);
                    System.out.println("Mencapai Finish pada langkah dengan ID iterasi: " + iterasiSolusiPertama);
                } else {
                    System.out.println("\nTidak ada solusi yang ditemukan (tidak mencapai Finish).");
                }
                tampilLangkah = false;
            } else if (pilihanMenu == 3) {
                tampilLangkah = false;
                System.out.println("\nMemproses untuk hasil akhir (berhenti di F pertama, jalur akan bervariasi)...");
                // NOTE: Mulai proses backtracking dari titik awal (tanpa tampil langkah)
                findPath(0, papanAwal, mulaiBaris, mulaiKolom, HP_AWAL, 1, papanKarakter[mulaiBaris][mulaiKolom]);

                if (solusiDitemukan && solusiPertama != null) {
                    System.out.println("\n=== HASIL AKHIR (Berhenti di F pertama) ===");
                    System.out.println("Solusi pertama yang ditemukan mencapai Finish:");
                    tampilkanPapan(papanKarakter, solusiPertama, hpSolusiPertama, langkahSolusiPertama, true);
                    System.out.println("Sisa HP: " + hpSolusiPertama);
                    System.out.println("Jumlah Sel di Path Solusi: " + langkahSolusiPertama);
                    System.out.println("Mencapai Finish pada langkah dengan ID iterasi: " + iterasiSolusiPertama);
                } else {
                    System.out.println("\nTidak ada solusi yang ditemukan (tidak mencapai Finish).");
                }
            } else if (pilihanMenu == 0) {
                break;
            } else {
                System.out.println("Pilihan tidak valid.");
            }
        }
        input.close();
    }

    // NOTE: Inisialisasi papan dan objek-objeknya
    static void buatPapan() {
        for (int i = 0; i < UKURAN; i++) {
            for (int j = 0; j < UKURAN; j++) {
                papanKarakter[i][j] = '-';
            }
        }
        papanKarakter[mulaiBaris][mulaiKolom] = 'S';
        papanKarakter[akhirBaris][akhirKolom] = 'F';
        for (int[] b : daftarBalok) papanKarakter[b[0]][b[1]] = 'B';
        for (int[] a : daftarApi) papanKarakter[a[0]][a[1]] = 'A';
        for (int[] w : daftarAir) papanKarakter[w[0]][w[1]] = 'a';
        for (int[] h : daftarHeal) papanKarakter[h[0]][h[1]] = 'H';
        papanKarakter[lokasiTeleport[0][0]][lokasiTeleport[0][1]] = 'T';
        papanKarakter[lokasiTeleport[1][0]][lokasiTeleport[1][1]] = 'T';
    }

    // NOTE: Membuat salinan papan (untuk setiap cabang backtracking)
    static int[][] copyPapan(int[][] papan) {
        if (papan == null) return null;
        int[][] baru = new int[papan.length][];
        for (int i = 0; i < papan.length; i++) {
            baru[i] = Arrays.copyOf(papan[i], papan[i].length);
        }
        return baru;
    }

    // NOTE: Mengecek apakah posisi aman untuk dilalui
    static boolean aman(int baris, int kolom, int[][] copyPapan, int idIterasi) {
        if (baris < 0 || baris >= UKURAN || kolom < 0 || kolom >= UKURAN) return false;
        if (papanKarakter[baris][kolom] == 'B') return false;
        if (copyPapan[baris][kolom] == idIterasi) return false;
        return true;
    }

    // NOTE: Menghitung jumlah langkah pada jalur solusi
    static int hitungLangkah(int[][] papan) {
        if (papan == null) return 0;
        int jumlah = 0;
        for (int i = 0; i < UKURAN; i++) {
            for (int j = 0; j < UKURAN; j++) {
                if (papan[i][j] != 0) {
                    jumlah++;
                }
            }
        }
        return jumlah;
    }

    // NOTE: FUNGSI BACKTRACKING UTAMA
    static void findPath(int depth, int[][] copyPapanSebelum,
                         int baris, int kolom, int hpSekarang, int idIterasi, char karakterSebelumnya) {

        if (hentikanSemua) {
            return;
        }

        int[][] copyPapan = copyPapan(copyPapanSebelum);
        copyPapan[baris][kolom] = idIterasi;

        // NOTE: Tampilkan langkah jika diaktifkan
        if (tampilLangkah) {
            System.out.println("\n------------------------------------------");
            System.out.println("Iterasi: " + idIterasi + ", Depth ke-" + (depth + 1) +
                               " di (" + baris + "," + kolom + ")");
            System.out.println("HP: " + hpSekarang + ", Cell Sebelumnya: '" + karakterSebelumnya + "' -> Cell Sekarang: '" + papanKarakter[baris][kolom] + "'");
            tampilkanPapan(papanKarakter, copyPapan, hpSekarang, depth + 1, false);
            try { Thread.sleep(jedaMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        // NOTE: Jika sudah sampai finish
        if (baris == akhirBaris && kolom == akhirKolom) {
            solusiDitemukan = true;
            int panjangLangkah = hitungLangkah(copyPapan);

            if (tampilLangkah) {
                System.out.println("!!! SAMPAI FINISH (PERTAMA KALI) !!! HP: " + hpSekarang + ", Total Sel di Path: " + panjangLangkah + ", Iterasi saat ini: " + idIterasi);
            }

            if (solusiPertama == null) {
                hpSolusiPertama = hpSekarang;
                langkahSolusiPertama = panjangLangkah;
                solusiPertama = copyPapan(copyPapan);
                iterasiSolusiPertama = idIterasi;
                hentikanSemua = true;
                if (tampilLangkah) {
                    System.out.println(">>> SOLUSI PERTAMA DITEMUKAN DAN DICATAT! Pencarian akan dihentikan.");
                }
            }
            return;
        }

        // NOTE: Batas iterasi untuk mencegah infinite loop
        if (idIterasi > UKURAN * UKURAN * 10 ) {
             if(tampilLangkah) System.out.println("Batas iterasi maksimum ("+ (UKURAN*UKURAN*10) +") tercapai tanpa menemukan F. Menghentikan cabang ini.");
             return;
        }

        boolean sudahBergerak = false;
        List<Integer> urutanArah = new ArrayList<>();
        for (int k = 0; k < 4; k++) { urutanArah.add(k); }
        Collections.shuffle(urutanArah);

        // NOTE: Coba semua arah (rekursi/backtracking)
        for (int idxArah : urutanArah) {
            if (hentikanSemua) return;

            int barisBaru = baris + deltaBaris[idxArah];
            int kolomBaru = kolom + deltaKolom[idxArah];

            if (aman(barisBaru, kolomBaru, copyPapan, idIterasi)) {
                sudahBergerak = true;
                int hpBerikut = hpSekarang;
                char karakterSekarang = papanKarakter[baris][kolom];
                char karakterTujuan = papanKarakter[barisBaru][kolomBaru];
                int barisSetelahEfek = barisBaru; int kolomSetelahEfek = kolomBaru;
                boolean teleport = false; String pesanEfek = "";

                // NOTE: Efek cell khusus
                if (karakterTujuan == 'a') { hpBerikut -= 1; pesanEfek = "Kena Air (-1 HP)"; }
                else if (karakterTujuan == 'A') { hpBerikut -= 2; pesanEfek = "Kena Api (-2 HP)"; }
                else if (karakterTujuan == 'H') { hpBerikut = Math.min(hpBerikut + 1, HP_MAKS); pesanEfek = "Dapat Heal (+1 HP)"; }
                else if (karakterTujuan == 'T') {
                    pesanEfek = "Masuk Teleport ";
                    if (barisBaru == lokasiTeleport[0][0] && kolomBaru == lokasiTeleport[0][1]) {
                        barisSetelahEfek = lokasiTeleport[1][0]; kolomSetelahEfek = lokasiTeleport[1][1];
                    } else {
                        barisSetelahEfek = lokasiTeleport[0][0]; kolomSetelahEfek = lokasiTeleport[0][1];
                    }
                    teleport = true; karakterSekarang = 'T';
                    pesanEfek += "ke (" + barisSetelahEfek + "," + kolomSetelahEfek + ")";
                }
                // NOTE: Efek combo api-air
                if (!teleport && ((karakterSekarang == 'A' && karakterTujuan == 'a') || (karakterSekarang == 'a' && karakterTujuan == 'A'))) {
                    hpBerikut = 0;
                    pesanEfek += (pesanEfek.isEmpty() ? "" : ", ") + "COMBO Api-Air! HP jadi 0";
                }

                if (tampilLangkah && !pesanEfek.isEmpty()) {
                     System.out.println("Bergerak " + namaArah[idxArah] + " ke (" + barisBaru + "," + kolomBaru + "): " + pesanEfek + ". HP jadi: " + hpBerikut);
                }

                // NOTE: Jika HP habis, mulai iterasi baru
                if (hpBerikut <= 0) {
                    if (hentikanSemua) return;
                    if (tampilLangkah) { System.out.println("HP Habis... Mulai iterasi baru.");}
                    int[][] papanBaruIterasi = copyPapan(copyPapan);
                    papanBaruIterasi[barisBaru][kolomBaru] = idIterasi;
                    if(teleport){ papanBaruIterasi[barisSetelahEfek][kolomSetelahEfek] = idIterasi; }
                    findPath(0, papanBaruIterasi, (teleport ? barisSetelahEfek : barisBaru), (teleport ? kolomSetelahEfek : kolomBaru), HP_AWAL, idIterasi + 1, papanKarakter[(teleport ? barisSetelahEfek : barisBaru)][(teleport ? kolomSetelahEfek : kolomBaru)]);
                    return;
                } else {
                    if (hentikanSemua) return;
                    findPath(depth + 1, copyPapan, (teleport ? barisSetelahEfek : barisBaru), (teleport ? kolomSetelahEfek : kolomBaru), hpBerikut, idIterasi, karakterSekarang);
                }
            }
        }

        // NOTE: Jika tidak bisa bergerak, mulai iterasi baru dari posisi sekarang
        if (!sudahBergerak && !(baris == akhirBaris && kolom == akhirKolom) ) {
            if (hentikanSemua) return;
            if (tampilLangkah) { System.out.println("Iterasi " + idIterasi + " STUCK... Mulai iterasi baru.");}
            findPath(0, copyPapan, baris, kolom, hpSekarang, idIterasi + 1, papanKarakter[baris][kolom]);
            return;
        }
    }

    // NOTE: Menampilkan papan beserta jalur solusi/percobaan
    static void tampilkanPapan(char[][] papanDasar, int[][] overlayJalur, int infoHp, int infoLangkah, boolean solusiAkhir) {
        System.out.println("\n=== MAP (HP: " + infoHp + ", Langkah ke/Jml Sel: " + infoLangkah + ") ===");
        if (solusiAkhir && iterasiSolusiPertama != -1) {
            System.out.println("--- Solusi Pertama yang Mencapai Finish (pada Iterasi ID: " + iterasiSolusiPertama + ") ---");
        }
        for (int i = 0; i < UKURAN; i++) {
            for (int j = 0; j < UKURAN; j++) System.out.print("+---");
            System.out.println("+");
            for (int j = 0; j < UKURAN; j++) {
                char karakterPeta = papanDasar[i][j];
                int tandaJalur = (overlayJalur != null) ? overlayJalur[i][j] : 0;
                if (karakterPeta == 'S' || karakterPeta == 'F' || karakterPeta == 'B' || karakterPeta == 'A' || karakterPeta == 'a' || karakterPeta == 'H' || karakterPeta == 'T') {
                    System.out.print("| " + karakterPeta + " ");
                } else if (tandaJalur != 0) {
                    System.out.printf("|%3d", tandaJalur);
                } else {
                    System.out.print("|   ");
                }
            }
            System.out.println("|");
        }
        for (int j = 0; j < UKURAN; j++) System.out.print("+---");
        System.out.println("+");
    }
}