import java.util.Scanner;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
// import java.util.Random; // Jika ingin seed tetap

public class ProjekAlpro {
    // ... (konstanta dan variabel global lainnya sama) ...
    static final int SIZE = 10;
    static final int INITIAL_HP = 10;
    static final int MAX_HP = 10;
    static int DELAY_MS = 500;

    static char[][] map = new char[SIZE][SIZE];

    static final int startY = SIZE - 1;
    static final int startX = 0;
    static final int finishY = 0;
    static final int finishX = SIZE - 1;

    static int[][] balok = {{1,3}, {4,2}, {7,8}};
    static int[][] api = {{4,7}, {6,2}, {8,5}};
    static int[][] air = {{4,8}, {7,3}, {2,7}};
    static int[][] heal = {{6,4}, {3,5}, {2,1}};
    static int[][] teleport_locs = {{0,7}, {9,3}};

    static int[] dy = {-1, 1, 0, 0};
    static int[] dx = {0, 0, -1, 1};
    static String[] move_names = {"ATAS", "BAWAH", "KIRI", "KANAN"};

    static int[][] firstFoundSolutionPath = null; // Akan menyimpan solusi pertama yang ditemukan
    static int firstFoundHp = -1;
    static int firstFoundSteps = -1;
    static int firstFoundIterationId = -1;
    static boolean stopAllSearch = false; // Flag untuk menghentikan semua pencarian setelah F pertama ditemukan
    // Hapus variabel bestSolution karena kita hanya peduli solusi pertama
    // static int[][] bestSolutionPath = null;
    // static int bestHp = -1;
    // static int bestSteps = Integer.MAX_VALUE;
    // static int bestSolutionIterationId = -1;

    static boolean solutionFoundThisRun = false; // Tetap berguna untuk tahu apakah F pernah tercapai
    static boolean display_steps = false;


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        generateMap();

        while (true) {
            System.out.println("\n=== MENU ===");
            System.out.println("1. Tampilkan MAP Dasar");
            System.out.println("2. Proses Perjalanan (Berhenti di F pertama, variasi jalur)");
            System.out.println("3. Langsung Hasil Akhir (Berhenti di F pertama, variasi jalur)");
            System.out.println("0. Keluar");
            System.out.print("Pilihanmu: ");
            int pilihan = sc.nextInt();
            sc.nextLine();

            // Reset state untuk setiap pencarian baru
            firstFoundSolutionPath = null;
            firstFoundHp = -1;
            firstFoundSteps = -1;
            firstFoundIterationId = -1;
            stopAllSearch = false; // PENTING: Reset flag ini
            solutionFoundThisRun = false;


            int[][] initialPathBoard = new int[SIZE][SIZE];

            if (pilihan == 1) {
                printMapWithOverlay(map, initialPathBoard, INITIAL_HP, 0, false);
            } else if (pilihan == 2) {
                display_steps = true;
                System.out.println("\nMemulai proses perjalanan (berhenti di F pertama, jalur akan bervariasi)...");
                System.out.print("Masukkan delay antar langkah (ms), misal 500: ");
                DELAY_MS = sc.nextInt(); sc.nextLine();
                findPath(0, initialPathBoard, startY, startX, INITIAL_HP, 1, map[startY][startX]);

                if (solutionFoundThisRun && firstFoundSolutionPath != null) {
                    System.out.println("\n=== PROSES SELESAI (Berhenti di F pertama) ===");
                    System.out.println("Solusi pertama yang ditemukan mencapai Finish:");
                    printMapWithOverlay(map, firstFoundSolutionPath, firstFoundHp, firstFoundSteps, true);
                    System.out.println("Sisa HP: " + firstFoundHp);
                    System.out.println("Jumlah Sel di Path Solusi: " + firstFoundSteps);
                    System.out.println("Mencapai Finish pada langkah dengan ID iterasi: " + firstFoundIterationId);
                } else {
                    System.out.println("\nTidak ada solusi yang ditemukan (tidak mencapai Finish).");
                }
                display_steps = false;
            } else if (pilihan == 3) {
                display_steps = false;
                System.out.println("\nMemproses untuk hasil akhir (berhenti di F pertama, jalur akan bervariasi)...");
                findPath(0, initialPathBoard, startY, startX, INITIAL_HP, 1, map[startY][startX]);

                if (solutionFoundThisRun && firstFoundSolutionPath != null) {
                    System.out.println("\n=== HASIL AKHIR (Berhenti di F pertama) ===");
                    System.out.println("Solusi pertama yang ditemukan mencapai Finish:");
                    printMapWithOverlay(map, firstFoundSolutionPath, firstFoundHp, firstFoundSteps, true);
                    System.out.println("Sisa HP: " + firstFoundHp);
                    System.out.println("Jumlah Sel di Path Solusi: " + firstFoundSteps);
                    System.out.println("Mencapai Finish pada langkah dengan ID iterasi: " + firstFoundIterationId);
                } else {
                    System.out.println("\nTidak ada solusi yang ditemukan (tidak mencapai Finish).");
                }
            } else if (pilihan == 0) {
                break;
            } else {
                System.out.println("Pilihan tidak valid.");
            }
        }
        sc.close();
    }

    static void generateMap() {
        // ... (sama)
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                map[i][j] = '-';
            }
        }
        map[startY][startX] = 'S';
        map[finishY][finishX] = 'F';
        for (int[] b : balok) map[b[0]][b[1]] = 'B';
        for (int[] a : api) map[a[0]][a[1]] = 'A';
        for (int[] w : air) map[w[0]][w[1]] = 'a';
        for (int[] h : heal) map[h[0]][h[1]] = 'H';
        map[teleport_locs[0][0]][teleport_locs[0][1]] = 'T';
        map[teleport_locs[1][0]][teleport_locs[1][1]] = 'T';
    }

    static int[][] copyIntArray(int[][] original) {
        // ... (sama)
        if (original == null) return null;
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return copy;
    }

    static boolean isSafe(int r, int c, int[][] current_path_board, int iteration_id) {
        // ... (sama)
        if (r < 0 || r >= SIZE || c < 0 || c >= SIZE) return false;
        if (map[r][c] == 'B') return false;
        if (current_path_board[r][c] == iteration_id) return false;
        return true;
    }

    static int countTotalStepsInPath(int[][] pathBoard) {
        // ... (sama)
        if (pathBoard == null) return 0;
        int count = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (pathBoard[i][j] != 0) {
                    count++;
                }
            }
        }
        return count;
    }

    static void findPath(int steps_in_current_iteration, int[][] path_board_before_this_step,
                         int r, int c, int current_hp, int iteration_id, char char_of_cell_just_left) {

        // === PERUBAHAN 1: Cek flag stopAllSearch di awal ===
        if (stopAllSearch) {
            return; // Jika sudah ada F ditemukan, hentikan cabang ini
        }

        int[][] current_path_board = copyIntArray(path_board_before_this_step);
        current_path_board[r][c] = iteration_id;

        if (display_steps) {
            System.out.println("\n------------------------------------------");
            System.out.println("Iterasi: " + iteration_id + ", Langkah ke-" + (steps_in_current_iteration + 1) +
                               " di (" + r + "," + c + ")");
            System.out.println("HP: " + current_hp + ", Cell Sebelumnya: '" + char_of_cell_just_left + "' -> Cell Sekarang: '" + map[r][c] + "'");
            printMapWithOverlay(map, current_path_board, current_hp, steps_in_current_iteration + 1, false);
            try { Thread.sleep(DELAY_MS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        if (r == finishY && c == finishX) {
            solutionFoundThisRun = true; // Tetap berguna
            int path_length = countTotalStepsInPath(current_path_board);

            if (display_steps) {
                System.out.println("!!! SAMPAI FINISH (PERTAMA KALI) !!! HP: " + current_hp + ", Total Sel di Path: " + path_length + ", Iterasi saat ini: " + iteration_id);
            }

            // === PERUBAHAN 2: Simpan solusi pertama dan set flag stopAllSearch ===
            if (firstFoundSolutionPath == null) { // Hanya simpan jika ini yang pertama
                firstFoundHp = current_hp;
                firstFoundSteps = path_length;
                firstFoundSolutionPath = copyIntArray(current_path_board);
                firstFoundIterationId = iteration_id;
                stopAllSearch = true; // SET FLAG UNTUK MENGHENTIKAN SEMUA PENCARIAN LAIN
                if (display_steps) {
                    System.out.println(">>> SOLUSI PERTAMA DITEMUKAN DAN DICATAT! Pencarian akan dihentikan.");
                }
            }
            return; // Selesai untuk cabang ini, dan pencarian global juga akan berhenti
        }

        // Tidak perlu lagi batas iterasi yang terlalu tinggi jika kita berhenti di F pertama,
        // tapi bisa dipertahankan sebagai pengaman jika F tidak pernah tercapai.
        if (iteration_id > SIZE * SIZE * 10 ) { // Mungkin bisa dikurangi
             if(display_steps) System.out.println("Batas iterasi maksimum ("+ (SIZE*SIZE*10) +") tercapai tanpa menemukan F. Menghentikan cabang ini.");
             return;
        }

        boolean moved_in_this_iteration_branch = false;
        List<Integer> moveOrderIndices = new ArrayList<>();
        for (int k = 0; k < 4; k++) { moveOrderIndices.add(k); }
        Collections.shuffle(moveOrderIndices);

        for (int moveIndex : moveOrderIndices) {
            // === PERUBAHAN 3: Cek flag stopAllSearch sebelum rekursi ===
            if (stopAllSearch) return;

            int nr = r + dy[moveIndex];
            int nc = c + dx[moveIndex];

            if (isSafe(nr, nc, current_path_board, iteration_id)) {
                moved_in_this_iteration_branch = true;
                int next_hp = current_hp;
                char char_at_current_rc = map[r][c];
                char char_at_next_rc = map[nr][nc];
                int next_r_after_effect = nr; int next_c_after_effect = nc;
                boolean teleported = false; String effect_msg = "";

                // ... (Logika efek rintangan, teleport, combo sama) ...
                if (char_at_next_rc == 'a') { next_hp -= 1; effect_msg = "Kena Air (-1 HP)"; }
                else if (char_at_next_rc == 'A') { next_hp -= 2; effect_msg = "Kena Api (-2 HP)"; }
                else if (char_at_next_rc == 'H') { next_hp = Math.min(next_hp + 1, MAX_HP); effect_msg = "Dapat Heal (+1 HP)"; }
                else if (char_at_next_rc == 'T') {
                    effect_msg = "Masuk Teleport ";
                    if (nr == teleport_locs[0][0] && nc == teleport_locs[0][1]) {
                        next_r_after_effect = teleport_locs[1][0]; next_c_after_effect = teleport_locs[1][1];
                    } else {
                        next_r_after_effect = teleport_locs[0][0]; next_c_after_effect = teleport_locs[0][1];
                    }
                    teleported = true; char_at_current_rc = 'T';
                    effect_msg += "ke (" + next_r_after_effect + "," + next_c_after_effect + ")";
                }
                if (!teleported && ((char_at_current_rc == 'A' && char_at_next_rc == 'a') || (char_at_current_rc == 'a' && char_at_next_rc == 'A'))) {
                    next_hp = 0;
                    effect_msg += (effect_msg.isEmpty() ? "" : ", ") + "COMBO Api-Air! HP jadi 0";
                }


                if (display_steps && !effect_msg.isEmpty()) {
                     System.out.println("Bergerak " + move_names[moveIndex] + " ke (" + nr + "," + nc + "): " + effect_msg + ". HP jadi: " + next_hp);
                }

                if (next_hp <= 0) {
                    if (stopAllSearch) return; // Cek lagi sebelum panggil iterasi baru
                    // ... (logika HP habis sama)
                    if (display_steps) { System.out.println("HP Habis... Mulai iterasi baru.");}
                    int[][] board_for_new_iter = copyIntArray(current_path_board);
                    board_for_new_iter[nr][nc] = iteration_id;
                    if(teleported){ board_for_new_iter[next_r_after_effect][next_c_after_effect] = iteration_id; }
                    findPath(0, board_for_new_iter, (teleported ? next_r_after_effect : nr), (teleported ? next_c_after_effect : nc), INITIAL_HP, iteration_id + 1, map[(teleported ? next_r_after_effect : nr)][(teleported ? next_c_after_effect : nc)]);
                    return;
                } else {
                    if (stopAllSearch) return; // Cek lagi sebelum rekursi normal
                    findPath(steps_in_current_iteration + 1, current_path_board, (teleported ? next_r_after_effect : nr), (teleported ? next_c_after_effect : nc), next_hp, iteration_id, char_at_current_rc);
                }
            }
        }

        if (!moved_in_this_iteration_branch && !(r == finishY && c == finishX) ) {
            if (stopAllSearch) return; // Cek lagi sebelum panggil iterasi baru karena stuck
            // ... (logika stuck sama)
            if (display_steps) { System.out.println("Iterasi " + iteration_id + " STUCK... Mulai iterasi baru.");}
            findPath(0, current_path_board, r, c, current_hp, iteration_id + 1, map[r][c]);
            return;
        }
    }

    static void printMapWithOverlay(char[][] baseMap, int[][] pathOverlay, int hp_info, int steps_info_display, boolean isFinalSolution) {
        // ... (sama, tapi mungkin ingin menyesuaikan pesan jika isFinalSolution dan itu adalah firstFoundSolutionPath)
        System.out.println("\n=== MAP (HP: " + hp_info + ", Langkah ke/Jml Sel: " + steps_info_display + ") ===");
        if (isFinalSolution && firstFoundIterationId != -1) { // Cek firstFoundIterationId
            System.out.println("--- Solusi Pertama yang Mencapai Finish (pada Iterasi ID: " + firstFoundIterationId + ") ---");
        }
        // ... (sisa printMapWithOverlay sama) ...
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) System.out.print("+---");
            System.out.println("+");
            for (int j = 0; j < SIZE; j++) {
                char mapChar = baseMap[i][j];
                int pathMark = (pathOverlay != null) ? pathOverlay[i][j] : 0;
                if (mapChar == 'S' || mapChar == 'F' || mapChar == 'B' || mapChar == 'A' || mapChar == 'a' || mapChar == 'H' || mapChar == 'T') {
                    System.out.print("| " + mapChar + " ");
                } else if (pathMark != 0) {
                    System.out.printf("|%3d", pathMark);
                } else {
                    System.out.print("|   ");
                }
            }
            System.out.println("|");
        }
        for (int j = 0; j < SIZE; j++) System.out.print("+---");
        System.out.println("+");
    }
}