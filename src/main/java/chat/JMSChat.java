package chat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class JMSChat extends Application {
	private MessageProducer msgproducer;
	private Session session;
	private String codeUser;
	ObservableList<String> observListmsgs;

	public static void main(String[] args) {
		Application.launch(JMSChat.class);

	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("JMS Chat");
		/*** header ***/
		BorderPane borderPane = new BorderPane(); // permet de disposer l'ecran en 5 zones=> N S C E O ; element root
		HBox hbox = new HBox(); hbox.setPadding(new Insets(10)); //équivalent d'une div horizontale
		hbox.setSpacing(10); hbox.setBackground(new Background(new BackgroundFill(Color.CHOCOLATE, CornerRadii.EMPTY, Insets.EMPTY)));
		
		Label labelCode = new Label("Code:");
		TextField texfieldcode = new TextField("c1"); 
		texfieldcode.setPromptText("code"); // placeholder
		
		Label labelhost = new Label("Host:");
		TextField texfieldhost = new TextField("localhost"); 
		texfieldhost.setPromptText("Host");
		
		Label labelport = new Label("Port:");
		TextField texfieldport = new TextField("61616"); 
		texfieldport.setPromptText("Port");
		
		Button buttonConnecter = new Button("Connecter");
		
		hbox.getChildren().add(labelCode);
		hbox.getChildren().add(texfieldcode);
		hbox.getChildren().add(labelhost);
		hbox.getChildren().add(texfieldhost);
		hbox.getChildren().add(labelport);
		hbox.getChildren().add(texfieldport);
		hbox.getChildren().add(buttonConnecter);
		borderPane.setTop(hbox);
		/*** fin header ***/
		
		/***  debut centre inputs ***/
		VBox vbox = new VBox(); //vertical box
		GridPane gridpane = new GridPane();
		HBox hbox2 = new HBox();
		vbox.getChildren().add(gridpane);
		vbox.getChildren().add(hbox2);
		borderPane.setCenter(vbox);
		
		Label labelto = new Label("To: ");
		TextField texfieldto = new TextField("C1");texfieldto.setPrefWidth(250);
		Label labelmessage = new Label("Message: ");
		TextArea textareamessage = new TextArea(); textareamessage.setPrefWidth(250);
		Button buttonEnvoyer = new Button("Envoyer");
		Label labelimage = new Label("Image: ");
		File f= new File("images");
		ObservableList<String> observListImgs=FXCollections.observableArrayList(f.list());
		ComboBox<String> comboboximages= new ComboBox<String>(observListImgs);
		comboboximages.getSelectionModel().select(0);
		Button buttonEnvoyerImage = new Button("Envoyer Image");
		
		gridpane.setPadding(new Insets(10));
		gridpane.setVgap(10); gridpane.setHgap(10); // vertical et horizontal 
		textareamessage.setPrefRowCount(2);
		
		gridpane.add(labelto, 0, 0); gridpane.add(texfieldto, 1, 0);
		gridpane.add(labelmessage, 0, 1); gridpane.add(textareamessage, 1, 1); gridpane.add(buttonEnvoyer, 2, 1);
		gridpane.add(labelimage, 0, 2);gridpane.add(comboboximages, 1, 2); gridpane.add(buttonEnvoyerImage, 2, 2);
		/***  fin centre inputs ***/
		
		//lorsqu'on ajoute un msg à la liste observListmsgs, il s'affiche automatiqt à listviewmsgs
		observListmsgs = FXCollections.observableArrayList();
		ListView<String> listviewmsgs = new ListView<>(observListmsgs);
		File f2 = new File("images/"+comboboximages.getSelectionModel().getSelectedItem());
		Image image=new Image(f2.toURI().toString());
		ImageView imgview = new ImageView(image);
		imgview.setFitWidth(320);
		imgview.setFitHeight(240);
		hbox2.getChildren().add(listviewmsgs);
		hbox2.getChildren().add(imgview);
		hbox2.setPadding(new Insets(10));
		hbox2.setSpacing(10);
		
		Scene scene = new Scene(borderPane, 800, 500);
		primaryStage.setScene(scene);
		primaryStage.show();
		
		/***  boutons ***/
		comboboximages.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				File f3=new File("images/"+newValue);
				Image image = new Image(f3.toURI().toString());
				imgview.setImage(image);	
			}
		
		});
		
		buttonConnecter.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				try {
					codeUser=texfieldcode.getText();
					String host=texfieldhost.getText();
					int port=Integer.parseInt(texfieldport.getText());
					ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://"+host+":"+port);
					Connection connection = connectionFactory.createConnection();
					connection.start();
					session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
					Destination destination = session.createTopic("chat.topic");
					MessageConsumer consumer = session.createConsumer(destination,"code='"+codeUser+"'");
					msgproducer=session.createProducer(destination);
					msgproducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
					consumer.setMessageListener(message->{
							try {
								if(message instanceof TextMessage) {
								TextMessage textmessage = (TextMessage)message;
								observListmsgs.add(message.getStringProperty("sender")+" said :"+textmessage.getText());
								}else if(message instanceof StreamMessage) {
									StreamMessage streamMess = (StreamMessage) message;
									String nomPhoto = streamMess.readString();
									observListmsgs.add("reception photo:"+nomPhoto);
									int sizephoto = streamMess.readInt();
									byte[] data= new byte[sizephoto];
									streamMess.readBytes(data);
									ByteArrayInputStream bis = new ByteArrayInputStream(data); //on construit un stream à partir d'un tableau d'octets
									Image image = new Image(bis);
									imgview.setImage(image);
								}
							} catch (JMSException e) {
								e.printStackTrace();
							}
						
					});
					hbox.setDisable(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		
		buttonEnvoyer.setOnAction(event -> {
			try {
				TextMessage textmsg=session.createTextMessage();
				textmsg.setText(textareamessage.getText());
				textmsg.setStringProperty("code", texfieldto.getText());
				textmsg.setStringProperty("sender", texfieldcode.getText());
				msgproducer.send(textmsg);
					observListmsgs.add("me : "+textareamessage.getText());
				
			} catch (JMSException e) {
				e.printStackTrace();
			}
		});
		
		
		buttonEnvoyerImage.setOnAction(event -> {
			try {
				StreamMessage streamMess = session.createStreamMessage();
				streamMess.setStringProperty("code", texfieldto.getText());
				File f4 = new File("images/"+comboboximages.getSelectionModel().getSelectedItem());
				FileInputStream fis = new FileInputStream(f4);
				byte[]data=new byte[(int)f4.length()]; //j'ai un tableau de byte dont la taille est la taille du fichier(ex:si fichier de 4M alors tableau de 4M)
				fis.read(data); //et on transfère les données de fis vers le tableau de bytes data
				streamMess.writeString(comboboximages.getSelectionModel().getSelectedItem());
				streamMess.writeInt(data.length);
				streamMess.writeBytes(data);
				msgproducer.send(streamMess);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
	}

}
