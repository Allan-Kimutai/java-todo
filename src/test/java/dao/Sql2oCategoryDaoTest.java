package dao;

import models.Category;
import models.Task;

import org.sql2o.Connection;
import org.sql2o.Sql2o;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Sql2oCategoryDaoTest {
    private Sql2oCategoryDao categoryDao;
    private Sql2oTaskDao taskDao;
    private Connection conn;

    @BeforeEach
    public void setUp() throws Exception {
        String connectionString = "jdbc:h2:mem:testing;INIT=RUNSCRIPT from 'classpath:db/create.sql'";
        Sql2o sql2o = new Sql2o(connectionString, "", "");
        categoryDao = new Sql2oCategoryDao(sql2o);
        taskDao = new Sql2oTaskDao(sql2o);
        conn = sql2o.open();
    }

    @AfterEach
    public void tearDown() throws Exception {
        conn.close();
    }

    @Test
    public void addingCategorySetsId() throws Exception {
        Category category = setupNewCategory();
        int originalCategoryId = category.getId();
        categoryDao.add(category);
        Assertions.assertNotEquals(originalCategoryId, category.getId());
    }

    @Test
    public void existingCategoriesCanBeFoundById() throws Exception {
        Category category = setupNewCategory();
        categoryDao.add(category);
        Category foundCategory = categoryDao.findById(category.getId());
        Assertions.assertEquals(category, foundCategory);
    }

    @Test
    public void addedCategoriesAreReturnedFromGetAll() throws Exception {
        Category category = setupNewCategory();
        categoryDao.add(category);
        Assertions.assertEquals(1, categoryDao.getAll().size());
    }

    @Test
    public void noCategoriesReturnsEmptyList() throws Exception {
        Assertions.assertEquals(0, categoryDao.getAll().size());
    }

    @Test
    public void updateChangesCategoryContent() throws Exception {
        String initialDescription = "Yardwork";
        Category category = new Category (initialDescription);
        categoryDao.add(category);
        categoryDao.update(category.getId(),"Cleaning");
        Category updatedCategory = categoryDao.findById(category.getId());
        Assertions.assertNotEquals(initialDescription, updatedCategory.getName());
    }

    @Test
    public void deleteByIdDeletesCorrectCategory() throws Exception {
        Category category = setupNewCategory();
        categoryDao.add(category);
        categoryDao.deleteById(category.getId());
        Assertions.assertEquals(0, categoryDao.getAll().size());
    }

    @Test
    public void clearAllClearsAllCategories() throws Exception {
        Category category = setupNewCategory();
        Category otherCategory = new Category("Cleaning");
        categoryDao.add(category);
        categoryDao.add(otherCategory);
        int daoSize = categoryDao.getAll().size();
        categoryDao.clearAllCategories();
        Assertions.assertTrue(daoSize > 0 && daoSize > categoryDao.getAll().size());
    }

    @Test
    public void getAllTasksByCategoryReturnsTasksCorrectly() throws Exception {
        Category category = setupNewCategory();
        categoryDao.add(category);
        int categoryId = category.getId();
        Task newTask = new Task("mow the lawn", categoryId);
        Task otherTask = new Task("pull weeds", categoryId);
        Task thirdTask = new Task("trim hedge", categoryId);
        taskDao.add(newTask);
        taskDao.add(otherTask); //we are not adding task 3 so we can test things precisely.
        Assertions.assertEquals(2, categoryDao.getAllTasksByCategory(categoryId).size());
        Assertions.assertTrue(categoryDao.getAllTasksByCategory(categoryId).contains(newTask));
        Assertions.assertTrue(categoryDao.getAllTasksByCategory(categoryId).contains(otherTask));
        Assertions.assertFalse(categoryDao.getAllTasksByCategory(categoryId).contains(thirdTask)); //things are accurate!
    }

    // helper method
    public Category setupNewCategory(){
        return new Category("Yardwork");
    }
}