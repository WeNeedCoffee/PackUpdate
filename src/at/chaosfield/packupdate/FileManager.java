package at.chaosfield.packupdate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Jakob (XDjackieXD) Riepler & Phillip (Canitzp) Canitz
 */
public class FileManager {

	public static BufferedReader getOnlineFile(String fileUrl) throws IOException {
		return new BufferedReader(new BufferedReader(new InputStreamReader(new URL(fileUrl).openStream())));
	}

	public static boolean deleteLocalFile(String fileName) {
		File file = new File(fileName);
		return file.delete();
	}

	public static boolean deleteLocalFolderContents(String path) {
		File file = new File(path);
		if (file.exists())
			for (File f : file.listFiles())
				if (!f.isDirectory())
					f.delete();
				else
					return file.mkdir();
		return true;
	}

	public static boolean unzipLocalFile(String zipFile, String outputPath) {

		byte[] buffer = new byte[1024];

		try {
			File input = new File(zipFile);
			File output = new File(outputPath);

			if (!output.exists()) {
				output.mkdir();
			}

			ZipInputStream zis = new ZipInputStream(new FileInputStream(input));
			ZipEntry ze;

			Boolean hadFiles = false;

			while ((ze = zis.getNextEntry()) != null) {
				hadFiles = true;

				if (ze.isDirectory())
					continue;

				File file = new File(outputPath + File.separator + ze.getName());
				new File(file.getParent()).mkdirs();
				FileOutputStream fos = new FileOutputStream(file);
				int length;
				while ((length = zis.read(buffer)) > 0)
					fos.write(buffer, 0, length);
				fos.close();
			}
			zis.closeEntry();
			zis.close();

			return hadFiles;
		} catch (Exception e) {
			return false;
		}
	}

	public static BufferedReader getLocalFile(String fileName) throws IOException {
		File file = new File(fileName);
		BufferedReader reader = null;
		for (int i = 0; i < 3; i++) {
			try {
				reader = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
		}
		return reader;
	}

	private static HashMap<String, String[]> parsePackinfo(BufferedReader packinfo) throws IOException {
		HashMap<String, String[]> parsedInfo = new HashMap<>();
		String tmp;
		while ((tmp = packinfo.readLine()) != null) {
			if (!(tmp.equals("") || tmp.startsWith("#"))) {
				String[] parsed = tmp.split(",");
				String[] t = new String[parsed.length - 1];
				for (int i = 1; i < parsed.length; i++) {
					t[i - 1] = parsed[i];
				}
				parsedInfo.put(parsed[0], t);
			}
		}
		return parsedInfo;
	}

	public static HashMap<String, String[]> getAvailableUpdates(String onlineVersionFile, String localVersionFile) throws IOException {
		HashMap<String, String[]> onlinePackInfo = parsePackinfo(getOnlineFile(onlineVersionFile));
		HashMap<String, String[]> localPackInfo = parsePackinfo(getLocalFile(localVersionFile));
		HashMap<String, String[]> needsUpdate = new HashMap<>();
		if (onlinePackInfo.isEmpty())
			return needsUpdate;
		for (Map.Entry<String, String[]> entry : onlinePackInfo.entrySet()) {
			if (localPackInfo.containsKey(entry.getKey())) {
				if (!localPackInfo.get(entry.getKey())[0].equals(entry.getValue()[0])) {
					String[] data = new String[entry.getValue().length + 1];
					int i = 1;
					data[0] = localPackInfo.get(entry.getKey())[0];
					for (String s : entry.getValue()) {
						data[i] = s;
						i++;
					}
					needsUpdate.put(entry.getKey(), data);
				}
			} else {
				String[] data = new String[entry.getValue().length + 1];
				int i = 1;
				data[0] = "";
				for (String s : entry.getValue()) {
					data[i] = s;
					i++;
				}
				needsUpdate.put(entry.getKey(), data);
			}
			localPackInfo.remove(entry.getKey());
		}
		for (Map.Entry<String, String[]> entry : localPackInfo.entrySet()) {
			String[] data = new String[entry.getValue().length + 1];
			int i = 1;
			data[0] = "delete";
			for (String s : entry.getValue()) {
				data[i] = s;
				i++;
			}
			needsUpdate.put(entry.getKey(), data);
		}
		return needsUpdate;
	}

	public static boolean writeLocalConfig(HashMap<String, String[]> objects, String fileName) {

		HashMap<String, String[]> packInfo = new HashMap<>();

		try {
			packInfo = parsePackinfo(getLocalFile(fileName));
		} catch (IOException e) {
			System.out.println("[PackInfo] Warning: could not get previous config. Ignore this if it is the first launch of the pack.");
		}

		for (Map.Entry<String, String[]> entry : objects.entrySet()) {
			String[] parsed = entry.getValue();
			String[] t = new String[parsed.length - 1];
			for (int i = 1; i < parsed.length; i++) {
				t[i - 1] = parsed[i];
			}
			packInfo.put(entry.getKey(), t);
		}

		try {
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			for (Map.Entry<String, String[]> entry : packInfo.entrySet()) {

				if (entry.getValue()[0].equals(""))
					continue;
				String s = entry.getKey();
				for (String e : entry.getValue()) {
					s += "," + e;
				}
				writer.println(s);
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
