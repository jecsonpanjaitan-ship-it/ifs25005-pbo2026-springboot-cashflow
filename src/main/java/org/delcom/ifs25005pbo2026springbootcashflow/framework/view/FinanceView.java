package org.delcom.ifs25005pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Section;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25005pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25005pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25005pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25005pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25005pbo2026springbootcashflow.usecase.FinanceUseCase;

import java.util.Comparator;
import java.util.List;

/**
 * Tampilan web Vaadin aplikasi Cashflow.
 * Halaman memakai landmark semantik (main, header, section, footer), satu H1,
 * label pada semua kolom input, dan warna dengan kontras sesuai WCAG AA.
 */
@Route("")
@PageTitle("Cashflow - Catatan Keuangan Pribadi")
public class FinanceView extends Main {

    private static final String TEXT_DARK = "#111827";
    private static final String TEXT_MUTED = "#374151";
    private static final String GREEN = "#15803D";
    private static final String RED = "#B91C1C";
    private static final String BLUE = "#1D4ED8";

    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField("Cari keterangan");
    private final RadioButtonGroup<SortOption> sortGroup = new RadioButtonGroup<>("Urutkan");
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span();
    private final Span totalCount = new Span("0");
    private final Span totalIncome = new Span();
    private final Span totalExpense = new Span();
    private final Span balance = new Span();

    private String keyword = "";

    public FinanceView(FinanceUseCase financeUseCase, FinancePresenter presenter) {
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;

        getStyle()
                .set("display", "block")
                .set("box-sizing", "border-box")
                .set("max-width", "1200px")
                .set("margin", "0 auto")
                .set("padding", "1rem")
                .set("color", TEXT_DARK);

        statusLabel.setId("status-label");
        balance.setId("balance-value");
        configureGrid();
        configureToolbar();

        add(buildHeader(), buildSummary(), buildToolbar(), buildListSection(), buildFooter());
        refreshAll();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        attachEvent.getUI().getPage().executeJs(
                "document.documentElement.lang = 'id';"
                        + "const shell = document.getElementById('initial-shell');"
                        + "if (shell) { shell.remove(); }");
    }

    // ------------------------------------------------------------------ layout

    private Header buildHeader() {
        H1 title = new H1("Cashflow");
        title.getStyle().set("margin", "0").set("font-size", "2rem").set("color", TEXT_DARK);
        Paragraph subtitle = new Paragraph(
                "Catatan keuangan pribadi: pantau pemasukan, pengeluaran, dan saldo Anda.");
        subtitle.getStyle().set("margin", "0.25rem 0 1rem").set("color", TEXT_MUTED);
        return new Header(title, subtitle);
    }

    private Section buildSummary() {
        Div cards = new Div(
                createSummaryCard("Total Transaksi", totalCount, "#DBEAFE", "#1E3A8A", BLUE),
                createSummaryCard("Total Pemasukan", totalIncome, "#DCFCE7", "#14532D", GREEN),
                createSummaryCard("Total Pengeluaran", totalExpense, "#FEE2E2", "#7F1D1D", RED),
                createSummaryCard("Saldo Saat Ini", balance, "#FEF3C7", "#78350F", "#B45309")
        );
        cards.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(210px, 1fr))")
                .set("gap", "1rem");
        Section section = new Section(cards);
        section.getElement().setAttribute("aria-label", "Ringkasan keuangan");
        return section;
    }

    private Div createSummaryCard(String label, Span value, String background, String textColor,
                                  String accent) {
        Span caption = new Span(label);
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.9rem")
                .set("font-weight", "600")
                .set("color", textColor);

        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.35rem")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("line-height", "1.2")
                .set("overflow-wrap", "anywhere")
                .set("color", textColor);

        Div card = new Div(caption, value);
        card.getStyle()
                .set("background", background)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem")
                .set("min-height", "5rem")
                .set("box-sizing", "border-box");
        return card;
    }

    private Section buildToolbar() {
        Button searchButton = createButton("Cari", VaadinIcon.SEARCH, "#E5E7EB", TEXT_DARK, event -> searchTransactions());
        Button refreshButton = createButton("Muat Ulang", VaadinIcon.REFRESH, "#E5E7EB", TEXT_DARK, event -> refreshAll());
        Button incomeButton = createButton("Tambah Pemasukan", VaadinIcon.PLUS, GREEN, "#FFFFFF",
                event -> openAddDialog(TransactionType.INCOME));
        Button expenseButton = createButton("Tambah Pengeluaran", VaadinIcon.MINUS, RED, "#FFFFFF",
                event -> openAddDialog(TransactionType.EXPENSE));

        Div searchRow = new Div(searchField, searchButton, refreshButton);
        searchRow.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("align-items", "flex-end")
                .set("gap", "0.75rem")
                .set("flex", "1 1 380px");

        Div addRow = new Div(incomeButton, expenseButton);
        addRow.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("align-items", "flex-end")
                .set("gap", "0.75rem");

        Div row = new Div(searchRow, addRow);
        row.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("justify-content", "space-between")
                .set("gap", "1rem")
                .set("margin-top", "1.25rem");

        Div sortRow = new Div(sortGroup);
        sortRow.getStyle().set("margin-top", "0.75rem");

        Section section = new Section(row, sortRow);
        section.getElement().setAttribute("aria-label", "Pencarian, pengurutan, dan tambah transaksi");
        return section;
    }

    private Section buildListSection() {
        H2 heading = new H2("Daftar Transaksi");
        heading.getStyle().set("font-size", "1.35rem").set("margin", "1.5rem 0 0.25rem").set("color", TEXT_DARK);
        statusLabel.getStyle().set("margin", "0 0 0.75rem").set("color", TEXT_MUTED);
        return new Section(heading, statusLabel, grid);
    }

    private Footer buildFooter() {
        Paragraph note = new Paragraph(
                "Data disimpan sementara di memori dan akan hilang saat aplikasi di-restart.");
        note.getStyle().set("margin", "1rem 0 0").set("font-size", "0.875rem").set("color", "#4B5563");
        return new Footer(note);
    }

    private Button createButton(String text, VaadinIcon vaadinIcon, String background, String color,
                                ComponentEventListener<ClickEvent<Button>> listener) {
        Icon icon = vaadinIcon.create();
        icon.getElement().setAttribute("aria-hidden", "true");
        Button button = new Button(text, icon, listener);
        styleButton(button, background, color);
        return button;
    }

    private void styleButton(Button button, String background, String color) {
        button.getStyle()
                .set("background", background)
                .set("color", color)
                .set("min-height", "44px")
                .set("min-width", "44px")
                .set("cursor", "pointer");
    }

    // -------------------------------------------------------------------- grid

    private void configureGrid() {
        grid.addColumn(Transaction::getId)
                .setHeader("ID")
                .setKey("id")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Transaction::getId);

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setKey("description")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        grid.addColumn(transaction -> presenter.formatMoney(transaction.getAmount()))
                .setHeader("Jumlah")
                .setKey("amount")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.END)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getAmount));

        grid.addColumn(transaction -> presenter.formatType(transaction.getType()))
                .setHeader("Tipe")
                .setKey("type")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getType));

        grid.addColumn(presenter::formatCreatedAt)
                .setHeader("Waktu")
                .setKey("createdAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Tindakan")
                .setKey("tindakan")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        emptyState.getStyle().set("color", TEXT_MUTED);
        grid.setEmptyStateComponent(emptyState);
        grid.setAllRowsVisible(true);
        grid.getElement().setAttribute("aria-label", "Daftar transaksi keuangan");
    }

    private Div createActionButtons(Transaction transaction) {
        Icon icon = VaadinIcon.TRASH.create();
        icon.getElement().setAttribute("aria-hidden", "true");
        Button deleteButton = new Button("Hapus", icon, event -> openDeleteDialog(transaction));
        deleteButton.getElement().setAttribute("aria-label", "Hapus transaksi " + transaction.getDescription());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        styleButton(deleteButton, "transparent", RED);
        return new Div(deleteButton);
    }

    // ----------------------------------------------------------------- toolbar

    private void configureToolbar() {
        searchField.setPlaceholder("Contoh: gaji");
        searchField.setPrefixComponent(hiddenIcon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(event -> searchTransactions());
        searchField.getStyle().set("flex", "1 1 220px");

        sortGroup.setItems(SortOption.values());
        sortGroup.setItemLabelGenerator(presenter::formatSortOption);
        sortGroup.addValueChangeListener(event -> reloadGrid());
    }

    private Icon hiddenIcon(VaadinIcon vaadinIcon) {
        Icon icon = vaadinIcon.create();
        icon.getElement().setAttribute("aria-hidden", "true");
        return icon;
    }

    // ----------------------------------------------------------------- dialogs

    private void openAddDialog(TransactionType type) {
        boolean income = type == TransactionType.INCOME;

        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setWidthFull();
        descriptionField.setMaxLength(100);

        TextField amountField = new TextField("Jumlah (Rp)");
        amountField.setWidthFull();
        amountField.setHelperText("Contoh: 1000000 atau 1500000.50");

        Div body = new Div(descriptionField, amountField);
        body.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "0.5rem");

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(income ? "Tambah Pemasukan" : "Tambah Pengeluaran");
        dialog.setWidth("500px");
        dialog.setMaxWidth("95vw");

        Button saveButton = createButton("Simpan", VaadinIcon.CHECK, income ? GREEN : RED, "#FFFFFF", event -> {
            try {
                Transaction created = income
                        ? financeUseCase.addIncome(descriptionField.getValue(), amountField.getValue())
                        : financeUseCase.addExpense(descriptionField.getValue(), amountField.getValue());
                showSuccess(presenter.showAddSuccess(created) + " | "
                        + presenter.formatBalance(financeUseCase.getBalance()));
                dialog.close();
                refreshAll();
            } catch (ValidationException ex) {
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        Button cancelButton = createButton("Batal", VaadinIcon.CLOSE, "#E5E7EB", TEXT_DARK, event -> dialog.close());
        dialog.add(body, buildDialogActions(cancelButton, saveButton));
        dialog.open();
    }

    private void openDeleteDialog(Transaction transaction) {
        Paragraph message = new Paragraph(
                "Yakin ingin menghapus transaksi \"" + transaction.getDescription()
                        + "\" (ID " + transaction.getId() + ")?");
        message.getStyle().set("margin", "0");

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Menghapus Transaksi");
        dialog.setWidth("500px");
        dialog.setMaxWidth("95vw");

        Button confirmButton = createButton("Hapus", VaadinIcon.TRASH, RED, "#FFFFFF", event -> {
            deleteTransaction(transaction);
            dialog.close();
        });
        Button cancelButton = createButton("Batal", VaadinIcon.CLOSE, "#E5E7EB", TEXT_DARK, event -> dialog.close());
        dialog.add(message, buildDialogActions(cancelButton, confirmButton));
        dialog.open();
    }

    private Div buildDialogActions(Button... buttons) {
        Div actions = new Div(buttons);
        actions.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("justify-content", "flex-end")
                .set("gap", "0.75rem")
                .set("margin-top", "1.25rem");
        return actions;
    }

    // ----------------------------------------------------------------- actions

    private void deleteTransaction(Transaction transaction) {
        try {
            financeUseCase.removeTransaction(transaction.getId());
            showSuccess(presenter.showRemoveSuccess());
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
        }
        refreshAll();
    }

    private void searchTransactions() {
        keyword = searchField.getValue().trim();
        reloadGrid();
    }

    private void refreshAll() {
        keyword = "";
        searchField.clear();
        sortGroup.clear();
        reloadGrid();
    }

    /** Memuat ulang grid berdasarkan kata kunci dan opsi pengurutan saat ini. */
    private void reloadGrid() {
        boolean searching = !keyword.isEmpty();
        List<Transaction> items = searching
                ? financeUseCase.searchTransactions(keyword)
                : financeUseCase.getAllTransactions();

        SortOption option = sortGroup.getValue();
        if (option != null) {
            items = items.stream().sorted(option.comparator()).toList();
        }

        emptyState.setText(searching ? presenter.showSearchEmpty() : presenter.showEmpty());
        grid.setItems(items);
        updateSummary();

        String title = searching ? presenter.showSearchTitle(keyword) : presenter.showListTitle();
        statusLabel.setText(items.isEmpty() ? title + " " + emptyState.getText() : title);
    }

    private void updateSummary() {
        totalCount.setText(String.valueOf(financeUseCase.getAllTransactions().size()));
        totalIncome.setText(presenter.formatMoney(financeUseCase.getTotalIncome()));
        totalExpense.setText(presenter.formatMoney(financeUseCase.getTotalExpense()));
        balance.setText(presenter.formatMoney(financeUseCase.getBalance()));
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}