package org.delcom.ifs25005pbo2026springbootcashflow.domain.repository;

import org.delcom.ifs25005pbo2026springbootcashflow.domain.entity.Transaction;

import java.math.BigDecimal;

/**
 * Port spesifik transaksi yang memperluas generic {@link IRepository}.
 * Menambahkan pembuatan transaksi baru dengan penomoran ID otomatis.
 */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {
    /** Menyimpan transaksi pemasukan baru dan mengembalikannya lengkap dengan ID. */
    Transaction saveIncome(String description, BigDecimal amount);

    /** Menyimpan transaksi pengeluaran baru dan mengembalikannya lengkap dengan ID. */
    Transaction saveExpense(String description, BigDecimal amount);
}
