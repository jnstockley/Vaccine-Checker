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

public class VaccineChecker {
	
	 @SuppressWarnings("unchecked")
	    private static void addEmail(BufferedReader reader, String file, boolean update) throws IOException, ParseException {
	        Console con = System.console();
	        System.out.print("What's the from name: ");
	        String fromName = reader.readLine();
	        System.out.print("What's the from email: ");
	        String fromEmail = reader.readLine();
	        System.out.print("What's the to name: ");
	        String toName = reader.readLine();
	        System.out.print("What's the to email: ");
	        String toEmail = reader.readLine();
	        System.out.print("What's the subject line: ");
	        String subject = reader.readLine();
	        System.out.print("What's the SMTP server: ");
	        String smtpServer = reader.readLine();
	        System.out.print("What's the SMTP port: ");
	        String smtpPort = reader.readLine();
	        System.out.print("What's the SMTP password: ");
	        String smtpPassword = "";
	        try {
	            smtpPassword = String.valueOf(con.readPassword());
	        } catch (Exception e) {
	            System.out.println("Can't use password reader, defaulting to plaintext reading!");
	            System.out.print("What's the SMTP password: ");
	            smtpPassword = reader.readLine();
	        }
	        JSONObject email = new JSONObject();
	        email.put("fromName", fromName);
	        email.put("fromEmail", fromEmail);
	        email.put("toName", toName);
	        email.put("toEmail", toEmail);
	        email.put("subject", subject);
	        email.put("smtpServer", smtpServer);
	        email.put("smtpPort", smtpPort);
	        email.put("smtpPassword", smtpPassword);
	        if (new File(file).exists() && new File(file).length() != 0) {
	            JSONParser parser = new JSONParser();
	            JSONObject jsonData = (JSONObject) parser.parse(new FileReader(file));
	            if (!jsonData.isEmpty() && jsonData.containsKey("email")) {
	                System.err.println("VaccineCheckerSetup: Email object already present, please remove or change!");
	                System.exit(1);
	            }
	            jsonData.put("email", email);
	            FileWriter writer = new FileWriter(file);
	            writer.write(jsonData.toJSONString());
	            writer.close();
	        } else if (new File(file).length() == 0) {
	            FileWriter writer = new FileWriter(file);
	            JSONObject data = new JSONObject();
	            data.put("email", email);
	            writer.write(data.toJSONString());
	            writer.close();
	            if (update) {
	                System.out.println("Email has been changed!");
	            } else {
	                System.out.println("Email has been added!");
	            }
	        } else {
	            System.err.println("VaccineCheckerSetu: File doesn't exist, please create the file");
	            System.exit(1);
	        }

	    }

	    private static void changeEmail(BufferedReader reader, String file) throws IOException, ParseException {
	        removeEmail(reader, file, true);
	        addEmail(reader, file, true);
	    }

	    private static void removeEmail(BufferedReader reader, String file, boolean update) throws FileNotFoundException, IOException, ParseException {
	        if (new File(file).exists() && new File(file).length() != 0) {
	            JSONParser parser = new JSONParser();
	            JSONObject jsonData = (JSONObject) parser.parse(new FileReader(file));
	            if (jsonData.containsKey("email")) {
	                jsonData.remove("email");
	                FileWriter writer = new FileWriter(file);
	                writer.write(jsonData.toJSONString());
	                writer.close();
	                if (!update) {
	                    System.out.println("Email removed. Make sure to add an email back before running VaccineChecker!");
	                }
	            } else {
	                System.err.println("VaccineCheckerrSetup: No emails found in the file, please add an email first!");
	                System.exit(1);
	            }
	        } else {
	            System.err.println("VaccineCheckerSetup: File doesn't exist or is empty, please create the file or add data to the file!");
	            System.exit(1);
	        }
	    }

	    @SuppressWarnings("unchecked")
	    private static void addPerson(BufferedReader reader, String file, boolean update) throws IOException, ParseException {
	        System.out.print("What's the person's name: ");
	        String name = reader.readLine();
	        System.out.print("Type all the emails you want assoicated with this person, seperated by a comma: ");
	        String email = reader.readLine();
	        System.out.print("What's the person's state: ");
	        String state = reader.readLine().toUpperCase();
	        System.out.print("What's the person's zipcode: ");
	        String zipcode = reader.readLine();
	        System.out.print("What vaccine does the person want (leave blank for any): ");
	        String vaccine = reader.readLine().toLowerCase();
	        if (vaccine.contains("johnson")) {
	            vaccine = "jj";
	        }
	        System.out.print("What radius to search for (leave blank for any): ");
	        String radius = reader.readLine();
	        System.out.print("Does the person only need the second does (Y\\N): ");
	        String dose = reader.readLine().toLowerCase();
	        if (dose.equals("y")) {
	            dose = "2nd_dose_only";
	        } else {
	            dose = "";
	        }
	        System.out.print("Enter a date for a vaccine (leave blank for any): ");
	        String date = reader.readLine();
	        JSONObject person = new JSONObject();
	        List<String> emails = Arrays.asList(email.split("\\s*,\\s*"));
	        person.put("name", name);
	        person.put("emails", emails);
	        person.put("state", state);
	        person.put("zipcode", zipcode);
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
	        if (new File(file).exists() && new File(file).length() != 0) {
	            JSONParser parser = new JSONParser();
	            JSONObject jsonData = (JSONObject) parser.parse(new FileReader(file));
	            FileWriter writer = new FileWriter(file);
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
	            writer.write(jsonData.toJSONString());
	            writer.close();
	        } else if (new File(file).length() == 0) {
	            FileWriter writer = new FileWriter(file);
	            JSONObject data = new JSONObject();
	            JSONArray personArray = new JSONArray();
	            personArray.add(person);
	            data.put("people", personArray);
	            writer.write(data.toJSONString());
	            writer.close();
	            if (update) {
	                System.out.println("Person has been changed!");
	            } else {
	                System.out.println("Person has been added!");
	            }
	        } else {
	            System.err.println("VaccineCheckerSetup: File doesn't exist, please create the file");
	            System.exit(1);
	        }
	    }

	    private static void changePerson(BufferedReader reader, String file) throws FileNotFoundException, IOException, ParseException {
	        removePerson(reader, file, true);
	        addPerson(reader, file, true);
	    }

	    @SuppressWarnings("rawtypes")
	    private static void removePerson(BufferedReader reader, String file, boolean update) throws FileNotFoundException, IOException, ParseException {
	        if (new File(file).exists() && new File(file).length() != 0) {
	            JSONParser parser = new JSONParser();
	            JSONObject jsonData = (JSONObject) parser.parse(new FileReader(file));
	            if (jsonData.containsKey("people")) {
	                JSONArray people = (JSONArray) jsonData.get("people");
	                System.out.println("Select person to remove from below: ");
	                for (int i = 0; i < people.size(); i++) {
	                    System.out.println(i + 1 + ". " + new JSONObject((Map) people.get(i)).get("name"));
	                }
	                System.out.print("Option: ");
	                int option = Integer.parseInt(reader.readLine()) - 1;
	                people.remove(option);
	                FileWriter writer = new FileWriter(file);
	                writer.write(jsonData.toJSONString());
	                writer.close();
	                if (!update) {
	                    System.out.println("Person removed. Make sure to have at least one person to be checked!");
	                }
	            } else {
	                System.err.println("VaccineCheckerrSetup: No emails found in the file, please add an email first!");
	                System.exit(1);
	            }
	        } else {
	            System.err.println("VaccineCheckerSetup: File doesn't exist or is empty, please create the file or add data to the file!");
	            System.exit(1);
	        }
	    }

	    @SuppressWarnings("unchecked")
	    private static void upgradeConfig(String file) throws FileNotFoundException, IOException, ParseException {
	        if (new File(file).exists() && new File(file).length() != 0) {
	            JSONParser parser = new JSONParser();
	            JSONObject jsonData = (JSONObject) parser.parse(new FileReader(file));
	            if (!jsonData.containsKey("version")) {
	                jsonData.put("version", "1.0");
	                JSONArray people = (JSONArray) jsonData.get("people");
	                for (int i = 0; i < people.size(); i++) {
	                    JSONObject person = (JSONObject) people.get(i);
	                    String email = person.get("email").toString();
	                    JSONArray emails = new JSONArray();
	                    emails.add(email);
	                    person.remove("email");
	                    person.put("emails", emails);
	                    if(person.containsKey("dose")) {
	                    	person.remove("dose");
	                    	person.put("dose", "2nd_dose_only");
	                    }
	                }
	                jsonData.remove("people");
	                jsonData.put("people", people);
	                FileWriter writer = new FileWriter(file);
	                writer.write(jsonData.toJSONString());
	                writer.close();
	                System.out.println("Config file has been updated!");
	                System.exit(0);
	            } else {
	                System.out.println("Config file is already up to date!");
	                System.exit(0);
	            }
	        } else {
	            System.err.println("VaccineCheckerSetup: File doesn't exit or is empty, please create the file or add data to the file!");
	            System.exit(1);
	        }
	    }
	
	private static void setup(String filepath) throws IOException, ParseException {
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
        int option = Integer.parseInt(reader.readLine());
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
		if(args.length == 1) {
			setup(args[0]);
		} else if (args.length < 2) {
			System.err.println("Vaccine Checker: Too litle arguments provided!");
			System.exit(1);
		} else if (args.length > 3) {
			System.err.println("Vaccine Checker: Too many arguments provided!");
			System.exit(1);
		}
		boolean testing = false;
		if(args.length == 3) {
			testing = true;
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
					if(testing) {
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