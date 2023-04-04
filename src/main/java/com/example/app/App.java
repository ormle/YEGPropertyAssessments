package com.example.app;


import javafx.scene.control.*;


import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    public static void main(String[] args){
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        // set the title and size of the stage and show it
        stage.setTitle("YEG");
        stage.setWidth(800);
        stage.setHeight(700);
        stage.show();

        //Creates the tab pane that will hold all the
        TabPane tabPane = new TabPane();
        //makes it, so you can't close any of the tabs
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        //Creating the different tabs, additional tabs can be created here
        Tab propertyTab = new Tab("Properties");
        Tab businessTab = new Tab("Businesses");

        //Set content of property tab
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/PropertyAsessmentsDataView.fxml"));
        propertyTab.setContent(fxmlLoader.load());
        //Set content of business tab
        //fxmlLoader = new FXMLLoader(getClass().getResource("/BusinessesDataView.fxml"));
        fxmlLoader = new FXMLLoader(getClass().getResource("/test.fxml"));
        businessTab.setContent(fxmlLoader.load());

        // create a JavaFX scene with a stack pane as the root node, and add it to the scene
        tabPane.getTabs().addAll(propertyTab, businessTab);
        Scene scene = new Scene(tabPane, 720, 640);
        stage.setScene(scene);
    }

}