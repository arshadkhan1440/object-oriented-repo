package com.foodordering.ui;

import com.foodordering.model.CartItem;
import com.foodordering.model.Customer;
import com.foodordering.model.MenuItem;
import com.foodordering.model.Order;
import com.foodordering.model.OrderSummary;
import com.foodordering.repository.MenuRepository;
import com.foodordering.repository.OrderRepository;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodOrderingFrame extends JFrame {
    private static final Color GREEN = new Color(39, 112, 78);
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final List<MenuItem> menuItems;
    private final List<CartItem> cartItems = new ArrayList<>();
    private final DefaultTableModel menuModel = model("ID", "Food", "Category", "Description", "Price");
    private final DefaultTableModel cartModel = model("Food", "Quantity", "Unit price", "Subtotal");
    private final JTable menuTable = new JTable(menuModel);
    private final JTable cartTable = new JTable(cartModel);
    private final JTextField nameField = new JTextField(14);
    private final JTextField emailField = new JTextField(18);
    private final JTextField phoneField = new JTextField(12);
    private final JTextField addressField = new JTextField(30);
    private final JLabel totalLabel = new JLabel("Total: ₹0.00");
    private JPanel rootPanel;
    private JButton themeButton;
    private boolean darkTheme;

    public FoodOrderingFrame(MenuRepository menuRepository, OrderRepository orderRepository) throws SQLException {
        super("FoodOrder | Online Food Ordering");
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
        this.menuItems = menuRepository.findAvailableItems();
        buildWindow();
        refreshMenu();
        refreshCart();
    }

    private void buildWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(920, 680));
        setSize(1080, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(14, 14));

        rootPanel = new JPanel(new BorderLayout(14, 14));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        setContentPane(rootPanel);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("FoodOrder");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(GREEN);
        JLabel subtitle = new JLabel("Choose your food, add it to your cart, and place your order.");
        subtitle.setForeground(new Color(90, 100, 94));
        JPanel titleBlock = new JPanel(new BorderLayout(0, 4));
        titleBlock.setOpaque(false);
        titleBlock.add(title, BorderLayout.NORTH);
        titleBlock.add(subtitle, BorderLayout.SOUTH);
        JButton historyButton = new JButton("View My Orders");
        historyButton.addActionListener(event -> showOrderHistory());
        themeButton = new JButton("Dark theme");
        themeButton.addActionListener(event -> {
            darkTheme = !darkTheme;
            themeButton.setText(darkTheme ? "White theme" : "Dark theme");
            applyTheme();
        });
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerActions.setOpaque(false);
        headerActions.add(themeButton);
        headerActions.add(historyButton);
        header.add(titleBlock, BorderLayout.WEST);
        header.add(headerActions, BorderLayout.EAST);
        rootPanel.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 0.55;
        c.fill = GridBagConstraints.BOTH; c.insets = new Insets(0, 0, 12, 0);
        center.add(makeMenuPanel(), c);
        c.gridy = 1; c.weighty = 0.45; c.insets = new Insets(0, 0, 0, 0);
        center.add(makeCartPanel(), c);
        rootPanel.add(center, BorderLayout.CENTER);
        rootPanel.add(makeCheckoutPanel(), BorderLayout.SOUTH);
        applyTheme();
    }

    private JPanel makeMenuPanel() {
        JPanel panel = cardPanel("Available menu");
        configureTable(menuTable);
        menuTable.getColumnModel().getColumn(0).setPreferredWidth(42);
        menuTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        menuTable.getColumnModel().getColumn(3).setPreferredWidth(260);
        menuTable.setSelectionBackground(new Color(205, 235, 214));
        panel.add(new JScrollPane(menuTable), BorderLayout.CENTER);

        JButton addButton = new JButton("ADD SELECTED TO CART");
        stylePrimary(addButton);
        addButton.setPreferredSize(new Dimension(210, 40));
        addButton.addActionListener(event -> addSelectedMenuItem());
        JPanel actions = new JPanel(new BorderLayout(12, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 231, 226)),
                BorderFactory.createEmptyBorder(8, 4, 2, 4)));
        JLabel hint = new JLabel("Select a food row, then add it to your cart:");
        hint.setForeground(new Color(75, 87, 79));
        actions.add(hint, BorderLayout.CENTER);
        actions.add(addButton, BorderLayout.EAST);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel makeCartPanel() {
        JPanel panel = cardPanel("Your cart");
        configureTable(cartTable);
        panel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JButton increaseButton = new JButton("+ Quantity");
        JButton decreaseButton = new JButton("− Quantity");
        JButton removeButton = new JButton("Remove item");
        increaseButton.addActionListener(event -> changeQuantity(1));
        decreaseButton.addActionListener(event -> changeQuantity(-1));
        removeButton.addActionListener(event -> removeSelectedCartItem());
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        totalLabel.setForeground(GREEN);

        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 8));
        buttons.setOpaque(false);
        buttons.add(increaseButton);
        buttons.add(decreaseButton);
        buttons.add(removeButton);
        actions.add(buttons, BorderLayout.WEST);
        actions.add(totalLabel, BorderLayout.EAST);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel makeCheckoutPanel() {
        JPanel panel = cardPanel("Customer and delivery details");
        JPanel details = new JPanel(new GridBagLayout());
        details.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 5, 4, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0; details.add(new JLabel("Name *"), c);
        c.gridx = 1; details.add(nameField, c);
        c.gridx = 2; details.add(new JLabel("Email *"), c);
        c.gridx = 3; details.add(emailField, c);
        c.gridx = 0; c.gridy = 1; details.add(new JLabel("Phone"), c);
        c.gridx = 1; details.add(phoneField, c);
        c.gridx = 2; details.add(new JLabel("Delivery address *"), c);
        c.gridx = 3; c.weightx = 1; details.add(addressField, c);

        JButton placeOrderButton = new JButton("Place order");
        stylePrimary(placeOrderButton);
        placeOrderButton.addActionListener(event -> placeOrder());
        c.gridx = 4; c.gridy = 0; c.gridheight = 2; c.weightx = 0; c.insets = new Insets(4, 12, 4, 4);
        details.add(placeOrderButton, c);
        panel.add(details, BorderLayout.CENTER);
        return panel;
    }

    private void addSelectedMenuItem() {
        if (menuItems.isEmpty()) {
            showMessage("The database menu is empty. Add food rows to menu_items, then restart the app.",
                    "No menu items", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int row = menuTable.getSelectedRow();
        if (row < 0) {
            showMessage("Select a food item from the menu first.", "Choose an item", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int itemId = (int) menuModel.getValueAt(menuTable.convertRowIndexToModel(row), 0);
        MenuItem item = menuItems.stream().filter(candidate -> candidate.getId() == itemId).findFirst().orElse(null);
        if (item == null) return;

        String quantityText = JOptionPane.showInputDialog(this, "Quantity for " + item.getName() + ":", "1");
        if (quantityText == null) return;
        try {
            int quantity = Integer.parseInt(quantityText.trim());
            if (quantity < 1 || quantity > 99) throw new NumberFormatException();
            CartItem existing = cartItems.stream()
                    .filter(cartItem -> cartItem.getMenuItem().getId() == item.getId()).findFirst().orElse(null);
            if (existing == null) cartItems.add(new CartItem(item, quantity));
            else existing.setQuantity(existing.getQuantity() + quantity);
            refreshCart();
        } catch (NumberFormatException exception) {
            showMessage("Enter a whole-number quantity from 1 to 99.", "Invalid quantity", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void changeQuantity(int change) {
        int row = selectedCartRow();
        if (row < 0) return;
        CartItem item = cartItems.get(row);
        int quantity = item.getQuantity() + change;
        if (quantity <= 0) cartItems.remove(row);
        else item.setQuantity(quantity);
        refreshCart();
    }

    private void removeSelectedCartItem() {
        int row = selectedCartRow();
        if (row >= 0) {
            cartItems.remove(row);
            refreshCart();
        }
    }

    private int selectedCartRow() {
        if (cartItems.isEmpty()) {
            showMessage("Your cart is empty.", "Cart is empty", JOptionPane.INFORMATION_MESSAGE);
            return -1;
        }
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            showMessage("Select an item in your cart first.", "Choose an item", JOptionPane.INFORMATION_MESSAGE);
            return -1;
        }
        return cartTable.convertRowIndexToModel(row);
    }

    private void placeOrder() {
        if (cartItems.isEmpty()) {
            showMessage("Add at least one item to your cart.", "Cart is empty", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        if (name.isEmpty() || email.isEmpty() || address.isEmpty()) {
            showMessage("Name, email, and delivery address are required.", "Missing details", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            showMessage("Enter a valid email address.", "Invalid email", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Place this order for " + CURRENCY.format(cartTotal()) + "?",
                "Confirm order", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        try {
            BigDecimal placedTotal = cartTotal();
            Customer customer = new Customer(name, email, phone, address);
            Order order = new Order(new ArrayList<>(cartItems));
            int orderId = orderRepository.placeOrder(customer, order);
            cartItems.clear();
            refreshCart();
            showMessage("Your order was saved successfully.\nOrder ID: " + orderId
                    + "\nStatus: PLACED\nTotal: " + CURRENCY.format(placedTotal),
                    "Order placed", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException exception) {
            showMessage("The order could not be saved. No partial order was kept.\n" + exception.getMessage(),
                    "Order error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showOrderHistory() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            email = JOptionPane.showInputDialog(this, "Enter the email used for your orders:", "View My Orders",
                    JOptionPane.QUESTION_MESSAGE);
            if (email == null) return;
            email = email.trim();
        }
        if (email.isEmpty()) return;
        try {
            List<OrderSummary> orders = orderRepository.findOrdersByEmail(email);
            if (orders.isEmpty()) {
                showMessage("No orders were found for that email address.", "Order history", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            DefaultTableModel history = model("Order ID", "Date", "Status", "Total");
            for (OrderSummary order : orders) {
                history.addRow(new Object[]{order.getId(), order.getDate() == null ? "" : DATE_FORMAT.format(order.getDate()),
                        order.getStatus(), CURRENCY.format(order.getTotal())});
            }
            JTable table = new JTable(history);
            configureTable(table);
            JDialog dialog = new JDialog(this, "Order history", true);
            dialog.add(new JScrollPane(table));
            dialog.setSize(650, 320);
            dialog.setLocationRelativeTo(this);
            applyThemeTo(dialog.getContentPane());
            dialog.setVisible(true);
        } catch (SQLException exception) {
            showMessage("Could not load order history.\n" + exception.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshMenu() {
        menuModel.setRowCount(0);
        for (MenuItem item : menuItems) {
            menuModel.addRow(new Object[]{item.getId(), item.getName(), item.getCategory(),
                    item.getDescription(), CURRENCY.format(item.getPrice())});
        }
        if (menuItems.isEmpty()) {
            menuModel.addRow(new Object[]{"", "No available items", "", "Add items to menu_items in MySQL", ""});
        }
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        BigDecimal total = cartTotal();
        for (CartItem item : cartItems) {
            cartModel.addRow(new Object[]{item.getMenuItem().getName(), item.getQuantity(),
                    CURRENCY.format(item.getMenuItem().getPrice()), CURRENCY.format(item.getSubtotal())});
        }
        if (cartItems.isEmpty()) cartModel.addRow(new Object[]{"Cart is empty", "", "", ""});
        totalLabel.setText("Total: " + CURRENCY.format(total));
    }

    private BigDecimal cartTotal() {
        return cartItems.stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private static JPanel cardPanel(String heading) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 231, 226)),
                BorderFactory.createEmptyBorder(10, 12, 6, 12)));
        JLabel label = new JLabel(heading);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setForeground(new Color(45, 58, 49));
        panel.add(label, BorderLayout.NORTH);
        return panel;
    }

    private static void configureTable(JTable table) {
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
    }

    private static void stylePrimary(JButton button) {
        button.putClientProperty("themePrimary", Boolean.TRUE);
        button.setBackground(new Color(24, 126, 76));
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 13f));
    }

    private void applyTheme() {
        rootPanel.setBackground(darkTheme ? new Color(31, 34, 38) : new Color(246, 248, 246));
        applyThemeTo(rootPanel);
    }

    private void applyThemeTo(Component component) {
        Color card = darkTheme ? new Color(45, 49, 55) : Color.WHITE;
        Color text = darkTheme ? new Color(235, 238, 241) : new Color(45, 58, 49);
        Color muted = darkTheme ? new Color(184, 191, 198) : new Color(90, 100, 94);
        Color input = darkTheme ? new Color(57, 62, 69) : Color.WHITE;
        Color accent = darkTheme ? new Color(97, 207, 143) : GREEN;

        if (component == rootPanel) {
            component.setBackground(darkTheme ? new Color(31, 34, 38) : new Color(246, 248, 246));
        } else if (component instanceof JPanel panel && panel.isOpaque()) {
            panel.setBackground(card);
        }

        if (component instanceof JLabel label) {
            label.setForeground(label == totalLabel ? accent : (darkTheme ? text : muted));
        } else if (component instanceof JButton button) {
            boolean primary = Boolean.TRUE.equals(button.getClientProperty("themePrimary"));
            button.setUI(new BasicButtonUI());
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            button.setBackground(primary
                    ? (darkTheme ? new Color(31, 139, 85) : new Color(24, 126, 76))
                    : (darkTheme ? new Color(63, 69, 77) : new Color(239, 243, 240)));
            button.setForeground(primary ? Color.WHITE : text);
            button.setBorderPainted(false);
        } else if (component instanceof JTextField field) {
            field.setBackground(input);
            field.setForeground(text);
            field.setCaretColor(text);
        } else if (component instanceof JTable table) {
            table.setBackground(card);
            table.setForeground(text);
            table.setGridColor(darkTheme ? new Color(75, 81, 89) : new Color(225, 231, 226));
            table.setSelectionBackground(darkTheme ? new Color(55, 99, 76) : new Color(205, 235, 214));
            table.setSelectionForeground(text);
            styleTableHeader(table.getTableHeader(), darkTheme ? new Color(57, 62, 69) : new Color(239, 243, 240), text);
        } else if (component instanceof JScrollPane scrollPane) {
            scrollPane.setBackground(card);
            scrollPane.getViewport().setBackground(card);
            styleScrollBar(scrollPane.getVerticalScrollBar());
            styleScrollBar(scrollPane.getHorizontalScrollBar());
        }

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyThemeTo(child);
            }
        }
    }

    private void styleTableHeader(JTableHeader header, Color background, Color foreground) {
        header.setOpaque(true);
        header.setBackground(background);
        header.setForeground(foreground);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                           boolean focused, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, selected, focused, row, column);
                label.setOpaque(true);
                label.setBackground(background);
                label.setForeground(foreground);
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1,
                                darkTheme ? new Color(75, 81, 89) : new Color(215, 222, 216)),
                        BorderFactory.createEmptyBorder(0, 6, 0, 6)));
                return label;
            }
        });
    }

    private void styleScrollBar(javax.swing.JScrollBar scrollBar) {
        Color track = darkTheme ? new Color(38, 42, 47) : new Color(239, 243, 240);
        Color thumb = darkTheme ? new Color(93, 101, 111) : new Color(177, 188, 179);
        scrollBar.setOpaque(true);
        scrollBar.setBackground(track);
        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = thumb;
                thumbHighlightColor = thumb.brighter();
                thumbDarkShadowColor = thumb.darker();
                trackColor = track;
            }
        });
    }

    private void showMessage(String message, String title, int kind) {
        JOptionPane.showMessageDialog(this, message, title, kind);
    }
}
