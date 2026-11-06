package org.delcom.ifs25005pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25005pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25005pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005pbo2026springbootcashflow.domain.entity.TransactionType;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * Presenter yang memformat data dari use case menjadi teks siap tampil.
 * Memisahkan logika tampilan dari entity, use case, dan view.
 */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Map<SortOption, String> SORT_LABELS = Map.of(
            SortOption.AMOUNT_ASC, "Jumlah terkecil",
            SortOption.AMOUNT_DESC, "Jumlah terbesar",
            SortOption.INCOME_FIRST, "Pemasukan dulu",
            SortOption.EXPENSE_FIRST, "Pengeluaran dulu"
    );

    /** Memformat uang dengan pemisah ribuan dan dua desimal, contoh: Rp 1,000,000.00. */
    public String formatMoney(BigDecimal amount) {
        return String.format(Locale.US, "Rp %,.2f", amount);
    }

    /** Label tampilan untuk jenis transaksi. */
    public String formatType(TransactionType type) {
        return type == TransactionType.INCOME ? "Pemasukan" : "Pengeluaran";
    }

    /** Memformat satu transaksi menjadi baris teks. */
    public String format(Transaction transaction) {
        return String.format(
                Locale.US,
                "[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatMoney(transaction.getAmount()),
                formatType(transaction.getType())
        );
    }

    /** Memformat waktu pencatatan transaksi. */
    public String formatCreatedAt(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Memformat saldo. */
    public String formatBalance(BigDecimal balance) {
        return "Saldo: " + formatMoney(balance);
    }

    /** Label tampilan untuk opsi pengurutan. */
    public String formatSortOption(SortOption option) {
        return SORT_LABELS.get(option);
    }

    /** Judul daftar saat menampilkan semua transaksi. */
    public String showListTitle() {
        return "Daftar Transaksi";
    }

    /** Judul daftar saat menampilkan hasil pencarian. */
    public String showSearchTitle(String keyword) {
        return "Hasil Pencarian: \"" + keyword + "\"";
    }

    /** Pesan saat belum ada transaksi. */
    public String showEmpty() {
        return "- Data transaksi belum tersedia!";
    }

    /** Pesan saat pencarian tidak menemukan transaksi. */
    public String showSearchEmpty() {
        return "- Transaksi tidak ditemukan!";
    }

    /** Pesan sukses setelah menambah transaksi. */
    public String showAddSuccess(Transaction transaction) {
        return "Berhasil menambah transaksi: " + format(transaction);
    }

    /** Pesan sukses setelah menghapus transaksi. */
    public String showRemoveSuccess() {
        return "Berhasil menghapus transaksi.";
    }

    /** Pesan gagal saat menghapus transaksi. */
    public String showRemoveFailed(int id) {
        return String.format(Locale.US, "[!] Gagal menghapus transaksi dengan ID: %d.", id);
    }

    /** Pesan validasi dari domain. */
    public String showValidationError(String message) {
        return "[!] " + message;
    }
}
