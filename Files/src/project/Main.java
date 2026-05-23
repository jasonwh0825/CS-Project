package project;
import javafx.application.Application; 	// 必須繼承Application才能建立GUI
import javafx.scene.Scene;			// 畫面場景的容器，裝著要顯示的元件
import javafx.scene.control.Button;		// 按鈕控制元件
import javafx.scene.layout.BorderPane;	// 版面配置
import javafx.stage.Stage;			// 主視窗，類似一個窗戶或容器

public class Main extends Application {	// JavaFX 要求繼承Application並複寫start()
    @Override
    public void start(Stage stage) throws Exception {	// start()為JavaFX進入點
        Button button = new Button("Hello, JavaFX");	// 設置按鈕內容
        BorderPane borderedPane = new BorderPane();	// 排版容器
        borderedPane.setCenter(button);
        Scene scene = new Scene(borderedPane,300,300);	// 畫面場景設置
        stage.setScene(scene);
        stage.setTitle("Binge");				// 視窗名稱
        stage.show();						// 顯示視窗
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);	//啟動JavaFX，自動呼叫start(Stage)
    }
}