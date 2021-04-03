import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class VaccineCheckerSetup {

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

    public static void main(String args[]) throws NumberFormatException, IOException, ParseException {
        if (args.length > 1) {
            System.err.println("VaccinecCheckerSetup: Too many arguments provided!");
            System.exit(1);
        } else if (args.length < 1) {
            System.err.println("VaccineCheckerSetup: No arguments provided!");
            System.exit(1);
        }
        System.out.println("What do you want to do:");
        System.out.println("1. Add email account");
        System.out.println("2. Change email account");
        System.out.println("3. Remove email account");
        System.out.println("4. Add person to check");
        System.out.println("5. Change person being checked");
        System.out.println("6. Remove person being checked");
        System.out.println("7. Upgrade config file");
        System.out.print("Option: ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int option = Integer.parseInt(reader.readLine());
        switch (option) {
            case 1:
                addEmail(reader, args[0], false);
                break;
            case 2:
                changeEmail(reader, args[0]);
                break;
            case 3:
                removeEmail(reader, args[0], false);
                break;
            case 4:
                addPerson(reader, args[0], false);
                break;
            case 5:
                changePerson(reader, args[0]);
                break;
            case 6:
                removePerson(reader, args[0], false);
                break;
            case 7:
                upgradeConfig(args[0]);
                break;
            default:
                System.err.println("VaccineCheckerSetup: Option in not valid");
                System.exit(1);
        }
    }
}