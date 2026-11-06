package org.delcom.ifs25005pbo2026springbootcashflow.usecase;

import org.delcom.ifs25005pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25005pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25005pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25005pbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Use case yang menangani logika bisnis pencatatan keuangan.
 * Tidak melakukan I/O (tanpa komponen UI / System.out); hanya memproses data
 * dan melempar checked exception domain jika operasi tidak valid.
 */
public class FinanceUseCase {
    /** Panjang maksimum keterangan transaksi. */
    static final int MAX_DESCRIPTION_LENGTH = 100;

    /** Jumlah maksimum satu transaksi. */
    static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999999.99");

    /** Port repository yang di-inject dari luar (Dependency Inversion). */
    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Mengambil semua transaksi yang tersedia. */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /** Menambahkan pemasukan baru setelah validasi keterangan dan jumlah. */
    public Transaction addIncome(String description, String amount) throws ValidationException {
        String cleanDescription = validateDescription(description);
        BigDecimal value = parseAmount(amount);
        return transactionRepository.saveIncome(cleanDescription, value);
    }

    /** Menambahkan pengeluaran baru setelah validasi keterangan dan jumlah. */
    public Transaction addExpense(String description, String amount) throws ValidationException {
        String cleanDescription = validateDescription(description);
        BigDecimal value = parseAmount(amount);
        return transactionRepository.saveExpense(cleanDescription, value);
    }

    /** Menghapus transaksi berdasarkan ID; melempar exception jika tidak ditemukan. */
    public void removeTransaction(int id) throws EntityNotFoundException {
        boolean deleted = transactionRepository.deleteById(id);
        if (!deleted) {
            throw new EntityNotFoundException(id);
        }
    }

    /** Mencari transaksi yang keterangannya mengandung kata kunci (case-insensitive). */
    public List<Transaction> searchTransactions(String keyword) {
        String lowerKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        return transactionRepository.findBy(
                transaction -> transaction.getDescription().toLowerCase().contains(lowerKeyword));
    }

    /** Mengurutkan transaksi sesuai kriteria {@link SortOption} yang dipilih. */
    public List<Transaction> sortTransactions(SortOption option) {
        return transactionRepository.findAll().stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Total seluruh pemasukan. */
    public BigDecimal getTotalIncome() {
        return transactionRepository.findAll().stream()
                .filter(Transaction::isIncome)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Total seluruh pengeluaran. */
    public BigDecimal getTotalExpense() {
        return transactionRepository.findAll().stream()
                .filter(transaction -> !transaction.isIncome())
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Saldo saat ini: total pemasukan dikurangi total pengeluaran. */
    public BigDecimal getBalance() {
        return transactionRepository.findAll().stream()
                .map(Transaction::signedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String validateDescription(String description) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        String clean = description.trim();
        if (clean.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException(
                    "Keterangan maksimal " + MAX_DESCRIPTION_LENGTH + " karakter");
        }
        return clean;
    }

    private BigDecimal parseAmount(String amount) throws ValidationException {
        if (amount == null || amount.trim().isEmpty()) {
            throw new ValidationException("Jumlah tidak boleh kosong");
        }
        BigDecimal value;
        try {
            value = new BigDecimal(amount.trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Jumlah harus berupa angka", ex);
        }
        if (value.signum() <= 0) {
            throw new ValidationException("Jumlah harus lebih besar dari 0");
        }
        if (value.compareTo(MAX_AMOUNT) > 0) {
            throw new ValidationException("Jumlah terlalu besar");
        }
        return value;
    }
}
