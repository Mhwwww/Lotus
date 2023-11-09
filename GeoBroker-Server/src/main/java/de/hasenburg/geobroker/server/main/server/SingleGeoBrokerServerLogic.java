package de.hasenburg.geobroker.server.main.server;

import de.hasenburg.geobroker.commons.Utility;
import de.hasenburg.geobroker.commons.communication.ZMQProcessManager;
import de.hasenburg.geobroker.server.communication.ZMQProcessStarter;
import de.hasenburg.geobroker.server.main.Configuration;
import de.hasenburg.geobroker.server.matching.SingleGeoBrokerMatchingLogic;
import de.hasenburg.geobroker.server.storage.TopicAndGeofenceMapper;
import de.hasenburg.geobroker.server.storage.client.ClientDirectory;
import net.pwall.json.JSONArray;
import net.pwall.json.JSONObject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SingleGeoBrokerServerLogic implements IServerLogic {

	private static final Logger logger = LogManager.getLogger();
	private static final Logger logger2 = LogManager.getLogger();
	private Configuration configuration;
	private SingleGeoBrokerMatchingLogic matchingLogic;
	private ZMQProcessManager processManager;
	private ClientDirectory clientDirectory;

	@Override
	public void loadConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void initializeFields() {
		clientDirectory = new ClientDirectory();
		TopicAndGeofenceMapper topicAndGeofenceMapper = new TopicAndGeofenceMapper(configuration);

		matchingLogic = new SingleGeoBrokerMatchingLogic(clientDirectory, topicAndGeofenceMapper);
		processManager = new ZMQProcessManager();
	}

	@Override
	public void startServer() {
		ZMQProcessStarter.runZMQProcess_Server(processManager,
				"0.0.0.0",
				configuration.getPort(),
				configuration.getBrokerId());
		for (int number = 1; number <= configuration.getMessageProcessors(); number++) {
			ZMQProcessStarter.runZMQProcess_MessageProcessor(processManager,
					configuration.getBrokerId(),
					number,
					matchingLogic,
					0);
		}
		logger.info("Started server successfully!");
	}

	@Override
	public void serverIsRunning() {
		AtomicBoolean keepRunning = new AtomicBoolean(true);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> keepRunning.set(false)));

		while (keepRunning.get()) {
			//logger.info(clientDirectory.toString());

			if (clientDirectory.getNumberOfClients() > 0){
				String[] splitStrings = clientDirectory.toString().split("}(?=Client\\{heartbeat=)");
				//logger.info(splitStrings);
				for (String s : splitStrings) {
					logger.info(s);
					if (!s.contains("subscriptions=\n" +
							"Subscription")){
						String heartbeat = extractHeartbeat(s);
						writeLogToFile(heartbeat, s);
					}

				}
			}

			Utility.sleepNoLog(3000, 0);
		}
	}

	@Override
	public void cleanUp() {
		processManager.tearDown(2000);
		logger.info("Tear down completed");
	}

	/*****************************************************************
	 * Generated methods
	 ****************************************************************/

	public ClientDirectory getClientDirectory() {
		return clientDirectory;
	}


	private static String extractHeartbeat(String clientEntry) {
		// Extract the heartbeat from the clientEntry string
		int startIndex = clientEntry.indexOf("heartbeat=") + 10;
		int endIndex = clientEntry.indexOf(",", startIndex);
		return clientEntry.substring(startIndex, endIndex);
	}

	private static void writeLogToFile(String heartbeat, String clientEntry) {
		try (FileWriter writer = new FileWriter("./logs/publisher_" + heartbeat + ".log", true)) {
			Date now = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String currentTime = dateFormat.format(now);

			writer.write(currentTime + " Publisher information is: " + clientEntry);
			writer.write("\n");
			writer.flush();
			System.out.println("Write successfully to client_" + heartbeat + ".log");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}








