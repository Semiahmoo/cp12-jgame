package ca.hapke.util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.ProtectionDomain;

import javax.imageio.ImageIO;

/**
 * @author Mr. Hapke
 *
 */
public abstract class FileUtil {

	public static File getFileNotInBinFolder(ProtectionDomain pd, String fn) throws IOException {
		URL url = pd.getCodeSource().getLocation();

		File f;
		File dir = new File(url.getFile());
		int i = 0;
		while (i < 100) {
			f = new File(dir.getAbsolutePath() + File.separatorChar + fn);
			if (f.exists())
				return f;
			dir = dir.getParentFile();
			if (dir == null || !dir.canRead())
				return null;
			i++;
		}
		System.err.println("File not found");
		return null;
	}

	public static Image getImage(ProtectionDomain pd, String folder, String filename) {
		try {
			File f = getFileNotInBinFolder(pd, folder + File.separatorChar + filename);
			BufferedImage img = ImageIO.read(f);
			return img;
		} catch (IOException e) {
			return null;
		}
	}

}
