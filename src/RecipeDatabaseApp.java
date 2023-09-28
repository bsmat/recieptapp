import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class RecipeDatabaseApp extends JFrame {

    //Подключение к базе данных
    private static final String DB_URL = "jdbc:sqlite:recipes.db";
    private Connection connection;

    // Компоненты Gui
    private JList<String> recipeList;
    private JTextArea recipeDetailsArea;

    public RecipeDatabaseApp() {
        super("Application for recipes");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Создание компонентов GUI
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.NORTH);

        JPanel recipePanel = createRecipePanel();
        add(recipePanel, BorderLayout.CENTER);

        // Подключение к базе данных
        connectToDatabase();

        // Загрузка списка рецептов
        loadRecipeList();

        setVisible(true);
    }

    //Создание панели ввода с полями и кнопками

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2));

        JLabel nameLabel = new JLabel("Label:");
        JTextField recipeNameField = new JTextField();
        JLabel ingredientsLabel = new JLabel("Ingredients:");
        JTextArea ingredientsArea = new JTextArea();
        JLabel cookingTimeLabel = new JLabel("Cooking time:");
        JTextField cookingTimeField = new JTextField();
        JButton addButton = new JButton("Add");
        JButton deleteButton = new JButton("Delete");

        //Добавление слушателей событий для кнопок добавления и удаления
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Получение данных из полей
                String name = recipeNameField.getText();
                String ingredients = ingredientsArea.getText();
                String cookingTime = cookingTimeField.getText();

                //Вызов метода добавления рецепта
                addRecipe(name, ingredients, cookingTime);

                // Обновление списка рецептов
                loadRecipeList();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Получение выбранного рецепта из списка
                String selectedRecipe = recipeList.getSelectedValue();
                if (selectedRecipe != null) {
                    // Вызов метода удаления рецепта
                    deleteRecipe(selectedRecipe);

                    // Обновление списка рецептов
                    loadRecipeList();
                }
            }
        });

        // Добавление компанентов на панель ввода

        panel.add(nameLabel);
        panel.add(recipeNameField);
        panel.add(ingredientsLabel);
        panel.add(ingredientsArea);
        panel.add(cookingTimeLabel);
        panel.add(cookingTimeField);
        panel.add(addButton);
        panel.add(deleteButton);

        return panel;
    }

    // Создание панели с JList и JTextArea для отображения списка рецептов и деталей выбранного рецепта
    private JPanel createRecipePanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2));

        recipeList = new JList<>();
        recipeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScrollPane = new JScrollPane(recipeList);

        recipeDetailsArea = new JTextArea();
        recipeDetailsArea.setEditable(false);
        JScrollPane detailsScrollPane = new JScrollPane(recipeDetailsArea);

        // Добавление компанентов на панель рецептов
        panel.add(listScrollPane);
        panel.add(detailsScrollPane);

        //Добавление слушателей событий для выбора рецепта из списка
        recipeList.addListSelectionListener(e -> {
            // При выборе рецепта обновляем отображение его деталей
            String selectedRecipe = recipeList.getSelectedValue();
            if (selectedRecipe != null) {
                // Получение деталей рецепта и установка их в текстовую область
                String details = getRecipeDetails(selectedRecipe);
                recipeDetailsArea.setText(details);
            }
        });

        return panel;
    }

    // Подключение к базе данных
    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            createRecipeTable();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to the database!", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    // Создание таблицы рецептов если она не существует
    private void createRecipeTable() {
        try {
            Statement statement = connection.createStatement();
            String createTableQuery = "CREATE TABLE IF NOT EXISTS recipes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "ingredients TEXT," +
                    "cooking_time TEXT" +
                    ")";
            statement.execute(createTableQuery);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error with creating a recipe table!", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    // Добавление рецепта в базу данных
    private void addRecipe(String name, String ingredients, String cookingTime) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO recipes (name, ingredients, cooking_time) VALUES (?, ?, ?)");
            statement.setString(1, name);
            statement.setString(2, ingredients);
            statement.setString(3, cookingTime);
            statement.executeUpdate();
            statement.close();

            JOptionPane.showMessageDialog(this, "The recipe has been added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error with adding recipe", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Удаление рецепта из базы данных
    private void deleteRecipe(String recipeName) {
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM recipes WHERE name = ?");
            statement.setString(1, recipeName);
            statement.executeUpdate();
            statement.close();

            JOptionPane.showMessageDialog(this, "The recipe has been successfully deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error with deleting a recipe!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Загрузка списка рецептов из базы данных
    private void loadRecipeList() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT name FROM recipes");

            DefaultListModel<String> recipeListModel = new DefaultListModel<>();
            while (resultSet.next()) {
                String recipeName = resultSet.getString("name");
                recipeListModel.addElement(recipeName);
            }
            resultSet.close();
            statement.close();

            recipeList.setModel(recipeListModel);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading the recipe list!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Получение деталей выбранного рецепта из базы данных
    private String getRecipeDetails(String recipeName) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM recipes WHERE name = ?");
            statement.setString(1, recipeName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String ingredients = resultSet.getString("ingredients");
                String cookingTime = resultSet.getString("cooking_time");

                StringBuilder detailsBuilder = new StringBuilder();
                detailsBuilder.append("ID: ").append(id).append("\n");
                detailsBuilder.append("Label: ").append(name).append("\n");
                detailsBuilder.append("Ingredients: ").append(ingredients).append("\n");
                detailsBuilder.append("Cooking time: ").append(cookingTime).append("\n");

                resultSet.close();
                statement.close();

                return detailsBuilder.toString();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error with receiving recipe details!", "Error", JOptionPane.ERROR_MESSAGE);
        }

        return "";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new RecipeDatabaseApp();
            }
        });
    }
}
