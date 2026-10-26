package com.foodordering;

import com.foodordering.model.CartItem;
import com.foodordering.model.Customer;
import com.foodordering.model.MenuItem;
import com.foodordering.repository.AuthRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Polished, single-window GUI for the complete FoodOrder demo flow. */
public class FoodOrderingApp extends JFrame {
    private static final Color INK = new Color(34, 48, 41);
    private static final Color MUTED = new Color(111, 126, 117);
    private static final Color GREEN = new Color(31, 125, 78);
    private static final Color GREEN_DARK = new Color(24, 94, 59);
    private static final Color LIME = new Color(221, 239, 211);
    private static final Color BG = new Color(247, 249, 246);
    private static final Color LINE = new Color(226, 233, 227);
    private static final String[] RESTAURANTS = {"Spice Garden", "Burger Barn", "Pasta Place"};
    private static final String[] EMOJIS = {"🍛", "🍔", "🍝"};
    private static final List<List<MenuItem>> MENUS = List.of(
            List.of(item(1, "Paneer Tikka", "Starters", "Smoky grilled paneer with mint chutney", "220"),
                    item(2, "Butter Chicken", "Main course", "Creamy tomato curry with warming spices", "340"),
                    item(3, "Garlic Naan", "Sides", "Freshly baked, brushed with garlic butter", "55"),
                    item(4, "Mango Lassi", "Drinks", "A cool, sweet mango yogurt drink", "90")),
            List.of(item(1, "Classic Burger", "Burgers", "Grilled patty, crisp lettuce and house sauce", "240"),
                    item(2, "Crispy Fries", "Sides", "Golden fries with a little sea salt", "110"),
                    item(3, "Veggie Burger", "Burgers", "Bean patty with fresh greens", "210"),
                    item(4, "Chocolate Shake", "Drinks", "A thick and creamy chocolate shake", "150")),
            List.of(item(1, "Penne Alfredo", "Pasta", "Silky parmesan cream sauce", "290"),
                    item(2, "Arrabbiata", "Pasta", "Penne tossed in a lively tomato sauce", "260"),
                    item(3, "Garlic Bread", "Sides", "Toasted sourdough with herb butter", "120"),
                    item(4, "Tiramisu", "Dessert", "Coffee, cocoa and mascarpone", "180"))
    );
    private final CardLayout cards = new CardLayout();
    private final JPanel screens = new JPanel(cards);
    private final JPanel root = new JPanel(new BorderLayout());
    private final JLabel userLabel = new JLabel("Welcome");
    private final JLabel stepLabel = new JLabel("YOUR NEXT GREAT MEAL STARTS HERE");
    private final JButton backButton = softButton("← Back");
    private String currentScreen = "auth";
    private final List<CartItem> cart = new ArrayList<>();
    private AuthRepository accounts;
    private Customer customer;
    private int restaurant = -1;
    private int rating = 0;
    private int deliveryStep = 0;
    private String orderId;
    private final JTextField nameField = field("Your name");
    private final JTextField emailField = field("you@example.com");
    private final JPasswordField passwordField = new JPasswordField();
    private final JTextField addressField = field("Street, area, city, PIN code");
    private final JTextField phoneField = field("Optional phone number");
    private boolean registerMode;
    private JPanel authNameBlock;
    private JLabel checkoutTotalLabel;

    public FoodOrderingApp() {
        super("FoodOrder | Good food, right on time");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(930, 680));
        setSize(1100, 780);
        setLocationRelativeTo(null);
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(22, 34, 26, 34));
        root.add(makeHeader(), BorderLayout.NORTH);
        screens.setOpaque(false);
        screens.add(makeAuthScreen(), "auth");
        screens.add(makeRestaurantScreen(), "restaurants");
        screens.add(makeMenuScreen(), "menu");
        screens.add(makePaymentScreen(), "payment");
        screens.add(makeDeliveryScreen(), "delivery");
        screens.add(makeRatingScreen(), "rating");
        root.add(screens, BorderLayout.CENTER);
        setContentPane(root);
        cards.show(screens, "auth");
    }

    private JComponent makeHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 11, 0));
        brand.setOpaque(false);
        JLabel mark = new JLabel("F");
        mark.setHorizontalAlignment(SwingConstants.CENTER);
        mark.setFont(new Font("SansSerif", Font.BOLD, 21));
        mark.setForeground(Color.WHITE);
        mark.setOpaque(true);
        mark.setBackground(GREEN);
        mark.setPreferredSize(new Dimension(40, 40));
        JLabel title = new JLabel("foodorder");
        title.setFont(new Font("SansSerif", Font.BOLD, 21));
        title.setForeground(INK);
        brand.add(mark);
        brand.add(title);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        right.setOpaque(false);
        backButton.setVisible(false);
        backButton.addActionListener(e -> goBack());
        right.add(backButton);
        stepLabel.setForeground(GREEN);
        stepLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        right.add(stepLabel);
        userLabel.setForeground(MUTED);
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        right.add(userLabel);
        header.add(brand, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));
        return header;
    }

    private JComponent makeAuthScreen() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);
        JPanel card = panel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(470, 535));
        GridBagConstraints c = constraints();
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        JLabel eyebrow = eyebrow("WELCOME TO FOODORDER");
        card.add(eyebrow, c);
        c.gridy++;
        JLabel title = heading("Good food is closer.");
        card.add(title, c);
        c.gridy++;
        JLabel subtitle = body("Sign in or create an account to start your order.");
        card.add(subtitle, c);
        c.gridy++; c.insets = new Insets(24, 0, 16, 0);
        JPanel tabs = new JPanel(new GridLayout(1, 2, 8, 0));
        tabs.setOpaque(false);
        JButton loginTab = softButton("Log in");
        JButton registerTab = softButton("Create account");
        loginTab.addActionListener(e -> setRegisterMode(false));
        registerTab.addActionListener(e -> setRegisterMode(true));
        tabs.add(loginTab); tabs.add(registerTab);
        card.add(tabs, c);
        c.gridy++; c.gridwidth = 2; c.insets = new Insets(0, 0, 0, 0);
        authNameBlock = new JPanel(); authNameBlock.setOpaque(false); authNameBlock.setLayout(new BoxLayout(authNameBlock, BoxLayout.Y_AXIS));
        JLabel nameLabel = fieldLabel("FULL NAME");
        authNameBlock.add(nameLabel); authNameBlock.add(Box.createVerticalStrut(6));
        nameField.setPreferredSize(new Dimension(0, 43));
        authNameBlock.add(nameField);
        authNameBlock.setVisible(false);
        card.add(authNameBlock, c);
        c.gridy++; c.insets = new Insets(15, 0, 6, 0);
        card.add(fieldLabel("EMAIL ADDRESS"), c);
        c.gridy++; c.insets = new Insets(0, 0, 0, 0);
        emailField.setPreferredSize(new Dimension(0, 43));
        card.add(emailField, c);
        c.gridy++; c.insets = new Insets(15, 0, 6, 0);
        card.add(fieldLabel("PASSWORD"), c);
        c.gridy++; c.insets = new Insets(0, 0, 0, 0);
        styleField(passwordField);
        passwordField.setPreferredSize(new Dimension(0, 43));
        card.add(passwordField, c);
        c.gridy++; c.insets = new Insets(20, 0, 0, 0);
        JButton continueButton = primaryButton("Continue  →");
        continueButton.setPreferredSize(new Dimension(0, 46));
        continueButton.addActionListener(e -> authenticate());
        card.add(continueButton, c);
        c.gridy++; c.insets = new Insets(14, 0, 0, 0);
        JLabel note = body("Your account is stored securely. Passwords are never saved as plain text.");
        note.setFont(new Font("SansSerif", Font.PLAIN, 11));
        card.add(note, c);
        outer.add(card);
        return outer;
    }

    private void setRegisterMode(boolean register) {
        registerMode = register;
        authNameBlock.setVisible(register);
        screens.revalidate(); screens.repaint();
    }

    private void authenticate() {
        String email = emailField.getText().trim().toLowerCase(Locale.ROOT);
        char[] password = passwordField.getPassword();
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            error("Enter a valid email address."); return;
        }
        if (password.length == 0) { error("Enter your password."); return; }
        try {
            if (accounts == null) accounts = new AuthRepository();
            if (registerMode) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) { error("Enter your name."); return; }
                if (password.length < 8) { error("Choose a password with at least 8 characters."); return; }
                try {
                    accounts.register(name, email, password);
                    customer = new Customer(name, email, "", "");
                } catch (SQLException ex) {
                    if (ex.getErrorCode() == 1062) { error("That email already has an account. Choose Log in."); return; }
                    throw ex;
                }
            } else {
                customer = accounts.login(email, password);
                if (customer == null) { error("Email or password did not match. Try again."); return; }
            }
            userLabel.setText("Hi, " + customer.getName());
            show("restaurants", "PICK YOUR RESTAURANT");
        } catch (SQLException | IllegalStateException ex) {
            if (ex instanceof IllegalStateException) {
                error("The launcher did not pass a MySQL username to the app. Close this window and start again with Start FoodOrder.bat.");
            } else {
                error("Can't connect to " + System.getenv("FOOD_DB_URL") + ". Check that MySQL is running, the database exists, and the username/password are correct.\n\nDetails: " + ex.getMessage());
            }
        } finally {
            java.util.Arrays.fill(password, '\0');
            passwordField.setText("");
        }
    }

    private JComponent makeRestaurantScreen() {
        JPanel page = page();
        page.add(eyebrow("STEP 1 OF 5  ·  RESTAURANTS"));
        page.add(heading("Where would you like to eat?"));
        page.add(body("Pick a kitchen. We’ll show you the menu and get dinner moving."));
        page.add(Box.createVerticalStrut(22));
        JPanel grid = new JPanel(new GridLayout(1, 3, 15, 0));
        grid.setOpaque(false);
        String[] descriptions = {"Aromatic curries & tandoor", "Burgers, fries & shakes", "Fresh pasta & comfort food"};
        String[] times = {"25–35 min", "20–30 min", "25–35 min"};
        for (int i = 0; i < RESTAURANTS.length; i++) {
            final int choice = i;
            JPanel tile = panel();
            tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
            JLabel emoji = new JLabel(EMOJIS[i]); emoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 54));
            emoji.setAlignmentX(Component.LEFT_ALIGNMENT);
            tile.add(emoji); tile.add(Box.createVerticalStrut(17));
            JLabel name = headingSmall(RESTAURANTS[i]); name.setAlignmentX(Component.LEFT_ALIGNMENT); tile.add(name);
            JLabel desc = body(descriptions[i]); desc.setAlignmentX(Component.LEFT_ALIGNMENT); tile.add(desc);
            tile.add(Box.createVerticalStrut(18));
            JLabel time = pill("◷  " + times[i]); time.setAlignmentX(Component.LEFT_ALIGNMENT); tile.add(time);
            tile.add(Box.createVerticalGlue());
            JButton choose = primaryButton("View menu  →"); choose.setAlignmentX(Component.LEFT_ALIGNMENT);
            choose.addActionListener(e -> { restaurant = choice; cart.clear(); updateText(screens, "Your feedback helps Spice Garden make every order better.", "Your feedback helps " + RESTAURANTS[restaurant] + " make every order better."); show("menu", "BUILD YOUR ORDER"); refreshMenu(); });
            tile.add(Box.createVerticalStrut(20)); tile.add(choose);
            grid.add(tile);
        }
        page.add(grid);
        page.add(Box.createVerticalGlue());
        return page;
    }

    private JPanel menuList;
    private JPanel cartList;
    private JLabel cartTotalLabel;
    private JComponent makeMenuScreen() {
        JPanel page = page();
        page.add(eyebrow("STEP 2 OF 5  ·  MENU"));
        JPanel titleRow = new JPanel(new BorderLayout()); titleRow.setOpaque(false);
        JPanel titleBlock = new JPanel(); titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS)); titleBlock.setOpaque(false);
        titleBlock.add(heading("Make it a meal."));
        titleBlock.add(body("Choose your favourites from " + RESTAURANTS[0] + "."));
        titleRow.add(titleBlock, BorderLayout.WEST);
        JButton change = softButton("← Change restaurant");
        change.addActionListener(e -> show("restaurants", "PICK YOUR RESTAURANT")); titleRow.add(change, BorderLayout.EAST);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, titleRow.getPreferredSize().height));
        page.add(titleRow); page.add(Box.createVerticalStrut(16));
        JPanel columns = new JPanel(new GridLayout(1, 2, 16, 0)); columns.setOpaque(false);
        JPanel menuCard = panel(); menuCard.setLayout(new BorderLayout(0, 12));
        menuCard.add(sectionTitle("THE MENU"), BorderLayout.NORTH);
        menuList = new JPanel(); menuList.setOpaque(false); menuList.setLayout(new BoxLayout(menuList, BoxLayout.Y_AXIS));
        JScrollPane menuScroll = new JScrollPane(menuList);
        menuScroll.setBorder(BorderFactory.createEmptyBorder());
        menuScroll.getViewport().setBackground(Color.WHITE);
        menuCard.add(menuScroll, BorderLayout.CENTER);
        JPanel cartCard = panel(); cartCard.setLayout(new BorderLayout(0, 12));
        JPanel cartHeading = new JPanel(new BorderLayout()); cartHeading.setOpaque(false);
        cartHeading.add(sectionTitle("YOUR ORDER"), BorderLayout.WEST);
        cartTotalLabel = new JLabel(money(BigDecimal.ZERO)); cartTotalLabel.setForeground(GREEN_DARK); cartTotalLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        cartHeading.add(cartTotalLabel, BorderLayout.EAST); cartCard.add(cartHeading, BorderLayout.NORTH);
        cartList = new JPanel(); cartList.setOpaque(false); cartList.setLayout(new BoxLayout(cartList, BoxLayout.Y_AXIS));
        JScrollPane cartScroll = new JScrollPane(cartList);
        cartScroll.setBorder(BorderFactory.createEmptyBorder());
        cartScroll.getViewport().setBackground(Color.WHITE);
        cartCard.add(cartScroll, BorderLayout.CENTER);
        JButton checkout = primaryButton("Continue to checkout  →");
        checkout.addActionListener(e -> {
            if (cart.isEmpty()) { error("Add at least one dish to your order."); return; }
            checkoutTotalLabel.setText("Order total     " + money(total()));
            show("payment", "PAYMENT & DELIVERY");
        });
        cartCard.add(checkout, BorderLayout.SOUTH);
        columns.add(menuCard); columns.add(cartCard); page.add(columns);
        return page;
    }

    private void refreshMenu() {
        if (menuList == null || restaurant < 0) return;
        Component[] comps = screens.getComponents();
        // Update restaurant name in the title copy.
        updateText(screens, "Choose your favourites from " + RESTAURANTS[0] + ".", "Choose your favourites from " + RESTAURANTS[restaurant] + ".");
        menuList.removeAll();
        for (MenuItem item : MENUS.get(restaurant)) {
            JPanel row = new JPanel(new BorderLayout(12, 8)); row.setOpaque(false); row.setBorder(new EmptyBorder(10, 4, 10, 4));
            JPanel words = new JPanel(); words.setOpaque(false); words.setLayout(new BoxLayout(words, BoxLayout.Y_AXIS));
            JLabel name = headingSmall(item.getName()); name.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel category = eyebrow(item.getCategory().toUpperCase(Locale.ROOT)); category.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel desc = body(item.getDescription()); desc.setAlignmentX(Component.LEFT_ALIGNMENT);
            words.add(category); words.add(Box.createVerticalStrut(3)); words.add(name); words.add(desc);
            JPanel action = new JPanel(); action.setOpaque(false); action.setLayout(new BoxLayout(action, BoxLayout.Y_AXIS));
            JLabel price = new JLabel(money(item.getPrice())); price.setFont(new Font("SansSerif", Font.BOLD, 15)); price.setForeground(INK);
            JButton add = primaryButton("+ Add"); add.addActionListener(e -> addItem(item));
            action.add(price); action.add(Box.createVerticalStrut(7)); action.add(add);
            row.add(words, BorderLayout.CENTER); row.add(action, BorderLayout.EAST);
            row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,LINE), new EmptyBorder(10,4,10,4)));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
            menuList.add(row);
        }
        refreshCart(); menuList.revalidate(); menuList.repaint();
    }

    private void addItem(MenuItem item) {
        CartItem existing = cart.stream().filter(c -> c.getMenuItem().getId() == item.getId()).findFirst().orElse(null);
        if (existing == null) cart.add(new CartItem(item, 1)); else existing.setQuantity(existing.getQuantity() + 1);
        refreshCart();
    }

    private void refreshCart() {
        if (cartList == null) return;
        cartList.removeAll();
        if (cart.isEmpty()) {
            JLabel empty = body("Your order is empty. Add a dish to get started."); empty.setBorder(new EmptyBorder(24, 4, 24, 4)); cartList.add(empty);
        }
        for (CartItem item : cart) {
            JPanel row = new JPanel(new BorderLayout(8, 4)); row.setOpaque(false); row.setBorder(new EmptyBorder(10, 3, 10, 3));
            JLabel title = headingSmall(item.getMenuItem().getName());
            JLabel amount = body(item.getQuantity() + " × " + money(item.getMenuItem().getPrice()));
            JPanel text = new JPanel(); text.setOpaque(false); text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS)); text.add(title); text.add(amount);
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0)); actions.setOpaque(false);
            JButton minus = miniButton("−"); minus.addActionListener(e -> { item.setQuantity(item.getQuantity()-1); if(item.getQuantity()<=0)cart.remove(item); refreshCart(); });
            JLabel qty = new JLabel(String.valueOf(item.getQuantity())); qty.setBorder(new EmptyBorder(0,5,0,5));
            JButton plus = miniButton("+"); plus.addActionListener(e -> { item.setQuantity(item.getQuantity()+1); refreshCart(); });
            actions.add(minus); actions.add(qty); actions.add(plus);
            row.add(text, BorderLayout.CENTER); row.add(actions, BorderLayout.EAST); cartList.add(row);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        }
        if (cartTotalLabel != null) cartTotalLabel.setText(money(total()));
        cartList.revalidate(); cartList.repaint();
    }

    private JComponent makePaymentScreen() {
        JPanel page = page();
        page.add(eyebrow("STEP 3 OF 5  ·  CHECKOUT")); page.add(heading("Almost at your door."));
        page.add(body("Where should we bring your order, and how would you like to pay?"));
        page.add(Box.createVerticalStrut(18));
        JPanel columns = new JPanel(new GridLayout(1,2,16,0)); columns.setOpaque(false);
        JPanel details = panel(); details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.add(sectionTitle("DELIVERY DETAILS")); details.add(Box.createVerticalStrut(15));
        details.add(fieldLabel("DELIVERY ADDRESS")); details.add(Box.createVerticalStrut(6)); details.add(addressField);
        details.add(Box.createVerticalStrut(15)); details.add(fieldLabel("PHONE (OPTIONAL)")); details.add(Box.createVerticalStrut(6)); details.add(phoneField);
        JPanel pay = panel(); pay.setLayout(new BoxLayout(pay, BoxLayout.Y_AXIS)); pay.add(sectionTitle("PAYMENT METHOD")); pay.add(Box.createVerticalStrut(16));
        JRadioButton cod = radio("Cash on delivery", "Pay when your order arrives");
        JRadioButton upi = radio("UPI", "Demo payment · no details collected");
        JRadioButton card = radio("Credit or debit card", "Demo payment · no details collected");
        ButtonGroup group = new ButtonGroup(); group.add(cod); group.add(upi); group.add(card); cod.setSelected(true);
        pay.add(cod); pay.add(Box.createVerticalStrut(12)); pay.add(upi); pay.add(Box.createVerticalStrut(12)); pay.add(card);
        pay.add(Box.createVerticalGlue());
        checkoutTotalLabel = new JLabel("Order total     " + money(total())); checkoutTotalLabel.setFont(new Font("SansSerif", Font.BOLD, 19)); checkoutTotalLabel.setForeground(GREEN_DARK);
        pay.add(checkoutTotalLabel); pay.add(Box.createVerticalStrut(14));
        JButton confirm = primaryButton("Place my order  →");
        confirm.addActionListener(e -> {
            if (addressField.getText().trim().isEmpty()) { error("Enter your delivery address."); return; }
            orderId = "FD" + (System.currentTimeMillis() % 1_000_000);
            deliveryStep = 0;
            show("delivery", "ORDER CONFIRMED"); updateDelivery();
        });
        pay.add(confirm);
        columns.add(details); columns.add(pay); page.add(columns); page.add(Box.createVerticalGlue());
        return page;
    }

    private JLabel deliveryStatus;
    private JProgressBar deliveryProgress;
    private JLabel orderDetails;
    private JButton deliveryNext;
    private JComponent makeDeliveryScreen() {
        JPanel outer = new JPanel(new GridBagLayout()); outer.setOpaque(false);
        JPanel card = panel(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setPreferredSize(new Dimension(670, 550));
        JLabel check = new JLabel("✓"); check.setOpaque(true); check.setBackground(LIME); check.setForeground(GREEN_DARK); check.setFont(new Font("SansSerif",Font.BOLD,32)); check.setHorizontalAlignment(SwingConstants.CENTER); check.setPreferredSize(new Dimension(66,66)); check.setMaximumSize(new Dimension(66,66)); check.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(check); card.add(Box.createVerticalStrut(13));
        JLabel title = heading("Your order is in motion."); title.setAlignmentX(Component.CENTER_ALIGNMENT); card.add(title);
        deliveryStatus = body("The restaurant is preparing your food."); deliveryStatus.setFont(new Font("SansSerif",Font.PLAIN,15)); deliveryStatus.setAlignmentX(Component.CENTER_ALIGNMENT); card.add(deliveryStatus);
        orderDetails = body(""); orderDetails.setAlignmentX(Component.CENTER_ALIGNMENT); card.add(orderDetails);
        card.add(Box.createVerticalStrut(25));
        deliveryProgress = new JProgressBar(0, 4); deliveryProgress.setValue(1); deliveryProgress.setStringPainted(false); deliveryProgress.setForeground(GREEN); deliveryProgress.setBackground(LIME); deliveryProgress.setMaximumSize(new Dimension(500, 10)); deliveryProgress.setAlignmentX(Component.CENTER_ALIGNMENT); card.add(deliveryProgress);
        JPanel stages = new JPanel(new GridLayout(1,4)); stages.setOpaque(false); stages.setMaximumSize(new Dimension(550,45));
        for(String s:new String[]{"Preparing","Ready","On the way","Delivered"}) { JLabel l=body(s); l.setHorizontalAlignment(SwingConstants.CENTER); l.setFont(new Font("SansSerif",Font.PLAIN,11)); stages.add(l); }
        card.add(Box.createVerticalStrut(8)); card.add(stages); card.add(Box.createVerticalGlue());
        deliveryNext = primaryButton("Next delivery update  →"); deliveryNext.setAlignmentX(Component.CENTER_ALIGNMENT);
        deliveryNext.addActionListener(e -> {
            if (deliveryStep < 3) { deliveryStep++; updateDelivery(); }
            else show("rating", "RATE YOUR EXPERIENCE");
        });
        card.add(deliveryNext);
        outer.add(card); return outer;
    }

    private void updateDelivery() {
        String[] updates = {"The restaurant is preparing your food.", "Your order is packed and ready for pickup.",
                "Your delivery partner is on the way.", "Delivered! Enjoy every bite."};
        deliveryStatus.setText(updates[deliveryStep]);
        orderDetails.setText("Order " + orderId + "  ·  " + RESTAURANTS[restaurant] + "  ·  " + money(total()));
        deliveryProgress.setValue(deliveryStep + 1);
        deliveryNext.setText(deliveryStep == 3 ? "Rate your order  →" : "Next delivery update  →");
    }

    private JLabel[] stars;
    private JComponent makeRatingScreen() {
        JPanel outer = new JPanel(new GridBagLayout()); outer.setOpaque(false);
        JPanel card = panel(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setPreferredSize(new Dimension(570, 490));
        JLabel emoji = new JLabel("✦"); emoji.setFont(new Font("SansSerif",Font.PLAIN,55)); emoji.setForeground(new Color(240,175,59)); emoji.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(emoji); card.add(Box.createVerticalStrut(10));
        JLabel title = heading("How was your meal?"); title.setAlignmentX(Component.CENTER_ALIGNMENT); card.add(title);
        JLabel sub = body("Your feedback helps " + RESTAURANTS[0] + " make every order better."); sub.setAlignmentX(Component.CENTER_ALIGNMENT); card.add(sub);
        stars = new JLabel[5]; JPanel starRow = new JPanel(new FlowLayout(FlowLayout.CENTER,8,10)); starRow.setOpaque(false);
        for(int i=0;i<5;i++){ final int score=i+1; JLabel star=new JLabel("☆"); star.setFont(new Font("Segoe UI Symbol",Font.PLAIN,44)); star.setForeground(new Color(229,169,52)); star.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); star.addMouseListener(new java.awt.event.MouseAdapter(){@Override public void mouseClicked(java.awt.event.MouseEvent e){rating=score; updateStars();}}); stars[i]=star; starRow.add(star); }
        card.add(Box.createVerticalStrut(22)); card.add(starRow);
        JTextArea comment = new JTextArea(4, 35); comment.setLineWrap(true); comment.setWrapStyleWord(true); comment.setFont(new Font("SansSerif",Font.PLAIN,13)); comment.setBorder(new EmptyBorder(9,10,9,10)); comment.setBackground(BG);
        JScrollPane commentScroll = new JScrollPane(comment); commentScroll.setBorder(BorderFactory.createLineBorder(LINE)); commentScroll.setMaximumSize(new Dimension(430,95)); commentScroll.setAlignmentX(Component.CENTER_ALIGNMENT); card.add(Box.createVerticalStrut(12)); card.add(commentScroll);
        JButton finish=primaryButton("Submit rating  →"); finish.setAlignmentX(Component.CENTER_ALIGNMENT); finish.addActionListener(e->{if(rating==0){error("Choose a star rating first.");return;} JOptionPane.showMessageDialog(this,"Thanks for the " + rating + "/5 rating!","Feedback received",JOptionPane.INFORMATION_MESSAGE); cart.clear(); restaurant=-1; rating=0; addressField.setText(""); phoneField.setText(""); userLabel.setText("Hi, " + customer.getName()); show("restaurants","PICK YOUR RESTAURANT");});
        card.add(Box.createVerticalStrut(18)); card.add(finish); outer.add(card); return outer;
    }

    private void updateStars(){for(int i=0;i<stars.length;i++)stars[i].setText(i<rating?"★":"☆");}
    private void show(String screen,String step){currentScreen=screen;cards.show(screens,screen);stepLabel.setText(step);backButton.setVisible(!screen.equals("auth")&&!screen.equals("delivery"));}
    private void goBack(){switch(currentScreen){case "restaurants" -> show("auth","YOUR NEXT GREAT MEAL STARTS HERE");case "menu" -> show("restaurants","PICK YOUR RESTAURANT");case "payment" -> show("menu","BUILD YOUR ORDER");case "rating" -> show("delivery","ORDER DELIVERED");default -> {}}}
    private static JPanel page(){JPanel p=new JPanel();p.setOpaque(false);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));return p;}
    private static JPanel panel(){JPanel p=new JPanel();p.setBackground(Color.WHITE);p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),new EmptyBorder(20,20,20,20)));return p;}
    private static GridBagConstraints constraints(){GridBagConstraints c=new GridBagConstraints();c.gridx=0;c.gridy=0;c.weightx=1;c.fill=GridBagConstraints.HORIZONTAL;c.insets=new Insets(0,0,0,0);return c;}
    private static JLabel eyebrow(String s){JLabel l=new JLabel(s);l.setForeground(GREEN);l.setFont(new Font("SansSerif",Font.BOLD,11));l.setBorder(new EmptyBorder(0,0,8,0));return l;}
    private static JLabel heading(String s){JLabel l=new JLabel(s);l.setForeground(INK);l.setFont(new Font("SansSerif",Font.BOLD,29));l.setBorder(new EmptyBorder(0,0,5,0));return l;}
    private static JLabel headingSmall(String s){JLabel l=new JLabel(s);l.setForeground(INK);l.setFont(new Font("SansSerif",Font.BOLD,16));return l;}
    private static JLabel body(String s){JLabel l=new JLabel(s);l.setForeground(MUTED);l.setFont(new Font("SansSerif",Font.PLAIN,13));return l;}
    private static JLabel sectionTitle(String s){JLabel l=eyebrow(s);l.setBorder(new EmptyBorder(0,0,8,0));return l;}
    private static JLabel fieldLabel(String s){JLabel l=eyebrow(s);l.setForeground(MUTED);return l;}
    private static JLabel pill(String s){JLabel l=new JLabel(s);l.setOpaque(true);l.setBackground(LIME);l.setForeground(GREEN_DARK);l.setBorder(new EmptyBorder(7,10,7,10));l.setFont(new Font("SansSerif",Font.BOLD,12));return l;}
    private static JTextField field(String hint){JTextField f=new JTextField();f.setToolTipText(hint);styleField(f);return f;}
    private static void styleField(JTextField f){f.setFont(new Font("SansSerif",Font.PLAIN,14));f.setForeground(INK);f.setBackground(new Color(250,251,249));f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),new EmptyBorder(10,12,10,12)));f.setPreferredSize(new Dimension(200,43));}
    private static JButton primaryButton(String s){JButton b=new JButton(s);b.setFont(new Font("SansSerif",Font.BOLD,13));b.setForeground(Color.WHITE);b.setBackground(GREEN);b.setOpaque(true);b.setContentAreaFilled(true);b.setBorderPainted(false);b.setFocusPainted(false);b.setBorder(new EmptyBorder(11,16,11,16));b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;}
    private static JButton softButton(String s){JButton b=new JButton(s);b.setFont(new Font("SansSerif",Font.BOLD,12));b.setForeground(GREEN_DARK);b.setBackground(LIME);b.setOpaque(true);b.setContentAreaFilled(true);b.setBorderPainted(false);b.setFocusPainted(false);b.setBorder(new EmptyBorder(10,12,10,12));return b;}
    private static JButton miniButton(String s){JButton b=new JButton(s);b.setFont(new Font("SansSerif",Font.BOLD,12));b.setForeground(GREEN_DARK);b.setBackground(LIME);b.setFocusPainted(false);b.setBorder(new EmptyBorder(3,8,3,8));return b;}
    private static JRadioButton radio(String title,String description){JRadioButton b=new JRadioButton("  " + title + "    ·    " + description);b.setOpaque(false);b.setForeground(INK);b.setFont(new Font("SansSerif",Font.PLAIN,13));return b;}
    private static MenuItem item(int id,String name,String category,String desc,String price){return new MenuItem(id,name,desc,category,new BigDecimal(price));}
    private BigDecimal total(){return cart.stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO,BigDecimal::add);}
    private static String money(BigDecimal amount){return "₹"+amount.setScale(2);}
    private void error(String text){JOptionPane.showMessageDialog(this,text,"FoodOrder",JOptionPane.WARNING_MESSAGE);}
    private void updateText(Component component,String oldText,String newText){if(component instanceof JLabel label && oldText.equals(label.getText()))label.setText(newText);if(component instanceof Container c)for(Component child:c.getComponents())updateText(child,oldText,newText);}
}
