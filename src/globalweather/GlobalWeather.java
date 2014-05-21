package globalweather;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 *
 * @author Alejandro Perez
 * @webServiceUrl http://www.webservicex.com/globalweather.asmx?WSDL
 * 
 */
public class GlobalWeather
{

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException
    {
        // Initialize frame
        JFrame frame = new JFrame("Global Weather");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new GridLayout(4, 1));
        frame.setResizable(false);

        // Title
        JLabel title = new JLabel("Global Weather");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));

        // Labels
        JLabel cityLabel = new JLabel("Select a city:");
        JLabel countryLabel = new JLabel("Type a country:");
        JLabel locationLabel = new JLabel("", JLabel.CENTER);
        JLabel conditionsLabel = new JLabel("", JLabel.CENTER);
        JLabel temperatureLabel = new JLabel("", JLabel.CENTER);

        // Text field
        JComboBox<String> listOfCities = new JComboBox<>();
        JTextField inputCountry = new JTextField("Spain", 15);
        cityLabel.setLabelFor(inputCountry);
        cityLabel.setLabelFor(listOfCities);

        // Panels
        JPanel titlePanel = new JPanel();
        JPanel inputsPanel = new JPanel(new FlowLayout());
        JPanel resultsPanel = new JPanel(new GridLayout(3, 1));

        // Buttons
        JButton submit = new JButton("Get weather!");
        JButton getCities = new JButton("Get cities");

        // Add components to frame
        frame.getContentPane().add(titlePanel);
        frame.getContentPane().add(inputsPanel);
        frame.getContentPane().add(resultsPanel);
        titlePanel.add(title);
        inputsPanel.add(countryLabel);
        inputsPanel.add(inputCountry);
        inputsPanel.add(getCities);
        inputsPanel.add(cityLabel);
        inputsPanel.add(listOfCities);
        inputsPanel.add(submit);
        resultsPanel.add(locationLabel);
        resultsPanel.add(conditionsLabel);
        resultsPanel.add(temperatureLabel);

        // Get cities button event
        getCities.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String countryText = inputCountry.getText();

                if (countryText.isEmpty())
                {
                    JOptionPane.showMessageDialog(null, "Please, enter a country and then click on 'Get cities'.");
                    return;
                }
                else
                {
                    String[] arrayOfCities = (String[]) getCitiesFromXMLResponse(countryText).toArray(new String[0]);
                    if (arrayOfCities.length == 0)
                    {
                        JOptionPane.showMessageDialog(null, "Error loading cities. Click the button again.");
                        return;
                    }
                    else
                    {
                        listOfCities.removeAllItems();

                        for (String city : arrayOfCities)
                        {
                            listOfCities.addItem(city);
                        }

                        frame.revalidate();
                        frame.pack();
                        frame.repaint();
                    }

                }
            }
        });

        // Get weather button event
        submit.addActionListener(new ActionListener()
        {
            // Action to execute when submit button is clicked
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (listOfCities.getItemCount() == 0)
                {
                    JOptionPane.showMessageDialog(null, "Please, first click on 'Get cities'.");
                    return;
                }
                
                String cityText = listOfCities.getSelectedItem().toString();
                String countryText = inputCountry.getText();

                if (countryText.isEmpty())
                {
                    JOptionPane.showMessageDialog(null, "Please, before submitting type a country and then click on 'Get cities'.");
                    return;
                }
                else
                {

                    HashMap weatherData = getWeatherDataFromXMLResponse(cityText, countryText);
                    if (weatherData == null)
                    {
                        JOptionPane.showMessageDialog(null, "Error loading, please check the inputs!");
                        return;
                    }

                    locationLabel.setText(weatherData.get("location").toString().split("\\(")[0].trim()); // get only city name and country
                    locationLabel.setFont(new Font(Font.SERIF, Font.ITALIC + Font.BOLD, 24));
                    conditionsLabel.setText(weatherData.get("conditions").toString().toUpperCase());
                    conditionsLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
                    temperatureLabel.setText(weatherData.get("temperature").toString().split("\\(")[1].split("\\)")[0]); // get temperature between parentheses
                    temperatureLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 18));

                    frame.revalidate();
                    frame.pack();
                    frame.repaint();
                }
            }
        });

        // Show frame
        frame.pack();
        frame.setLocationRelativeTo(null); // center frame in screen
        frame.setVisible(true);
    }

    public static HashMap getWeatherDataFromXMLResponse(String city, String country)
    {
        HashMap<String, String> data = new HashMap<>();

        try
        {
            // Get service response
            String xmlResponse = getWeather(city, country);

            if (xmlResponse.equalsIgnoreCase("Data Not Found"))
            {
                return null;
            }

            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlResponse));

            // Parse XML and get CurrentWeather tag
            Document doc = db.parse(is);
            NodeList nodes = doc.getElementsByTagName("CurrentWeather");

            Element element = (Element) nodes.item(0);

            NodeList location = element.getElementsByTagName("Location");
            Element line = (Element) location.item(0);
            data.put("location", getCharacterDataFromElement(line));

            NodeList skyConditions = element.getElementsByTagName("SkyConditions");
            line = (Element) skyConditions.item(0);
            data.put("conditions", getCharacterDataFromElement(line));

            NodeList temperature = element.getElementsByTagName("Temperature");
            line = (Element) temperature.item(0);
            data.put("temperature", getCharacterDataFromElement(line));
        }
        catch (IOException | ParserConfigurationException | SAXException ex)
        {
            return null;
        }

        return data;
    }

    public static ArrayList getCitiesFromXMLResponse(String country)
    {
        ArrayList<String> listOfCities = new ArrayList<>();

        try
        {
            // Get service response
            String xmlResponse = getCitiesByCountry(country);

            if (xmlResponse.equalsIgnoreCase("Data Not Found"))
            {
                return null;
            }

            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlResponse));

            // Parse XML and get CurrentWeather tag
            Document doc = db.parse(is);
            NodeList nodes = doc.getElementsByTagName("Table");

            for (int i = 0; i < nodes.getLength(); i++)
            {
                Element element = (Element) nodes.item(i);

                NodeList location = element.getElementsByTagName("City");
                Element line = (Element) location.item(0);
                listOfCities.add(getCharacterDataFromElement(line));
            }

        }
        catch (IOException | ParserConfigurationException | SAXException ex)
        {
            return null;
        }

        Collections.sort(listOfCities);

        return listOfCities;
    }

    public static String getCharacterDataFromElement(Element e)
    {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData)
        {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }

    private static String getWeather(java.lang.String cityName, java.lang.String countryName)
    {
        net.webservicex.GlobalWeather service = new net.webservicex.GlobalWeather();
        net.webservicex.GlobalWeatherSoap port = service.getGlobalWeatherSoap();
        return port.getWeather(cityName, countryName);
    }

    private static String getCitiesByCountry(java.lang.String countryName)
    {
        net.webservicex.GlobalWeather service = new net.webservicex.GlobalWeather();
        net.webservicex.GlobalWeatherSoap port = service.getGlobalWeatherSoap();
        return port.getCitiesByCountry(countryName);
    }

}
