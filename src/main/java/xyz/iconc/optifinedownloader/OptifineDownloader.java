package xyz.iconc.optifinedownloader;

import io.netty.bootstrap.ServerBootstrapConfig;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.Main;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

@Mod(
        modid = OptifineDownloader.MOD_ID,
        name = OptifineDownloader.MOD_NAME,
        version = OptifineDownloader.VERSION,
        clientSideOnly = true

)
public class OptifineDownloader {

    public static final String MOD_ID = "optifinedownloader";
    public static final String MOD_NAME = "OptifineDownloader";
    public static final String VERSION = "1.0.0";

    private static final String FILE_NAME = "OptiFine_1.12.2_HD_U_G5.jar";


    private static final String BASE_OPTIFINE_URL = "https://optifine.net/";
    private static final String USER_AGENT = "User-Agent\", \"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:95.0) Gecko/20100101 Firefox";
    private static final String BASE_OPTIFINE_DOWNLOAD_PAGE = "adloadx?f=" + FILE_NAME;


    private static final String BEGIN_INDEX_STRING = "downloadx?f=OptiFine_1.12.2_HD_U_G5.jar";
    private static final String END_INDEX_STRING = "' onclick='onDownload()'>OptiFine 1.12.2 HD U G5</a>";

    private static final Logger LOGGER = LogManager.getLogger(OptifineDownloader.class);


    @Mod.Instance(MOD_ID)
    public static OptifineDownloader INSTANCE;


    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        //Minecraft.getMinecraft().shutdown();
        //Main.main();
        //Runtime.getRuntime()
        File file = new File("./mods/" + FILE_NAME);
        if (!file.exists()) DownloadOptifine(GetOptifineDownloadLink());
    }


    public static String GetOptifineDownloadLink() {
        LOGGER.info("Obtaining download url...");
        String content;

        try {
            URL url = new URL(BASE_OPTIFINE_URL + BASE_OPTIFINE_DOWNLOAD_PAGE);
            URLConnection c = url.openConnection();
            c.setRequestProperty("User-Agent", USER_AGENT);

            content = stringFromInputStream(c.getInputStream());
        } catch (IOException e) {
            LOGGER.error(e.toString());
            return null;
        }


        int start = content.indexOf(BEGIN_INDEX_STRING);
        int end = content.indexOf(END_INDEX_STRING);

        String downloadUrl = BASE_OPTIFINE_URL + content.substring(start, end);
        LOGGER.info("Download URL located at: " + downloadUrl);
        return downloadUrl;
    }

    public static boolean DownloadOptifine(String downloadUrl) {
        LOGGER.info("Downloading Optifine...");
        String fileLocation = "./mods/" + FILE_NAME;

        URLConnection conn;
        try {
            URL url = new URL(downloadUrl);
            conn = url.openConnection();
            conn.setRequestProperty("User-Agent", USER_AGENT);
        } catch (IOException e) {
            LOGGER.error(e.toString());
            return false;
        }



        try (BufferedInputStream inputStream = new BufferedInputStream(conn.getInputStream());
             FileOutputStream outputStream = new FileOutputStream(fileLocation)) {
            byte[] data = new byte[1024];
            int content;
            while ((content = inputStream.read(data, 0, 1024)) != -1) {
                outputStream.write(data, 0, content);
            }
        } catch (IOException e) {
            LOGGER.error(e.toString());
            LOGGER.error("Unable to download optifine...");
            return false;
        }
        LOGGER.info("Optifine Downloaded to: " + fileLocation);
        return true;
    }

    private static String stringFromInputStream(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

        String line = br.readLine();
        StringBuilder stringBuilder = new StringBuilder();
        while (line != null) {
            stringBuilder.append((line));
            line = br.readLine();
        }
        br.close();

        return stringBuilder.toString();
    }

    public static void main(String[] args) throws IOException {
        DownloadOptifine(GetOptifineDownloadLink());
    }
}