package org.delcom.ifs25005pbo2026springbootcashflow.domain.entity;

import java.util.Comparator;

/**
 * Kriteria pengurutan transaksi.
 * Setiap opsi membawa comparator-nya sendiri sehingga logika pengurutan
 * terpusat di domain, bukan tersebar sebagai angka ajaib di view/use case.
 */
public enum SortOption {
    /** Jumlah dari yang terkecil. */
    AMOUNT_ASC(Comparator.comparing(Transaction::getAmount)),

    /** Jumlah dari yang terbesar. */
    AMOUNT_DESC(Comparator.comparing(Transaction::getAmount).reversed()),

    /** Pemasukan ditampilkan terlebih dahulu. */
    INCOME_FIRST(Comparator.comparing(Transaction::getType)),

    /** Pengeluaran ditampilkan terlebih dahulu. */
    EXPENSE_FIRST(Comparator.comparing(Transaction::getType).reversed());

    /** Comparator yang digunakan untuk mengurutkan daftar transaksi. */
    private final Comparator<Transaction> comparator;

    SortOption(Comparator<Transaction> comparator) {
        this.comparator = comparator;
    }

    /** Mengembalikan comparator yang sesuai dengan opsi ini. */
    public Comparator<Transaction> comparator() {
        return comparator;
    }
}
