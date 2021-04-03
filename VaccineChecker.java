import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

public class VaccineChecker {

    private static String[] getEmailConf(String filePath) throws FileNotFoundException, IOException, ParseException {
        String[] emailConf = new String[8];
        JSONParser parser = new JSONParser();
        JSONObject jsonData = (JSONObject) parser.parse(new FileReader(filePath));
        JSONObject email = (JSONObject) jsonData.get("email");
        if (email.containsKey("fromName") && email.containsKey("fromEmail") && email.containsKey("toName") && email.containsKey("toEmail") && email.containsKey("subject") && email.containsKey("smtpServer") && email.containsKey("smtpPort") && email.containsKey("smtpPassword")) {
            emailConf[0] = email.get("fromName").toString();
            emailConf[1] = email.get("fromEmail").toString();
            emailConf[2] = email.get("toName").toString();
            emailConf[3] = email.get("toEmail").toString();
            emailConf[4] = email.get("subject").toString();
            emailConf[5] = email.get("smtpServer").toString();
            emailConf[6] = email.get("smtpPort").toString();
            emailConf[7] = email.get("smtpPassword").toString();
        } else {
            System.err.println("Vaccine Checker: Missing or invalid email setting. Please check config file!");
            System.exit(1);
        }

        return emailConf;
    }

    private static void sendEmail(String url, String name, List < String > emailAddress, String file) throws FileNotFoundException, IOException, ParseException {
        String[] emailConf = getEmailConf(file);
        String plainMessage = "There seems to be a COVID-19 Vaccine available!\n" +
            "Please visit: " + url + "\n" +
            "Good Luck!";
        String htmlMessage = "<p>There seems to be a COVID-19 Vaccine available!</p></br>" +
            "<p>Please click <a href=" + url + ">here</a> to see where the appointment is!</p></br>" +
            "<p>Good Luck!</p>";
        Mailer mailer = MailerBuilder
            .withSMTPServer(emailConf[5], Integer.parseInt(emailConf[6]), emailConf[1], emailConf[7])
            .buildMailer();
        Email email = EmailBuilder.startingBlank()
            .from(emailConf[0], emailConf[1])
            .to(emailConf[2], emailConf[3])
            .bcc(name, emailAddress)
            .withSubject(emailConf[4])
            .withPlainText(plainMessage)
            .withHTMLText(htmlMessage)
            .buildEmail();
        mailer.sendMail(email);
    }

    private static boolean findAppointments(WebDriver browser, String url, String date) throws java.text.ParseException {
    	browser.get(url);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.out.println("Thread sleep issue");
            e.printStackTrace();
        }
        if (browser.findElements(By.className("alert-warning")).size() > 0) {
            return false;
        }
        List < WebElement > results = browser.findElements(By.className("location-result"));
        if (date.equals("")) {
            if (results.size() > 0) {
                return true;
            }
        } else {
            for (WebElement location: results) {
                for (WebElement locationDate: location.findElements(By.className("mb-3"))) {
                    Date curDate = new SimpleDateFormat("MM/dd/yyyy").parse(locationDate.getText());
                    Date aptDate = new SimpleDateFormat("MM/dd/yyyy").parse(date);
                    if (aptDate.compareTo(curDate) <= 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void main(String[] args) throws InterruptedException, FileNotFoundException, IOException, ParseException, java.text.ParseException {
        if (args.length < 2) {
            System.err.println("Vaccine Checker: Too litle arguments provided!");
            System.exit(1);
        } else if (args.length > 2) {
            System.err.println("Vaccine Checker: Too many arguments provided!");
            System.exit(1);
        }
        System.setProperty("webdriver.edge.driver", args[1]);
        String baseUrl = "https://www.vaccinespotter.org/";
        JSONParser parser = new JSONParser();
        JSONObject jsonData = (JSONObject) parser.parse(new FileReader(args[0]));
        JSONArray people = (JSONArray) jsonData.get("people");
        if (!people.isEmpty()) {
            EdgeOptions options = new EdgeOptions();
            options.addArguments("headless");
            options.addArguments("disable-gpu");
            WebDriver browser = new EdgeDriver(options);
            for (int i = 0; i < people.size(); i++) {
                int zipcode = 0;
                String dose = "";
                String radius = "any";
                String vaccine = "";
                String name = "";
                String state = "";
                String date = "";
                JSONObject person = (JSONObject) people.get(i);
                JSONArray jsonEmails = (JSONArray) person.get("emails");
                List < String > emails = new ArrayList < String > ();
                for (int j = 0; j < jsonEmails.size(); j++) {
                    emails.add(jsonEmails.get(j).toString());
                }
                name = person.get("name").toString();
                zipcode = Integer.parseInt(person.get("zipcode").toString());
                state = person.get("state").toString();
                if (person.containsKey("vaccine")) {
                    vaccine = person.get("vaccine").toString();
                }
                if (person.containsKey("radius")) {
                    radius = person.get("radius").toString();
                }
                if (person.containsKey("dose")) {
                    dose = person.get("dose").toString();
                }
                if (person.containsKey("date")) {
                    date = person.get("date").toString();
                }
                String url = baseUrl + state + "/?vaccine_type=" + vaccine + "&zip=" + zipcode + "&radius=" + radius + "&appointment_type=" + dose;
                if (findAppointments(browser, url, date)) {
                	System.out.println(name);
                    //sendEmail(url, name, emails, args[0]);
                } else {
                    System.out.println("No Appointments for " + name + "!");
                }
            }
            browser.close();
        } else {
            System.out.println("No people to check!");
        }

    }
}