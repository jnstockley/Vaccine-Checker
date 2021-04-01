# Vaccine-Checker
A simple java program that uses web scrapping technology to determine if a vaccine is available in your location and sends you an email.

# VaccineCheckerSetup
This is a simple program which allows for easy adding, modifying, and removing of people and email address. This program is in beta and could result in data loss in the config file. Please backup your config file before using it. If you experience data loss, please report an issue and describe how the data loss occurred. The VaccineCheckerSetup jar file takes a single argument, the full path to the config file. Please make sure the file config file exists before running the program.

# Dependencies
The only dependency is Java 15+

# Conf file Set-Up
Any fields with a '*' are required and the program will not work if they are not provided. Any field with a '?' is optional and can be used to get a more accurate update of vaccine availability.
Any other keys in the JSON file will be ignored but the program will run the same, as long as the conf file is valid.

# Web Driver Set-Up
This program uses Microsoft Edge Chromium to perform the web scraping. You will need to make sure Microsoft Edge Chromium is downloaded on your computer. To download it click [here](https://www.microsoft.com/en-us/edge). If you want to run this program on a Linux distribution, you will need to download the Insider Build of edge, which can be found [here](https://www.microsoftedgeinsider.com/en-us/download/)

Once the web browser is installed you will need to download the selenium web driver for the exact version of your edge install. Click [here](https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/) to download the right version.

# Running the Program
After having the conf file setup and the browser downloaded with the web driver, then you can run the program. The program takes 2 arguments.
  1. Path to the conf file. Make sure to copy the full path for this
  2. Path to the web driver. Make sure to copy the full path for this and make sure the path is to the actual driver, not the folder. It should either have no file extension or a  .exe extension

The command to run the program is java -jar VaccineChecker.jar conf.json msedgedriver

# Auto run the program
The recommended way to run this program automatically is by creating a crontab. On Linux type `crontab -e` to edit your local crontab. I would recommend having the program run every 2 minutes. Crontab syntax for every two minutes is `*/2 * * * *` then the command. Make sure you pass the full file path for the config file and the web driver. If you need help with the crontab syntax I recommend this [website](https://crontab.guru/)

# Special Thanks
Special thanks to the covid vaccine spotter GitHub project for making this possible. Please check them out [@GUI](https://github.com/GUI/covid-vaccine-spotter)

# Questions, Concerns, Feedback?
Any questions, comments, concerns, feedback, or improvements are appreciated. Add an issue and I will determine how to accommodate any feedback or improvements.
