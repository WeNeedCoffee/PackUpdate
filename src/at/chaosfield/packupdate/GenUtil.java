package at.chaosfield.packupdate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.common.io.Files;
import coffee.weneed.utils.FileUtil;

public class GenUtil {
	static String tempdir = "C:\\Users\\Daleth\\Documents\\GitHub\\Convergence\\temp\\";

	static Map<String, String[]> getFiles(File dir, String parent, String parentnormal) throws UnsupportedEncodingException {
		Map<String, String[]> cl = new HashMap<>();
		int i = 0;
		List<String> l = new ArrayList<>();
		for (File file : dir.listFiles()) {
			if (file.isFile()) {
				String n = l.contains(file.getName().split("\\.")[0]) ? file.getName().split("\\.")[0] + (++i) : file.getName().split("\\.")[0];
				l.add(n);
				cl.put("/" + parentnormal + "/" + file.getName(), new String[] { n, "1", "/" + parentnormal + "/" + file.getName(), "https://raw.githubusercontent.com/Dalethium/Convergence/master/" + parent.replaceAll("\\+", "%20").replaceAll("%2F", "/") + "/" + URLEncoder.encode(file.getName(), "UTF-8").replaceAll("\\+", "%20"), "client" });
			} else if (file.isDirectory()) {
				cl.putAll(getFiles(file, URLEncoder.encode(parentnormal, "UTF-8") + "/" + URLEncoder.encode(file.getName(), "UTF-8"), parentnormal + "/" + file.getName()));
			}
		}
		return cl;
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

	private static HashMap<String, String[]> parsePackinfo(BufferedReader br) throws IOException {
		HashMap<String, String[]> parsedInfo = new HashMap<>();
		String tmp;
		while ((tmp = br.readLine()) != null) {
			if (!(tmp.equals("") || tmp.startsWith("#"))) {
				String[] parsed = tmp.split(",");
				parsedInfo.put(parsed[0], parsed);
			}
		}
		return parsedInfo;
	}

	static Map<String, String[]> getUpdatedFiles(File old, File nw, String parent, String parentnormal, Map<String, String[]> par) throws IOException {
		Map<String, String[]> cl = new HashMap<>();
		int i = 0;
		for (File file : nw.listFiles()) {
			if (file.isFile()) {
				String f = "/" + parentnormal + "/" + file.getName();
				boolean found = false;
				int n = par.containsKey(f) ? Integer.valueOf(par.get(f)[1]) : 1;

				if (old.isDirectory()) {
					for (File o : old.listFiles()) {
						if (o.getName().equals(file.getName())) {
							if (!FileUtil.areSame(o, file) && par.containsKey(f)) {
								n = Integer.valueOf(par.get(f)[1]) + 1;
								File ff = new File(tempdir + parentnormal + "\\");
								ff.mkdirs();
								Files.copy(file, new File(tempdir + parentnormal + "\\" + file.getName()));
							}

							found = true;
							break;
						}
					}
					if (!found) {
						File ff = new File(tempdir + parentnormal + "\\");
						ff.mkdirs();
						Files.copy(file, new File(tempdir + parentnormal + "\\" + file.getName()));
					}
				}
				cl.put("/" + parentnormal + "/" + file.getName(), new String[] { "/" + parentnormal + "/" + file.getName(), String.valueOf(n), "https://raw.githubusercontent.com/Dalethium/Convergence/master/" + parent.replaceAll("\\+", "%20").replaceAll("%2F", "/") + "/" + URLEncoder.encode(file.getName(), "UTF-8").replaceAll("\\+", "%20"), "client" });

			} else if (file.isDirectory()) {
				cl.putAll(getUpdatedFiles(new File(old.getAbsolutePath() + "\\" + file.getName() + "\\"), file, URLEncoder.encode(parentnormal, "UTF-8") + "/" + URLEncoder.encode(file.getName(), "UTF-8"), parentnormal + "/" + file.getName(), par));
			}
		}
		return cl;
	}
	

	static Map<String, String[]> pruneOldFiles(File nw, File old, String parent, String parentnormal, Map<String, String[]> par) throws IOException {
		Map<String, String[]> cl = new HashMap<>();
		int i = 0;
		for (File file : old.listFiles()) {
			if (file.isFile()) {
				boolean found = false;

				if (nw.isDirectory()) {
					for (File o : nw.listFiles()) {
						if (o.getName().equals(file.getName())) {
							File ff = new File(tempdir + parentnormal + "\\");
							ff.mkdirs();
							Files.copy(o, new File(tempdir + parentnormal + "\\" + o.getName()));
							break;
						}
					}
				}
			} else if (file.isDirectory()) {
				pruneOldFiles(new File(nw.getAbsolutePath() + "\\" + file.getName() + "\\"), file, URLEncoder.encode(parentnormal, "UTF-8") + "/" + URLEncoder.encode(file.getName(), "UTF-8"), parentnormal + "/" + file.getName(), par);
			}
		}
		return cl;
	}

	public static void main(String[] args) throws IOException {
		pruneOldFiles(new File("C:\\Users\\Daleth\\Documents\\GitHub\\Convergence\\config\\"), new File("S:\\Minecraft\\MultiMC\\instances\\Convergence - Roots\\.minecraft\\config\\"), "config", "config", parsePackinfo(getLocalFile("S:\\Minecraft\\MultiMC\\instances\\Convergence - Roots\\.minecraft\\files.csv")));
		Map<String, String[]> sts = getUpdatedFiles(new File("C:\\Users\\Daleth\\Documents\\GitHub\\Convergence\\config\\"), new File("S:\\Minecraft\\MultiMC\\instances\\Convergence - Roots\\.minecraft\\config\\"), "config", "config", parsePackinfo(getLocalFile("S:\\Minecraft\\MultiMC\\instances\\Convergence - Roots\\.minecraft\\files.csv")));
		List<String[]> strs = new ArrayList<>();
		strs.addAll(sts.values());
		Collections.sort(strs, (a, b) -> a[0].compareTo(b[0]));
		for (String[] s : strs) {
			String ss = "";
			for (String sss : s) {
				ss += "," + sss;
			}
			ss = ss.replaceFirst(",", "");
			System.out.println(ss);
		}
		System.out.println("/resourcepacks/Faithful32_MC1.12.2_v1.0.5.zip,1,https://raw.githubusercontent.com/Dalethium/Convergence/master/resourcepacks/Faithful32_MC1.12.2_v1.0.5.zip,client\r\n" + "/resourcepacks/ctp_1.2_MCv3.zip,1,https://raw.githubusercontent.com/Dalethium/Convergence/master/resourcepacks/ctp_1.2_MCv3.zip,client\r\n" + "/resourcepacks/Faithful 1.12.2-rv4.zip,1,https://raw.githubusercontent.com/Dalethium/Convergence/master/resourcepacks/Faithful%201.12.2-rv4.zip,client\r\n" + "/resourcepacks/ModdedFaithful 1.12.2-rv1.zip,1,https://raw.githubusercontent.com/Dalethium/Convergence/master/resourcepacks/ModdedFaithful%201.12.2-rv1.zip,client\r\n" + "/emojicord/dictionary/SFTAS.json,1,https://raw.githubusercontent.com/Dalethium/Convergence/master/emojicord/dictionary/SFTAS.json,client\r\n" + "/options.txt,1,https://raw.githubusercontent.com/Dalethium/Convergence/master/options.txt,client\r\n" + "/optionsof.txt,5,https://raw.githubusercontent.com/Dalethium/Convergence/master/optionsof.txt,client\r\n" + "/optionsshaders.txt,4,https://raw.githubusercontent.com/Dalethium/Convergence/master/optionsshaders.txt,client");

		/*List<String[]> strs = new ArrayList<>();
		strs.addAll(getFiles(new File("S:\\Minecraft\\Convergence- Roots\\.minecraft\\config\\"), "config", "config"));
		
		for (String[] s : strs) {
			String ss = "";
			for (String sss : s) {
				ss +=  "," + sss;
			}
			ss = ss.replaceFirst(",", "");
			System.out.println(ss);
		}*/

		/*for (String s : fls) {
			System.out.println(s);
		}*/
		/*System.out.println("////////////////////////////////////////////");
		for (String s : flsb) {
			System.out.println(s);
		}
		/*for (File file : directoryb.listFiles()) {
			if (file.isFile()) {
				try {
					Files.move(file, new File(file.getParentFile() + "/" + file.getName().replaceAll("[^a-zA-Z0-9\\.]", "")));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		*/

	}

	static void a() throws UnsupportedEncodingException {

		String f = "S:\\Minecraft\\mcss\\servers\\Convergence - Roots\\mods\\";
		String fb = "S:\\Minecraft\\MultiMC\\instances\\Convergence - Roots\\.minecraft\\mods\\";
		File directory = new File(f);
		File directoryb = new File(fb);
		List<String> ls = new ArrayList<>();
		for (File file : directory.listFiles()) {
			if (file.isFile()) {
				ls.add(file.getName());
			}
		}
		List<String> lsb = new ArrayList<>();
		for (File file : directoryb.listFiles()) {
			if (file.isFile()) {
				lsb.add(file.getName());
			}
		}
		List<String> fls = new ArrayList<>();

		List<String> flsb = new ArrayList<>();
		for (String s : ls) {
			if (!lsb.contains(s))
				fls.add(s);
		}

		for (String s : lsb) {
			if (!ls.contains(s))
				flsb.add(s);
		}
		List<String[]> cl = new ArrayList<>();
		for (File file : directoryb.listFiles()) {
			if (file.isFile()) {
				cl.add(new String[] { file.getName().split("\\.")[0].split("-")[0], "1", "https://raw.githubusercontent.com/Dalethium/Convergence/master/mods/" + URLEncoder.encode(file.getName(), "UTF-8").replaceAll("\\+", "%20"), flsb.contains(file.getName()) ? "client" : "both" });
			}
		}
		for (String[] s : cl) {
			String ss = "";
			for (String sss : s) {
				ss += "," + sss;
			}
			ss = ss.replaceFirst(",", "");
			System.out.println(ss);
		}
	}

}
