package controller.employee.pages;

import controller.UserSessionController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import model.Datasource;
import model.Order;
import model.OrderDetail;
import model.Product;

/**
 * This class handles the users orders page.
 *
 * @author Sajmir Doko
 */
public class UserOrdersController {
    public TableView tableOrdersPage;

    /**
     * This method lists all the orders to the view table.
     * It starts a new Task, gets all the products from the database then bind the results to the view.
     *
     * @since 1.0.0
     */
    @FXML
    public void listOrders() {

        Task<ObservableList<Order>> getAllOrdersTask = new Task<ObservableList<Order>>() {
            @Override
            protected ObservableList<Order> call() {
                return FXCollections.observableArrayList(Datasource.getInstance().getAllUserOrders(Datasource.ORDER_BY_NONE, UserSessionController.getUserId()));
            }
        };

        tableOrdersPage.itemsProperty().bind(getAllOrdersTask.valueProperty());
        new Thread(getAllOrdersTask).start();

    }

    @FXML
    private void addActionButton(){
        TableColumn colBtnEdit = new TableColumn("Actions");
        Callback<TableColumn<OrderDetail, Void>, TableCell<OrderDetail, Void>> cellFactory = new Callback<TableColumn<OrderDetail, Void>, TableCell<OrderDetail, Void>>(){

            @Override
            public TableCell<OrderDetail, Void> call(final TableColumn<OrderDetail, Void> param){
                return new TableCell<OrderDetail, Void>(){
                    private final Button viewDetail = new Button("Details");
                    private final HBox buttonsPane = new HBox();
                    {
                        viewDetail.getStyleClass().add("button");
                        viewDetail.getStyleClass().add("xs");
                        viewDetail.getStyleClass().add("primary");
                        //todo on action
                    }

                    {
                        buttonsPane.setSpacing(10);
                        buttonsPane.setAlignment(Pos.CENTER);
                        buttonsPane.getChildren().add(viewDetail);
                    }
                };
            }
        };
        colBtnEdit.setCellFactory(cellFactory);

        tableOrdersPage.getColumns().add(colBtnEdit);
    }

    public void btnOrdersSearchOnAction(ActionEvent actionEvent) {
        // TODO
        //  Add orders search functionality.
        System.out.println("TODO: Add orders search functionality.");
    }
}
