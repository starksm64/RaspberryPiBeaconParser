import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.stage.Stage;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class PlotBatteryRSSI {
   static public class RSSIChart extends Application {
      private static List<Data<Long, Integer>> data;

      @Override
      public void start(Stage stage) {
         stage.setTitle("Beacon RSSI");
         //defining the axes
         final NumberAxis xAxis = new NumberAxis();
         final NumberAxis yAxis = new NumberAxis();
         xAxis.setLabel("Minute");
         xAxis.setAutoRanging(true);
         xAxis.setForceZeroInRange(false);
         //creating the chart
         final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);

         lineChart.setTitle("Beacon RSSI");
         //defining a series
         XYChart.Series series = new XYChart.Series();
         series.setName("RSSI");
         //populating the series with data
         series.getData().addAll(data);

         Scene scene = new Scene(lineChart, 800, 600);
         lineChart.getData().add(series);

         stage.setScene(scene);
         stage.show();
      }

      public static void run(String[] args, List<Data<Long, Integer>> data) {
         RSSIChart.data = data;
         launch(args);
      }
   }

   public static void main(String[] args) throws Exception {
      String destinationName = "rawHeartbeatEvents_60";
      // Local connection
      Properties props = new Properties();
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
      props.setProperty("connectionfactory.myFactoryLookup", "amqp://192.168.1.107:5672");
      Context context = new InitialContext(props);
      ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
      Connection connection = factory.createConnection("guest", "guest");
      connection.start();
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      System.out.printf("Connected to broker\n");
      Queue destination = session.createQueue(destinationName);
      List<Data<Long, Integer>> data = browseData(session, destination);
      RSSIChart.run(args, data);
      session.close();
      connection.close();
   }

   private static List<Data<Long, Integer>> browseData(Session session, Queue destination) throws JMSException {
      List<Data<Long, Integer>> data = new ArrayList<>();
      QueueBrowser browser = session.createBrowser(destination);
      Enumeration<Message> msgs = browser.getEnumeration();
      Long minTime = Long.MAX_VALUE;
      Long maxTime = Long.MIN_VALUE;
      while (msgs.hasMoreElements()) {
         Message msg = msgs.nextElement();
         Integer rssi = msg.getIntProperty("rssi");
         Long time = msg.getLongProperty("time");
         minTime = Math.min(minTime, time);
         maxTime = Math.max(maxTime, time);
         data.add(new Data<>(time, rssi));
      }
      browser.close();
      long duration = maxTime - minTime;
      long hours = duration / (3600*1000);
      double days = hours / 24.0;
      System.out.printf("minTime=%s, maxTime=%s, hours=%d, days=%.2f\n", minTime, maxTime, hours, days);
      return data;
   }
}
