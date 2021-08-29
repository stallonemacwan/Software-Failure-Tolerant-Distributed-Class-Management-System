package Client;

import DCMSApp.*;
import Conf.Const;
import Utility.LogManager;
import Conf.Location;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class ManagerClient {
	static DCMS dcmsImplMTL, dcmsImplLVL, dcmsImplDDO;
	static LogManager logManager;
	public static void main(String args[]) throws IOException {
		Implementation serverloc = null;
		while (true) {
			try {
				Scanner sc = new Scanner(System.in);
				System.out.println("Enter the managerID");
				String managerID = sc.next();  //input string
				String prefix = "";
				if (managerID.length() > 3) {
					prefix = managerID.substring(0, 3);  //substring containing first 3 characters
				}
				while (!check_valid_managerID(managerID, prefix)) {
					System.out.println("Manager ID not valid." + " Insert a valid ManagerID");
					managerID = sc.next();
					if (managerID.length() > 3) {
						prefix = managerID.substring(0, 3);
					}
				}
				new File(Const.LOG_DIR + managerID).mkdir();
				logManager = new LogManager(managerID);
				logManager.logger.log(Level.INFO, "Log client started!");
				if (managerID.contains("MTL")) {
					serverloc = new Implementation(args, Location.MTL, managerID);
				} else if (managerID.contains("LVL")) {
					serverloc = new Implementation(args, Location.LVL, managerID);
				} else if (managerID.contains("DDO")) {
					serverloc = new Implementation(args, Location.DDO, managerID);
				} else {
					System.out.println("ManagerID not valid, Insert a valid ManagerID.");
					continue;
				}
				int input = 1;
				while (input != 0) {
					System.out.println("----- Select which operation you want to perform -----");
					System.out.println("1. Creating a teacher record (TR)");
					System.out.println("2. Creating a student record (SR)");
					System.out.println("3. Edit record");
					System.out.println("4. Display total Record count of all servers.");
					System.out.println("5. Transfer records.");
					System.out.println("6. Kill the Primary Server");
					System.out.println("7. Logout manager");

					try {
						Integer choice = sc.nextInt();

						switch (choice) {
							case 1:
								System.out.println("Enter First name:");
								String firstName = sc.next();
								while (hasNumbers(firstName)) {
									System.out.println("A name can not contain numbers, please insert valid input.");
									firstName = sc.next();
								}
								System.out.println("Enter Last name:");
								String lastName = sc.next();
								while (hasNumbers(lastName)) {
									System.out.println("A name can not contain numbers, please insert valid input.");
									lastName = sc.next();
								}
								System.out.println("Enter Address:");
								sc.nextLine();
								String address = sc.nextLine();
								while (address == null || address.equals("")) {
									System.out.println("Address can not be null, please insert valid input.");
									address = sc.next();
								}
								System.out.println("Enter Phone number in the format (514-888-9999):");
								String phoneNumber = sc.nextLine();
								while (phoneNumber.length() != 12 || hasAlpha(phoneNumber)) {
									System.out.println("Invalid phone number");
									System.out.println("Try another");
									phoneNumber = sc.nextLine();
								}
								System.out.println("Specialization courses:");
								String specializationCourses = sc.nextLine();
								String TRlocation;
								String location = null;
								while (true) {
									System.out.println("Enter the Location (MTL/LVL/DDO) according to manager ID used");
									location = sc.next();

									logManager.logger.log(Level.INFO,
											"Validating the status" + " entered (ManagerID:" + managerID + ")");
									while(!location.equals(prefix))
									{
										System.out.println("Invalid location entered, Please insert appropriate location");
										location = sc.next();
									}
									TRlocation = location;
									break;
								}
								System.out.println(serverloc.createTRecord(managerID, firstName + "," + lastName + ","
										+ address + "," + phoneNumber + "," + specializationCourses + "," + TRlocation));
								break;
							case 2:
								System.out.println("Enter first name of the student");
								String FirstName = sc.next();
								System.out.println("Enter  last name of the student");
								String LastName = sc.next();
								System.out.println("Enter the number of courses registered by the student");
								String courseCount = sc.next();
								while (hasAlpha(courseCount)){
									System.out.println("Please enter a valid number in numerics");
									courseCount = sc.next();
								}
								int coursesCount = Integer.parseInt(courseCount);

								System.out.println(
										"Enter the " + coursesCount + " courses(one by one) registered by the student");
								String courses = null;
								for (int n = 0; n < coursesCount; n++) {
									String course = sc.next();
									if (n == 0)
										courses = course;
									else
										courses = courses + "/" + course;
								}

								String status = null;
								String statusDate = null;

								while (true) {
									System.out.println("Enter the status of student: (Active/Inactive)");
									status = sc.next();
									if ((status.toUpperCase().equals("ACTIVE")))
										break;
									else if ((status.toUpperCase().equals("INACTIVE")))
										break;
									else {
										System.out.println("Status assigned Invalid!");
										status = "Invalid Status";
										continue;
									}
								}
								if ((status.toUpperCase().equals("ACTIVE"))) {
									while (true) {
										System.out.println(
												"Enter the date (Format dd-mm-yyyy)");
										Pattern datePattern = Pattern.compile("([0-3][0-9])-([0-1][1-9])-([0-9]{4})");
										statusDate = sc.next();
										Matcher matcherDate = datePattern.matcher(statusDate);
										if (matcherDate.matches())
											break;
										else {
											System.out.println("Invalid Date Format - enter in correct format");
											continue;
										}
									}
								} else if ((status.toUpperCase().equals("INACTIVE"))) {
									while (true) {
										System.out.println(
												"Enter the date(Format dd-mm-yyyy)");
										Pattern datePattern = Pattern.compile("([0-3][0-9])-([0-1][1-9])-([0-9]{4})");
										statusDate = sc.next();
										Matcher matcherDate = datePattern.matcher(statusDate);
										if (matcherDate.matches())
											break;
										else {
											System.out.println("Invalid Date Format");
											continue;
										}
									}
								}
								System.out.println(serverloc.createSRecord(managerID, FirstName + "," + LastName + "," + courses + "," + status + "," + statusDate));
								break;
							case 3:
								System.out.println("Enter the Record ID");
								String recordID = sc.next();
								String type = recordID.substring(0, 2);
								String fieldName = null;
								String newCourses = null;
								int fieldNum = 0;
								if (type.equals("TR")) {
									System.out.println("What do you want ot change? (1.Address 2.Phone or 3.Location)");
									try {
										fieldNum = Integer.parseInt((sc.next()));
									} catch (NumberFormatException e) {
										System.out.println("Wrong choice");
										continue;
									}
									if (fieldNum == 1)
										fieldName = "Address";
									else if (fieldNum == 2)
										fieldName = "Phone";
									else if (fieldNum == 3)
										fieldName = "Location";
									else
										System.out.print("Wrong selection of input to edit record");
								} else if (type.equals("SR")) {
									System.out.println("What do you want ot change? (1.CoursesRegistered 2.Status or 3.statusDate)");
									fieldNum = Integer.parseInt((sc.next()));
									if (fieldNum == 1)
										fieldName = "CoursesRegistered";
									else if (fieldNum == 2)
										fieldName = "Status";
									else if (fieldNum == 3)
										fieldName = "StatusDate";
									else
										System.out.print("Wrong choice");

								} else {
									System.out.println("Wrong record ID.");
									continue;
								}
								if (fieldName.equals("CoursesRegistered")) {
									System.out.println("Enter the number of courses registered by the student");
									courseCount = sc.next();
									while (hasAlpha(courseCount)){
										System.out.println("Please enter a valid number in numerics");
										courseCount = sc.next();
									}
									coursesCount = Integer.parseInt(courseCount);
									String NewCourses = null;
									System.out.println("Enter the new courses registered by the student");
									for (int n = 0; n < coursesCount; n++) {
										String temp = sc.next();
										if (n == 0)
											NewCourses = temp;
										else
											NewCourses = NewCourses + "/" + temp;
									}
									System.out.println(serverloc.editRecord(managerID, recordID, fieldName, NewCourses));
								} else {
									System.out.println("Enter the value of the field to be updated");
									String newValue = null;

									if (fieldName.equals("Phone")) {
										while (true) {
											System.out.println("Enter the new Phone number in (514-888-9999) format");
											phoneNumber = sc.next();
											Pattern pattern = Pattern.compile("\\d{3}-\\d{3}-\\d{4}");
											Matcher matcher = pattern.matcher(phoneNumber);
											if (matcher.matches()) {
												newValue = phoneNumber;
												break;
											} else {
												System.out.println("Invalid new phone number");
												logManager.logger.log(Level.INFO,
														"Validation Failed for new phone number and exiting the program(ManagerID:" + managerID + ")");
												continue;
											}
										}
									} else if (fieldName.equals("Location")) {
										while (true) {
											System.out.println("Enter the new Location(MTL/LVL/DDO)");
											location = sc.next();
											if (location.equalsIgnoreCase("LVL") || location.equalsIgnoreCase("MTL")
													|| location.equalsIgnoreCase("DDO")) {
												newValue = location;
												break;
											} else {
												System.out.println("Invalid new Location");
												logManager.logger.log(Level.INFO,
														"Validation Failed for new location and exiting the program(ManagerID:" + managerID + ")");
												continue;
											}
										}

									} else if (fieldName.equals("Status")) {
										while (true) {
											System.out.println("Enter the status of student (Active/Inactive)");
											newValue = sc.next();
											status = newValue;
											if ((status.toUpperCase().equals("ACTIVE")))
												break;
											else if ((status.toUpperCase().equals("INACTIVE")))
												break;
											else {
												System.out.println("Status assigned Invalid!");
												status = "Invalid Status";
												continue;
											}
										}
									} else if (fieldName.equals("StatusDate")) {
										while (true) {
											System.out.println("Enter the new Date (Format dd-mm-yyyy)");
											Pattern datePattern = Pattern.compile("([0-3][0-9])-([0-1][1-9])-([0-9]{4})");
											statusDate = sc.next();
											newValue = statusDate;
											Matcher matcherDate = datePattern.matcher(statusDate);
											if (matcherDate.matches())
												break;
											else {
												System.out.println("Invalid Date Format - enter in correct format");
												continue;
											}
										}
									} else {
										newValue = sc.next();

									}
									System.out.println(serverloc.editRecord(managerID, recordID, fieldName, newValue));
								}
								break;
							case 4:
								System.out.println("Total Record Count from all " + Const.SERVERS_COUNT_TOT + " servers is: " + serverloc.getRecordCounts(managerID));
								break;
							case 5:
								System.out.println("Enter the record ID");
								recordID = sc.next();
								System.out.println("Enter the location(MTL/LVL/DDO)");
								location = sc.next();
								while (!location.equalsIgnoreCase("MTL") && !location.equalsIgnoreCase("LVL")
										&& !location.equalsIgnoreCase("DDO")) {
									System.out.println("Invalid loaction.");
									location = sc.next();
								}
								serverloc.transferRecord(managerID, recordID, location);
								break;
							case 6:
								while (true) {
									System.out.println("Enter the Location (MTL/LVL/DDO)");
									location = sc.next();
									if (!location.equalsIgnoreCase("MTL") && !location.equalsIgnoreCase("LVL") && !location.equalsIgnoreCase("DDO")) {
										continue;
									} else {
										System.out.println(serverloc.killServer(location));
										break;
									}
								}
								break;
							case 7:
								input = 0;
								break;
							default:
								System.out.println("Invalid choice. Try again");
								break;
						}
					} catch (NumberFormatException e) {
						System.out.println("Please");
						continue;
					} catch (Exception e) {
						System.out.println("Exception in Client -> " + e.getMessage());
						System.out.println("Invalid Input");
						continue;
					}

				}
				System.out.println("Manager with " + managerID + "is logging out");
			} catch (StringIndexOutOfBoundsException e) {
				System.out.println("Invalid ManagerID");

			}
		}
	}
		public static boolean check_valid_managerID (String input, String prefix){
			if ((!(prefix.equals("LVL")||prefix.equals("MTL") || prefix.equals("DDO")))|| input.length() != 7) {
				return false;
			}
			boolean check = hasAlpha(input.substring(3, input.length()));
			return !check;
		}
	public static boolean hasAlpha(String input) {
		char[] chars = input.toCharArray();
		for (char c : chars) {
			if (Character.isLetter(c)) {
				return true;
			}
		}
		return false;

	}
	public static boolean hasNumbers(String input) {
		char[] chars = input.toCharArray();
		for (char c : chars) {
			if (Character.isDigit(c)) {
				return true;
			}
		}
		return false;
	}
	}
