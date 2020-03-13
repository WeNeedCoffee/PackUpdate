package at.chaosfield.packupdate;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import coffee.weneed.utils.NetUtil;
import coffee.weneed.utils.StringUtil;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

/**
 * Created by Jakob (XDjackieXD) Riepler
 */
public class FxController {

	@FXML
	private Label status;

	@FXML
	private ProgressBar progress;

	//private List<String> parameters;

	private Stage primaryStage;

	public void setMain(PackUpdate main) {

		this.primaryStage = main.primaryStage;
		//this.parameters = main.getParameters().getRaw();
		String remote_root = "https://raw.githubusercontent.com/WeNeedCoffee/Convergence/master/";
		String local_root = main.getParameters().getRaw().get(0);
		boolean server = main.getParameters().getRaw().get(1).equalsIgnoreCase("server") ? true : false;

		Task<List<String>> updater = new Task<List<String>>() {
			@Override
			protected List<String> call() {

				class ErrorLog extends ArrayList<String> {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public boolean add(String e) {
						System.out.println(e);
						return super.add(e);
					}
				}

				List<String> ret = new ErrorLog();
				HashMap<String, String[]> mods = new HashMap<>();
				HashMap<String, String[]> new_mods = null;

				HashMap<String, String[]> curse = new HashMap<>();
				HashMap<String, String[]> new_curse = null;

				HashMap<String, String[]> files = new HashMap<>();
				HashMap<String, String[]> new_files = null;

				int current = 0;
				int toupdate = 0;

				try {
					new_mods = FileManager.getAvailableUpdates(remote_root + "mods.csv", local_root + File.separator + "mods.csv");
					toupdate += new_mods.size();
					updateMessage("To Update: " + toupdate);
				} catch (IOException e) {
					ret.add("[PackUpdate] Downloading \"" + remote_root + "mods.csv" + "\" failed.");
					e.printStackTrace();
				}

				try {
					new_curse = FileManager.getAvailableUpdates(remote_root + "curse.csv", local_root + File.separator + "curse.csv");
					toupdate += new_curse.size();
					updateMessage("To Update: " + toupdate);
				} catch (IOException e) {
					ret.add("[PackUpdate] Downloading \"" + remote_root + "curse.csv" + "\" failed.");
					e.printStackTrace();
				}

				try {
					new_files = FileManager.getAvailableUpdates(remote_root + "files.csv", local_root + File.separator + "files.csv");
					toupdate += new_files.size();
					updateMessage("To Update: " + toupdate);
				} catch (IOException e) {
					ret.add("[PackUpdate] Downloading \"" + remote_root + "files.csv" + "\" failed.");
					e.printStackTrace();
				}

				if (new_mods != null) {
					updateProgress(current, toupdate);

					final String modsPath = local_root + File.separator + "mods" + File.separator;

					for (Map.Entry<String, String[]> entry : new_mods.entrySet()) {
						String type = entry.getValue()[3];
						if ((type.equalsIgnoreCase("client") && server) || (type.equalsIgnoreCase("server") && !server)) {
							current++;
							updateProgress(current, toupdate);
							continue;
						}
						updateMessage("Updating " + entry.getKey());
						if (entry.getValue()[0].equalsIgnoreCase("delete")) {
							if (!FileManager.deleteLocalFile(modsPath + entry.getKey() + "-" + entry.getValue()[2] + ".jar")) {
								ret.add("[" + entry.getKey() + "] " + "Warning: Deletion of file " + entry.getKey() + "-" + entry.getValue()[2] + ".jar failed.\n" + "Either someone touched the mod's file manually or this is a bug.");
							}
							mods.put(entry.getKey(), entry.getValue());
							current++;
							updateProgress(current, toupdate);
							System.out.println("Successfully removed " + entry.getKey());
							continue;
						}
						//0=old or empty
						//1=new
						//2=url
						//3=side
						String url = entry.getValue()[2];
						//delete the old file
						if (!entry.getValue()[0].equals("")) {
							if (!FileManager.deleteLocalFile(modsPath + entry.getKey() + "-" + entry.getValue()[0] + ".jar")) {
								ret.add("[" + entry.getKey() + "] " + "Warning: Deletion of file " + entry.getKey() + "-" + entry.getValue()[0] + ".jar failed.\n" + "Either someone touched the mod's file manually or this is a bug.");
							}
						}
						try {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							new File(modsPath + entry.getKey() + "-" + entry.getValue()[1] + ".jar").getParentFile().mkdirs();
							NetUtil.downloadFile(url, modsPath + entry.getKey() + "-" + entry.getValue()[1] + ".jar");
						} catch (IOException e) {
							ret.add("[" + entry.getKey() + "] " + "Download failed.");
							continue;
						}

						mods.put(entry.getKey(), entry.getValue());

						current++;
						updateProgress(current, toupdate);
						System.out.println("Successfully updated " + entry.getKey());
					}
				}

				if (!FileManager.writeLocalConfig(mods, local_root + File.separator + "mods.csv"))
					ret.add("[PackInfo]" + "Error writing " + "mods.csv");

				//https://curse.nikky.moe/api/addon/238222/file/2682936
				//https://curse.nikky.moe/api/addon/365927/file/2897867

				if (new_curse != null) {
					updateProgress(current, toupdate);

					final String modsPath = local_root + File.separator + "mods" + File.separator;

					for (Map.Entry<String, String[]> entry : new_curse.entrySet()) {
						String type = entry.getValue()[4];
						if ((type.equalsIgnoreCase("client") && server) || (type.equalsIgnoreCase("server") && !server)) {
							current++;
							updateProgress(current, toupdate);
							continue;
						}

						updateMessage("Updating " + entry.getKey());
						if (entry.getValue()[0].equalsIgnoreCase("delete")) {
							System.out.println(entry.getValue()[0]);
							if (!FileManager.deleteLocalFile(modsPath + entry.getKey() + "-" + entry.getValue()[2] + ".jar")) {
								ret.add("[" + entry.getKey() + "] " + "Warning: Deletion of file " + entry.getKey() + "-" + entry.getValue()[2] + ".jar failed.\n" + "Either someone touched the mod's file manually or this is a bug.");
							}
							mods.put(entry.getKey(), entry.getValue());
							current++;
							updateProgress(current, toupdate);
							System.out.println("Successfully removed " + entry.getKey());
							continue;
						}
						String url = "";
						try {
							;
							System.out.println(entry.getValue()[0]);
							String ourl = new JSONObject(new String(NetUtil.downloadUrl("http://dms.zapto.org:801/api/addon/" + entry.getValue()[2] + "/file/" + entry.getValue()[3]))).getString("downloadUrl");
							String start = StringUtil.substr(ourl, 0, ourl.length() - StringUtil.getEnd(ourl, "/").length() - 1 + "/".length());
							String end = StringUtil.getEnd(ourl, "/");
							end = URLEncoder.encode(end, "UTF-8");
							end = end.replaceAll("\\+", "%20");
							url = start + end;
						} catch (Exception e1) {
							e1.printStackTrace();
							ret.add("[" + entry.getKey() + "] " + "Downloading file data failed.");
							continue;
						}
						if (!entry.getValue()[0].equals("")) {
							if (!FileManager.deleteLocalFile(modsPath + entry.getKey() + "-" + entry.getValue()[0] + ".jar")) {
								ret.add("[" + entry.getKey() + "] " + "Warning: Deletion of file " + entry.getKey() + "-" + entry.getValue()[0] + ".jar failed.\n" + "Either someone touched the mod's file manually or this is a bug.");
							}
						}
						try {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							new File(modsPath + entry.getKey() + "-" + entry.getValue()[1] + ".jar").getParentFile().mkdirs();
							NetUtil.downloadFile(url, modsPath + entry.getKey() + "-" + entry.getValue()[1] + ".jar");
						} catch (Exception e) {
							e.printStackTrace();
							ret.add("[" + entry.getKey() + "] " + "Download failed.");
							continue;
						}
						curse.put(entry.getKey(), entry.getValue());
						current++;
						updateProgress(current, toupdate);
						System.out.println("Successfully updated " + entry.getKey());
					}
				}

				if (!FileManager.writeLocalConfig(curse, local_root + File.separator + "curse.csv"))
					ret.add("[PackInfo]" + "Error writing " + "curse.csv");

				if (new_files != null) {
					updateProgress(current, toupdate);

					for (Map.Entry<String, String[]> entry : new_files.entrySet()) {
						String type = entry.getValue()[3];
						if ((type.equalsIgnoreCase("client") && server) || (type.equalsIgnoreCase("server") && !server)) {
							continue;
						}
						updateMessage("Updating " + entry.getKey());
						if (entry.getValue()[0].equalsIgnoreCase("delete")) {
							if (entry.getKey().contains(File.separatorChar + "")) {
								if (!FileManager.deleteLocalFile(local_root + entry.getKey())) {
									ret.add("[" + entry.getKey() + "] " + "Warning: Deletion of file " + entry.getKey() + " failed.\n" + "Either someone touched the mod's file manually or this is a bug.");
								}
							}
							files.put(entry.getKey(), entry.getValue());

							current++;
							updateProgress(current, toupdate);
							continue;
						}
						String url = "" + entry.getValue()[2];

						if (!url.equals("")) {
							if (!entry.getValue()[0].equals(""))
								if (!FileManager.deleteLocalFile(local_root + entry.getKey())) {
									ret.add("[" + entry.getKey() + "] " + "Warning: Deletion of file " + entry.getKey() + " failed.\n" + "Either someone touched the mod's file manually or this is a bug.");
								}
							try {
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								new File(local_root + entry.getKey()).getParentFile().mkdirs();
								NetUtil.downloadFile(url, local_root + entry.getKey());
							} catch (IOException e) {
								e.printStackTrace();
								ret.add("[" + entry.getKey() + "] " + "Download failed.");
								continue;
							}

						} else {
							if (!FileManager.deleteLocalFile(local_root + entry.getKey())) {
								ret.add("[" + entry.getKey() + "] " + "Warning: Deletion of file " + entry.getKey() + " failed.\n" + "Either someone touched the mod's file manually or this is a bug.");
							}
						}

						files.put(entry.getKey(), entry.getValue());

						current++;
						updateProgress(current, toupdate);
						System.out.println("Successfully updated " + entry.getKey());
					}
				}

				if (!FileManager.writeLocalConfig(files, local_root + File.separator + "files.csv"))
					ret.add("[PackInfo]" + "Error writing " + "files.csv");

				return ret;
			}
		};

		progress.progressProperty().bind(updater.progressProperty());
		status.textProperty().bind(updater.messageProperty());
		updater.setOnSucceeded(t -> {
			List<String> returnValue = (List<String>) updater.getValue();
			if (returnValue.size() > 0) {
				main.errorAlert(returnValue);
			}
			primaryStage.close();
		});
		new Thread(updater).start();
	}

	/*
	 * 
								case "resourcepack":
									if (!url.equals("")) { 
										try {
											NetUtil.downloadFile(url, resourcePacksPath + entry.getKey() + "-" + entry.getValue()[0] + ".zip");
										} catch (IOException e) {
											ret.add("[" + entry.getKey() + "] " + "Download failed.");
											continue;
										}
										if (!entry.getValue()[1].equals("")) 
											if (!FileManager.deleteLocalFile(resourcePacksPath + entry.getKey() + "-" + entry.getValue()[1] + ".zip")) {
												ret.add("[" + entry.getKey() + "] " + "Warning: Deletion of file " + entry.getKey() + "-" + entry.getValue()[1] + ".zip failed.\n" + "Either someone touched the resource pack's file manually or this is a bug.");
												// continue;
											}
									} else {
										if (!FileManager.deleteLocalFile(resourcePacksPath + entry.getKey() + "-" + entry.getValue()[1] + ".zip")) {
											ret.add("[" + entry.getKey() + "] " + "Warning: Deletion of file " + entry.getKey() + "-" + entry.getValue()[1] + ".zip failed.\n" + "Either someone touched the resource pack's file manually or this is a bug.");
											// continue;
										}
									}
									break;
	
								case "config":
									if (!url.equals("")) {
										if (!FileManager.deleteLocalFolderContents(configPath)) {
											ret.add("[" + entry.getKey() + "] " + "Either deleting the config folder's content or creating an empty config folder failed.");
											continue;
										}
	
										try {
											NetUtil.downloadFile(url, configPath + File.separator + entry.getKey() + "-" + entry.getValue()[0] + ".zip");
										} catch (IOException e) {
											ret.add("[" + entry.getKey() + "] " + "Download failed.");
											continue;
										}
	
										if (!FileManager.unzipLocalFile(configPath + File.separator + entry.getKey() + "-" + entry.getValue()[0] + ".zip", configPath + File.separator)) {
											ret.add("[" + entry.getKey() + "] " + "Unpack failed: The zip file seems to be corrupted.");
											continue;
										}
									} else {
										if (!FileManager.deleteLocalFolderContents(configPath)) {
											ret.add("[" + entry.getKey() + "] " + "Either deleting the config folder's content or creating an empty config folder failed.");
											continue;
										}
									}
									break;
	
								case "resources":
									if (!url.equals("")) { 
										if (!FileManager.deleteLocalFolderContents(resourcesPath + File.separator + "resources")) { 
											ret.add("[" + entry.getKey() + "] " + "Either deleting the resources folder's content or creating an empty resources folder failed.");
											continue;
										}
										if (!FileManager.deleteLocalFolderContents(resourcesPath + File.separator + "scripts")) {
											ret.add("[" + entry.getKey() + "] " + "Either deleting the scripts folder's content or creating an empty scripts folder failed.");
											continue;
										}
	
										try {
											NetUtil.downloadFile(url, resourcesPath + File.separator + "resources" + File.separator + entry.getKey() + "-" + entry.getValue()[0] + ".zip");
										} catch (IOException e) {
											ret.add("[" + entry.getKey() + "] " + "Download failed.");
											continue;
										}
	
										if (!FileManager.unzipLocalFile(resourcesPath + File.separator + "resources" + File.separator + entry.getKey() + "-" + entry.getValue()[0] + ".zip", resourcesPath + File.separator)) {
											ret.add("[" + entry.getKey() + "] " + "Unpack failed: The zip file seems to be corrupted.");
											continue;
										}
									} else {
										if (!FileManager.deleteLocalFolderContents(resourcesPath + File.separator + "resources")) {
											ret.add("[" + entry.getKey() + "] " + "Either deleting the resources folder's content or creating an empty resources folder failed.");
											continue;
										}
										if (!FileManager.deleteLocalFolderContents(resourcesPath + File.separator + "scripts")) {
											ret.add("[" + entry.getKey() + "] " + "Either deleting the scripts folder's content or creating an empty scripts folder failed.");
											continue;
										}
									}
									break;
	
								default:*/

}
