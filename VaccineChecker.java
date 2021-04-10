import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

/**
 * A Java Program that scrapes https://vaccinespotter.org using Selenium
 * with a personalized URL, based on supplied criteria in a JSON config file,
 * to see if a vaccine appointment might be available. If so, the program will
 * send an email or emails to the desired person or people in the JSON config file
 * letting them know of the possible appointment and with a link to check the site
 * and book an appointment
 * @author Jack N. Stockley
 * 
 * @version 1.5
 *
 */

public class VaccineChecker {

	/**
	 * Adds an email account to the passed config file. Helps with sending an email
	 * when an appointment might be available.
	 * @param reader The BufferedReader to help with getting input from the console
	 * to pass to the JSON config file
	 * @param file A valid JSON file path to store the email config to
	 * @param update True if the program is being called from an update function, false otherwise
	 */
	@SuppressWarnings("unchecked")
	private static void addEmail(BufferedReader reader, String file, boolean update) {
		// Console for password reading when not in IDE
		Console con = System.console();
		// Prompt user for required info
		System.out.print("What's the from name: ");
		String fromName = "";
		try {
			fromName = reader.readLine();
		} catch (IOException e1) {
			System.err.println("From Name not valid! Exiting...");
			System.exit(1);
		}
		System.out.print("What's the from email: ");
		String fromEmail = "";
		try {
			fromEmail = reader.readLine();
		} catch (IOException e1) {
			System.err.println("From Email not valid! Exiting...");
			System.exit(1);
		}
		System.out.print("What's the to name: ");
		String toName = "";
		try {
			toName = reader.readLine();
		} catch (IOException e1) {
			System.err.println("To Name not valid! Exiting...");
			System.exit(1);
		}
		System.out.print("What's the to email: ");
		String toEmail = "";
		try {
			toEmail = reader.readLine();
		} catch (IOException e1) {
			System.err.println("To Email not valid! Exiting...");
			System.exit(1);
		}
		System.out.print("What's the subject line: ");
		String subject = "";
		try {
			subject = reader.readLine();
		} catch (IOException e1) {
			System.err.println("Subject Line not valid! Exiting...");
			System.exit(1);
		}
		System.out.print("What's the SMTP server: ");
		String smtpServer = "";
		try {
			smtpServer = reader.readLine();
		} catch (IOException e1) {
			System.err.println("SMTP Server not valid! Exiting...");
			System.exit(1);
		}
		System.out.print("What's the SMTP port: ");
		String smtpPort = "";
		try {
			smtpPort = reader.readLine();
		} catch (IOException e1) {
			System.err.println("SMTP Port not valid! Exiting...");
			System.exit(1);
		}
		System.out.print("What's the SMTP password: ");
		String smtpPassword = "";
		// Check if program is run in eclipse to know if password reader can be used
		if (System.getenv("eclipse42") != null) {
			try {
				smtpPassword = reader.readLine();
			} catch (IOException e) {
				System.err.println("SMTP password not valid! Exiting...");
				System.exit(1);
			}
		} else {
			smtpPassword = String.valueOf(con.readPassword());
		}
		JSONObject email = new JSONObject();
		// Add the data inputed to JSONObject
		email.put("fromName", fromName);
		email.put("fromEmail", fromEmail);
		email.put("toName", toName);
		email.put("toEmail", toEmail);
		email.put("subject", subject);
		email.put("smtpServer", smtpServer);
		email.put("smtpPort", smtpPort);
		email.put("smtpPassword", smtpPassword);
		// Makes sure the file exists and it's not empty and then writes data to file
		if (new File(file).exists() && new File(file).length() != 0) {
			JSONParser parser = new JSONParser();
			JSONObject jsonData = null;
			try {
				jsonData = (JSONObject) parser.parse(new FileReader(file));
			} catch (FileNotFoundException e) {
				System.err.println(file + " not found! Exiting...");
				System.exit(1);
			} catch (IOException e) {
				System.err.println(file + " is not valid! Exiting...");
				System.exit(1);
			} catch (ParseException e) {
				System.err.println(file + " is not valid JSON! Exiting...");
				System.exit(1);
			}
			if (!jsonData.isEmpty() && jsonData.containsKey("email")) {
				System.err.println("VaccineCheckerSetup: Email object already present, please remove or change!");
				System.exit(1);
			}
			jsonData.put("email", email);
			FileWriter writer = null;
			try {
				writer = new FileWriter(file);
			} catch (IOException e) {
				System.err.println(file + " not found! Exiting...");
				System.exit(1);
			}
			try {
				writer.write(jsonData.toJSONString());
				writer.close();
			} catch (IOException e) {
				System.err.println("Error writing to " + file + "! Exiting...");
				System.exit(1);
			}
			// Checks if file is empty and writes data to file
		} else if (new File(file).length() == 0) {
			FileWriter writer = null;
			try {
				writer = new FileWriter(file);
			} catch (IOException e) {
				System.err.println(file + " not found! Exiting...");
				System.exit(1);
			}
			JSONObject data = new JSONObject();
			data.put("email", email);
			try {
				writer.write(data.toJSONString());
				writer.close();
			} catch (IOException e) {
				System.err.println("Error writing to " + file + "! Exiting...");
				System.exit(1);
			}
			// Checks if the function was called from an update function
			if (update) {
				System.out.println("Email has been changed!");
			} else {
				System.out.println("Email has been added!");
			}
			// Gives the user an error saying their is no file.
		} else {
			System.err.println("File doesn't exist, please create the file");
			System.exit(1);
		}

	}

	/**
	 * Program to allow updating email account config, runs removeEmail and then addEmail
	 * @param reader The BufferedReader to help with getting input from the console
	 * to pass to the JSON config file
	 * @param file A valid JSON file path to store the email config to
	 */
	private static void changeEmail(BufferedReader reader, String file) {
		removeEmail(reader, file, true);
		addEmail(reader, file, true);
	}

	/**
	 * Removes an email account from the passed config file.
	 * @param reader The BufferedReader to help with getting input from the console
	 * to pass to the JSON config file
	 * @param file A valid JSON file path to remove the email config from
	 * @param update True if the program is being called from an update function, false otherwise
	 */
	private static void removeEmail(BufferedReader reader, String file, boolean update) {
		// Check to make sure the file exists and the file isn't empty
		if (new File(file).exists() && new File(file).length() != 0) {
			JSONParser parser = new JSONParser();
			JSONObject jsonData = null;
			try {
				jsonData = (JSONObject) parser.parse(new FileReader(file));
			} catch (FileNotFoundException e) {
				System.err.println(file + " not found! Exiting...");
				System.exit(1);
			} catch (IOException e) {
				System.err.println(file + " is not valid! Exiting...");
				System.exit(1);
			} catch (ParseException e) {
				System.err.println(file + " is not valid JSON! Exiting...");
				System.exit(1);
			}
			// Makes sure the file contains the email JSON key which stores the email data
			if (jsonData.containsKey("email")) {
				jsonData.remove("email");
				FileWriter writer = null;
				try {
					writer = new FileWriter(file);
				} catch (IOException e) {
					System.err.println(file + " not found! Exiting...");
					System.exit(1);
				}
				try {
					writer.write(jsonData.toJSONString());
					writer.close();
				} catch (IOException e) {
					System.err.println("Error writing to " + file + "! Exiting...");
					System.exit(1);
				}

				// Checks if the function was called from a update function
				if (!update) {
					System.out.println("Email removed. Make sure to add an email back before running VaccineChecker!");
				}
			} else {
				System.err.println("No emails found in the file, please add an email first!");
				System.exit(1);
			}
		} else {
			System.err.println("File doesn't exist or is empty, please create the file or add data to the file!");
			System.exit(1);
		}
	}

	/**
	 * Adds a person to the passed config file. Checks the person against data scrapped from
	 * vaccinespottter.org to see if a vaccine appointment is available
	 * @param reader The BufferedReader to help with getting input from the console
	 * to pass to the JSON config file
	 * @param file A valid JSON file path to store the email config to
	 * @param update True if the program is being called from an update function, false otherwise
	 */
	@SuppressWarnings("unchecked")
	private static void addPerson(BufferedReader reader, String file, boolean update) {
		// Prompts user for required and optional info
		System.out.print("What's the person's name: ");
		String name = "";
		try {
			name = reader.readLine();
		} catch (IOException e) {
			System.err.println("Person's name is not valid! Exiting...");
			System.exit(1);
		}
		System.out.print("Type all the emails you want assoicated with this person, seperated by a comma, leave empty for no emails: ");
		String email = "";
		try {
			email = reader.readLine();
		} catch (IOException e) {
			System.err.println("Person's email is not valid! Exiting...");
			System.exit(1);
		}
		System.out.print("What's the person's state: ");
		String state = "";
		try {
			state = reader.readLine().toUpperCase();
		} catch (IOException e) {
			System.err.println("Person's state is not valid! Exiting...");
			System.exit(1);
		}
		System.out.print("What's the person's zipcode: ");
		String zipcode = "";
		try {
			zipcode = reader.readLine();
		} catch (IOException e) {
			System.err.println("Person's zipcode is not valid! Exiting...");
			System.exit(1);
		}
		System.out.print("What vaccine does the person want (leave blank for any): ");
		String vaccine = "";
		try {
			vaccine = reader.readLine().toLowerCase();
		} catch (IOException e) {
			System.err.println("Person's vaccine type is not valid! Exiting...");
			System.exit(1);
		}
		if (vaccine.contains("johnson")) {
			vaccine = "jj";
		}
		System.out.print("What radius to search for (leave blank for any): ");
		String radius = "";
		try {
			radius = reader.readLine();
		} catch (IOException e) {
			System.err.println("Person's search radius is not valid! Exiting...");
			System.exit(1);
		}
		System.out.print("Does the person only need the second dose (Y\\N): ");
		String dose = "";
		try {
			dose = reader.readLine().toLowerCase();
		} catch (IOException e) {
			System.err.println("Person's dose type is not valid! Exiting...");
			System.exit(1);
		}
		if (dose.equals("y")) {
			dose = "2nd_dose_only";
		} else {
			dose = "";
		}
		System.out.print("Enter a date for a vaccine (leave blank for any): ");
		String date = "";
		try {
			date = reader.readLine();
		} catch (IOException e) {
			System.err.println("Person's vaccine date is not valid! Exiting...");
			System.exit(1);
		}
		// Puts the required data into the JSONObject
		JSONObject person = new JSONObject();
		person.put("name", name);
		person.put("state", state);
		person.put("zipcode", zipcode);
		// Checks if optional data was provided and if so, adds it to the JSONObject
		if (!email.equals("")) {
			List < String > emails = Arrays.asList(email.split("\\s*,\\s*"));
			person.put("emails", emails);
		}
		if (!vaccine.equals("")) {
			person.put("vaccine", vaccine);
		}
		if (!radius.equals("")) {
			person.put("radius", radius);
		}
		if (!dose.equals("")) {
			person.put("dose", dose);
		}
		if (!date.equals("")) {
			person.put("date", date);
		}
		// Makes sure the file exists and that the file isn't empty
		if (new File(file).exists() && new File(file).length() != 0) {
			JSONParser parser = new JSONParser();
			JSONObject jsonData = null;
			try {
				jsonData = (JSONObject) parser.parse(new FileReader(file));
			} catch (FileNotFoundException e) {
				System.err.println(file + " not found! Exiting...");
				System.exit(1);
			} catch (IOException e) {
				System.err.println(file + " not valid! Exiting...");
				System.exit(1);
			} catch (ParseException e) {
				System.err.println(file + " is not valid JSON! Exiting...");
				System.exit(1);
			}
			FileWriter writer = null;
			try {
				writer = new FileWriter(file);
			} catch (IOException e) {
				System.err.println(file + " is not found! Exiting...");
				System.exit(1);
			}
			JSONArray jsonArray = new JSONArray();
			if (jsonData.containsKey("people")) {
				jsonArray = (JSONArray) jsonData.get("people");
			}
			if (!jsonData.isEmpty() && !jsonArray.isEmpty()) {
				jsonArray.add(person);
				jsonData.remove("people");
				jsonData.put("people", jsonArray);
			} else {
				JSONArray personArray = new JSONArray();
				personArray.add(person);
				jsonData.put("people", personArray);
			}
			try {
				writer.write(jsonData.toJSONString());
				writer.close();
			} catch (IOException e) {
				System.err.println("Error writing to " + file + "! Exiting...");
				System.exit(1);
			}
			// Checks if the file is empty
		} else if (new File(file).length() == 0) {
			FileWriter writer = null;
			try {
				writer = new FileWriter(file);
			} catch (IOException e) {
				System.err.println(file + " is not found! Exiting...");
				System.exit(1);
			}
			JSONObject data = new JSONObject();
			JSONArray personArray = new JSONArray();
			personArray.add(person);
			data.put("people", personArray);
			try {
				writer.write(data.toJSONString());
				writer.close();
			} catch (IOException e) {
				System.err.println("Error writing to " + file + "! Exiting...");
				System.exit(1);
			}
			// Checks if the function was called from an update function
			if (update) {
				System.out.println("Person has been changed!");
			} else {
				System.out.println("Person has been added!");
			}
		} else {
			System.err.println("File doesn't exist, please create the file");
			System.exit(1);
		}
	}

	/**
	 * Program to allow updating people check, runs removePerson and then addPerson
	 * @param reader The BufferedReader to help with getting input from the console
	 * to pass to the JSON config file
	 * @param file A valid JSON file path to store the email config to
	 */
	private static void changePerson(BufferedReader reader, String file) {
		removePerson(reader, file, true);
		addPerson(reader, file, true);
	}

	/**
	 * Removes a person from the passed config file.
	 * @param reader The BufferedReader to help with getting input from the console
	 * to pass to the JSON config file
	 * @param file A valid JSON file path to remove the email config from
	 * @param update True if the program is being called from an update function, false otherwise
	 */
	@SuppressWarnings("rawtypes")
	private static void removePerson(BufferedReader reader, String file, boolean update) {
		// Checks to make sure the file exists and isn't empty
		if (new File(file).exists() && new File(file).length() != 0) {
			JSONParser parser = new JSONParser();
			JSONObject jsonData = null;
			try {
				jsonData = (JSONObject) parser.parse(new FileReader(file));
			} catch (FileNotFoundException e) {
				System.err.println(file + " is not found! Exiting...");
				System.exit(1);
			} catch (IOException e) {
				System.err.println(file + " is not valid! Exiting...");
				System.exit(1);
			} catch (ParseException e) {
				System.err.println(file + " is not valid JSON! Exiting...");
				System.exit(1);
			}
			// Checks to make sure the person key is in the JOSN file
			if (jsonData.containsKey("people")) {
				JSONArray people = (JSONArray) jsonData.get("people");
				System.out.println("Select person to remove from below: ");
				for (int i = 0; i < people.size(); i++) {
					System.out.println(i + 1 + ". " + new JSONObject((Map) people.get(i)).get("name"));
				}
				System.out.print("Option: ");
				int option = -999;
				try {
					option = Integer.parseInt(reader.readLine()) - 1;
				} catch (NumberFormatException e) {
					System.err.println(option + " is not a valid number! Exiting...");
					System.exit(1);
				} catch (IOException e) {
					System.err.println(option + " is not valid! Exiting...");
					System.exit(1);
				}
				people.remove(option);
				FileWriter writer = null;
				try {
					writer = new FileWriter(file);
				} catch (IOException e) {
					System.err.println(file + " is not found! Exiting...");
					System.exit(1);
				}
				try {
					writer.write(jsonData.toJSONString());
					writer.close();
				} catch (IOException e) {
					System.err.println("Error writing to " + file + "! Exiting...");
					System.exit(1);
				}
				// Checks if the function was called from an update function
				if (!update) {
					System.out.println("Person removed. Make sure to have at least one person to be checked!");
				}
			} else {
				System.err.println("No emails found in the file, please add an email first!");
				System.exit(1);
			}
		} else {
			System.err.println("File doesn't exist or is empty, please create the file or add data to the file!");
			System.exit(1);
		}
	}

	/**
	 * Helper function to help with upgrading old config file if new features are added
	 * @param file The old JSON config file to be updated
	 */
	@SuppressWarnings("unchecked")
	private static void upgradeConfig(String file) {
		// Checks if the file exits and it isn't empty
		if (new File(file).exists() && new File(file).length() != 0) {
			JSONParser parser = new JSONParser();
			JSONObject jsonData = null;
			try {
				jsonData = (JSONObject) parser.parse(new FileReader(file));
			} catch (FileNotFoundException e) {
				System.err.println(file + " is not found! Exiting...");
				System.exit(1);
			} catch (IOException e) {
				System.err.println(file + " is not valid! Exiting...");
				System.exit(1);
			} catch (ParseException e) {
				System.err.println(file + " is not valid JSON! Exiting...");
				System.exit(1);
			}
			// Checks if the file is an old version that doesn't have a version key
			if (!jsonData.containsKey("version")) {
				jsonData.put("version", "1.0");
				// Upgrades the JSON file to support new features
				JSONArray people = (JSONArray) jsonData.get("people");
				for (int i = 0; i < people.size(); i++) {
					JSONObject person = (JSONObject) people.get(i);
					String email = person.get("email").toString();
					JSONArray emails = new JSONArray();
					emails.add(email);
					person.remove("email");
					person.put("emails", emails);
					if (person.containsKey("dose")) {
						person.remove("dose");
						person.put("dose", "2nd_dose_only");
					}
				}
				jsonData.remove("people");
				jsonData.put("people", people);
				FileWriter writer = null;
				try {
					writer = new FileWriter(file);
				} catch (IOException e) {
					System.err.println(file + " is not found! Exiting...");
					System.exit(1);
				}
				try {
					writer.write(jsonData.toJSONString());
					writer.close();
				} catch (IOException e) {
					System.err.println("Error writing to " + file + "! Exiting...");
					System.exit(1);
				}
				System.out.println("Config file has been updated!");
				System.exit(0);
			} else {
				System.out.println("Config file is already up to date!");
				System.exit(0);
			}
		} else {
			System.err.println("File doesn't exit or is empty, please create the file or add data to the file!");
			System.exit(1);
		}
	}

	/**
	 * Helper function to help with setting up a JSON config file to work with Vaccine Checker program
	 * @param filepath The JSON config file path to be modified
	 */
	private static void setup(String filepath) {
		// Prompts the user for the action they want to run
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("What do you want to do:");
		System.out.println("1. Add email account");
		System.out.println("2. Change email account");
		System.out.println("3. Remove email account");
		System.out.println("4. Add person to check");
		System.out.println("5. Change person being checked");
		System.out.println("6. Remove person being checked");
		System.out.println("7. Upgrade config file");
		System.out.print("Option: ");
		int option = -999;
		try {
			option = Integer.parseInt(reader.readLine());
		} catch (NumberFormatException e) {
			System.err.println(option + " is not a valid number! Exiting...");
			System.exit(1);
		} catch (IOException e) {
			System.err.println(option + " is not valid! Exiting...");
			System.exit(1);
		}
		// Switch statement to help direct the program to the correct function
		switch (option) {
		case 1:
			addEmail(reader, filepath, false);
			break;
		case 2:
			changeEmail(reader, filepath);
			break;
		case 3:
			removeEmail(reader, filepath, false);
			break;
		case 4:
			addPerson(reader, filepath, false);
			break;
		case 5:
			changePerson(reader, filepath);
			break;
		case 6:
			removePerson(reader, filepath, false);
			break;
		case 7:
			upgradeConfig(filepath);
			break;
		default:
			System.err.println("VaccineCheckerSetup: Option in not valid");
			System.exit(1);
		}
	}

	/**
	 * Helper function to get the required data to send an email if a vaccine is available
	 * @param filePath The JSON config file with the required data
	 * @return A string array with the data from the JSON config file
	 */
	private static String[] getEmailConf(String filePath) {
		String[] emailConf = new String[8];
		JSONParser parser = new JSONParser();
		JSONObject jsonData = null;
		try {
			jsonData = (JSONObject) parser.parse(new FileReader(filePath));
		} catch (FileNotFoundException e) {
			System.err.println(filePath + " is not found! Exiting...");
			System.exit(1);
		} catch (IOException e) {
			System.err.println(filePath + " is not valid! Exiting...");
			System.exit(1);
		} catch (ParseException e) {
			System.err.println(filePath + " is not valid JSON! Exiting...");
			System.exit(1);
		}
		JSONObject email = (JSONObject) jsonData.get("email");
		// Makes sure the JSON file has the required data
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
			System.err.println("Missing or invalid email setting. Please check config file!");
			System.exit(1);
		}
		return emailConf;
	}

	/**
	 * Sends an email to the user(s) that a vaccine appointment might be available
	 * @param url Personalized website url which shows the user where, when, and type of vaccine that might be available.
	 * Link direct user to https://vaccinespotter.org
	 * @param name The name of person who might have an appointment that meets their criteria
	 * @param emailAddress The extra email addresses that will be BCC, if any, the same email
	 * @param file The JSON config file with email data
	 */
	private static void sendEmail(String url, String name, List < String > emailAddress, String file) {
		// Gets the email data
		String[] emailConf = getEmailConf(file);
		// Sets up message for plain text emails
		String plainMessage = "Hello " + name + ",\nThere seems to be a COVID-19 Vaccine available!\n" +
				"Please visit: " + url + "\n" +
				"Good Luck!";
		// Sets up message for HTTML emails
		String htmlMessage = "<p>Hello " + name + ",</p><br>" +
				"<p>There seems to be a COVID-19 Vaccine available!</p><br>" +
				"<p>Please click <a href=" + url + ">here</a> to see where the appointment is!</p><br>" +
				"<p>Good Luck!</p>";
		// Sets up email server and account
		Mailer mailer = MailerBuilder
				.withSMTPServer(emailConf[5], Integer.parseInt(emailConf[6]), emailConf[1], emailConf[7])
				.buildMailer();
		// Build the email and sends it
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

	/**
	 * Scraps https://vaccinespotter.org for available appointments based on persons criteria
	 * @param browser WebDriver which is used to scrap the web site and find appointments
	 * @param url Personalized url to https://vaccinespotter.org which is used to determine
	 * if an appointment is available based on their criteria
	 * @param date Optional date of when the user needs to get the vaccine
	 * @return True if a vaccine might be available otherwise false
	 */
	private static boolean findAppointments(WebDriver browser, String url, String date) {
		// Launches the website and waits 5 seconds for it to load
		browser.get(url);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			System.out.println("Thread sleep issue");
			e.printStackTrace();
		}
		// Checks if a 'no appointment' warning if displayed
		if (browser.findElements(By.className("alert-warning")).size() > 0) {
			return false;
		}
		// Creates a list of all 'available' appointments
		List < WebElement > results = browser.findElements(By.className("location-result"));
		// Checks if date is empty and determines if size is greater than 0 and returns true
		if (date.equals("")) {
			if (results.size() > 0) {
				return true;
			}
			// Checks to make sure the date is equal to or later then supplied date and returns 
			// true if at least one appointment is valid
		} else {
			for (WebElement location: results) {
				for (WebElement locationDate: location.findElements(By.className("mb-3"))) {
					Date curDate = null;
					try {
						curDate = new SimpleDateFormat("MM/dd/yyyy").parse(locationDate.getText());
					} catch (java.text.ParseException e) {
						System.err.println(locationDate.getText() + " is not a valid date! Exiting...");
						System.exit(1);
					}
					Date aptDate = null;
					try {
						aptDate = new SimpleDateFormat("MM/dd/yyyy").parse(date);
					} catch (java.text.ParseException e) {
						System.err.println(date + " is not a valid date! Exiting...");
						System.exit(1);
					}
					if (aptDate.compareTo(curDate) <= 0) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * The main function to run the program
	 * @param args Arguments to run the program. Requires at least one.
	 * If only one argument is supplied, it should be a JSON config file
	 * and will run the setup part of the program.
	 * If two arguments are provided they should be a JSON config file and
	 * a msedgedriver, in that order, this will run the checking part of the program.
	 * If a third argument supplied the program will check for appointment but will not
	 * send an email, this should only be used for testing purposes.
	 */
	public static void main(String[] args) {
		// Check if program needs to run setup part
		if (args.length == 1) {
			setup(args[0]);
			System.exit(0);
			// Checks it no arguments are provided and exits
		} else if (args.length == 0) {
			System.err.println("Vaccine Checker: Too litle arguments provided!");
			System.exit(1);
			// Checks if more then 3 arguments are provided and exits
		} else if (args.length > 3) {
			System.err.println("Vaccine Checker: Too many arguments provided!");
			System.exit(1);
		}
		boolean testing = false;
		// If 3 arguments are provided, sets testing to true
		if (args.length == 3) {
			testing = true;
		}
		// Sets up WebDriver and JSONParser
		System.setProperty("webdriver.edge.driver", args[1]);
		String baseUrl = "https://www.vaccinespotter.org/";
		JSONParser parser = new JSONParser();
		JSONObject jsonData = null;
		//Reads data from JSON config
		try {
			jsonData = (JSONObject) parser.parse(new FileReader(args[0]));
		} catch (FileNotFoundException e) {
			System.err.println(args[0] + " is not found! Exiting...");
			System.exit(1);
		} catch (IOException e) {
			System.err.println(args[0] + " is not valid! Exiting...");
			System.exit(1);
		} catch (ParseException e) {
			System.err.println(args[0] + " is not valid JSON! Exiting...");
			System.exit(1);
		}
		// Makes sure at least 1 person is provided in the config file to check
		JSONArray people = (JSONArray) jsonData.get("people");
		if (!people.isEmpty()) {
			EdgeOptions options = new EdgeOptions();
			options.addArguments("headless");
			options.addArguments("disable-gpu");
			// Sets up the WebDriver with optitons
			WebDriver browser = null;
			try {
				browser = new EdgeDriver(options);

			} catch(IllegalStateException e) {
				System.err.println(args[1] + " is not a valid Edge Driver! Exiting...");
				System.exit(1);
			}
			// Parses the data in the JSON config file, and makes a personalized url to check
			for (int i = 0; i < people.size(); i++) {
				int zipcode = 0;
				String dose = "";
				String radius = "any";
				String vaccine = "";
				String name = "";
				String state = "";
				String date = "";
				JSONObject person = (JSONObject) people.get(i);
				List < String > emails = new ArrayList < String > ();
				if (person.containsKey("emails")) {
					JSONArray jsonEmails = (JSONArray) person.get("emails");
					for (int j = 0; j < jsonEmails.size(); j++) {
						emails.add(jsonEmails.get(j).toString());
					}
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
				// Build the url and checks the website to see if appointments are available
				String url = baseUrl + state + "/?vaccine_type=" + vaccine + "&zip=" + zipcode + "&radius=" + radius + "&appointment_type=" + dose;
				// Sends email if true otherwise prints no appointments available
				if (findAppointments(browser, url, date)) {
					// Doesn't send email if testing mode is enabled
					if (testing) {
						System.out.println("Email would be sent, to " + name);
					} else {
						sendEmail(url, name, emails, args[0]);
					}
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