package org.delcom.ifs25005pbo2026springbootcashflow.domain.entity;

/**
 * Jenis transaksi keuangan.
 * Urutan deklarasi (INCOME lalu EXPENSE) dipakai oleh {@link SortOption}.
 */
public enum TransactionType {
    /** Pemasukan: menambah saldo. */
    INCOME,

    /** Pengeluaran: mengurangi saldo. */
    EXPENSE
}
