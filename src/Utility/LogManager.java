package Utility;

import Conf.Const;

import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogManager {
	public Handler fileHandler = null;
	public Logger logger;

	public LogManager(String serverName) {
		logger = Logger.getLogger(serverName);
		try {
			fileHandler = new FileHandler(Const.LOG_DIR + serverName + "\\" + serverName + ".log", true);
			SimpleFormatter formatter = new SimpleFormatter();
			fileHandler.setFormatter(formatter);
			logger.setUseParentHandlers(false);
			logger.addHandler(fileHandler);
			logger.setLevel(Level.INFO);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in logger -> " + e.getMessage());
		}
	}

	public LogManager(String replicaId,String serverName ) {
		logger = Logger.getLogger(replicaId+"_"+serverName);
		try {
			fileHandler = new FileHandler(
					Const.LOG_DIR + "\\" + replicaId +"\\" + serverName + "\\" + replicaId+"_"+serverName + ".log", true);
			SimpleFormatter formatter = new SimpleFormatter();
			fileHandler.setFormatter(formatter);
			logger.setUseParentHandlers(false);
			logger.addHandler(fileHandler);
			logger.setLevel(Level.INFO);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in logger -> " + e.getMessage());
		}
	}
}
