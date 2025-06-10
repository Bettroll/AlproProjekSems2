import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ProjekAlproGUI {
    // Game constants
    static final int UKURAN = 10, HP_AWAL = 10, HP_MAKS = 10;
    static final int[][] daftarBalok = {{1,3}, {4,2}, {7,8}};
    static final int[][] daftarApi = {{4,7}, {6,2}, {8,5}};
    static final int[][] daftarAir = {{4,8}, {7,3}, {2,7}};
    static final int[][] daftarHeal = {{6,4}, {3,5}, {2,1}};
    static final int[][] lokasiTeleport = {{0,7}, {9,3}};
    static final int[] deltaBaris = {-1, 1, 0, 0}, deltaKolom = {0, 0, -1, 1};
    static final int mulaiBaris = UKURAN-1, mulaiKolom = 0, akhirBaris = 0, akhirKolom = UKURAN-1; 
    
    // Game state
    static char[][] papanKarakter = new char[UKURAN][UKURAN];
    static int[][] solusiPertama = null;
    static int hpSolusiPertama, langkahSolusiPertama, iterasiSolusiPertama;
    static boolean solusiDitemukan, hentikanSemua, tampilLangkah;
    static int jedaMs = 500;
    
    // GUI components
    static JLabel[][] mapLabels = new JLabel[UKURAN][UKURAN];
    static JLabel hpLabel, langkahLabel, iterasiLabel;
    static JFrame frame;

    public static void main(String[] args) {
        buatPapan();
        
        // Setup main frame
        frame = new JFrame("TO The Finnish");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Main panel with menu and game area
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.DARK_GRAY);
        
        // Menu panel (left side)
        JPanel menuPanel = createMenuPanel();
        mainPanel.add(menuPanel, BorderLayout.WEST);
        
        // Game area (center)
        JPanel gamePanel = createGamePanel();
        mainPanel.add(gamePanel, BorderLayout.CENTER);
        
        // Info panel (right side)
        JPanel infoPanel = createInfoPanel();
        mainPanel.add(infoPanel, BorderLayout.EAST);
        
        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }
    
    private static JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(Color.BLACK);
        menuPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title label
        JLabel titleLabel = new JLabel("To the Finish");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 60));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(new EmptyBorder(0, 0, 40, 0));
        
        // Buttons
        JButton[] buttons = {
            createMenuButton("1. Tampilkan MAP Dasar"),
            createMenuButton("2. Proses Perjalanan (Animasi)"),
            createMenuButton("3. Langsung Hasil Akhir"),
            createMenuButton("0. Keluar")
        };
        
        // Button actions
        buttons[0].addActionListener(e -> showBaseMap());
        buttons[1].addActionListener(e -> startAnimatedSolution());
        buttons[2].addActionListener(e -> findImmediateSolution());
        buttons[3].addActionListener(e -> frame.dispose());
        buttons[3].setBackground(Color.RED);
        
        // Add components to menu panel
        menuPanel.add(titleLabel);
        for (JButton button : buttons) {
            menuPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            menuPanel.add(button);
        }
        
        return menuPanel;
    }
    
    private static JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(300, 60));
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setBackground(Color.GRAY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        return button;
    }
    
    private static JPanel createGamePanel() {
        JPanel gamePanel = new JPanel(new BorderLayout());
        gamePanel.setBackground(Color.BLACK);
        gamePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Map panel
        JPanel mapPanel = new JPanel(new GridLayout(UKURAN, UKURAN));
        mapPanel.setBackground(Color.BLACK);
        
        Font fontMap = new Font("Monospaced", Font.BOLD, 28);
        for (int i = 0; i < UKURAN; i++) {
            for (int j = 0; j < UKURAN; j++) {
                mapLabels[i][j] = new JLabel(" ", SwingConstants.CENTER);
                mapLabels[i][j].setOpaque(true);
                mapLabels[i][j].setBackground(Color.WHITE);
                mapLabels[i][j].setFont(fontMap);
                mapLabels[i][j].setBorder(BorderFactory.createLineBorder(Color.GRAY));
                mapPanel.add(mapLabels[i][j]);
            }
        }
        
        gamePanel.add(mapPanel, BorderLayout.CENTER);
        return gamePanel;
    }
    
    private static JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.DARK_GRAY);
        infoPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Legend title
        JLabel legendTitle = new JLabel("LEGENDA:");
        legendTitle.setForeground(Color.WHITE);
        legendTitle.setFont(new Font("Arial", Font.BOLD, 24));
        legendTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Legend items
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new GridLayout(0, 1, 5, 5));
        legendPanel.setBackground(Color.DARK_GRAY);
        legendPanel.setBorder(new EmptyBorder(20, 10, 20, 10));
        
        addLegendItem(legendPanel, "S (Start)", Color.GREEN);
        addLegendItem(legendPanel, "F (Finish)", Color.ORANGE);
        addLegendItem(legendPanel, "B (Balok)", Color.DARK_GRAY);
        addLegendItem(legendPanel, "A (Api)", Color.RED);
        addLegendItem(legendPanel, "a (Air)", Color.CYAN);
        addLegendItem(legendPanel, "H (Heal)", Color.PINK);
        addLegendItem(legendPanel, "T (Teleport)", Color.MAGENTA);
        addLegendItem(legendPanel, "Jalur", Color.YELLOW);
        
        // Status info
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(0, 1, 5, 5));
        statusPanel.setBackground(Color.DARK_GRAY);
        statusPanel.setBorder(new EmptyBorder(20, 10, 20, 10));
        
        hpLabel = createStatusLabel("HP: -");
        langkahLabel = createStatusLabel("Langkah: -");
        iterasiLabel = createStatusLabel("Iterasi: -");
        
        statusPanel.add(hpLabel);
        statusPanel.add(langkahLabel);
        statusPanel.add(iterasiLabel);
        
        // Add components to info panel
        infoPanel.add(legendTitle);
        infoPanel.add(legendPanel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        infoPanel.add(createStatusTitle("STATUS:"));
        infoPanel.add(statusPanel);
        
        return infoPanel;
    }
    
    private static void addLegendItem(JPanel panel, String text, Color color) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBackground(Color.DARK_GRAY);
        
        JLabel colorLabel = new JLabel("   ");
        colorLabel.setOpaque(true);
        colorLabel.setBackground(color);
        colorLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JLabel textLabel = new JLabel(text);
        textLabel.setForeground(Color.WHITE);
        textLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        
        itemPanel.add(colorLabel, BorderLayout.WEST);
        itemPanel.add(textLabel, BorderLayout.CENTER);
        itemPanel.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
        
        panel.add(itemPanel);
    }
    
    private static JLabel createStatusTitle(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }
    
    private static JLabel createStatusLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.PLAIN, 18));
        return label;
    }
    
    private static void showBaseMap() {
        resetSolusi();
        updateMapPanel(papanKarakter, null);
        updateStatusLabels(-1, -1, -1);
        JOptionPane.showMessageDialog(frame, "Map dasar telah ditampilkan.");
    }
    
    private static void startAnimatedSolution() {
        resetSolusi();
        tampilLangkah = true;
        String jedaStr = JOptionPane.showInputDialog(frame, "Masukkan jeda antar langkah (ms), misal 500:", "500");
        try { jedaMs = Integer.parseInt(jedaStr); } catch (Exception ex) { jedaMs = 500; }
        
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                findPath(0, new int[UKURAN][UKURAN], mulaiBaris, mulaiKolom, HP_AWAL, 1, papanKarakter[mulaiBaris][mulaiKolom]);
                return null;
            }
            
            @Override
            protected void done() {
                if (solusiDitemukan && solusiPertama != null) {
                    updateMapPanel(papanKarakter, solusiPertama);
                    updateStatusLabels(hpSolusiPertama, langkahSolusiPertama, iterasiSolusiPertama);
                } else {
                    JOptionPane.showMessageDialog(frame, "Tidak ada solusi yang ditemukan (tidak mencapai Finish).");
                }
                tampilLangkah = false;
            }
        }.execute();
    }
    
    private static void findImmediateSolution() {
        resetSolusi();
        tampilLangkah = false;
        findPath(0, new int[UKURAN][UKURAN], mulaiBaris, mulaiKolom, HP_AWAL, 1, papanKarakter[mulaiBaris][mulaiKolom]);
        if (solusiDitemukan && solusiPertama != null) {
            updateMapPanel(papanKarakter, solusiPertama);
            updateStatusLabels(hpSolusiPertama, langkahSolusiPertama, iterasiSolusiPertama);
        } else {
            JOptionPane.showMessageDialog(frame, "Tidak ada solusi yang ditemukan (tidak mencapai Finish).");
        }
    }
    
    private static void updateStatusLabels(int hp, int langkah, int iterasi) {
        hpLabel.setText("HP: " + (hp >= 0 ? hp : "-"));
        langkahLabel.setText("Langkah: " + (langkah >= 0 ? langkah : "-"));
        iterasiLabel.setText("Iterasi: " + (iterasi >= 0 ? iterasi : "-"));
    }
    
    static void buatPapan() {
        for (char[] row : papanKarakter) Arrays.fill(row, '-');
        papanKarakter[mulaiBaris][mulaiKolom] = 'S';
        papanKarakter[akhirBaris][akhirKolom] = 'F';
        for (int[] b : daftarBalok) papanKarakter[b[0]][b[1]] = 'B';
        for (int[] a : daftarApi) papanKarakter[a[0]][a[1]] = 'A';
        for (int[] w : daftarAir) papanKarakter[w[0]][w[1]] = 'a';
        for (int[] h : daftarHeal) papanKarakter[h[0]][h[1]] = 'H';
        for (int[] t : lokasiTeleport) papanKarakter[t[0]][t[1]] = 'T';
    }
    
    static void resetSolusi() {
        solusiPertama = null;
        hpSolusiPertama = langkahSolusiPertama = iterasiSolusiPertama = -1;
        solusiDitemukan = hentikanSemua = false;
        updateStatusLabels(-1, -1, -1);
    }
    
    static int[][] copyPapan(int[][] papan) {
        int[][] baru = new int[UKURAN][UKURAN];
        for (int i = 0; i < UKURAN; i++) baru[i] = Arrays.copyOf(papan[i], UKURAN);
        return baru;
    }
    
    static boolean aman(int baris, int kolom, int[][] papan, int iterasi) {
        return baris >= 0 && baris < UKURAN && kolom >= 0 && kolom < UKURAN &&
               papanKarakter[baris][kolom] != 'B' && papan[baris][kolom] != iterasi;
    }
    
    static int hitungLangkah(int[][] papan) {
        int jumlah = 0;
        for (int[] row : papan)
            for (int v : row) if (v != 0) jumlah++;
        return jumlah;
    }
    
    static void findPath(int depth, int[][] papanSebelum, int baris, int kolom, int hp, int iterasi, char prevChar) {
        if (hentikanSemua) return;

        int[][] papan = copyPapan(papanSebelum);
        papan[baris][kolom] = iterasi;

        if (tampilLangkah) {
            SwingUtilities.invokeLater(() -> {
                updateMapPanel(papanKarakter, papan);
                updateStatusLabels(hp, hitungLangkah(papan), iterasi);
            });
            try { Thread.sleep(jedaMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        if (hentikanSemua) return;

        if (baris == akhirBaris && kolom == akhirKolom) {
            solusiDitemukan = true;
            int langkah = hitungLangkah(papan);
            if (solusiPertama == null) {
                hpSolusiPertama = hp;
                langkahSolusiPertama = langkah;
                solusiPertama = copyPapan(papan);
                iterasiSolusiPertama = iterasi;
                hentikanSemua = true;
            }
            return;
        }

        if (iterasi > UKURAN*UKURAN*10) return;

        List<Integer> urutanArah = Arrays.asList(0,1,2,3);
        Collections.shuffle(urutanArah);
        boolean sudahBergerak = false;

        for (int idx : urutanArah) {
            int nb = baris + deltaBaris[idx], nk = kolom + deltaKolom[idx];
            if (!aman(nb, nk, papan, iterasi)) continue;
            sudahBergerak = true;
            int nextHp = hp;
            char now = papanKarakter[baris][kolom], tujuan = papanKarakter[nb][nk];
            int nb2 = nb, nk2 = nk;
            boolean teleport = false;

            if (tujuan == 'a') nextHp--;
            else if (tujuan == 'A') nextHp -= 2;
            else if (tujuan == 'H') nextHp = Math.min(nextHp+1, HP_MAKS);
            else if (tujuan == 'T') {
                if (nb == lokasiTeleport[0][0] && nk == lokasiTeleport[0][1]) {
                    nb2 = lokasiTeleport[1][0]; nk2 = lokasiTeleport[1][1];
                } else {
                    nb2 = lokasiTeleport[0][0]; nk2 = lokasiTeleport[0][1];
                }
                teleport = true; now = 'T';
            }
            
            if (!teleport && ((now == 'A' && tujuan == 'a') || (now == 'a' && tujuan == 'A'))) nextHp = 0;

            if (nextHp <= 0) {
                int[][] papanBaru = copyPapan(papan);
                papanBaru[nb][nk] = iterasi;
                if (teleport) papanBaru[nb2][nk2] = iterasi;
                findPath(0, papanBaru, teleport ? nb2 : nb, teleport ? nk2 : nk, HP_AWAL, iterasi+1, papanKarakter[teleport ? nb2 : nb][teleport ? nk2 : nk]);
                return;
            } else {
                findPath(depth+1, papan, teleport ? nb2 : nb, teleport ? nk2 : nk, nextHp, iterasi, now);
            }
        }

        if (!sudahBergerak && !(baris == akhirBaris && kolom == akhirKolom)) {
            findPath(0, papan, baris, kolom, hp, iterasi+1, papanKarakter[baris][kolom]);
        }
    }
    
    public static void updateMapPanel(char[][] papanDasar, int[][] overlayJalur) {
        for (int i = 0; i < UKURAN; i++) {
            for (int j = 0; j < UKURAN; j++) {
                char c = papanDasar[i][j];
                int jalur = (overlayJalur != null) ? overlayJalur[i][j] : 0;
                
                if (c == 'S') {
                    mapLabels[i][j].setText("S");
                    mapLabels[i][j].setBackground(Color.GREEN);
                } else if (c == 'F') {
                    mapLabels[i][j].setText("F");
                    mapLabels[i][j].setBackground(Color.ORANGE);
                } else if (c == 'B') {
                    mapLabels[i][j].setText("B");
                    mapLabels[i][j].setBackground(Color.DARK_GRAY);
                } else if (c == 'A') {
                    mapLabels[i][j].setText("A");
                    mapLabels[i][j].setBackground(Color.RED);
                } else if (c == 'a') {
                    mapLabels[i][j].setText("a");
                    mapLabels[i][j].setBackground(Color.CYAN);
                } else if (c == 'H') {
                    mapLabels[i][j].setText("H");
                    mapLabels[i][j].setBackground(Color.PINK);
                } else if (c == 'T') {
                    mapLabels[i][j].setText("T");
                    mapLabels[i][j].setBackground(Color.MAGENTA);
                } else if (jalur != 0) {
                    mapLabels[i][j].setText("" + jalur);
                    mapLabels[i][j].setBackground(Color.YELLOW);
                } else {
                    mapLabels[i][j].setText("");
                    mapLabels[i][j].setBackground(Color.WHITE);
                }
            }
        }
    }
}