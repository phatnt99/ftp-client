package com.ftp.phatng.ftpphat.services.impls;

import com.ftp.phatng.ftpphat.services.FtpClientService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;
import org.springframework.stereotype.Service;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.Util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

@Service
public class FtpClientServiceImpl implements FtpClientService {
    @Override
    public String getFiles() {
        FTPClient ftpClient = construct();
        try {
            FTPFile[] files = ftpClient.listFiles("");
            FTPFile[] directories = ftpClient.listDirectories();
            System.out.println("receive");

            //retrieve file
            for(FTPFile file : files) {
                InputStream fileStream = ftpClient.retrieveFileStream(file.getName());

                List<Map<String, String>> out = readInputStream(fileStream, 2, ',');
                System.out.println("Done");
                // try upload file
                // currently not working with InputStream got from FTP
//                uploadFile("copy2.csv", fileStream);
            }
        } catch (Exception e)
        {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        return null;
    }

    @Override
    public boolean uploadFile(String path, InputStream inputStream) throws Exception {
//        log.info("sending to tcs ftp");
        FTPClient ftpClient = construct();
        ftpClient.setFileTransferMode(FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE);
        boolean result = false;
        try {
            InputStream inputStream2 = new ByteArrayInputStream("hello world".getBytes());
            result = ftpClient.storeFile(path, inputStream);
            if (result) {
                System.out.println("uploaded");
//                log.info("sending to tcs ftp succeeded");
            } else {
                throw new Exception("sending to tcs ftp failed");
            }
        } catch (Exception ex) {
//            log.info("sending to tcs ftp failed {}", ex.getMessage());
            throw ex;
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                throw ex;
            }
        }
        return result;
    }

    private FTPClient construct() {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect("localhost", 21);
            ftpClient.login("root", "123456");
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        } catch (Exception e) {

        }
        return ftpClient;
    }

    private List<Map<String, String>> readInputStream(InputStream inputStream, int firstRow, char delimiterChar) {
        List<Map<String, String>> data = new ArrayList<>();
        ICsvMapReader mapReader = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            mapReader = new CsvMapReader(br, new CsvPreference.Builder('"', (int) delimiterChar, "\r\n").build());
            String[] firstRowData = mapReader.getHeader(false);

            String[] excelCharacter = IntStream.range(0, firstRowData.length)
                    .mapToObj(index -> CellReference.convertNumToColString(index))
                    .toArray(String[]::new);
            Map<String, String> customerMap = new HashMap<>();

            if (firstRow == 1 && Arrays.stream(firstRowData).anyMatch(item -> StringUtils.isNotEmpty(item))) {
                Util.filterListToMap(customerMap, excelCharacter, Arrays.asList(firstRowData));
                data.add(customerMap);
            }

            while ((customerMap = mapReader.read(excelCharacter)) != null) {
                if (mapReader.getRowNumber() >= firstRow && customerMap.entrySet().stream().anyMatch(e -> StringUtils.isNotBlank(e.getValue()))) {
                    customerMap.put("RowIndex", String.valueOf(mapReader.getRowNumber()));
                    data.add(customerMap);
                }
            }

        } catch (Exception e) {
//            LOGGER.error("Exception when read csv file, message = {}", e.getMessage());
//            throw new PtbcProcessException("Error when read csv file", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
            try {
                if (null != mapReader) {
                    mapReader.close();
                }
            } catch (IOException e) {
//                LOGGER.error("Error when closing input stream. {}", e.getMessage());
            }
        }
        return data;
    }
}
