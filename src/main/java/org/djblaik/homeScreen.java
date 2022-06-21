package org.djblaik;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.djblaik.parameterFilter.ParamFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;


@SuppressWarnings({"unused", "Convert2Lambda"})
public class homeScreen {
    private static HttpServer server;
    private JButton serverStart;
    private JPanel mainPanel;
    private JButton serverStop;
    private JLabel serverStatusTitle;
    private JTextField portNo;

    private JLabel portNolabel;
    private JLabel serverStatus;
    private JButton receivingButton;
    private JLabel receivingLabel;
    private static boolean serverState;
    private static final String POST_CSV_FILE = "fosPost.csv";
    private static final String GET_CSV_FILE = "fosGet.csv";
    private static final ResourceBundle FosEn = ResourceBundle.getBundle("Fos_En");

    public homeScreen() {
        serverStart.addActionListener(new ActionListener() { //handle start button click
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    startServer();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });
        serverStop.addActionListener(new ActionListener() { //handle stop button click
            @Override
            public void actionPerformed(ActionEvent e) {
                stopServer();
            }
        });
    }

    //create the main window
    public static void main(String[] args) {
        ImageIcon logo = new ImageIcon(Objects.requireNonNull(homeScreen.class.getClassLoader().getResource("weatherstationicon.png")));
        JFrame frame = new JFrame(FosEn.getString("fos.logger"));
        frame.setContentPane(new homeScreen().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(logo.getImage());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    //create the server
    private void createServer() throws IOException {
        int port = Integer.parseInt(portNo.getText());
        server = HttpServer.create(new InetSocketAddress(port), 0);
        HttpContext context = server.createContext("/");
        context.getFilters().add(new ParamFilter());
        server.setExecutor(null);
        context.setHandler(this::handleRequest);
    }
    //handle incoming requests
    private void handleRequest(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, 0);
        OutputStream os = exchange.getResponseBody();
        @SuppressWarnings("unchecked")LinkedHashMap<String, String> params = (LinkedHashMap<String, String>) exchange.getAttribute(FosEn.getString("parameters"));
        if(exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            WritePostCsv(params);
        }
        else {
            WriteGetCsv(params);
        }
        os.close();
    }
    //switch the server on and off
        private void startServer() throws IOException {
            if (!serverState){
                createServer();
                server.start();
                serverState = true;
                serverStatus.setText(FosEn.getString("server.started"));

            }
        }

        private void stopServer() {
            if (serverState) {
                server.stop(0);
                serverState = false;
                serverStatus.setText(FosEn.getString("server.stopped"));
            }
        }

    //Write post data to csv file
        private void WritePostCsv(LinkedHashMap<String, String> params) throws IOException {
           //blink the receiver button light
            try {
                receivingButton.setBackground(Color.green);
                Thread.sleep(600);
                receivingButton.setBackground(Color.black);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Path path = Path.of(POST_CSV_FILE); //get the file path
            List<String> headers = params.keySet().stream().toList();
            List<String> valueList = params.values().stream().toList();
            BufferedWriter writer;
            //check if the file exists, if not create a new file
            if (!Files.exists(path)) {
                writer = Files.newBufferedWriter(path);
                    CSVPrinter csvPrinter = new CSVPrinter(writer,CSVFormat.Builder.create().build());
                    csvPrinter.printRecord(headers);
                    csvPrinter.printRecord(valueList);

                    csvPrinter.flush();
                    csvPrinter.close();
            }   else {
                //write the data to the file
                writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND);
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.Builder.create().build());
                    List<String> valueList2 = params.values().stream().toList();
                    csvPrinter.printRecord(valueList2);

                    csvPrinter.flush();
                    csvPrinter.close();
            }
            writer.close();
        }
   //write get data to file
    private void WriteGetCsv(LinkedHashMap<String, String> params) throws IOException {
        //blink the receiver button light
        try {
            receivingButton.setBackground(Color.green);
            Thread.sleep(600);
            receivingButton.setBackground(Color.black);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Path path = Path.of(GET_CSV_FILE);
        List<String> headers = params.keySet().stream().toList();
        List<String> valueList = params.values().stream().toList();
        //check if the file exists, if not create a new file
        if (!Files.exists(path)) {
            BufferedWriter writer = Files.newBufferedWriter(path);
            CSVPrinter csvPrinter = new CSVPrinter(writer,CSVFormat.Builder.create().build());
            csvPrinter.printRecord(headers);
            csvPrinter.printRecord(valueList);

            csvPrinter.flush();
            csvPrinter.close();
            writer.close();
        }   else {
            //write the data to file
            BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND);
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.Builder.create().build());
            List<String> valueList2 = params.values().stream().toList();
            csvPrinter.printRecord(valueList2);

            csvPrinter.flush();
            csvPrinter.close();
            writer.close();
        }
    }
}






